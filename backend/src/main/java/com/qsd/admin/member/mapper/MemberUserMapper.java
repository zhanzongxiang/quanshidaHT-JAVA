package com.qsd.admin.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.member.entity.MemberUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MemberUserMapper extends BaseMapper<MemberUser> {

    @Select("""
        select id, tenant_id, phone, wechat_openid, wechat_unionid, wechat_bind_time, password_hash, nickname, full_name, avatar_url, status, remark,
               last_login_at, deleted, created_at, updated_at
        from member_user
        where tenant_id = #{tenantId}
          and phone = #{phone}
          and deleted = 0
        limit 1
        """)
    MemberUser selectByPhone(@Param("tenantId") Long tenantId, @Param("phone") String phone);

    @Select("""
        select id, tenant_id, phone, wechat_openid, wechat_unionid, wechat_bind_time, password_hash, nickname, full_name, avatar_url, status, remark,
               last_login_at, deleted, created_at, updated_at
        from member_user
        where tenant_id = #{tenantId}
          and id = #{id}
          and deleted = 0
        limit 1
        """)
    MemberUser selectActiveById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
        select id, tenant_id, phone, wechat_openid, wechat_unionid, wechat_bind_time, password_hash, nickname, full_name, avatar_url, status, remark,
               last_login_at, deleted, created_at, updated_at
        from member_user
        where tenant_id = #{tenantId}
          and wechat_openid = #{openid}
          and deleted = 0
        limit 1
        """)
    MemberUser selectByWechatOpenid(@Param("tenantId") Long tenantId, @Param("openid") String openid);

    @Select("""
        <script>
        select id, tenant_id, phone, wechat_openid, wechat_unionid, wechat_bind_time, password_hash, nickname, full_name, avatar_url, status, remark,
               last_login_at, deleted, created_at, updated_at
        from member_user
        where tenant_id = #{tenantId}
          and deleted = 0
          <if test="keyword != null and keyword != ''">
            and (
              phone like concat('%', #{keyword}, '%')
              or coalesce(nickname, '') like concat('%', #{keyword}, '%')
              or coalesce(full_name, '') like concat('%', #{keyword}, '%')
            )
          </if>
          <if test="status != null and status != ''">
            and status = #{status}
          </if>
        order by updated_at desc, id desc
        </script>
        """)
    List<MemberUser> selectAdminList(
        @Param("tenantId") Long tenantId,
        @Param("keyword") String keyword,
        @Param("status") String status
    );
}
