<template>
  <div class="borrowbook_container">
    <!-- 页面标题 -->
    <div class="header"><p>借阅图书</p></div>

    <!-- 借阅操作区 - 垂直居中布局 -->
    <div class="banner">
      <!-- 借阅证号输入框 -->
      <div class="card_number">
        <el-input v-model.number="borrowInfo.cardNumber" placeholder="请输入借阅证号">
          <el-button slot="prepend" icon="el-icon-notebook-2"></el-button>
        </el-input>
      </div>

      <!-- 图书编号输入框 -->
      <div class="book_number">
        <el-input v-model.number="borrowInfo.bookNumber" placeholder="请输入图书编号">
          <el-button slot="prepend" icon="el-icon-collection"></el-button>
        </el-input>
      </div>

      <!-- 借阅日期时间选择器 -->
      <div class="borrow_date">
        <el-date-picker
            v-model="borrowInfo.borrowDate"
            default-time="12:00:00"
            placeholder="选择日期时间"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
        >
        </el-date-picker>
      </div>

      <!-- 借阅按钮 -->
      <div class="borrow_button">
        <el-button type="primary" @click="borrowBook">借阅</el-button>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 图书借阅办理组件
 *
 * 功能：输入借阅证号+图书编号+借阅时间 → 提交借阅记录
 * 特点：表单自动清空、管理员ID自动获取、时间格式统一
 */
export default {
  data() {
    return {
      // 借阅信息表单
      borrowInfo: {
        cardNumber: "",      // 借阅证号
        bookNumber: "",      // 图书编号
        borrowDate: "",      // 借阅日期时间
        bookAdminId: 0       // 办理管理员ID（从sessionStorage获取）
      },
    };
  },

  methods: {
    /**
     * 提交图书借阅
     *
     * 流程：
     * 1. 验证借阅时间不为空
     * 2. 从sessionStorage获取管理员ID
     * 3. 调用借阅接口
     * 4. 成功后清空表单，支持连续办理
     *
     * 接口：POST bookadmin/borrowBook
     * 参数：{cardNumber, bookNumber, borrowDate, bookAdminId}
     */
    async borrowBook() {
      // 验证借阅时间
      if (this.borrowInfo.borrowDate === "") {
        return this.$message.error({
          message: "借阅时间不能为空",
          duration: 1000
        })
      }

      // 从sessionStorage获取当前管理员ID
      this.borrowInfo.bookAdminId = parseInt(window.sessionStorage.getItem('bookAdminId'))

      const {data: res} = await this.$http.post('bookadmin/borrowBook', this.borrowInfo)

      if (res.status !== 200) {
        return this.$message.error(res.msg)
      }

      this.$message.success(res.msg)

      // 清空表单，方便连续办理下一笔借阅
      this.borrowInfo.bookNumber = "";
      this.borrowInfo.cardNumber = "";
      this.borrowInfo.borrowDate = "";
    }
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

    /* 日期选择器微调左间距 */

    .borrow_date {
      margin-left: 8px;
    }

    /* 借阅按钮增加上边距，与表单拉开距离 */

    .borrow_button {
      margin-top: 30px;
    }
  }
}
</style>