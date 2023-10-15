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
ALTER TABLE user ADD COLUMN  tags varchar(1024) null comment '标签列表';
-- auto-generated definition
create table tag
(
    id         int auto_increment comment 'id'
        primary key,
    tagName    varchar(256)                       null comment '标签名',
    userId     bigint                             null comment '用户id',
    parentId   bigint                             null comment '父标签id',
    isParent   tinyint                            null comment '0 - 不是,1 - 标签',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除',
    constraint uniIdx_tagName
        unique (tagName)
)
    comment '标签';

create index idx_userId
    on tag (userId);


-- 队伍表
create table team
(
    id           bigint auto_increment comment 'id'
        primary key,
    name   varchar(256)                   not null comment '队伍名称',
    description varchar(1024)                      null comment '描述',
    maxNum    int      default 1                 not null comment '最多人数',
    expireTime    datetime  null comment '过期时间',
    userId            int comment '用户id（队长id）',
    status    int      default 0                 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password varchar(512)                       null comment '密码',

    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0                 not null comment '是否删除'
) comment '队伍';


-- 维护用户表和队伍表的关系表
create table user_team
(
    id           bigint auto_increment comment 'id'
        primary key,
    userId            int comment '用户id', -- 跟用户表的id类型要保持一致
    teamId            bigint comment '队伍id',-- 跟队伍表的id类型要保持一致
    joinTime datetime  null comment '加入时间',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0                 not null comment '是否删除'
)
    comment '用户队伍关系';