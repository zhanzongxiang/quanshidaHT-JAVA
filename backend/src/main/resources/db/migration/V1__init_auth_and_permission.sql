create table if not exists admin_user (
    id bigint primary key auto_increment,
    username varchar(64) not null unique,
    password_hash varchar(255) not null,
    status varchar(32) not null default 'ENABLED',
    deleted tinyint not null default 0,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp
);

create table if not exists admin_role (
    id bigint primary key auto_increment,
    role_code varchar(64) not null unique,
    role_name varchar(64) not null
);

create table if not exists admin_user_role (
    id bigint primary key auto_increment,
    user_id bigint not null,
    role_id bigint not null,
    unique key uk_user_role(user_id, role_id)
);

create table if not exists permission (
    id bigint primary key auto_increment,
    perm_code varchar(128) not null unique,
    perm_name varchar(128) not null
);

create table if not exists admin_role_permission (
    id bigint primary key auto_increment,
    role_id bigint not null,
    permission_id bigint not null,
    unique key uk_role_permission(role_id, permission_id)
);

create table if not exists admin_menu (
    id bigint primary key auto_increment,
    parent_id bigint null,
    name varchar(64) not null,
    path varchar(128) not null,
    component varchar(128) not null,
    icon varchar(64) null,
    sort_no int not null default 0
);

create table if not exists admin_role_menu (
    id bigint primary key auto_increment,
    role_id bigint not null,
    menu_id bigint not null,
    unique key uk_role_menu(role_id, menu_id)
);

create table if not exists operation_log (
    id bigint primary key auto_increment,
    module_name varchar(64) not null,
    operation_type varchar(64) not null,
    operator varchar(64) not null,
    target_id varchar(128) not null,
    summary varchar(500) not null,
    created_at datetime not null default current_timestamp
);

create table if not exists login_log (
    id bigint primary key auto_increment,
    username varchar(64) not null,
    ip varchar(64) null,
    user_agent varchar(500) null,
    result varchar(32) not null,
    created_at datetime not null default current_timestamp
);

insert into admin_user(username, password_hash, status)
values ('admin', 'admin123', 'ENABLED')
on duplicate key update username = values(username);

insert into admin_role(role_code, role_name)
values ('super_admin', '超级管理员')
on duplicate key update role_name = values(role_name);

insert into permission(perm_code, perm_name)
values ('user:view', '查看用户'),
       ('user:edit', '编辑用户'),
       ('content:view', '查看内容'),
       ('content:edit', '编辑内容')
on duplicate key update perm_name = values(perm_name);

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
values (0, '工作台', '/dashboard', 'Dashboard', 'DataLine', 10),
       (0, '内容管理', '/content', 'Content', 'Document', 20)
on duplicate key update name = values(name);

insert into admin_user_role(user_id, role_id)
select u.id, r.id from admin_user u join admin_role r
where u.username = 'admin' and r.role_code = 'super_admin'
on duplicate key update user_id = values(user_id);

insert into admin_role_permission(role_id, permission_id)
select r.id, p.id from admin_role r join permission p
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);

insert into admin_role_menu(role_id, menu_id)
select r.id, m.id from admin_role r join admin_menu m
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);
