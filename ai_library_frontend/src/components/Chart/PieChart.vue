<template>
  <!-- ECharts饼图容器 - 撑满父元素 -->
  <div id="echart-pie" :style="{ width: '100%', height: '100%' }"></div>
</template>

<script>
import * as echarts from "echarts";

/**
 * ECharts饼图封装组件
 *
 * 功能：接收分类数据并渲染交互式饼图
 * 使用场景：图书分类占比统计、数据分布可视化
 * 调用方式：父组件通过ref调用 initChart(pieData)
 * 数据格式：[{name: "文学", value: 100}, {name: "科技", value: 200}]
 */
export default {
  methods: {
    /**
     * 初始化/更新饼图
     *
     * @param {Array} pieData - 饼图数据数组，格式：[{name: string, value: number}]
     *
     * 配置说明：
     * - title: 标题居中显示，标识图表主题
     * - tooltip: 悬停显示单项详细数据（触发类型：item）
     * - legend: 图例垂直排列在左侧，支持点击切换显示
     * - series: 饼图系列，radius:50%控制大小
     * - emphasis: 高亮样式，悬停时显示阴影增强交互感
     *
     * 注意：
     * - 未监听resize事件，窗口缩放时可能不会自适应（建议补充）
     * - option && myChart.setOption(option) 是防御性编程，确保option存在才渲染
     */
    initChart(pieData) {
      // 获取DOM元素并初始化ECharts实例
      var chartDom = document.getElementById("echart-pie");
      var myChart = echarts.init(chartDom);

      // 图表配置项
      var option = {
        // 标题组件 - 居中显示
        title: {
          text: "借书类型分析统计图",  // 标题文本
          left: "center",              // 位置：水平居中
        },

        // 提示框组件 - 鼠标悬停显示数据
        tooltip: {
          trigger: "item",  // 触发类型：数据项触发（饼图常用）
        },

        // 图例组件 - 垂直排列在左侧
        legend: {
          orient: "vertical",  // 排列方向：垂直
          left: "left",        // 位置：左侧对齐
        },

        // 数据系列配置
        series: [
          {
            name: "借书类型分析统计图",  // 系列名称，用于tooltip显示
            type: "pie",                 // 图表类型：饼图
            radius: "50%",               // 饼图半径：占容器50%
            data: pieData,               // 数据源

            // 高亮状态样式 - 悬停时生效
            emphasis: {
              itemStyle: {
                shadowBlur: 10,           // 阴影模糊程度
                shadowOffsetX: 0,         // 阴影水平偏移
                shadowColor: "rgba(0, 0, 0, 0.5)", // 阴影颜色（半透明黑色）
              },
            },
          },
        ],
      };

      // 应用配置项渲染图表（防御性编程：确保option存在）
      option && myChart.setOption(option);
    },
  },
};
</script>

<style>
/* 样式为空，使用全局样式 */
</style>