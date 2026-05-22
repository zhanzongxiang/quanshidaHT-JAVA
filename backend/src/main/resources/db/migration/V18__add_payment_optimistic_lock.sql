alter table pay_order add column version int not null default 0 after deleted;
