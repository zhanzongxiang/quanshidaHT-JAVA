package com.qsd.admin.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.payment.entity.PayReconcileRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PayReconcileRecordMapper extends BaseMapper<PayReconcileRecord> {

    @Select("""
        <script>
        select id, tenant_id, reconcile_date, channel, reconcile_status, diff_count, summary, created_at, updated_at
        from pay_reconcile_record
        where tenant_id = #{tenantId}
          <if test="channel != null and channel != ''">
            and channel = #{channel}
          </if>
        order by reconcile_date desc, id desc
        </script>
        """)
    List<PayReconcileRecord> selectListByChannel(@Param("tenantId") Long tenantId, @Param("channel") String channel);

    @Select("""
        select id, tenant_id, reconcile_date, channel, reconcile_status, diff_count, summary, created_at, updated_at
        from pay_reconcile_record
        where tenant_id = #{tenantId}
          and reconcile_date = #{reconcileDate}
          and channel = #{channel}
        limit 1
        """)
    PayReconcileRecord selectByDateAndChannel(
        @Param("tenantId") Long tenantId,
        @Param("reconcileDate") LocalDate reconcileDate,
        @Param("channel") String channel
    );

    @Select("""
        select id, tenant_id, reconcile_date, channel, reconcile_status, diff_count, summary, created_at, updated_at
        from pay_reconcile_record
        where tenant_id = #{tenantId}
          and id = #{id}
        limit 1
        """)
    PayReconcileRecord selectActiveById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}
