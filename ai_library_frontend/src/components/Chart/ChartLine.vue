<template>
  <!-- ECharts折线图容器 - 撑满父元素 -->
  <div id="echart-line" :style="{ width: '100%', height: '100%' }"></div>
</template>

<script>
import * as echarts from "echarts";

/**
 * ECharts折线图封装组件
 *
 * 功能：接收数据并渲染交互式折线图
 * 使用场景：借阅趋势分析、数据统计可视化
 * 调用方式：父组件通过ref调用 initChart(name, xData, yData)
 */
export default {
  methods: {
    /**
     * 初始化/更新折线图
     *
     * @param {string} name - 图例名称（如"借阅数量"）
     * @param {Array} xData - X轴数据数组（如日期 ["2024-01", "2024-02"]）
     * @param {Array} yData - Y轴数据数组（如数量 [100, 200]）
     *
     * 配置说明：
     * - title: 图表主标题，居中显示于顶部
     * - tooltip: 悬停显示详细数据，十字准星指示器
     * - legend: 图例位于底部，支持点击切换显示
     * - grid: 图表边距各10%，containLabel确保标签不溢出
     * - xAxis: 类目轴，boundaryGap:false使折线从Y轴开始
     * - yAxis: 数值轴，自动计算刻度
     * - series: 折线系列，name用于图例标识
     *
     * 响应式处理：
     * - 监听window.resize事件，自动调整图表尺寸
     * - 避免窗口缩放时图表变形
     */
    initChart(name, xData, yData) {
      // 获取DOM元素并初始化ECharts实例
      let getchart = echarts.init(document.getElementById("echart-line"));

      // 图表配置项
      var option = {
        // 标题组件 - 显示图表主标题
        title: {
          text: name,
          left: "center",
          top: "2%",
          textStyle: {
            fontSize: 18,
            fontWeight: "bold",
          },
        },

        // 提示框组件 - 鼠标悬停显示数据
        tooltip: {
          trigger: "axis",
          axisPointer: {type: "cross"},
        },

        // 图例组件 - 标识数据系列
        legend: {
          data: [name],
          bottom: "0%",
        },

        // 网格配置 - 控制图表区域边距
        grid: {
          top: "12%",
          left: "10%",
          right: "10%",
          bottom: "10%",
          containLabel: true,
        },

        // X轴配置 - 类目轴（如日期）
        xAxis: {
          type: "category",
          boundaryGap: false,
          data: xData,
          axisLabel: {
            formatter: "{value}",
            align: "center",
          },
        },

        // Y轴配置 - 数值轴
        yAxis: {
          type: "value",
        },

        // 数据系列配置
        series: [
          {
            name: name,
            type: "line",
            data: yData,
          },
        ],
      };

      // 应用配置项渲染图表
      getchart.setOption(option);

      // 监听窗口大小变化，自适应调整图表尺寸
      window.addEventListener("resize", () => {
        getchart.resize();
      });
    },
  },
};
</script>
