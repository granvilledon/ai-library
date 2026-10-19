import Vue from "vue"
import VueRouter from "vue-router"

// 安装Vue Router插件
Vue.use(VueRouter)

/**
 * 路由配置表
 *
 * 结构说明：
 * - path: 路由路径
 * - component: 对应组件（使用懒加载优化性能）
 * - redirect: 重定向路径
 * - children: 子路由（嵌套路由）
 * - meta: 元信息（如页面标题）
 * - name: 路由名称（用于命名路由跳转）
 *
 * 懒加载语法：() => import('@/components/xxx.vue')
 * 作用：只有访问该路由时才加载对应组件，减小首屏包体积
 */
const routes = [
    // ==================== 默认重定向 ====================
    {path: "/", redirect: "/login"},

    // ==================== 登录页面（无需认证）====================
    {path: "/login", component: () => import("@/components/Login.vue")},
    {path: "/loginmanage", component: () => import("@/components/BookManage/LoginBookManage.vue")},
    {path: "/loginadmin", component: () => import("@/components/Admin/LoginAdmin.vue")},

    // ==================== 读者端页面（需要认证）====================
    {
        path: "/home",
        component: () => import("@/components/Home/Home.vue"),
        redirect: "/index", // 默认显示首页
        meta: {role: "user"}, // 读者角色
        children: [
            {
                path: "/index",
                component: () => import("@/components/Index/Index.vue"),
                meta: {title: "首页", role: "user"}
            },
            {path: "/search", component: () => import("@/components/User/Search.vue"), meta: {role: "user"}},
            {path: "/rule", component: () => import("@/components/User/Rule.vue"), meta: {role: "user"}},
            {path: "/notice", component: () => import("@/components/User/Notice.vue"), meta: {role: "user"}},
            {path: "/information", component: () => import("@/components/User/Information.vue"), meta: {role: "user"}},
            {path: "/borrow", component: () => import("@/components/User/Borrow.vue"), meta: {role: "user"}},
            {path: "/violation", component: () => import("@/components/User/Violation.vue"), meta: {role: "user"}},
            {path: "/comment", component: () => import("@/components/User/Comment.vue"), meta: {role: "user"}},
            {path: "/intelligent", component: () => import("@/components/User/Intelligent.vue"), meta: {role: "user"}},
        ],
    },

    // ==================== 图书管理员端页面（需要认证）====================
    {
        path: "/homemanage",
        component: () => import("@/components/Home/HomeManage.vue"),
        meta: {role: "bookAdmin"}, // 图书管理员角色
        children: [
            {
                path: "/borrowbook",
                component: () => import("@/components/BookManage/BorrowBook.vue"),
                meta: {role: "bookAdmin"}
            },
            {
                path: "/returnbook",
                component: () => import("@/components/BookManage/ReturnBook.vue"),
                meta: {role: "bookAdmin"}
            },
            {
                path: "/borrowstatement",
                component: () => import("@/components/BookManage/BorrowStatement.vue"),
                meta: {role: "bookAdmin"}
            },
            {
                path: "/returnstatement",
                component: () => import("@/components/BookManage/ReturnStatement.vue"),
                meta: {role: "bookAdmin"}
            },
            {
                path: "/noticemanage",
                component: () => import("@/components/BookManage/NoticeManage.vue"),
                meta: {role: "bookAdmin"}
            },
            {
                path: "/bookexpire",
                name: "bookexpire",
                component: () => import("@/components/BookManage/BookExpire.vue"),
                meta: {role: "bookAdmin"}
            },
        ],
    },

    // ==================== 系统管理员端页面（需要认证）====================
    {
        path: "/homeadmin",
        component: () => import("@/components/Home/HomeAdmin.vue"),
        meta: {role: "admin"}, // 系统管理员角色
        children: [
            {path: "/bookmanage", component: () => import("@/components/Admin/BookManage.vue"), meta: {role: "admin"}},
            {path: "/booktype", component: () => import("@/components/Admin/BookType.vue"), meta: {role: "admin"}},
            {
                path: "/statementmanage",
                component: () => import("@/components/Admin/StatementManage.vue"),
                meta: {role: "admin"}
            },
            {
                path: "/statementsearch",
                component: () => import("@/components/Admin/StatementSearch.vue"),
                meta: {role: "admin"}
            },
            {
                path: "/statementrulemanage",
                component: () => import("@/components/Admin/StatementRuleManage.vue"),
                meta: {role: "admin"}
            },
            {
                path: "/bookadminmanage",
                component: () => import("@/components/Admin/BookAdminManage.vue"),
                meta: {role: "admin"}
            },
            {
                path: "/adminmanage",
                component: () => import("@/components/Admin/AdminManage.vue"),
                meta: {role: "admin"}
            },
            {
                path: "/intelligent_analysis",
                component: () => import("@/components/Admin/IntelligentAnalysis.vue"),
                meta: {role: "admin"}
            },
            {
                path: "/admin_intelligent",
                component: () => import("@/components/Admin/AdminIntelligent.vue"),
                meta: {role: "admin"}
            },
        ],
    },

    // ==================== 错误页面 ====================
    {path: "/404", component: () => import("@/components/404/404.vue")},
    // 捕获所有未匹配的路由，重定向到404页面（必须放在最后）
    {path: "/:pathMatch(.*)*", redirect: "/404"},
]

