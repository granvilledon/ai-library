<template>
  <div>
    <div style="width: 100%; height: 500px">
      <ChartLine ref="chart_line_one"/>
    </div>
    <div style="width: 100%; height: 500px; margin-top: 50px">
      <PieChart ref="chart_pie_one"></PieChart>
    </div>
  </div>
</template>

<script>
// 引入折线图组件
import ChartLine from "../Chart/ChartLine.vue";
// 引入饼图组件
import PieChart from "../Chart/PieChart.vue";

/**
 * 管理员数据看板组件
 *
 * 功能：展示图书馆借阅数据可视化图表
 * 特点：折线图展示借阅趋势、饼图展示借阅类型分布
 *
 * 数据来源：
 * - 借阅趋势数据：admin/getBorrowData
 * - 借阅类型统计：admin/getBorrowTypeStatistics
 */
export default {
  data() {
    return {
      // 图表名称
      name: "借阅量统计折线图",
      // 折线图X轴数据（默认示例数据）
      xData: ["", "2020-03", "2020-04", "2020-05"],
      // 折线图Y轴数据（默认示例数据）
      yData: [30, 132, 80, 134],
      // 图表完整数据对象
      allData: {
        xData: ["", "2020-03", "2020-04", "2020-05"],
        yData: [30, 132, 80, 134],
      },
      // 饼图数据数组
      pieData: [],
    };
  },
  methods: {},

  /**
   * 组件挂载后初始化图表数据
   *
   * 执行流程：
   * 1. 请求借阅趋势数据（折线图）
   * 2. 请求借阅类型统计数据（饼图）
   * 3. 处理饼图数据格式
   * 4. 初始化折线图和饼图组件
   */
  async mounted() {
    const {data: res} = await this.$http.get("admin/getBorrowData");

    if (res.status !== 200) {
      return this.$message.error(res.msg);
    }

    this.$message.success({
      message: res.msg,
      duration: 800,
    });

    const {data: res2} = await this.$http.get(
        "admin/getBorrowTypeStatistics"
    );

    if (res2.status !== 200) {
      return this.$message.error(res2.msg);
    }

    this.$message.success({
      message: res2.msg,
      duration: 800,
    });

    // 转换饼图数据格式
    for (var element of res2.data) {
      this.pieData.push({
        name: element.bookTypes,
        value: element.borrowNumbers,
      });
    }

    // 更新折线图数据
    this.allData.xData = res.data.borrowDates;
    this.allData.yData = res.data.borrowNumber;

    // 初始化图表组件
    this.$refs.chart_line_one.initChart(
        this.name,
        this.allData.xData,
        this.allData.yData
    );
    this.$refs.chart_pie_one.initChart(this.pieData);
  },

  // 注册子组件
  components: {
    ChartLine,
    PieChart,
  },
};
</script>
<style>
</style>