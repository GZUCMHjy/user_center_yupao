package com.louis.user_center.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author louis
 * @version 1.0
 * @date 2023/9/13 21:55
 */
@Data
public class Info{
    /**
     * excel文件封装成对象
     */
    // 单品编码
    // 注解指定要读取的列
    @ExcelProperty("单品编码")
    private String code;
    // 单品名称
    @ExcelProperty("单品名称")
    private String name;
}