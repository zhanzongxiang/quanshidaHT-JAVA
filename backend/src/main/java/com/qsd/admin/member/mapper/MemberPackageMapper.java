package com.qsd.admin.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.member.entity.MemberPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MemberPackageMapper extends BaseMapper<MemberPackage> {
    @Select("""
        select id, member_id, package_no, prealert_id, tracking_no, goods_name, warehouse_code, package_count, weight_kg, package_status, issue_flag, issue_type, issue_note, warehouse_in_at, created_at, updated_at
        from member_package
        where member_id = #{memberId}
        order by id desc
        """)
    List<MemberPackage> selectByMemberId(Long memberId);

    @Select("""
        select id, member_id, package_no, prealert_id, tracking_no, goods_name, warehouse_code, package_count, weight_kg, package_status, issue_flag, issue_type, issue_note, warehouse_in_at, created_at, updated_at
        from member_package
        where member_id = #{memberId}
          and package_status in ('pending_claim', 'in_stock', 'issue')
        order by id desc
        """)
    List<MemberPackage> selectActiveInventoryByMemberId(Long memberId);

    @Select("""
        <script>
        select id, member_id, package_no, prealert_id, tracking_no, goods_name, warehouse_code, package_count, weight_kg, package_status, issue_flag, issue_type, issue_note, warehouse_in_at, created_at, updated_at
        from member_package
        where 1 = 1
          <if test="status != null and status != ''">
            and package_status = #{status}
          </if>
          <if test="keyword != null and keyword != ''">
            and (
              package_no like concat('%', #{keyword}, '%')
              or tracking_no like concat('%', #{keyword}, '%')
              or goods_name like concat('%', #{keyword}, '%')
              or exists (
                select 1
                from member_user mu
                where mu.id = member_package.member_id
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
    List<MemberPackage> selectAdminList(@Param("keyword") String keyword, @Param("status") String status);

    @Select("""
        <script>
        select id, member_id, package_no, prealert_id, tracking_no, goods_name, warehouse_code, package_count, weight_kg, package_status, issue_flag, issue_type, issue_note, warehouse_in_at, created_at, updated_at
        from member_package
        where member_id = #{memberId}
          and id in
          <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
          </foreach>
        order by id asc
        </script>
        """)
    List<MemberPackage> selectByMemberIdAndIds(@Param("memberId") Long memberId, @Param("ids") List<Long> ids);

    @Update("""
        <script>
        update member_package
        set package_status = #{status},
            updated_at = now()
        where member_id = #{memberId}
          and id in
          <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
          </foreach>
        </script>
        """)
    void updateStatusByMemberIdAndIds(
        @Param("memberId") Long memberId,
        @Param("ids") List<Long> ids,
        @Param("status") String status
    );

    @Update("""
        <script>
        update member_package
        set package_status = 'shipment_submitted',
            updated_at = now()
        where member_id = #{memberId}
          and package_status in ('pending_claim', 'in_stock')
          and issue_flag = 0
          and id in
          <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
          </foreach>
        </script>
        """)
    int lockForShipment(@Param("memberId") Long memberId, @Param("ids") List<Long> ids);
}
