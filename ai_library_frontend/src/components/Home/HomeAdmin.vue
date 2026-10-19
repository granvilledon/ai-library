<template>
  <el-container class="home-container">
    <!-- 顶部导航栏：欢迎信息 + 管理员头像/名称/退出按钮 -->
    <el-header>
      <div>
        <span>欢迎登录图书管理系统</span>
        <span style="color: var(--chai-light); font-size: 16px">系统管理人员页面</span>
      </div>
      <div>
        <!-- 管理员头像 - 柴犬图标 -->
        <div>
          <el-avatar :size="35" :src="require('@/assets/images/dog.png')" style="margin-right: 10px"></el-avatar>
        </div>
        <!-- 管理员名称显示 -->
        <div class="user">管理员:{{ this.admin.adminName }}</div>
        <!-- 退出登录按钮 -->
        <el-button type="primary" @click="logout">退出</el-button>
      </div>
    </el-header>

    <el-container>
      <!-- 左侧侧边栏菜单 - 支持折叠展开 -->
      <el-aside :width="isCollapse ? '64px' : '200px'">
        <!-- 折叠切换按钮 -->
        <div class="toggle-button" @click="toggleCollapse">|||</div>

        <!-- 主导航菜单 - 路由模式 + 自定义主题色 -->
        <el-menu
            :collapse="isCollapse"
            :collapse-transition="false"
            :default-active="activePath"
            :router="true"
            active-background-color="var(--chai-primary)"
            active-text-color="var(--chai-text-primary)"
            background-color="var(--chai-light)"
            class="el-menu-vertical-demo"
            text-color="var(--chai-text-secondary)"
        >
          <!-- 书籍管理 -->
          <el-menu-item index="bookmanage" @click="saveNavState('bookmanage')">
            <i class="el-icon-notebook-1"></i>
            <span slot="title">书籍管理</span>
          </el-menu-item>

          <!-- 书籍类型管理 -->
          <el-menu-item index="booktype" @click="saveNavState('booktype')">
            <i class="el-icon-notebook-2"></i>
            <span slot="title">书籍类型管理</span>
          </el-menu-item>

          <!-- 借阅证管理 -->
          <el-menu-item index="statementmanage" @click="saveNavState('statementmanage')">
            <i class="el-icon-bank-card"></i>
            <span slot="title">借阅证管理</span>
          </el-menu-item>

          <!-- 借阅信息查询 -->
          <el-menu-item index="statementsearch" @click="saveNavState('statementsearch')">
            <i class="el-icon-search"></i>
            <span slot="title">借阅信息查询</span>
          </el-menu-item>

          <!-- 借阅规则管理 -->
          <el-menu-item index="statementrulemanage" @click="saveNavState('statementrulemanage')">
            <i class="el-icon-key"></i>
            <span slot="title">借阅规则管理</span>
          </el-menu-item>

          <!-- 图书管理员管理 -->
          <el-menu-item index="bookadminmanage" @click="saveNavState('bookadminmanage')">
            <i class="el-icon-user-solid"></i>
            <span slot="title">图书管理员管理</span>
          </el-menu-item>

          <!-- 数据看板 -->
          <el-menu-item index="adminmanage" @click="saveNavState('adminmanage')">
            <i class="el-icon-s-tools"></i>
            <span slot="title">数据看板</span>
          </el-menu-item>

          <!-- 数据智能分析 -->
          <el-menu-item index="intelligent_analysis" @click="saveNavState('intelligent_analysis')">
            <i class="el-icon-data-line"></i>
            <span slot="title">数据智能分析</span>
          </el-menu-item>

          <!-- AI大模型智能分析 -->
          <el-menu-item index="admin_intelligent" @click="saveNavState('admin_intelligent')">
            <i class="el-icon-chat-dot-square"></i>
            <span slot="title">AI大模型智能分析</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 - 动态渲染子路由组件 -->
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
/**
 * 系统管理员端主框架布局组件
 *
 * 功能：顶部导航栏（管理员信息+退出）+ 左侧可折叠菜单（9个管理模块）+ 右侧内容区
 * 特点：路由联动、菜单状态持久化、自定义主题色系（柴犬棕黄）、响应式折叠
 */
