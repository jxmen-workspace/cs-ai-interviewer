alter table subject
    modify created_at timestamp(6) default now() not null;

alter table subject
    modify updated_at timestamp(6) default now() not null on update now();
