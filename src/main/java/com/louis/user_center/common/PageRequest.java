package com.louis.user_center.common;


import lombok.Data;

import java.io.Serializable;

/**
 * @author louis
 * @version 1.0
 * @date 2023/10/1 22:37
 */
@Data
public class PageRequest implements Serializable {

    // 生成序列化ID——快捷键 alt+insert
    private static final long serialVersionUID = 8403636359282920634L;
    // 默认数值
    protected int pageSize = 10 ;
    protected  int pageNum = 1;
}
