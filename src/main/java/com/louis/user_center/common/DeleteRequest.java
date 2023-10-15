package com.louis.user_center.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author louis
 * @version 1.0
 * @date 2023/10/8 17:13
 */

@Data
public class DeleteRequest implements Serializable {


    private static final long serialVersionUID = 6615973193509868849L;

    private Long id;
}
