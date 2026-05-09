alter table pay_merchant_config
    add column app_secret varchar(128) null after app_id;

update pay_merchant_config
set app_secret = ''
where app_secret is null;
