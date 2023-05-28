-- auto-generated definition
create table user
(
    userName     varchar(256)                       null comment '用户名',
    userPassword varchar(256)                       null comment '密码',
    email        varchar(256)                       null comment '邮箱',
    phone        varchar(256)                       null comment '电话',
    isValid      tinyint                            null comment '账号是否有效',
    id           int auto_increment comment '用户id'
        primary key,
    gender       tinyint                            null comment '性别',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    userAccount  varchar(256)                       null comment '用户账号',
    avatarUrl    varchar(512)                       null,
    userRole     tinyint  default 0                 null comment '0--普通用户
1--管理员',
    planetCode   varchar(512)                       null comment '星球编号'
);

