update admin_menu
set name = '会员管理'
where path = '/members';

update admin_menu
set name = '支付管理'
where path = '/payments';

update permission
set perm_name = '查看会员'
where perm_code = 'member:view';

update permission
set perm_name = '编辑会员'
where perm_code = 'member:edit';

update permission
set perm_name = '查看支付'
where perm_code = 'payment:view';

update permission
set perm_name = '管理支付'
where perm_code = 'payment:edit';
