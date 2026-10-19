import Vue from 'vue'
import App from './App.vue'
import router from './router'
import './plugins/element.js'
import VueParticles from 'vue-particles'
import './assets/css/global.css'
import './assets/fonts/iconfont.css'
import {Message} from 'element-ui'
import ExportExcel from './components/Utils/ExportExcel.vue'
import htmlToPdf from './components/Utils/htmlToPdf.js'
import * as echarts from 'echarts'
import 'default-passive-events'
import _ from 'lodash'
// 弹幕插件（用户留言功能）
import {vueBaberrage} from 'vue-baberrage'
// ==================== Axios HTTP客户端配置 ====================
import axios from 'axios'
// NProgress进度条库
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// ==================== 全局组件注册 ====================
// Excel导出按钮组件（<export-excel>）- 使用 xlsx 库生成真正的 Excel 格式
Vue.component('exportExcel', ExportExcel)

// PDF导出插件（this.getPdf()）
Vue.use(htmlToPdf)

// ==================== 全局工具库挂载 ====================
// ECharts图表库（this.$echarts）
Vue.prototype.$echarts = echarts

// Lodash工具库（this._，如_.throttle防抖）
Vue.prototype._ = _

// ==================== 第三方插件注册 ====================
// 粒子背景特效（仅开发环境）
Vue.use(VueParticles)

Vue.use(vueBaberrage)

// API基础路径（开发环境指向本地后端）
// 示例：this.$http.post('user/login') → http://localhost:8080/api/user/login
axios.defaults.baseURL = 'http://localhost:8080/api/'
Vue.prototype.$http = axios

/**
 * Axios请求拦截器
 *
 * 作用：
 * 1. 显示顶部加载进度条（NProgress.start）
 * 2. 自动为每个请求添加JWT Token认证头
 *
 * 注意：
 * - Token从sessionStorage读取，格式："Bearer {token}"
 * - 如果Token不存在，header值为"Bearer null"（后端需处理）
 */
axios.interceptors.request.use(config => {
    NProgress.start()
    config.headers.Authorization = "Bearer " + window.sessionStorage.getItem('token')
    return config
})

/**
 * Axios响应拦截器
 *
 * 作用：
 * 1. 隐藏加载进度条（NProgress.done）
 * 2. 统一处理401未授权错误（Token过期或无效）
 *
 * 401处理流程：
 * - 显示错误提示
 * - 清空sessionStorage（Token、用户ID等）
 * - 强制跳转到登录页（router.replace避免历史记录）
 *
 * 注意：
 * - 只处理业务状态码401（response.data.status）
 * - 不处理HTTP状态码401（如需处理需添加额外判断）
 */
axios.interceptors.response.use(response => {
    NProgress.done()

    if (response.data.status === 401) {
        Message.error("未登录或登录过期，请重新登录")
        window.sessionStorage.clear()
        router.replace('/login')
    }

    return response
})

// 关闭生产环境提示（开发环境保持开启以便调试）
Vue.config.productionTip = false

/**
 * 创建并挂载Vue根实例
 *
 * 配置项：
 * - router: 注入路由实例
 * - render: 渲染函数，将App组件渲染为虚拟DOM
 *
 * 挂载点：#app（对应public/index.html中的<div id="app"></div>）
 */
new Vue({
    router,
    render: h => h(App)
}).$mount('#app')