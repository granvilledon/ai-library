<template>
  <!-- Swiper轮播图容器 -->
  <div class="swiper-container">
    <!-- 轮播内容包裹层 -->
    <div class="swiper-wrapper">
      <!-- 第一张幻灯片 - 书籍图片1 -->
      <div class="swiper-slide">
        <img :src="book1" alt="书籍1"/>
      </div>
      <!-- 第二张幻灯片 - 书籍图片2 -->
      <div class="swiper-slide">
        <img :src="book2" alt="书籍2"/>
      </div>
      <!-- 第三张幻灯片 - 书籍图片3 -->
      <div class="swiper-slide">
        <img :src="book3" alt="书籍3"/>
      </div>
    </div>

    <!-- 分页器（小圆点）- 白色样式 -->
    <div class="swiper-pagination swiper-pagination-white"></div>

    <!-- 左右切换按钮 - 白色样式 -->
    <div class="swiper-button-prev swiper-button-white"></div>
    <div class="swiper-button-next swiper-button-white"></div>
  </div>
</template>

<script>
// 引入Swiper库
import Swiper from "swiper";

/**
 * 首页轮播图组件
 *
 * 功能：展示3张书籍宣传图的自动轮播效果
 * 特点：自动播放、循环滚动、支持手动切换、带分页器和导航按钮
 *
 * 配置：
 * - 自动播放间隔：2秒
 * - 切换效果：滑动（slide）
 * - 循环模式：开启
 * - 用户交互后继续自动播放
 */
export default {
  data() {
    return {
      // 加载三张书籍宣传图片资源
      book1: require("@/assets/images/book1.jpg"),
      book2: require("@/assets/images/book2.jpg"),
      book3: require("@/assets/images/book3.jpg"),
    };
  },

  /**
   * 组件挂载后初始化Swiper实例
   *
   * 注意：必须在DOM渲染完成后才能初始化Swiper
   */
  mounted() {
    this.initSwiper();
  },

  methods: {
    /**
     * 初始化Swiper轮播图实例
     *
     * 配置项说明：
     * - loop: true - 开启循环播放模式
     * - autoplay: 2000 - 自动播放间隔2秒
     * - autoplayDisableOnInteraction: false - 用户操作后仍继续自动播放
     * - effect: "slide" - 切换效果为滑动
     * - pagination: 绑定分页器DOM元素
     * - prevButton/nextButton: 绑定左右切换按钮
     *
     * 注意：此版本使用的是Swiper旧版API（v5及之前）
     */
    initSwiper() {
      new Swiper(".swiper-container", {
        loop: true,                              // 循环播放
        autoplay: 2000,                          // 自动播放间隔（毫秒）
        autoplayDisableOnInteraction: false,     // 用户交互后不停止自动播放
        effect: "slide",                         // 切换效果：滑动
        pagination: ".swiper-pagination",        // 分页器选择器
        prevButton: ".swiper-button-prev",       // 上一张按钮选择器
        nextButton: ".swiper-button-next",       // 下一张按钮选择器
      });
    },
  },
};
</script>

<style scoped>
/* 引入Swiper样式文件 */
@import "../../../node_modules/swiper/dist/css/swiper.css";

/* 轮播图容器 - 固定尺寸 */
.swiper-container {
  width: 1000px;
  height: 650px;
  margin-top: 40px; /* 距离顶部间距 */
}

/* 幻灯片图片 - 填充容器并保持比例 */
.swiper-slide img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 保持宽高比并裁剪以填满容器 */
}
</style>