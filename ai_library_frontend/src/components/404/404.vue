<template>
  <!-- 404页面容器 - 全屏背景图展示 -->
  <div class="container">
  </div>
</template>

<script>
/**
 * 404错误页面组件
 *
 * 功能: 捕获未匹配路由,提供友好错误提示
 * 触发场景: 错误URL/已删除资源/路由配置遗漏
 *
 * 路由配置 (router/index.js):
 * { path: '*', component: () => import('@/components/404/404.vue') }
 */
export default {
  name: 'NotFound'
}
</script>

<style scoped>
/* 404页面容器 - 全屏沉浸式设计
 *
 * 布局: 100%宽度 + 100vh高度,确保全屏显示
 * 背景: 居中显示404图片,等比缩放覆盖整个容器
 *
 * 图片规范:
 * - 推荐尺寸: 1920x1080px (16:9)
 * - 文件大小: <200KB,需压缩优化
 * - 格式: PNG/WebP
 */
.container {
  width: 100%;
  height: 100vh; /* 视口高度,始终全屏 */

  /* 背景图: 居中不重复,等比覆盖 */
  background: url(../../assets/images/404.png) no-repeat center;
  background-size: cover; /* 等比缩放至完全覆盖,可能裁剪边缘 */
}
</style>
