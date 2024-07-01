create table chat
(
    id              bigint auto_increment comment '아이디',
    subject_id      bigint                     not null comment '주제',
    user_session_id varchar(255)               not null comment '사용자 세션 아이디',
    message         text                       not null comment '채팅', -- text: 65,535 bytes
    score           integer      default 0 comment '점수',
    chat_type       varchar(255)               not null comment '채팅 유형',
    created_at      timestamp(6) default now() not null comment '생성일',
    updated_at      timestamp(6) default now() not null on update now() comment '수정일',

    primary key (id),
    index idx_subject_userSessionId (subject_id, user_session_id)
) character set utf8;
