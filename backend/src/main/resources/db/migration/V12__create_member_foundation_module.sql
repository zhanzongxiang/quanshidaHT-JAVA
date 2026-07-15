create table if not exists member_user (
    id bigint primary key auto_increment,
    member_no varchar(32) not null unique,
    username varchar(64) not null unique,
    mobile varchar(32) not null unique,
    password_hash varchar(128) not null,
    nickname varchar(64) null,
    real_name varchar(64) null,
    level_code varchar(32) not null default 'normal',
    status varchar(16) not null default 'ENABLED',
    remark varchar(255) null,
    last_login_at datetime null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    deleted tinyint not null default 0
);

create table if not exists member_address (
    id bigint primary key auto_increment,
    member_id bigint not null,
    contact_name varchar(64) not null,
    contact_phone varchar(32) not null,
    country varchar(64) not null default '中国',
    province varchar(64) null,
    city varchar(64) null,
    district varchar(64) null,
    detail_address varchar(255) not null,
    postal_code varchar(32) null,
    is_default tinyint not null default 0,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    key idx_member_address_member_id(member_id)
);

create table if not exists package_prealert (
    id bigint primary key auto_increment,
    member_id bigint not null,
    prealert_no varchar(32) not null unique,
    tracking_no varchar(64) not null,
    courier_code varchar(32) null,
    warehouse_code varchar(32) null,
    goods_name varchar(128) not null,
    package_count int not null default 1,
    estimated_weight decimal(10,2) null,
    remark varchar(255) null,
    status varchar(32) not null default 'pending',
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    key idx_package_prealert_member_id(member_id),
    key idx_package_prealert_tracking_no(tracking_no)
);

insert into member_user(member_no, username, mobile, password_hash, nickname, real_name, level_code, status, remark)
values ('M100001', 'member01', '13800138000', '123456', '测试会员', '测试会员', 'normal', 'ENABLED', 'seed member')
on duplicate key update username = values(username);

insert into member_address(member_id, contact_name, contact_phone, country, province, city, district, detail_address, postal_code, is_default)
select id, '测试会员', '13800138000', '中国', '广东省', '深圳市', '南山区', '科技园测试地址 1 号', '518000', 1
from member_user
where username = 'member01'
  and not exists (
    select 1 from member_address a where a.member_id = member_user.id
  );

insert into package_prealert(member_id, prealert_no, tracking_no, courier_code, warehouse_code, goods_name, package_count, estimated_weight, remark, status)
select id, 'PA100001', 'YT123456789CN', 'YTO', 'SZ', '测试货物', 1, 2.50, 'seed prealert', 'pending'
from member_user
where username = 'member01'
  and not exists (
    select 1 from package_prealert p where p.prealert_no = 'PA100001'
  );
