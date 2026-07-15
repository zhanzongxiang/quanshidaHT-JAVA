create table if not exists member_package (
    id bigint primary key auto_increment,
    member_id bigint not null,
    package_no varchar(32) not null unique,
    prealert_id bigint null,
    tracking_no varchar(64) not null,
    goods_name varchar(128) not null,
    warehouse_code varchar(32) null,
    package_count int not null default 1,
    weight_kg decimal(10,2) null,
    package_status varchar(32) not null default 'pending_claim',
    issue_flag tinyint not null default 0,
    issue_type varchar(64) null,
    issue_note varchar(255) null,
    warehouse_in_at datetime null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    key idx_member_package_member_id(member_id),
    key idx_member_package_status(package_status)
);

create table if not exists member_shipment (
    id bigint primary key auto_increment,
    member_id bigint not null,
    shipment_no varchar(32) not null unique,
    address_id bigint null,
    shipment_status varchar(32) not null default 'submitted',
    package_count int not null default 0,
    total_weight decimal(10,2) null,
    remark varchar(255) null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    key idx_member_shipment_member_id(member_id)
);

create table if not exists member_shipment_package (
    id bigint primary key auto_increment,
    shipment_id bigint not null,
    package_id bigint not null,
    unique key uk_member_shipment_package(shipment_id, package_id)
);

create table if not exists member_order (
    id bigint primary key auto_increment,
    member_id bigint not null,
    shipment_id bigint null,
    order_no varchar(32) not null unique,
    order_status varchar(32) not null default 'pending_payment',
    payment_status varchar(32) not null default 'unpaid',
    amount decimal(10,2) not null default 0,
    currency_code varchar(16) not null default 'CNY',
    remark varchar(255) null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    key idx_member_order_member_id(member_id)
);

create table if not exists member_finance_record (
    id bigint primary key auto_increment,
    member_id bigint not null,
    order_id bigint null,
    record_no varchar(32) not null unique,
    record_type varchar(32) not null default 'charge',
    amount decimal(10,2) not null default 0,
    currency_code varchar(16) not null default 'CNY',
    record_status varchar(32) not null default 'pending',
    note varchar(255) null,
    created_at datetime not null default current_timestamp,
    key idx_member_finance_member_id(member_id)
);

insert into member_package(member_id, package_no, prealert_id, tracking_no, goods_name, warehouse_code, package_count, weight_kg, package_status, issue_flag, issue_type, issue_note, warehouse_in_at)
select u.id, 'PK100001', p.id, 'YT123456789CN', '测试货物', 'SZ', 1, 2.50, 'in_stock', 0, null, null, now()
from member_user u
left join package_prealert p on p.member_id = u.id and p.prealert_no = 'PA100001'
where u.username = 'member01'
  and not exists (select 1 from member_package mp where mp.package_no = 'PK100001');

insert into member_shipment(member_id, shipment_no, shipment_status, package_count, total_weight, remark)
select id, 'SH100001', 'submitted', 1, 2.50, 'seed shipment'
from member_user
where username = 'member01'
  and not exists (select 1 from member_shipment s where s.shipment_no = 'SH100001');

insert into member_shipment_package(shipment_id, package_id)
select s.id, p.id
from member_shipment s
join member_package p on p.package_no = 'PK100001'
where s.shipment_no = 'SH100001'
  and not exists (
    select 1 from member_shipment_package sp where sp.shipment_id = s.id and sp.package_id = p.id
  );

insert into member_order(member_id, shipment_id, order_no, order_status, payment_status, amount, currency_code, remark)
select u.id, s.id, 'OD100001', 'pending_payment', 'unpaid', 88.00, 'CNY', 'seed order'
from member_user u
join member_shipment s on s.shipment_no = 'SH100001'
where u.username = 'member01'
  and not exists (select 1 from member_order o where o.order_no = 'OD100001');

insert into member_finance_record(member_id, order_id, record_no, record_type, amount, currency_code, record_status, note)
select u.id, o.id, 'FR100001', 'charge', 88.00, 'CNY', 'pending', 'seed finance record'
from member_user u
join member_order o on o.order_no = 'OD100001'
where u.username = 'member01'
  and not exists (select 1 from member_finance_record f where f.record_no = 'FR100001');