export default {
  data() {
    return {
      menulist: [],  // 未使用的数据，建议删除

      // 未使用的图标映射对象，建议删除
      iconsObj: {
        125: "iconfont icon-user",
        103: "iconfont icon-tijikongjian",
        101: "iconfont icon-shangpin",
        102: "iconfont icon-danju",
        145: "iconfont icon-baobiao",
      },

      isCollapse: false,   // 侧边栏折叠状态（false:展开 true:折叠）
      activePath: "",      // 当前激活的菜单路径

      // 管理员信息对象（初始化为空值）
      admin: {
        adminId: Number,
        status: Number,
        adminName: "",
        username: "",
        password: "",
        createTime: "",
        updateTime: "",
      },
    };
  },

  /**
   * 组件创建时初始化数据
   *
   * 流程：
   * 1. 从sessionStorage恢复上次访问的菜单路径（默认"bookmanage"）
   * 2. 首次登录时强制跳转到书籍管理页（通过firstLoginDone标记）
   * 3. 获取当前登录管理员ID并调用接口加载完整管理员信息
   */
  async created() {
    // 恢复菜单选中状态（刷新页面后保持）
    this.activePath = window.sessionStorage.getItem("activePath") || "bookmanage";

    // 首次登录检测 - 确保登录后进入默认页面
    if (!window.sessionStorage.getItem("firstLoginDone")) {
      window.sessionStorage.setItem("firstLoginDone", "true");
      this.$router.push("/bookmanage");
    }

    // 从sessionStorage获取管理员ID
    const stringId = window.sessionStorage.getItem("adminId");
    const id = parseInt(stringId);
    this.admin.adminId = id;

    // 调用接口获取管理员详细信息
    const {data: res} = await this.$http.post("admin/getAdminInfo", this.admin);
    this.admin = res.data;
  },

  methods: {
    /**
     * 退出登录
     *
     * 流程：清空sessionStorage → 跳转到系统管理员登录页
     * 影响：所有本地存储的管理员信息、Token、菜单状态都会被清除
     */
    logout() {
      window.sessionStorage.clear();
      this.$router.push("/loginadmin");
    },

    /**
     * 切换侧边栏折叠状态
     *
     * 效果：展开(200px) ↔ 折叠(64px)
     * 用途：节省屏幕空间，专注内容区
     */
    toggleCollapse() {
      this.isCollapse = !this.isCollapse;
    },

    /**
     * 保存当前选中的菜单路径到sessionStorage
     *
     * 作用：刷新页面后保持菜单选中状态
     * 参数：activePath - 菜单项的index值（如"bookmanage"、"adminmanage"）
     */
    saveNavState(activePath) {
      window.sessionStorage.setItem("activePath", activePath);
      this.activePath = activePath;
    },
  },
};
</script>

<style lang="less" scoped>
/* 容器高度撑满全屏 */
.home-container {
  height: 100%;
}

/* 顶部导航栏 - 渐变背景 + Flex布局 */
.el-header {
  background: linear-gradient(135deg, var(--chai-primary) 0%, var(--chai-primary-dark) 100%);
  display: flex;
  justify-content: space-between; /* 左右两端对齐 */
  padding-left: 20px;
  align-items: center;
  color: var(--chai-text-primary);
  font-size: 20px;
  border-radius: 12px;
  margin-bottom: 10px;

  > div {
    display: flex;
    align-items: center;

    span {
      margin-left: 15px;
    }
  }
}

/* 左侧侧边栏 - 浅色背景 + 圆角边框 */
.el-aside {
  background-color: var(--chai-light);
  border-radius: 12px;
  overflow: hidden; /* 防止内容溢出圆角 */
  margin-right: 10px;
  border: 1px solid var(--chai-primary);

  .el-menu {
    border-right: none; /* 移除Element UI默认右边框 */
    border-radius: 12px;
  }
}

/* 主内容区 - 浅色背景 + 内边距 */
.el-main {
  background-color: var(--chai-light);
  padding: 20px;
  border-radius: 12px;
  border: 1px solid var(--chai-primary);
}

/* 图标统一右间距 */
.iconfont {
  margin-right: 10px;
}

/* 折叠切换按钮 - 悬停变色反馈 */
.toggle-button {
  background-color: var(--chai-primary);
  font-size: 10px;
  line-height: 24px;
  color: var(--chai-text-primary);
  text-align: center;
  letter-spacing: 0.2em; /* 字符间距拉大，增强视觉效果 */
  cursor: pointer;
  border-bottom: 1px solid var(--chai-primary-dark);
  border-radius: 12px 12px 0 0;
}

.toggle-button:hover {
  background-color: var(--chai-primary-dark);
  color: var(--chai-white);
}

/* 管理员名称显示样式 */
.user {
  margin-right: 15px;
  color: var(--chai-text-primary);
  font-size: 16px;
}

/* 菜单项悬停效果 - 半透明主色背景 */
.el-menu-item:hover {
  background-color: rgba(212, 165, 116, 0.3) !important;
}

/* 菜单项激活状态 - 实心主色背景 */
.el-menu-item.is-active {
  background-color: var(--chai-primary) !important;
  color: var(--chai-text-primary) !important;
}
</style>