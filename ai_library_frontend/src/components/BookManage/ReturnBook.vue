<template>
  <div class="borrowbook_container">
    <!-- 页面标题 -->
    <div class="header"><p>查看图书是否逾期</p></div>

    <!-- 查询操作区 - 垂直居中布局 -->
    <div class="banner">
      <!-- 图书编号输入框 -->
      <div class="book_number">
        <el-input v-model="bookNumber" placeholder="请输入图书编号">
          <el-button slot="prepend" icon="el-icon-collection"></el-button>
        </el-input>
      </div>

      <!-- 查询按钮 - 跳转到逾期详情页 -->
      <div class="query_button">
        <el-button type="warning" @click="goExpire">查询</el-button>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 图书逾期查询入口组件
 *
 * 功能：输入图书编号 → 验证图书存在性 → 跳转到归还处理页
 * 特点：两步式归还流程（先查询后处理）、路由传参
 */
export default {
  data() {
    return {
      bookNumber: "",  // 待查询的图书编号
    };
  },

  methods: {
    /**
     * 查询图书并跳转到逾期处理页
     *
     * 流程：
     * 1. 调用接口验证图书编号是否存在
     * 2. 验证通过则携带bookNumber跳转到BookExpire页面
     * 3. 验证失败则提示错误信息
     *
     * 接口：GET bookadmin/queryBookStatus/:bookNumber
     * 路由跳转：name: "bookexpire", query: {bookNumber}
     */
    async goExpire() {
      const {data: res} = await this.$http.get(
          "bookadmin/queryBookStatus/" + this.bookNumber
      );

      if (res.status !== 200) {
        return this.$message.error({
          message: res.msg,
          duration: 1000
        });
      }

      // 跳转到逾期详情/归还处理页面，通过query传递图书编号
      this.$router.push({
        name: "bookexpire",
        query: {
          bookNumber: this.bookNumber
        }
      });
    },
  },
};
</script>

<style lang="less" scoped>
/* 容器 - Flex垂直居中布局 */
.borrowbook_container {
  display: flex;
  flex-direction: column;
  align-items: center;

  /* 页面标题区域 */

  .header {
    width: 100%;
    height: 100px;

    p {
      color: var(--chai-text-primary);
      font-size: 30px;
      text-align: center;
      line-height: 60px;
    }
  }

  /* 操作区容器 - 60%宽度居中 */

  .banner {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 60%;
    height: 400px;

    /* 所有子元素统一上边距 */

    div {
      margin-top: 15px;
    }

    /* 查询按钮增加上边距，与输入框拉开距离 */

    .query_button {
      margin-top: 30px;
    }
  }
}
</style>