const {defineConfig} = require('@vue/cli-service')
const NodePolyfillPlugin = require('node-polyfill-webpack-plugin')
const path = require('path')

/**
 * Vue CLI构建配置
 *
 * 作用：自定义Webpack配置、开发服务器、环境区分
 * 注意：修改后需重启开发服务器
 */
module.exports = defineConfig({

    /**
     * 公共路径配置
     *
     * 作用：指定打包后静态资源的引用路径
     * '/' 表示从网站根目录加载资源
     * 如果不配置，可能导致生产环境资源404，页面空白
     */
    publicPath: '/',

    /**
     * Webpack配置
     */
    configureWebpack: {

        /**
         * 注入Node.js polyfill插件
         *
         * 原因：jspdf等库依赖Node.js模块（buffer/process/stream），浏览器环境需polyfill
         * 常见错误：未配置时报"Module not found: Can't resolve 'buffer'"或"process is not defined"
         */
        plugins: [new NodePolyfillPlugin()],

        /**
         * 模块解析配置
         */
        resolve: {
            alias: {
                /**
                 * 路径别名：@ 代表 ./src
                 *
                 * 示例：import Home from '@/components/Home/Home.vue'
                 * 注意：需同步配置jsconfig.json以支持IDE智能提示
                 */
                "@": path.resolve("./src")
            }
        },
    },

    /**
     * 转译依赖配置
     *
     * true表示转译node_modules中所有依赖（兼容旧版浏览器）
     * 性能影响：增加构建时间，如只需转译特定包可改为数组：['swiper', 'some-lib']
     */
    transpileDependencies: true,

    /**
     * ESLint保存时检查
     *
     * false关闭保存时的lint检查（提升开发体验），建议提交前统一运行npm run lint
     */
    lintOnSave: false,

    /**
     * 开发服务器配置
     */
    devServer: {
        /**
         * 监听地址：'0.0.0.0'允许局域网访问（便于手机调试）
         * 可选值：'localhost'（仅本地）或 '0.0.0.0'（推荐）
         */
        host: '0.0.0.0',

        /**
         * 端口号：8081（避免与默认8080冲突）
         */
        port: 8081,

        /**
         * WebSocket配置：用于热模块替换（HMR）
         */
        client: {
            webSocketURL: 'ws://0.0.0.0/ws',
        },
    },

    /**
     * Webpack链式配置（更细粒度控制）
     *
     * 与configureWebpack区别：
     * - configureWebpack: 合并对象，简单配置
     * - chainWebpack: 链式调用，复杂配置
     */
    chainWebpack: config => {

        /**
         * 生产环境配置（npm run build）
         */
        config.when(process.env.NODE_ENV === 'production', config => {
            /**
             * 设置入口为main-prod.js（移除粒子特效等开发工具，体积更小）
             */
            config.entry('app').clear().add('./src/main-prod.js')

            /**
             * 向index.html传递isProd变量（用于条件加载CDN资源）
             */
            config.plugin('html').tap(args => {
                args[0].isProd = true
                return args
            })
        })

        /**
         * 开发环境配置（npm run serve）
         */
        config.when(process.env.NODE_ENV === 'development', config => {
            /**
             * 设置入口为main-dev.js（包含粒子特效和调试工具）
             */
            config.entry('app').clear().add('./src/main-dev.js')

            config.plugin('html').tap(args => {
                args[0].isProd = false
                return args
            })
        })
    }
})
