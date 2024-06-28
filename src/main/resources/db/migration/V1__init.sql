create table subject
(
    id         bigint auto_increment,
    title      varchar(255) not null comment '제목',
    question   varchar(255) not null comment '질문',
    category   varchar(255) not null comment '카테고리',
    created_at timestamp(6),
    updated_at timestamp(6),
    primary key (id),
    unique (title, question)
);
