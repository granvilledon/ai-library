<template>
  <!-- AI智能分析聊天界面 -->
  <div class="chat-container">
    <h2 style="text-align: center;color:var(--chai-primary);">AI大模型智能分析</h2>
    <el-card class="chat" style="margin-top:10px;height: 80vh;border-radius: 15px;border-color: var(--chai-primary);">
      <!-- 聊天记录区域 - 可滚动 -->
      <el-card ref="chatContainer" class="main_chat">
        <!-- 遍历消息列表，显示用户提问和AI回复 -->
        <div v-for="item in messages" :key="item.id" class="message">
          <div v-if="item.inputMessage!=null&&item.inputMessage!==''" class="user-message">
            {{ item.inputMessage }}
          </div>
          <div v-if="item.aiResult!=null&&item.aiResult!==''" class="bot-message">{{ item.aiResult }}</div>
        </div>
      </el-card>

      <!-- 输入框区域 - 支持回车发送 -->
      <el-input
          v-model.trim="inputMessage"
          :disabled="loading"
          placeholder="请输入您的问题,例如:最近借阅趋势如何?有哪些热门书籍?"
          @keyup.enter.native="sendMessage"
      >
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
 * 管理员AI智能分析组件
 *
 * 功能：通过AI大模型分析图书馆数据（借阅趋势/热门书籍等）
 * 特点：支持历史对话记录、自动滚动到底部、加载状态提示
 */
export default {
  data() {
    return {
      messages: [],           // 消息列表 [{id, inputMessage, aiResult, userId}]
      inputMessage: "",       // 当前输入内容
      message: {              // 发送给后端的消息对象
        inputMessage: "",
        userId: null
      },
      loading: false          // 加载状态，防止重复提交
    }
  },
  methods: {
    /**
     * 滚动到聊天底部
     * 时机：发送消息后/接收回复后/加载历史记录后
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
     * 发送消息给AI
     * 流程：验证 → 显示用户消息 → 调用接口 → 显示AI回复
     */
    async sendMessage() {
      // 1. 验证输入不为空
      if (this.inputMessage.trim() === "") {
        return this.$message.info({
          message: "文本不能为空",
          duration: 1500
        })
      }

      // 2. 过滤无意义的纯数字/字母输入
      if (/^[\da-zA-Z]+$/.test(this.inputMessage.trim())) {
        this.$message.info({
          message: "请勿输入无意义的内容",
          duration: 1000
        });
        return;
      }

      const adminId = window.sessionStorage.getItem("adminId");

      // 3. 立即显示用户消息（乐观更新）
      this.messages.push({
        id: Date.now(),
        inputMessage: this.inputMessage,
        userId: parseInt(adminId)
      });

      this.scrollToBottom();

      // 4. 准备请求数据并清空输入框
      this.message.inputMessage = this.inputMessage.trim();
      this.message.userId = adminId;
      this.inputMessage = "";
      this.loading = true;

      // 5. 提示用户等待（AI接口响应较慢）
      this.$message.info({
        message: "文本输入越长，接口调用时间越长，请耐心等待10秒左右",
        duration: 2500
      })

      // 6. 调用后端AI接口
      const {data: res} = await this.$http.post("admin/aiIntelligentAnalysis", this.message)
      if (res.status !== 200) {
        this.loading = false;
        return this.$message.error(res.msg);
      }

      this.$message.success({
        message: res.msg,
        duration: 1500
      })

      // 7. 显示AI回复
      this.messages.push({
        id: Date.now(),
        aiResult: res.data,
        userId: adminId
      });

      this.scrollToBottom();
      this.loading = false;
    },

    /**
     * 加载历史对话记录
     * 时机：组件创建时自动调用
     */
    async getHistoryRecords() {
      const adminId = window.sessionStorage.getItem("adminId");
      try {
        const {data: res} = await this.$http.get("admin/getAiChatHistory/" + adminId)
        if (res.status !== 200) {
          this.messages = [];
          return;
        }

        this.messages = res.data || [];
        this.scrollToBottom();
      } catch (error) {
        this.messages = [];
      }
    }
  },
  created() {
    this.getHistoryRecords(); // 组件初始化时加载历史记录
  }
}
</script>

<style lang="less" scoped>
/* 聊天主容器 - 背景图+固定高度+垂直滚动 */
.main_chat {
  margin-bottom: 10px;
  height: 70vh;
  background-size: cover;
  background: url("~@/assets/images/background.jpg") no-repeat 0px 0px;
  border-radius: 10px;
  overflow-y: scroll; /* 超出部分垂直滚动 */
}

/* 消息容器 - 垂直排列 */
.message {
  display: flex;
  flex-direction: column;
}

/* 用户消息 - 右对齐，浅色背景 */
.user-message {
  background-color: var(--chai-light);
  padding: 10px;
  border-radius: 10px;
  align-self: flex-end; /* 靠右对齐 */
  text-align: right;
  margin-top: 10px;
  max-width: 80%; /* 限制最大宽度 */
  word-break: break-word; /* 长单词自动换行 */
}

/* AI回复 - 左对齐，浅色背景 */
.bot-message {
  background-color: var(--chai-light);
  padding: 10px;
  border-radius: 10px;
  text-align: left;
  align-self: flex-start; /* 靠左对齐 */
  margin-top: 10px;
  white-space: pre-wrap; /* 保留AI返回的换行符 */
  max-width: 80%;
  word-break: break-word;
}
</style>