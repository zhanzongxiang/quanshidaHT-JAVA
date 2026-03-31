<template>
  <div class="dashboard-grid">
    <el-card>
      <div class="metric">
        <div class="label">今日新增线索</div>
        <div class="value">18</div>
      </div>
    </el-card>
    <el-card>
      <div class="metric">
        <div class="label">在途运单</div>
        <div class="value">246</div>
      </div>
    </el-card>
    <el-card>
      <div class="metric">
        <div class="label">待跟进客户</div>
        <div class="value">31</div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.metric .label {
  color: var(--subtext);
  font-size: 13px;
}

.metric .value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
}
</style>
