package com.louis.user_center.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.louis.user_center.common.BaseResponse;
import com.louis.user_center.common.DeleteRequest;
import com.louis.user_center.common.ErrorCode;
import com.louis.user_center.common.ResultUtils;
import com.louis.user_center.exception.BusinessException;
import com.louis.user_center.model.domain.Team;
import com.louis.user_center.model.domain.User;
import com.louis.user_center.model.domain.UserTeam;
import com.louis.user_center.model.dto.TeamQuery;
import com.louis.user_center.model.request.TeamAddRequest;
import com.louis.user_center.model.request.TeamJoinRequest;
import com.louis.user_center.model.request.TeamQuitRequest;
import com.louis.user_center.model.request.TeamUpdateRequest;
import com.louis.user_center.model.vo.TeamUserVO;
import com.louis.user_center.model.vo.UserVO;
import com.louis.user_center.service.TeamService;
import com.louis.user_center.service.UserService;
import com.louis.user_center.service.UserTeamService;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/team")
@EnableScheduling // 主类开启定时任务
@Slf4j
public class TeamController {
    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    /**
     * 创建队伍 （增）
     * @param teamAddRequest
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        System.out.println(teamAddRequest);
        if(teamAddRequest == null){
            log.info("传递对象为空");
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        System.out.println(team);
        long result = teamService.addTeam(team, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除队伍
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){

        User loginUser = userService.getLoginUser(request);

        if( deleteRequest !=null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = deleteRequest.getId();
        boolean result = teamService.deleteTeam(id,loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // 成功就返回true
        return ResultUtils.success(true);
    }

    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request){
        // 1. 判断传入参数是否为空
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 2. 获取当前用户信息
        User loginUser = userService.getLoginUser(request);

        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        return ResultUtils.success(true);

    }

    /**
     * 查询队伍 （查）
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Team> getTeam( long id){
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    /**
     * 查询队伍列表 （查）
     * @param teamQuery
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery,HttpServletRequest request){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        // alt + enter 快速在接口创建重载方法
        // 0. 初步筛选队伍列表 空值和不符合业务逻辑的
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,isAdmin);
        // 1. 判断当前用户有无加入队伍
        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId",loginUser.getId());
            userTeamQueryWrapper.in("teamId",teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            Set<Integer> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getUserId).collect(Collectors.toSet());
            teamList.forEach(team ->{
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
//            throw new RuntimeException(e);
        }
        // 2. 查询加入队伍的用户信息（人数）-加字段
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId",teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team-> {
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(),new ArrayList<>()).size());
        });
        return ResultUtils.success(teamList);
    }

    /**
     * 分页查询队伍列表 （查）
     * @param teamQuery
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        // mp插件中page分页对象 获取两个参数 当前页数和页面大小（也就是多少条数据）
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> qw = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page, qw);
        return ResultUtils.success(resultPage);
    }

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param request
     * @return
     */
    @PostMapping("/join" )
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request){
        // TODO 优化
        //  就是可以采用AOP切面变成 将每个接口的判非空操作 独立出来 这样就可以降低代码重复率 提高开发效率
        if(teamJoinRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param request
     * @return
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam( @RequestBody TeamQuitRequest teamQuitRequest,HttpServletRequest request){
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取我创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery,HttpServletRequest request){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = userService.isAdmin(loginUser);
        // 获取我创建队伍的关键一行
        teamQuery.setUserId(loginUser.getId());
        // alt + enter 快速在接口创建重载方法
        List<TeamUserVO> teamlist = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(teamlist);
    }


    /**
     * 获取我加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery,HttpServletRequest request){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        // 取出重复的队伍id
        // Java8 语法
        // teamId userId (Map映射集合)
        // 1 , 2
        // 1 , 3
        // 2 , 3
        // 经过stream.collect 之后  相当于进行了整合
        // 1 => 2 , 3
        // 2 => 3
        Map<Integer, List<UserTeam>> listMap = userTeamList.stream().
                collect(Collectors.groupingBy(UserTeam::getUserId));
        List<Integer> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamList);
    }


}
