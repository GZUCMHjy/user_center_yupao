package com.louis.user_center.exception;

import com.louis.user_center.common.ErrorCode;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.sym.error;

// 继承运行类异常
public class BusinessException extends RuntimeException{
        //增加字段 提高继承父类的灵活性 使得更符合业务情景需求
        private  final int code;
        private  final String description;
        //继承RuntimeException类的属性message
    public BusinessException(String message,int code,String description) {
        //super指向上一个 即RuntimeException
        super(message);
        this.code=code;
        this.description=description;
    }

    public BusinessException(ErrorCode errorCode) {
        // 构造器继承 super 调用父类构造器（对于父类有些属性是私有的 就必须要通过构造器进行赋值初始化）
        super(errorCode.getMassage());
        this.code=errorCode.getCode();
        this.description=errorCode.getDescription();
    }
    public BusinessException(ErrorCode errorCode,String description){
        super(errorCode.getMassage());
        this.code=errorCode.getCode();
        this.description=description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
