package com.louis.user_center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.louis.user_center.model.domain.Team;
import com.louis.user_center.model.domain.User;
import com.louis.user_center.model.dto.TeamQuery;
import com.louis.user_center.model.request.TeamJoinRequest;
import com.louis.user_center.model.request.TeamQuitRequest;
import com.louis.user_center.model.request.TeamUpdateRequest;
import com.louis.user_center.model.vo.TeamUserVO;

import java.util.List;


/**
* @author 35064
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-10-01 21:42:30
*/
public interface TeamService extends IService<Team> {
    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 获取队伍列表（查询）
     * @param teamQuery
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 修改队伍
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param userLogin
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User userLogin);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    boolean deleteTeam(long id,User loginUser);
}
