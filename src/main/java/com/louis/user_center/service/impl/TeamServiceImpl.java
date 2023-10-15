package com.louis.user_center.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.louis.user_center.common.ErrorCode;
import com.louis.user_center.exception.BusinessException;
import com.louis.user_center.mapper.TeamMapper;

import com.louis.user_center.model.domain.Team;
import com.louis.user_center.model.domain.User;
import com.louis.user_center.model.domain.UserTeam;
import com.louis.user_center.model.dto.TeamQuery;
import com.louis.user_center.model.enums.TeamStatusEnums;
import com.louis.user_center.model.request.TeamJoinRequest;
import com.louis.user_center.model.request.TeamQuitRequest;
import com.louis.user_center.model.request.TeamUpdateRequest;
import com.louis.user_center.model.vo.TeamUserVO;
import com.louis.user_center.model.vo.UserVO;
import com.louis.user_center.service.TeamService;
import com.louis.user_center.service.UserService;
import com.louis.user_center.service.UserTeamService;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
* @author 35064
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-10-01 21:42:30
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务（原子性）
    public long addTeam(Team team, User loginUser) {
        // 1. 判断请求参数是否为空
        if(team == null){
             throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 当前用户是否登录
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final int userId = loginUser.getId();
        // 3. 校验信息
        // 3.1 队伍人数校验 (判空校验 和 业务逻辑的校验)
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0) ;
        if(maxNum < 1 || maxNum > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足要求");
        }
        // 3.2 队伍标题
        String name = team.getName();
        if(StringUtils.isBlank(name) || name.length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍名不满足要求");
        }
        // 3.3 队伍描述信息
        String description = team.getDescription();
        if(StringUtils.isBlank(description) || description.length()>512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"描述内容不满足要求");
        }
        // 3.4 status 是否公开 和 加密
        int  status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnums enums = TeamStatusEnums.getEnumsByValue(status);
        if(enums == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足要求");
        }
        // 3.5 如果队伍status 是加密状态， 一定要有密码，且密码 < = 32
        String password = team.getPassword();
        if(enums.equals(TeamStatusEnums.PRIVATE) && (StringUtils.isBlank(password) || password.length() > 32)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"设置密码不正确");
        }
        // 3.6 超时时间 大于 当前时间
