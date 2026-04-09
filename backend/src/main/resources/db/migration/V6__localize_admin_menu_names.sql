update admin_menu set name = '页面管理' where path = '/pages';
update admin_menu set name = '首页配置' where path = '/pages/home';
update admin_menu set name = '线路页面' where path = '/pages/service-lines';
update admin_menu set name = '新闻资讯' where path = '/news';
update admin_menu set name = '全局配置' where path = '/settings';
update admin_menu set name = '导航设置' where path = '/settings/navigation';
update admin_menu set name = '页脚设置' where path = '/settings/footer';
update admin_menu set name = '联系方式' where path = '/settings/contact';

update permission set perm_name = '查看新闻' where perm_code = 'news:view';
update permission set perm_name = '编辑新闻' where perm_code = 'news:edit';
update permission set perm_name = '查看设置' where perm_code = 'settings:view';
update permission set perm_name = '编辑设置' where perm_code = 'settings:edit';
