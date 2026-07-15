package com.qsd.admin.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.member.entity.PackagePrealert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PackagePrealertMapper extends BaseMapper<PackagePrealert> {

    @Select("""
        select id, member_id, prealert_no, tracking_no, courier_code, warehouse_code, goods_name, package_count, estimated_weight, remark, status, created_at, updated_at
        from package_prealert
        where member_id = #{memberId}
        order by id desc
        """)
    List<PackagePrealert> selectByMemberId(Long memberId);

    @Select("""
        <script>
        select id, member_id, prealert_no, tracking_no, courier_code, warehouse_code, goods_name, package_count, estimated_weight, remark, status, created_at, updated_at
        from package_prealert
        where 1 = 1
          <if test="status != null and status != ''">
            and status = #{status}
          </if>
          <if test="keyword != null and keyword != ''">
            and (
              prealert_no like concat('%', #{keyword}, '%')
              or tracking_no like concat('%', #{keyword}, '%')
              or goods_name like concat('%', #{keyword}, '%')
              or exists (
                select 1
                from member_user mu
                where mu.id = package_prealert.member_id
                  and (
                    mu.member_no like concat('%', #{keyword}, '%')
                    or mu.username like concat('%', #{keyword}, '%')
                    or mu.mobile like concat('%', #{keyword}, '%')
                  )
              )
            )
          </if>
        order by id desc
        </script>
        """)
    List<PackagePrealert> selectAdminList(@Param("keyword") String keyword, @Param("status") String status);
}
