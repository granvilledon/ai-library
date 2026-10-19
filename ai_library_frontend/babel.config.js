/**
 * Babel转译配置
 *
 * 作用：
 * - 将现代JavaScript语法（ES6+）转译为兼容旧浏览器的代码
 * - 配合.browserslistrc决定转译程度
 */
module.exports = {
    "presets": [
        "@vue/cli-plugin-babel/preset"  // Vue CLI默认预设
    ],
    "plugins": [
        [
            "component",  // Element UI按需引入插件
            {
                "libraryName": "element-ui",
                "styleLibraryName": "theme-chalk"
            }
        ]
    ]
}