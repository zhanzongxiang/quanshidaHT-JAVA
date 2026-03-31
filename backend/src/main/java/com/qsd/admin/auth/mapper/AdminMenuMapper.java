package com.qsd.admin.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.auth.entity.AdminMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminMenuMapper extends BaseMapper<AdminMenu> {

    @Select("""
        select distinct m.id, m.parent_id, m.name, m.path, m.component, m.icon, m.sort_no
        from admin_user_role ur
        join admin_role_menu rm on ur.role_id = rm.role_id
        join admin_menu m on rm.menu_id = m.id
        where ur.user_id = #{userId}
        order by m.sort_no asc, m.id asc
        """)
    List<AdminMenu> selectMenusByUserId(Long userId);
}