// 创建路由实例
const router = new VueRouter({
    routes,
})

/**
 * 获取当前用户角色
 *
 * 通过sessionStorage中存储的不同ID判断用户角色：
 * - userId: 普通读者
 * - bookAdminId: 图书管理员
 * - adminId: 系统管理员
 *
 * @returns {string|null} 用户角色标识
 */
function getCurrentUserRole() {
    if (window.sessionStorage.getItem("userId")) {
        return "user"
    } else if (window.sessionStorage.getItem("bookAdminId")) {
        return "bookAdmin"
    } else if (window.sessionStorage.getItem("adminId")) {
        return "admin"
    }
    return null
}

/**
 * 全局前置路由守卫
 *
 * 作用：每次路由跳转前检查用户是否已登录并验证角色权限
 *
 * 参数说明：
 * - to: 即将进入的目标路由对象
 * - from: 当前离开的路由对象
 * - next: 回调函数（必须调用才能继续导航）
 *   - next(): 放行
 *   - next('/path'): 跳转到指定路径
 *   - next(false): 中断导航
 *
 * 认证逻辑：
 * 1. 无token：
 *    - 访问登录页（/login、/loginmanage、/loginadmin）→ 放行
 *    - 访问其他页面 → 强制跳转到/login
 * 2. 有token：
 *    - 检查目标路由是否需要特定角色
 *    - 当前用户角色与目标路由要求角色匹配 → 放行
 *    - 角色不匹配 → 跳转到对应角色的首页
 */
router.beforeEach((to, from, next) => {
    const tokenStr = window.sessionStorage.getItem("token")
    const currentRole = getCurrentUserRole()

    if (!tokenStr) {
        // 未登录，只允许访问登录页面
        if (to.path === "/login" || to.path === "/loginmanage" || to.path === "/loginadmin") {
            next()
        } else {
            // 强制跳转到普通用户登录页
            next({path: "/login"})
        }
    } else {
        // 已登录，检查角色权限
        const requiredRole = to.meta.role

        if (requiredRole) {
            // 目标路由需要特定角色
            if (currentRole === requiredRole) {
                // 角色匹配，放行
                next()
            } else {
                // 角色不匹配，跳转到对应角色的首页
                switch (currentRole) {
                    case "user":
                        next({path: "/home"})
                        break
                    case "bookAdmin":
                        next({path: "/homemanage"})
                        break
                    case "admin":
                        next({path: "/homeadmin"})
                        break
                    default:
                        next({path: "/login"})
                }
            }
        } else {
            // 目标路由不需要特定角色（如登录页），放行
            next()
        }
    }
})

export default router