create table subject
(
    created_at timestamp(6),
    id         bigint auto_increment,
    updated_at timestamp(6),
    question   varchar(255) not null,
    title      varchar(255) not null,
    category   varchar(255) not null,
    primary key (id),
    unique (title, question)
);
