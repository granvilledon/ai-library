import Vue from 'vue'
import App from './App.vue'
import router from './router'
import './plugins/element.js'
import './assets/css/global.css'
import './assets/fonts/iconfont.css'
import {Message} from 'element-ui'
import ExportExcel from './components/Utils/ExportExcel.vue'
import htmlToPdf from './components/Utils/htmlToPdf.js'
import * as echarts from 'echarts'
import 'default-passive-events'
import {vueBaberrage} from 'vue-baberrage'
import axios from 'axios'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import _ from 'lodash'

// ==================== 全局组件和工具注册（与开发环境一致）====================
Vue.component('exportExcel', ExportExcel)  // Excel导出组件（基于xlsx库）
Vue.use(htmlToPdf)                         // PDF导出插件
Vue.prototype.$echarts = echarts           // ECharts图表库
Vue.prototype._ = _                        // Lodash工具库
Vue.use(vueBaberrage)                      // 弹幕插件

// ==================== Axios HTTP客户端配置 ====================
/**
 * 配置生产环境API基础路径
 *
 * 从环境变量读取：process.env.VUE_APP_API_BASE_URL
 * 需在.env.production文件中配置：VUE_APP_API_BASE_URL=https://api.yourlibrary.com/api/
 *
 * 注意：
 * - 如果环境变量未配置，默认为空字符串（请求发送到当前域名）
 * - 建议始终配置完整URL，避免跨域问题
 */
axios.defaults.baseURL = process.env.VUE_APP_API_BASE_URL || ''
Vue.prototype.$http = axios

/**
 * Axios请求拦截器
 *
 * 作用：
 * 1. 显示顶部加载进度条（NProgress.start）
 * 2. 自动为每个请求添加JWT Token认证头
 *
 * 注意：Token从sessionStorage读取，格式："Bearer {token}"
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
 * - 清空sessionStorage
 * - 强制跳转到登录页（router.replace避免历史记录）
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

// 关闭生产环境提示（减少控制台输出，提升性能）
Vue.config.productionTip = false

/**
 * 创建并挂载Vue根实例
 *
 * 生产环境与开发环境的区别：
 * - 使用main-prod.js作为入口（移除了粒子特效等开发工具）
 * - API地址指向生产服务器（通过环境变量配置）
 * - 代码经过压缩和优化
 */
new Vue({
    router,
    render: h => h(App)
}).$mount('#app')