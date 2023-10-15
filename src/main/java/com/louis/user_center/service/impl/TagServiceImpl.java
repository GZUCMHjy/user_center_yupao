package com.louis.user_center.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.louis.user_center.mapper.TagMapper;
import com.louis.user_center.model.domain.Tag;
import com.louis.user_center.service.TagService;
import org.springframework.stereotype.Service;

/**
* @author 35064
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2023-09-06 18:02:26
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {

}




