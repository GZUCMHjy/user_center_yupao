package com.louis.user_center.model.request;
import lombok.Data;

import java.io.Serializable;


/**
 * @author louis
 * @version 1.0
 * @date 2023/10/2 10:14
 */
// 删除队伍包装类
@Data
public class TeamQuitRequest implements Serializable {


    private static final long serialVersionUID = -4980402916117301063L;

    /**
     * 队伍id
     */
    private Long teamId;

}
