<template>
  <!-- 应用根容器 - 渲染当前路由匹配的组件 -->
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<script>
/**
 * Vue应用根组件
 *
 * 作用：
 * - 作为整个应用的入口点
 * - 通过<router-view>动态渲染路由匹配的组件
 * - 所有页面组件都嵌套在此容器中
 *
 * 注意：
 * - 全局样式应在此处或main.js中引入
 * - 不要在此组件中添加业务逻辑
 */
export default {
  name: 'app',
}
</script>

<style>
/* 全局样式已在 main.js 和 global.css 中统一管理 */
</style>
