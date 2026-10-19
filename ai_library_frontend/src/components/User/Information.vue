<template>
  <div class="information_container">
    <!-- 背景图片层 - 半透明效果 -->
    <div class="backgroundImg">
      <img alt="" src="@/assets/images/background.jpg"/>
    </div>

    <!-- 页面标题区 - 主标题+英文标语 -->
    <div class="information_header">
      <p>个人信息</p>
      <p>Nothing is impossible with a willing heart.</p>
    </div>

    <!-- 信息展示区 - 左右分栏布局（左侧信息/右侧按钮） -->
    <div v-loading="loading"
         class="information_banner"
         element-loading-background="rgba(0, 0, 0, 0.8)"
         element-loading-spinner="el-icon-loading"
         element-loading-text="拼命加载中"
    >
      <!-- 左侧：个人信息卡片 -->
      <div class="information_banner_left">
        <div v-if="show" class="banner_left_main">
          <!-- 借阅证编号 -->
          <div class="number">
            <i class="el-icon-collection-tag"></i> 借阅证编号: {{ this.user.cardNumber }}
          </div>

          <!-- 借阅证姓名 -->
          <div class="name">
            <i class="iconfont icon-gerenxinxi"></i> 借阅证姓名: {{ this.user.cardName }}
          </div>

          <!-- 规则编号 -->
          <div class="rule">
            <i class="iconfont icon-guizeshezhi"></i> 规则编号: {{ this.user.ruleNumber }}
          </div>

          <!-- 账户状态（1:可用 0:禁用） -->
          <div class="status">
            <i class="el-icon-refresh"></i> 状态: {{ this.user.status === 1 ? "可用" : "禁用" }}
          </div>
        </div>
      </div>

      <!-- 右侧：修改密码按钮 -->
      <div class="information_banner_right">
        <el-button v-if="show" class="changePWD" type="primary" @click="showEditDialog">
          修改密码
        </el-button>
      </div>

      <!-- 修改密码对话框 -->
      <el-dialog
          :visible.sync="editDialogVisible"
          title="修改密码"
          width="50%"
          @close="editDialogClosed"
      >
        <el-form
            ref="editFormRef"
            :model="editForm"
            :rules="editFormRules"
            label-width="120px"
        >
          <!-- 新密码输入框 -->
          <el-form-item label="新密码" prop="password">
            <el-input v-model="editForm.password" placeholder="请输入新密码" type="password"></el-input>
          </el-form-item>

          <!-- 确认密码输入框（带一致性校验） -->
          <el-form-item label="新密码" prop="confirmPassword">
            <el-input v-model="editForm.confirmPassword" placeholder="请再次输入新密码" type="password"></el-input>
          </el-form-item>
        </el-form>

        <span slot="footer" class="dialog-footer">
           <el-button @click="editDialogVisible = false">取 消</el-button>
           <el-button type="primary" @click="changePassword">确 定</el-button>
         </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
/**
 * 读者个人信息查看与密码修改组件
 *
 * 功能：展示用户基本信息（借阅证号/姓名/规则编号/状态）+ 修改密码
 * 特点：加载状态反馈、密码一致性校验、修改后强制重新登录
 */
