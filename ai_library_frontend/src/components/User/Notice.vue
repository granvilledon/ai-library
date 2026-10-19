<template>
  <div class="notice_container">
    <!-- 顶部滚动公告栏 - 渐变背景 + CSS动画 -->
    <div class="header">
      <div class="scroll-text">
        <i class="el-icon-s-opportunity"></i> {{ text }}
        <i class="el-icon-s-opportunity"></i>
      </div>
    </div>

    <!-- 公告列表区域 -->
    <div class="banner">
      <!-- 二级标题 -->
      <div class="banner_header"><p>近期公告</p></div>

      <!-- 公告卡片列表 - 支持加载状态 -->
      <div
          v-loading="loading"
          class="banner_main"
          element-loading-background="rgba(0, 0, 0, 0.8)"
          element-loading-spinner="el-icon-loading"
          element-loading-text="拼命加载中"
      >
        <!-- 遍历公告列表 - 标题+时间+内容 -->
        <div v-for="item in noticeList" :key="item.noticeId" class="banner_main_item">
          <!-- 卡片头部：标题和发布日期 -->
          <div class="banner_main_item_header">
            <p>{{ item.noticeTitle }} {{ item.createTime }}</p>
          </div>

          <!-- 卡片主体：公告详细内容 -->
          <div class="banner_main_item_main">
            <p>{{ item.noticeContent }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 读者端公告查看组件
 *
 * 功能：展示图书馆最新公告列表 + 顶部滚动标语
 * 特点：CSS跑马灯效果、卡片式布局、加载状态反馈
 */
export default {
  data() {
    return {
      text: "图书馆公告栏",  // 顶部滚动显示的文本
      noticeList: [],        // 公告数据数组 [{noticeId, noticeTitle, noticeContent, createTime}]
      loading: true          // 加载状态
    };
  },

  methods: {
    /**
     * 获取公告列表数据
     *
     * 接口：GET user/getNoticeList
     * 响应：[{noticeId, noticeTitle, noticeContent, createTime}, ...]
     *
     * 注意：方法名 getRuleList 存在命名歧义，建议修改为 getNoticeList
     */
    async getRuleList() {
      this.loading = true;
      const {data: res} = await this.$http.get("user/getNoticeList");

      if (res.status !== 200) {
        this.loading = false;
        return this.$message.error(res.msg);
      }

      this.noticeList = res.data;
      this.loading = false;
    }
  },

  created() {
    this.getRuleList(); // 组件初始化时加载公告列表
  }
};
</script>

<style lang="less" scoped>
/* 容器 - 最小高度100vh，内边距20px */
.notice_container {
  overflow: hidden;
  background-color: var(--chai-light);
  min-height: 100vh;
  padding: 20px;
}

/* 顶部滚动公告栏 - 渐变背景 */
.header {
  width: 100%;
  height: 50px;
  background: linear-gradient(135deg, var(--chai-primary) 0%, var(--chai-primary-dark) 100%);
  border-radius: 15px;
  color: var(--chai-text-primary);
  line-height: 50px;
  font-size: 24px;
  overflow: hidden; /* 隐藏超出部分，配合动画实现滚动效果 */
}

/* 滚动文本 - CSS动画实现跑马灯效果 */
.scroll-text {
  white-space: nowrap; /* 不换行 */
  animation: scroll 12s linear infinite; /* 12秒匀速无限循环 */
}

/* 定义从右向左的位移动画 */
@keyframes scroll {
  from {
    transform: translateX(100%); /* 起始位置：右侧屏幕外 */
  }
  to {
    transform: translateX(-100%); /* 结束位置：左侧屏幕外 */
  }
}

/* 公告列表容器 */
.banner {
  width: 100%;
  margin-top: 30px;
}

/* 二级标题区域 */
.banner_header {
  height: 80px;

  p {
    color: var(--chai-text-primary);
    font-size: 30px;
    text-align: center;
    line-height: 80px;
  }
}

/* 公告卡片列表 - Flex垂直居中排列 */
.banner_main {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 单个公告卡片 - 固定高度120px */
.banner_main_item {
  width: 80%;
  height: 120px;
  border-radius: 10px;
  overflow: hidden; /* 防止圆角溢出 */

  /* 第一个卡片无外边距 */

  &:nth-child(1) {
    background-color: var(--chai-light);
  }

  /* 第2个及以后的卡片统一上边距20px */

  &:nth-child(n + 2) {
    background-color: var(--chai-light);
    margin-top: 20px;
  }

  /* 卡片头部 - 主色背景 */

  .banner_main_item_header {
    height: 50px;
    border: 1px solid var(--chai-primary);
    box-sizing: border-box;
    background-color: var(--chai-primary);

    p {
      color: var(--chai-text-primary);
      text-align: center;
      line-height: 50px;
      font-size: 16px;
    }
  }

  /* 卡片主体 - 浅色背景 */

  .banner_main_item_main {
    height: 70px;
    background-color: var(--chai-light);
    border: 1px solid var(--chai-primary);
    box-sizing: border-box;
    text-align: center;

    p {
      line-height: 70px;
      color: var(--chai-text-secondary);
    }
  }
}
</style>