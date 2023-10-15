package com.louis.user_center.service.impl;



import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.louis.user_center.mapper.UserTeamMapper;
import com.louis.user_center.model.domain.UserTeam;
import com.louis.user_center.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author 35064
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-10-01 21:44:11
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