export default {
  data() {
    // 自定义验证器：确认密码必须与新密码一致
    var validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.editForm.password) {
        callback(new Error('两次输入密码不一致!'));
      } else {
        callback();
      }
    };

    return {
      // 用户信息对象
      user: {
        ruleNumber: Number,
        cardNumber: Number,
        status: Number,
        userId: Number,
        cardName: "",
        username: "",
        password: "",
        createTime: "",
        updateTime: "",
      },

      // 修改密码表单
      editForm: {
        password: "",          // 新密码
        confirmPassword: "",   // 确认密码
      },

      // 表单验证规则
      editFormRules: {
        password: [
          {required: true, message: "请输入新密码", trigger: "blur"},
          {min: 6, max: 15, message: "新密码长度在6-15个字符", trigger: "blur"}
        ],
        confirmPassword: [
          {validator: validatePass2, trigger: "blur"}  // 使用自定义验证器
        ]
      },

      editDialogVisible: false,  // 修改密码对话框显示状态
      show: false,               // 信息展示开关（数据加载完成后显示）
      loading: true              // 加载状态
    };
  },

  methods: {
    /**
     * 打开修改密码对话框
     */
    showEditDialog() {
      this.editDialogVisible = true;
    },

    /**
     * 关闭对话框时重置表单
     */
    editDialogClosed() {
      this.$refs.editFormRef.resetFields();
    },

    /**
     * 获取用户详细信息
     *
     * 流程：
     * 1. 从sessionStorage获取userId
     * 2. 调用接口查询用户信息
     * 3. 成功后显示信息卡片（show=true）并关闭loading
     *
     * 接口：GET user/getUserInfo/:userId
     */
    async getUserInformaton() {
      const userId = window.sessionStorage.getItem("userId");

      this.loading = true;
      const {data: res} = await this.$http.get(
          "user/getUserInfo/" + userId
      );

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.$message.success({
        message: res.msg,
        duration: 1000,
      });

      this.user = res.data;
      this.show = true;   // 数据加载完成，显示信息卡片
      this.loading = false;
    },

    /**
     * 修改密码
     *
     * 流程：
     * 1. 提交新密码和userId到后端
     * 2. 成功后清空sessionStorage（强制退出）
     * 3. 跳转到登录页重新登录
     *
     * 接口：POST user/updatePassword
     * 参数：{password, userId}
     *
     * 安全策略：
     * - 修改密码后立即清除会话，防止旧密码继续有效
     * - 用户必须使用新密码重新登录
     */
    async changePassword() {
      const {data: res} = await this.$http.post('user/updatePassword', {
        password: this.editForm.password,
        userId: window.sessionStorage.getItem('userId')
      })

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.$message.success(res.msg)
      this.editDialogVisible = false;
      this.$refs.editFormRef.resetFields();

      // 修改密码后清除会话并跳转登录页（安全策略）
      window.sessionStorage.clear();
      this.$router.push("/login");
    }
  },

  created() {
    this.getUserInformaton(); // 组件初始化时加载用户信息
  },
};
</script>

<style lang="less" scoped>
/* 容器 - 相对定位，撑满全屏 */
.information_container {
  position: relative;
  height: 100%;
}

/* 背景图片层 - 绝对定位覆盖全屏 */
.backgroundImg {
  position: absolute;
  width: 100%;
  height: 100%;

  img {
    width: 100%;
    height: 100%;
    opacity: 0.3; /* 更低透明度，突出前景内容 */
  }
}

/* 标题区域 - 居中双行文本 */
.information_header {
  height: 200px;
  text-align: center;

  p:nth-child(1) {
    line-height: 140px;
    color: var(--chai-text-primary);
    font-size: 36px;
  }

  p:nth-child(2) {
    color: var(--chai-text-secondary);
    font-size: 24px;
  }
}

/* 信息展示区 - Flex左右分栏 */
.information_banner {
  display: flex;
  flex-direction: row;
  height: 400px;

  /* 左侧信息卡片区域 */

  .information_banner_left {
    flex: 0.5;
    text-align: center;
  }

  /* 右侧按钮区域 */

  .information_banner_right {
    flex: 0.5;
    text-align: left;
    line-height: 400px;
    position: relative;
  }
}

/* 左侧信息主体 - 垂直居中 */
.banner_left_main {
  margin-top: 120px;
  color: var(--chai-text-primary);
  font-size: 20px;

  div {
    margin-top: 15px;
  }
}

/* 修改密码按钮 - 绝对定位垂直居中 */
.changePWD {
  position: absolute;
  top: 50%;
  left: 20px;
  transform: translateY(-50%);
  transition: transform 0.2s ease; /* 平滑过渡动画 */
}

/* 悬停效果 - 轻微上移 */
.changePWD:hover {
  transform: translateY(calc(-50% - 2px));
}
</style>