<template>
  <div class="expire_container">
    <!-- 页面标题 -->
    <div class="header">
      <p>归还图书</p>
    </div>

    <!-- 归还操作区 - 垂直居中布局 -->
    <div class="banner">
      <!-- 剩余天数显示 - 逾期时显示负数 -->
      <div class="expireDays">
        <p>还剩{{ this.expireInfo.expireDays }}天</p>
      </div>

      <!-- 图书编号（只读） -->
      <div class="bookNumber">
        <el-input v-model="expireInfo.bookNumber" :readonly="true">
          <template slot="prepend">图书编号</template>
          <el-button slot="prepend" icon="el-icon-collection"></el-button>
        </el-input>
      </div>

      <!-- 截止日期（只读） -->
      <div class="closeDate">
        <el-input v-model="expireInfo.closeDate" :readonly="true">
          <template slot="prepend">截止日期</template>
          <el-button slot="prepend" icon="el-icon-date"></el-button>
        </el-input>
      </div>

      <!-- 违章信息（可选填写） -->
      <div class="violationMsg">
        <el-input
            v-model="returnInfo.violationMessage"
            placeholder="若无违章信息,则不填"
        >
          <template slot="prepend">违章信息</template>
          <el-button slot="prepend" icon="iconfont icon-weizhangchaxun"></el-button>
        </el-input>
      </div>

      <!-- 管理员编号（只读，从sessionStorage获取） -->
      <div class="adminId">
        <el-input v-model="expireInfo.bookAdminId" :readonly="true">
          <template slot="prepend">管理员编号</template>
          <el-button slot="prepend" icon="el-icon-s-custom"></el-button>
        </el-input>
      </div>

      <!-- 归还日期时间选择器 -->
      <div class="returnDate">
        <el-date-picker
            v-model="returnInfo.returnDate"
            default-time="12:00:00"
            placeholder="选择归还日期时间"
            prefix-icon="el-icon-date"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
        >
        </el-date-picker>
      </div>

      <!-- 归还按钮 -->
      <div class="return_button">
        <el-button type="warning" @click="returnBook">归还</el-button>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 图书归还处理组件
 *
 * 功能：查询借阅到期信息 → 填写归还日期/违章信息 → 提交归还
 * 特点：自动计算剩余天数、逾期自动标记违章、只读字段保护
 */
export default {
  data() {
    return {
      // 归还信息表单
      returnInfo: {
        returnDate: "",           // 归还日期时间
        violationMessage: "",     // 违章信息（可选）
        bookNumber: 0,            // 图书编号
        violationAdminId: 0       // 处理管理员ID
      },

      // 到期信息展示（只读）
      expireInfo: {
        expireDays: 47,           // 剩余天数（负数表示逾期）
        bookNumber: 192,          // 图书编号
        closeDate: "",            // 截止日期
        bookAdminId: 0            // 当前管理员ID
      }
    };
  },

  methods: {
    /**
     * 获取图书到期信息
     *
     * 流程：
     * 1. 从路由参数获取bookNumber
     * 2. 调用接口查询到期信息
     * 3. 填充表单并设置管理员ID
     * 4. 逾期时自动填充违章信息
     *
     * 接口：GET bookadmin/queryOverdueInfo/:bookNumber
     */
    async getExpireInfo() {
      const bookNumber = this.$route.query.bookNumber;
      const {data: res} = await this.$http.get('bookadmin/queryOverdueInfo/' + bookNumber);

      if (res.status !== 200) {
        return this.$message.error(res.msg)
      }

      this.$message.success({
        message: res.msg,
        duration: 1000
      })

      this.expireInfo = res.data;

      // 从sessionStorage获取当前管理员ID
      const id = window.sessionStorage.getItem('bookAdminId')
      this.expireInfo.bookAdminId = parseInt(id);
      this.returnInfo.bookNumber = this.expireInfo.bookNumber;

      // 逾期时自动填充违章信息（expireDays < 0）
      if (this.expireInfo.expireDays < 0) {
        this.returnInfo.violationMessage = "逾期未还";
      }
    },

    /**
     * 提交图书归还
     *
     * 流程：
     * 1. 验证归还日期不为空
     * 2. 设置处理管理员ID
     * 3. 调用归还接口
     * 4. 成功后跳转到归还记录页
     *
     * 接口：POST bookadmin/returnBook
     */
    async returnBook() {
      // 验证归还日期
      if (this.returnInfo.returnDate === "") {
        this.$message.error({
          message: "归还日期不能为空",
          duration: 1000
        })
        return;
      }

      // 设置处理管理员ID
      this.returnInfo.violationAdminId = parseInt(window.sessionStorage.getItem('bookAdminId'))

      const {data: res} = await this.$http.post('bookadmin/returnBook', this.returnInfo)

      if (res.status !== 200) {
        return this.$message.error(res.msg)
      }

      this.$message.success({
        message: res.msg,
        duration: 1000
      })

      // 跳转到归还记录页面
      this.$router.push('/returnbook');
    }
  },

  created() {
    this.getExpireInfo(); // 组件初始化时加载到期信息
  }
};
</script>

<style lang="less" scoped>
/* 页面标题区域 */
.header {
  text-align: center;
  height: 100px;

  p {
    font-size: 30px;
    line-height: 100px;
  }
}

/* 操作区容器 - Flex垂直居中布局 */
.banner {
  height: 680px;
  display: flex;
  flex-direction: column;
  align-items: center;

  /* 剩余天数卡片 */

  .expireDays {
    margin-top: 20px;
    width: 700px;
    height: 100px;
    border: 2px solid #ccc;
    text-align: center;

    p {
      line-height: 100px;
      font-size: 24px;
    }
  }

  /* 第2个及以后的div元素统一上边距 */

  div:nth-child(n + 2) {
    margin-top: 25px;
  }
}
</style>