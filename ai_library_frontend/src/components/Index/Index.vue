<template>
  <!-- 首页容器 - 嵌入轮播图组件 -->
  <div class="index">
    <swiperVue/>
  </div>
</template>

<script>
// 引入轮播图子组件
import swiperVue from "../../components/Swiper/swiper.vue"

/**
 * 系统首页入口组件（读者端）
 *
 * 功能：作为首页路由的载体，展示轮播图内容
 * 特点：极简设计、仅包含Swiper组件、无额外业务逻辑
 *
 * 注意：
 * - 此组件是读者端Home框架下的默认首页（/index路由）
 * - 实际内容完全由Swiper组件提供
 */
export default {
  name: "index",

  // 注册子组件
  components: {
    swiperVue
  },
};
</script>