//        Date expireTime = team.getExpireTime();
//        if(new Date().after(expireTime)){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"超时啦!");
//        }
        // 3.7 校验用户不能创建超过5个的队伍数量
        // TODO Bug: 防止恶意用户刷接口
        QueryWrapper<Team> qw = new QueryWrapper<>();
        qw.eq("userId",userId);
        long hasTeamNum = this.count(qw);
        if(hasTeamNum >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多创建5个队伍");
        }
        // 3.8 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId((userId));

        boolean result = this.save(team);
        Long teamId = team.getId();
        if(!result || teamId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }
        // 3.9 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);;
        if(!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }
        return team.getId();
    }

    /**
     * 查询队伍列表
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin) {
        QueryWrapper<Team> qw = new QueryWrapper<>();
        // 校验
        if(teamQuery != null){
            Long id = teamQuery.getId();
            if(id != null && id > 0){
                qw.eq("id",id);
            }
            List<Integer> idList = teamQuery.getIdList();
            if(!CollectionUtils.isEmpty(idList)){
                qw.in("id",idList);
            }
            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)){
                qw.like("name",name);
            }
            String searchText = teamQuery.getSearchText();
            // TODO 有点问题
            if(StringUtils.isNotBlank(searchText)){
                // 这个mp这个连接 加 选择 加 模糊匹配查询 有点吊
                qw.and(queryWrapper -> queryWrapper.like("name",searchText).or().like("description",searchText));
            }
            String description = teamQuery.getDescription();
            if(StringUtils.isNotBlank(description)){
                // sql 里的like模糊匹配
                qw.like("description",description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum != null && maxNum > 0){
                // sql 里的where 字段 = xxx （进行条件匹配）
                qw.eq("maxNum",maxNum);
            }
            Integer userId = teamQuery.getUserId();
            if(userId!= null && userId > 0){
                qw.eq("userId",userId);
            }
            Integer status = teamQuery.getStatus();
            TeamStatusEnums enumsByValue = TeamStatusEnums.getEnumsByValue(status);
            // 默认 公开队伍
            if(enumsByValue == null){
                enumsByValue = TeamStatusEnums.PUBLIC;
            }
            // 查询时，不是管理员，队伍状态为私有，即没有权限
            if(!isAdmin && enumsByValue.equals(TeamStatusEnums.PRIVATE)){
                throw new BusinessException(ErrorCode.NOT_AUTH);
            }
            qw.eq("status",enumsByValue.getValue());
        }
        // 不展示 已过期的队伍
        // lt ---> less than
        // gt ---> greeter than
        qw.and(queryWrapper -> queryWrapper.gt("expireTime",new Date()).or().isNull("expireTime"));
        // qw（sql组合器对象组成完毕，传入到mp实现好的service接口函数中）
        List<Team> teamList = this.list(qw);
        if(CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        // 关联查询创建人的用户信息
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for(Team team : teamList){
            Integer userId = team.getUserId();
            if(userId == null){
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team,teamUserVO);
            // 脱敏用户信息
            if(user != null){
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user,userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser) {
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if(id == null || id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据id 获得当前要进行 修改的 队伍
        Team oldTeam = this.getById(id);
        // 每次获取到的对象 一定要进行判空操作！！！
        if(oldTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"查询队伍不存在");
        }
        // 如果你不是管理员 或者 你不是自己修改自己的账号 就不能修改队伍
        if(oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NOT_AUTH);
        }
        TeamStatusEnums enums = TeamStatusEnums.getEnumsByValue(teamUpdateRequest.getStatus());
        if(enums.equals(TeamStatusEnums.SECRET)){
            if(StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间必须设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,updateTeam);
        return this.updateById(updateTeam);
    }

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if(teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {

            throw new BusinessException(ErrorCode.PARAMS_ERROR,"加入的队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnums enumsByValue = TeamStatusEnums.getEnumsByValue(status);
        if(TeamStatusEnums.PRIVATE.equals(enumsByValue)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if(TeamStatusEnums.SECRET.equals(enumsByValue)){
            if(StringUtils.isBlank(password) || !password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
            }
        }
        // 限制用户加入的队伍数量
        // todo 当用户点击多次 如何避免资源奔溃
        // 加锁 让一个一个线程 排队执行！
        int userId = loginUser.getId();
        // 只有一个线程能获取到锁
        RLock lock = redissonClient.getLock("yupao:join_team");

        try{
            // 抢到锁 并执行
            // TODO
            //  同一个用户连续争抢同一把锁 可以实现资源互斥
            //  但是会影响其他用户对该资源的请求
            while (true){
                if(lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId",userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if(hasJoinNum > 5){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"最多创建和加入5个队伍");
                    }
                    // 不能重复加入已经加入的队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("teamId",teamId);
                    userTeamQueryWrapper.eq("userId",userId);
                    long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if(hasUserJoinTeam >0){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已加入该队伍");
                    }
                    // 已加入队伍的人数

                    long teamHasCount = this.countTeamUserByTeamId(teamId);
                    if(teamHasCount >= team.getMaxNum()){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已满");
                    }
                    // 修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        }catch (InterruptedException e){
            log.error("doCacheRecommendUser error", e);
            return false;
        }finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }

    }

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        // 传参判空
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        UserTeam userTeam = new UserTeam();
        Integer userId = loginUser.getId();
        // 对类进行set queryWrapper 可以对类的属性进行sql拼接
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        // 拼接的关键步骤 后面传入设置好的对象
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(userTeam);
        long count = userTeamService.count(queryWrapper);
        if(count == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未加入该队伍");
        }
        // 查询该队伍目前人数
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        // 队伍只剩下一个人 解散队伍
        if(teamHasJoinNum == 1){
            // 对Team表进行删除
            this.removeById(teamId);
        }else{
            // 队伍目前不止一人 进一步判断该用户是否是队长 可以进行解散权限操作
            // 是队长 退出队伍 并把队长角色 转接 给下一个最早来到的队伍的队员
            if(team.getUserId() == userId){
                // 把队伍转移给最早加入的用户
                // 1. 查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId",teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                // 没有下一个队员 或者只有自己的情况下 报错！
                if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Integer nextUserId = nextUserTeam.getUserId();
                // 更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextUserId);
                boolean result = this.updateById(updateTeam);
                if(!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队伍队长失败");
                }
            }
        }
        // 移除 用户队伍关系
        return userTeamService.remove(queryWrapper);
    }

    /**
     * 删除队伍
     * @param id
     * @param loginUser
     * @return
     */
    @Override
    // 为什么要加上这个事务注解
    // 因为该方法 进行了两次的删除(remove)操作 万一中途挂掉了 只删除一次操作 这样 关联表就会出现脏数据了
    // 所以方法出现了多个增、删、改的操作 都要加上事务注解！
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id,User loginUser) {
        // 检验队伍是否为空
        Team team = getTeamById(id);
        Long teamId = team.getId();
        // 检验当前用户是不是该队队长
        if(team.getUserId() != loginUser.getId()){
            throw new BusinessException(ErrorCode.NOT_AUTH,"无权限访问");
        }
        // 移除所有加入队伍的用户(关联)信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍关联信息失败");
        }
        // 同时还有删除队伍表数据信息
        return this.removeById(teamId);
    }

    // 简单封装一下

    /**
     * 获取当前队伍的人数
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(long teamId){
        QueryWrapper<UserTeam> qw = new QueryWrapper<>();
        qw.eq("teamId",teamId);
        return userTeamService.count(qw);
    }

    /**
     * 根据队伍id 查询队伍信息
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if(teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取请求解散的队伍
        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"该队伍不存在");
        }
        return team;
    }
}




