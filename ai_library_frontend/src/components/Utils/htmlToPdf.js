import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'

/**
 * HTML转PDF导出工具（Vue插件）
 *
 * 技术原理：
 * 1. html2canvas将DOM渲染为Canvas（截图）
 * 2. jsPDF将Canvas转换为PDF文档
 * 3. 自动分页处理（长页面切割为多个A4页）
 *
 * 使用方式：
 * 1. 在需要导出的容器上添加 id="pdfDom"
 * 2. 组件中调用：this.getPdf('文件名.pdf')
 *
 * 注意事项：
 * - 只能导出可见内容（隐藏DOM无法渲染）
 * - 图片需跨域配置或使用base64
 * - CSS样式可能不完全一致（特别是flex布局）
 * - 大页面可能导致内存溢出（建议分页导出）
 */
export default {

    /**
     * Vue插件安装方法
     *
     * 作用：将getPdf方法挂载到Vue原型，所有组件可通过this.getPdf()调用
     *
     * 安装示例：
     * import htmlToPdf from './components/Utils/htmlToPdf.js'
     * Vue.use(htmlToPdf)
     */
    install(Vue, options) {

        /**
         * PDF导出方法
         *
         * @param {string} htmlTitle - PDF文件名（如'报表.pdf'）
         *
         * 工作流程：
         * 1. 获取id为"pdfDom"的DOM元素
         * 2. html2canvas渲染为Canvas
         * 3. 计算A4比例，分割Canvas为多页
         * 4. 逐页添加到PDF文档并触发下载
         */
        Vue.prototype.getPdf = function (htmlTitle) {

            // 获取要导出的DOM元素
            var element = document.getElementById("pdfDom")

            if (!element) {
                console.error('未找到id="pdfDom"的元素')
                return
            }

            // 第一步：将DOM渲染为Canvas
            html2canvas(element, {
                logging: false  // 关闭控制台日志
            }).then(function (canvas) {

                // 第二步：创建PDF文档（纵向/A4/毫米单位）
                var pdf = new jsPDF('p', 'mm', 'a4')

                // 第三步：计算分页参数
                var ctx = canvas.getContext('2d'),
                    a4w = 190,           // A4可用宽度（mm，留10mm边距）
                    a4h = 277,           // A4可用高度（mm，留10mm边距）
                    imgHeight = Math.floor(a4h * canvas.width / a4w), // 每页像素高度
                    renderedHeight = 0   // 已渲染高度

                // 第四步：分页处理（循环截取Canvas）
                while (renderedHeight < canvas.height) {

                    // 创建临时Canvas存储当前页
                    var page = document.createElement("canvas")
                    page.width = canvas.width
                    // 最后一页可能不足一页高度，取较小值
                    page.height = Math.min(imgHeight, canvas.height - renderedHeight)

                    // 截取当前页对应的图像区域
                    page.getContext('2d').putImageData(
                        ctx.getImageData(
                            0,
                            renderedHeight,
                            canvas.width,
                            Math.min(imgHeight, canvas.height - renderedHeight)
                        ),
                        0,
                        0
                    )

                    // 将当前页添加到PDF（JPEG格式，质量100%）
                    pdf.addImage(
                        page.toDataURL('image/jpeg', 1.0),
                        'JPEG',
                        10, 10,                              // 左边距10mm，上边距10mm
                        a4w,                                 // 宽度190mm
                        Math.min(a4h, a4w * page.height / page.width) // 高度按比例缩放
                    )

                    // 更新已渲染高度
                    renderedHeight += imgHeight

                    // 如果还有内容，添加新页
                    if (renderedHeight < canvas.height) {
                        pdf.addPage()
                    }
                }

                // 第五步：触发下载
                pdf.save(htmlTitle)
            })
        }
    }
}
