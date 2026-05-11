<script lang="ts">
export default {
  onLaunch() {
    console.log('QSD miniapp launched')
  },
}
</script>

<style lang="scss">
page {
  background: #f4efe6;
  color: #1f2a26;
  font-size: 28rpx;
}

.app-shell {
  min-height: 100vh;
}

.page {
  min-height: 100vh;
  padding: 32rpx 28rpx 48rpx;
  box-sizing: border-box;
}

.card {
  background: #fffdf8;
  border: 2rpx solid #e9e0d2;
  border-radius: 28rpx;
  padding: 28rpx;
  box-shadow: 0 18rpx 30rpx rgba(52, 43, 28, 0.06);
}

.section-title {
  margin: 0 0 18rpx;
  font-size: 36rpx;
  font-weight: 700;
  color: #1d2f28;
}

.section-subtitle {
  margin: 0;
  color: #6f6a60;
  line-height: 1.6;
}

.button-primary {
  background: #0e6b56;
  color: #ffffff;
}

.button-secondary {
  background: #e9dfcf;
  color: #29453d;
}

.pill {
  display: inline-flex;
  align-items: center;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: #e4f2ed;
  color: #0e6b56;
  font-size: 24rpx;
}

.field-label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 26rpx;
  color: #5f5a50;
}

.field-stack {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.empty-text {
  color: #8b8476;
  text-align: center;
  padding: 36rpx 0;
}
</style>
