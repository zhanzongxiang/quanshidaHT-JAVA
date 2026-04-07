update admin_role
set role_name = '超级管理员'
where role_code = 'super_admin';

update permission
set perm_name = case perm_code
    when 'user:view' then '查看用户'
    when 'user:edit' then '编辑用户'
    when 'content:view' then '查看内容'
    when 'content:edit' then '编辑内容'
    else perm_name
end
where perm_code in ('user:view', 'user:edit', 'content:view', 'content:edit');

update admin_menu
set name = case path
    when '/dashboard' then '工作台'
    when '/content' then '内容管理'
    else name
end
where path in ('/dashboard', '/content');
