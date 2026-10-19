<template>
  <div class="chat-container">
    <!-- 页面标题 -->
    <h2 style="text-align: center;color:var(--chai-primary);">AI大模型智能推荐</h2>

    <!-- 聊天主容器 - 高度80vh -->
    <el-card class="chat" style="margin-top:10px;height: 80vh;border-radius: 15px;border-color: var(--chai-primary);">
      <!-- 聊天记录展示区 - 支持滚动 -->
      <el-card ref="chatContainer" class="main_chat">
        <!-- 遍历消息列表 - 交替显示用户输入和AI回复 -->
        <div v-for="item in messages" :key="item.id" class="message">
          <!-- 用户消息气泡 - 右对齐 -->
          <div v-if="item.inputMessage!=null&&item.inputMessage!==''" class="user-message">
            {{ item.inputMessage }}
          </div>

          <!-- AI回复气泡 - 左对齐 -->
          <div v-if="item.aiResult!=null&&item.aiResult!==''" class="bot-message">{{ item.aiResult }}</div>
        </div>
      </el-card>

      <!-- 输入框区域 - 支持回车发送 -->
      <el-input
          v-model.trim="inputMessage"
          :disabled="loading"
          placeholder="请输入您的要求，例如：我喜欢科幻类的书籍，请给我推荐"
          @keyup.enter.native="sendMessage"
      >
        <!-- 发送按钮 - 带加载状态 -->
        <el-button
            slot="append"
            v-loading="loading"
            :disabled="loading"
            icon="el-icon-s-promotion"
            @click="sendMessage"
        ></el-button>
      </el-input>
    </el-card>
  </div>
</template>

<script>
/**
 * AI大模型智能推荐对话组件
 *
 * 功能：与AI助手进行多轮对话获取图书推荐 + 历史记录回显
 * 特点：流式对话展示、自动滚动到底部、输入内容校验、异步加载提示
 */
export default {
  data() {
    return {
      messages: [],           // 对话消息数组 [{id, inputMessage, aiResult, userId}]
      inputMessage: "",       // 当前输入框内容

      // 发送给后端的单条消息模板
      message: {
        inputMessage: "",     // 用户输入
        aiResult: "",         // AI回复（初始为空）
        userId: null          // 用户ID
      },

      loading: false          // 加载状态（防止重复提交）
    }
  },

  methods: {
    /**
     * 自动滚动到聊天窗口底部
     *
     * 使用场景：
     * 1. 发送新消息后
     * 2. 接收AI回复后
     * 3. 加载历史记录后
     *
     * 注意：必须使用 $nextTick 确保DOM更新后再滚动
     */
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.chatContainer;
        if (container) {
          container.$el.scrollTop = container.$el.scrollHeight;
        }
      });
    },

    /**
     * 发送消息给AI并获取推荐结果
     *
     * 流程：
     * 1. 校验输入内容（非空/非纯数字字母）
     * 2. 立即在界面显示用户消息（乐观更新）
     * 3. 调用AI接口获取推荐（耗时约10秒）
     * 4. 显示AI回复并滚动到底部
     *
     * 接口：POST user/aiRecommend
     * 参数：{inputMessage, userId}
     * 响应：res.data - AI生成的推荐文本
     *
     * 注意：
     * - AI接口响应较慢，提前提示用户等待
     * - loading状态防止用户在等待期间重复点击
     */
    async sendMessage() {
      // 校验：不能为空
      if (this.inputMessage.trim() === "") {
        return this.$message.info({
          message: "文本不能为空",
          duration: 1500
        })
      }

      // 校验：不能是纯数字或纯字母（无意义内容）
      if (/^[\da-zA-Z]+$/.test(this.inputMessage.trim())) {
        this.$message.info({
          message: "请勿输入无意义的内容",
          duration: 1000
        });
        return;
      }

      const userId = window.sessionStorage.getItem("userId");

      // 立即显示用户消息（乐观更新，提升体验）
      this.messages.push({
        id: Date.now(),
        inputMessage: this.inputMessage,
        userId: parseInt(userId)
      });

      // 滚动到底部，让用户看到刚发送的消息
      this.scrollToBottom();

      // 准备请求数据
      this.message.inputMessage = this.inputMessage.trim();
      this.message.userId = userId;
      this.inputMessage = "";  // 清空输入框
      this.loading = true;     // 开启加载状态

      // 提示用户AI接口需要较长时间
      this.$message.info({
        message: "文本输入越长，接口调用时间越长，请耐心等待10秒左右",
        duration: 2500
      })

      // 调用AI推荐接口
      const {data: res} = await this.$http.post("user/aiRecommend", this.message)

      if (res.status !== 200) {
        this.loading = false;
        return this.$message.error(res.msg);
      }

      this.$message.success({
        message: res.msg,
        duration: 1500
      })

      // 显示AI回复
      this.messages.push({
        id: Date.now(),
        aiResult: res.data,
        userId: userId
      });

      // 再次滚动到底部，让用户看到AI回复
      this.scrollToBottom();
      this.loading = false;  // 关闭加载状态
    },

    /**
     * 获取历史对话记录
     *
     * 流程：
     * 1. 从sessionStorage获取userId
     * 2. 调用接口查询该用户的历史对话
     * 3. 成功后赋值给messages数组并滚动到底部
     * 4. 失败则清空消息列表（容错处理）
     *
     * 接口：GET user/getAiChatHistory/:userId
     * 响应：[{id, inputMessage, aiResult, userId}, ...]
     *
     * 注意：
     * - 使用 try-catch 捕获网络异常
     * - 失败时静默处理，不影响用户使用
     */
    async getHistoryRecords() {
      const userId = window.sessionStorage.getItem("userId");

      try {
        const {data: res} = await this.$http.get("user/getAiChatHistory/" + userId)

        if (res.status !== 200) {
          this.messages = [];
          return;
        }

        this.messages = res.data || [];
        this.scrollToBottom();  // 加载完成后滚动到底部
      } catch (error) {
        // 网络异常或其他错误，清空消息列表
        this.messages = [];
      }
    }
  },

  /**
   * 组件创建时加载历史对话记录
   */
  created() {
    this.getHistoryRecords();
  }
}
</script>

<style lang="less" scoped>
/* 聊天记录展示区 - 背景图 + 可滚动 */
.main_chat {
  margin-bottom: 10px;
  height: 70vh;
  background-size: cover;
  /* Less中引用alias路径需加~前缀 */
  background: url("~@/assets/images/background.jpg") no-repeat 0px 0px;
  border-radius: 10px;
  overflow-y: scroll; /* 垂直滚动 */
}

/* 消息容器 - Flex纵向排列 */
.message {
  display: flex;
  flex-direction: column;
}

/* 用户消息气泡 - 右对齐，浅色背景 */
.user-message {
  background-color: var(--chai-light);
  padding: 10px;
  border-radius: 10px;
  align-self: flex-end; /* 右对齐 */
  text-align: right;
  margin-top: 10px;
  max-width: 80%; /* 最大宽度限制 */
  word-break: break-word; /* 长单词自动换行 */
}

/* AI回复气泡 - 左对齐，浅色背景 */
.bot-message {
  background-color: var(--chai-light);
  padding: 10px;
  border-radius: 10px;
  text-align: left;
  align-self: flex-start; /* 左对齐 */
  margin-top: 10px;
  white-space: pre-wrap; /* 保留AI返回的换行符 */
  max-width: 80%;
  word-break: break-word;
}
</style>