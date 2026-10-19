<template>
  <div class="rule_container">
    <div class="header">
      <p>读者规则信息查看</p>
    </div>
    <div class="banner">
      <el-tooltip
          v-for="item in ruleList"
          :key="item.ruleId"
          v-loading="loading"
          effect="dark"
          element-loading-background="rgba(0, 0, 0, 0.8)"
          element-loading-spinner="el-icon-loading"
          element-loading-text="拼命加载中"
          placement="right"
      >
        <div slot="content" class="content_tip">可借阅数量: {{ item.bookLimitNumber }}<br/>可借阅天数: {{
            item.bookDays
          }}
          <br/>可借阅图书馆: {{ item.bookLimitLibrary }}<br/>过期扣费/天: {{ item.bookOverdueFee }}
        </div>
        <el-button style="font-size:16px">借阅证规则编号: {{ item.bookRuleId }}</el-button>
      </el-tooltip>

    </div>
  </div>
</template>

<script>
// 引入Element UI的Loading组件（未直接使用，预留扩展）

/**
 * 读者规则信息查看组件
 *
 * 功能：展示图书馆借阅规则列表，支持悬停查看详细规则信息
 * 特点：加载状态提示、悬停tooltip展示详细规则、响应式布局
 *
 * 规则信息包含：
 * - 借阅证规则编号
 * - 可借阅数量
 * - 可借阅天数
 * - 可借阅图书馆
 * - 过期扣费/天
 */
export default {
  data() {
    return {
      // 借阅规则列表数据
      ruleList: [],
      // 加载状态标识
      loading: true,
    };
  },
  methods: {
    /**
     * 获取借阅规则列表
     *
     * 通过GET请求从后端获取所有借阅规则信息
     * 请求成功后更新规则列表数据
     */
    async getRuleList() {
      const {data: res} = await this.$http.get("user/getRuleList");

      if (res.status !== 200) {
        this.loading = false;
        return this.$message.error(res.msg);
      }

      this.$message.success({
        message: res.msg,
        duration: 1000,
      });

      this.loading = false;
      this.ruleList = res.data;
    },
  },

  /**
   * 组件创建后自动加载借阅规则列表
   */
  created() {
    this.getRuleList();
  },
};
</script>

<style lang="less" scoped>
.rule_container {
  position: relative;
}

.header {
  position: absolute;
  left: 50%;
  transform: translate(-50%);

  p {
    font-size: 36px;
    color: var(--chai-primary); /* 柴犬棕黄 */
  }
}

.banner {
  display: flex;
  flex-direction: column;
  position: absolute;
  left: 50%;
  transform: translate(-50%);

}

.el-tooltip {
  width: 500px;
  font-size: 16px;
  color: var(--chai-text-primary);
}

.el-tooltip:hover {
  color: var(--chai-text-primary);
}

.el-tooltip:nth-child(1) {
  margin-top: 100px;
}

.el-tooltip:nth-child(n+2) {
  margin-top: 50px;
  margin-left: 0px;
}

.el-tooltip {
  background-color: var(--chai-primary); /* 柴犬棕黄 - 统一背景色 */
}

.content_tip {
  font-size: 16px;
  color: var(--chai-text-primary);
}
</style>