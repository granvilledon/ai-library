<template>
  <div class="login_container">
    <!-- 页面标题 -->
    <div class="login_title">用户登录页面</div>

    <!-- 登录表单卡片 -->
    <div class="login_box">
      <!-- 头像区域 - 柴犬图标 -->
      <div class="avatar_box">
        <img alt="" src="../assets/images/dog.jpg"/>
      </div>

      <!-- 登录表单 -->
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginFormRules" class="login_form" label-width="0px">
        <!-- 用户名输入框 - 带图标前缀 -->
        <el-form-item prop="username">
          <el-input v-model.trim="loginForm.username" prefix-icon="iconfont icon-gerenxinxi"></el-input>
        </el-form-item>

        <!-- 密码输入框 - 支持回车登录 -->
        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              :show-password="true"
              prefix-icon="iconfont icon-tianchongxing-"
              type="password"
              @keyup.enter.native="login"
          ></el-input>
        </el-form-item>

        <!-- 按钮组：登录/重置 -->
        <el-form-item class="btns">
          <el-button :loading="loginLoading" type="primary" @click="login">登录</el-button>
          <el-button type="info" @click="resetLoginForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 底部角色切换入口 -->
    <div class="footer">
      <span style="font-weight: bold;color:var(--chai-primary);margin-bottom: 10px">
        登录页面切换
      </span>
      <!-- 切换到图书管理员登录 -->
      <span>
        <i class="iconfont icon-guanliyuan" @click="goManage"></i>
      </span>
    </div>
  </div>
</template>

<script>
/**
 * 读者端登录组件
 *
 * 功能：读者身份验证 + Token存储 + 路由跳转
 * 特点：表单验证、回车快捷登录、加载状态反馈、双角色切换（读者/图管）
 */
export default {
  data() {
    return {
      // 登录表单数据（预设默认值方便测试）
      loginForm: {
        username: "团团",
        password: "123456",
      },

      // 表单验证规则
      loginFormRules: {
        username: [
          {required: true, message: "用户名不能为空", trigger: "blur"},
          {min: 2, max: 20, message: "长度在 2 到 20 个字符", trigger: "blur"},
        ],
        password: [
          {required: true, message: "密码不能为空", trigger: "blur"},
          {min: 6, max: 15, message: "长度在 6 到 15 个字符", trigger: "blur"},
        ],
      },

      loginLoading: false  // 登录按钮加载状态，防止重复提交
    };
  },

  methods: {
    /**
     * 重置登录表单
     *
     * 清除输入内容和验证状态，恢复到初始值
     */
    resetLoginForm() {
      this.$refs.loginFormRef.resetFields();
    },

    /**
     * 读者登录
     *
     * 流程：
     * 1. 表单验证 → 2. 调用登录接口 → 3. 保存Token和ID → 4. 跳转到读者首页
     *
     * 接口：POST user/login
     * 参数：{username, password}
     * 响应：{status: 200, map: {token, id}}
     *
     * 注意：
     * - 使用sessionStorage存储敏感信息，关闭浏览器自动清除
     * - loading状态防止网络慢时重复点击
     * - 第77行定义了salt变量但未使用，建议删除
     */
    login() {
      this.$refs.loginFormRef.validate(async (valid) => {
        if (!valid) return;

        this.loginLoading = true;

        const username = this.loginForm.username;
        const password = this.loginForm.password;

        const {data: res} = await this.$http.post(
            "user/login",
            {username, password}
        );

        if (res.status !== 200) {
          this.loginLoading = false;
          return this.$message.error(res.msg);
        }

        this.$message.success("登录成功");
        this.loginLoading = false;

        // 存储认证信息到sessionStorage
        window.sessionStorage.setItem("token", res.map.token);
        window.sessionStorage.setItem("userId", res.map.id);

        // 跳转到读者端首页框架
        this.$router.push("/home");
      });
    },

    /**
     * 切换到图书管理员登录页
     *
     * 路由跳转：/loginmanage（图书管理员登录）
     */
    goManage() {
      this.$router.push("/loginmanage");
    },
  },
};
</script>

<style lang="less" scoped>

/* 登录容器 - 全屏背景图 */
.login_container {
  background: url(~@/assets/images/background.jpg) no-repeat 0px 0px;
  background-size: cover;
  height: 100%;
}

/* 登录卡片 - 居中定位 */
.login_box {
  height: 300px;
  width: 450px;
  background-color: var(--chai-light);
  border-radius: 3px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%); /* 完美居中技巧 */

  /* 圆形头像框 - 悬浮于卡片上方 */

  .avatar_box {
    width: 130px;
    height: 130px;
    border: 1px solid var(--chai-divider);
    border-radius: 50%;
    padding: 10px;
    box-shadow: 0 0 10px var(--chai-divider); /* 轻微阴影增强立体感 */
    position: absolute;
    left: 50%;
    transform: translate(-50%, -50%);
    background-color: var(--chai-light);

    img {
      width: 100%;
      height: 100%;
      border-radius: 50%;
      background-color: var(--chai-divider);
    }
  }
}

/* 表单区域 - 位于卡片底部 */
.login_form {
  position: absolute;
  bottom: 0;
  width: 100%;
  padding: 0 20px;
  box-sizing: border-box;
}

/* 按钮组 - 右对齐 */
.btns {
  display: flex;
  justify-content: flex-end;
}

/* 页面标题 - 顶部居中 */
.login_title {
  position: relative;
  top: 5%;
  font-size: 36px;
  color: var(--chai-light);
  text-align: center;
  font-weight: 700;
  letter-spacing: 10px; /* 字间距拉大，增强视觉冲击力 */
}

/* 底部角色切换区 - 右下角固定 */
.footer {
  display: flex;
  position: absolute;
  flex-direction: column;
  bottom: 0;
  right: 0;
  width: 100px;
  height: 120px;

  span {
    text-align: center;
  }
}
</style>