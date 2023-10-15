package com.louis.user_center.model.enums;

/**
 * @author louis
 * @version 1.0
 * @date 2023/10/2 9:24
 */
// 队伍状态枚举
public enum TeamStatusEnums {

    PUBLIC (0,"公开"),
    PRIVATE(1,"私密"),
    SECRET(2,"加密");
    private int value;
    private String text;

    public static TeamStatusEnums getEnumsByValue(Integer value){
        if(value == null){
            return null;
        }
        TeamStatusEnums[] values = TeamStatusEnums.values();
        for(TeamStatusEnums enums : values){
            if(enums.getValue() == value){
                return enums;
            }
        }return null;
    }
    // 构造体
    TeamStatusEnums(int value ,String text){
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
