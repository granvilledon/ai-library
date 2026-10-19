<template>
  <div class="intelligent_analysis">
    <el-row :gutter="24">
      <el-col :span="12">
        <el-card class="analysis-card" shadow="always">
          <div slot="header" class="card-header">
            <span class="card-title">数据智能分析</span>
          </div>
          <el-form
              ref="form"
              :model="form"
              :rules="rules"
              enctype="multipart/form-data"
              label-width="100px"
          >
            <el-form-item label="分析目标" prop="goal">
              <el-input
                  v-model="form.goal"
                  :rows="4"
                  placeholder="请输入您的数据分析目标，例如：分析近3个月各类图书的借阅趋势"
                  type="textarea"
              ></el-input>
            </el-form-item>
            <el-form-item label="图表名称" prop="name">
              <el-input
                  v-model="form.name"
                  placeholder="请输入生成的图表名称"
              ></el-input>
            </el-form-item>
            <el-form-item label="图表类型" prop="chartType">
              <el-select
                  v-model="form.chartType"
                  placeholder="请选择图表类型"
                  style="width: 100%"
              >
                <el-option label="折线图" value="折线图"></el-option>
                <el-option label="柱状图" value="柱状图"></el-option>
                <el-option label="堆叠图" value="堆叠图"></el-option>
                <el-option label="饼图" value="饼图"></el-option>
                <el-option label="雷达图" value="雷达图"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="原始数据">
              <el-upload
                  ref="upload"
                  :auto-upload="false"
                  :file-list="fileList"
                  :limit="1"
                  :on-change="handleChange"
                  :on-exceed="handleExceed"
                  :on-remove="handleRemove"
                  action="https://jsonplaceholder.typicode.com/posts/"
                  class="upload-demo"
                  multiple
                  name="file"
              >
                <el-button icon="el-icon-upload2" size="mini" type="primary"
                >上传Excel文件
                </el-button>
                <div slot="tip" class="el-upload__tip">
                  只能上传xls/xlsx文件，且不超过1MB
                </div>
              </el-upload>
            </el-form-item>

            <el-form-item class="form-actions">
              <el-button
                  v-loading="submitting"
                  :disabled="submitting"
                  size="mini"
                  type="primary"
                  @click="onSubmit"
              >提交分析
              </el-button>
              <el-button
                  :disabled="submitting"
                  size="mini"
                  @click="resetForm"
              >重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card v-loading="submitting" class="analysis-card" shadow="always">
          <div slot="header" class="card-header">
            <span class="card-title">分析结论</span>
          </div>
          <div v-if="chart.genResult" class="result-content">
            {{ chart.genResult }}
          </div>
          <div v-else class="empty-hint">
            <i class="el-icon-info"></i>
            <span>请先在左侧填写信息并提交分析</span>
          </div>
        </el-card>
        <el-card v-loading="submitting" class="analysis-card chart-card" shadow="always">
          <div slot="header" class="card-header">
            <span class="card-title">可视化图表</span>
          </div>
          <div v-show="chart.genChart" class="chart-wrapper">
            <div
                ref="chartContainer"
                class="chartContainer"
            ></div>
          </div>
          <div v-show="!chart.genChart" class="empty-hint">
            <i class="el-icon-data-line"></i>
            <span>提交分析后将在此处显示可视化图表</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script>
// 引入ECharts图表库
import * as echarts from "echarts";

/**
 * 数据智能分析组件
 *
 * 功能：基于AI模型生成数据分析报告和可视化图表
 * 特点：支持多种图表类型、Excel数据上传、表单验证、异步分析
 *
 * 配置：
 * - 支持图表类型：折线图、柱状图、堆叠图、饼图、雷达图
 * - 文件限制：仅支持xls/xlsx格式，大小不超过1MB
 * - 分析目标输入限制：5-100个字符
 *
 * 接口：
 * - POST /admin/gen - 提交分析请求，返回分析结果和图表配置
 */
export default {
  data() {
    return {
      // 表单数据对象
      form: {
        goal: "",        // 分析目标描述
        name: "",        // 图表名称
        chartType: "",   // 图表类型
        adminId: undefined, // 当前管理员ID
      },
      // 上传的文件对象
      file: {},
      // 表单验证规则
      rules: {
        goal: [
          {required: true, message: "请输入分析目标", trigger: "blur"},
          {min: 5, max: 100, message: "长度在 5 到 100 个字符", trigger: "blur"},
        ],
      },
      // 图表数据对象
      chart: {
        genChart: "",   // 生成的图表配置
        genResult: "",  // 分析结论文本
        chartId: "",    // 图表ID
      },
      // 解析后的图表配置（用于ECharts渲染）
      resultChart: {},
      // 上传文件列表（用于展示）
      fileList: [],
      // 提交状态标识（防止重复提交）
      submitting: false,
    };
  },
  methods: {
    /**
     * 文件数量超过限制时触发
     *
     * @param {File[]} files - 待上传的文件列表
     * @param {File[]} fileList - 已上传的文件列表
     */
    handleExceed(files, fileList) {
      this.$message.info({
        message: "最多只能上传一个文件",
        duration: 1500,
      });
    },

    /**
     * 文件状态改变时触发（添加、上传成功、上传失败）
     *
     * 执行流程：
     * 1. 检查文件类型是否为xls或xlsx
     * 2. 检查文件大小是否超过1MB
     * 3. 验证通过后保存文件对象
     *
     * @param {Object} file - 当前操作的文件对象
     * @param {File[]} fileList - 当前文件列表
     */
    handleChange(file, fileList) {
      // 允许的文件类型：xls和xlsx
      const acceptTypeList = [
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      ];

      // 检查文件类型
      if (acceptTypeList.indexOf(file.raw.type) === -1) {
        this.$message.info({
          message: "只允许xls或者xlsx格式的文件上传",
          duration: 1500,
        });
        this.handleRemove(file);
        return;
      }

      // 检查文件大小（1MB）
      const acceptSize = 1 * 1024 * 1024;
      if (file.raw.size > acceptSize) {
        this.$message.info({
          message: "只允许文件大小在1MB之内的Excel文件",
          duration: 1500,
        });
        this.handleRemove(file);
        return;
      }

      // 保存文件对象
      this.file = file.raw;
    },

    /**
     * 提交分析请求
     *
     * 执行流程：
     * 1. 检查是否正在提交（防止重复提交）
     * 2. 构建FormData对象（包含文件和表单数据）
     * 3. 发送POST请求到AI分析接口
     * 4. 处理响应，更新图表数据和分析结果
     * 5. 渲染可视化图表
     */
    async onSubmit() {
      // 防止重复提交
      if (this.submitting) {
        return;
      }

      this.submitting = true;

      // 构建FormData对象
      const formData = new FormData();

      // 添加上传文件
      if (this.file) {
        formData.append("file", this.file);
      }

      // 设置管理员ID
      this.form.adminId = parseInt(window.sessionStorage.getItem("adminId"));

      // 添加表单字段
      for (const key in this.form) {
        formData.append(key, this.form[key]);
      }

      // 提示用户接口调用耗时
      this.$message.info({
        message: "接口调用花费时间在30秒左右，AI模型暂时不稳定",
        duration: 2500,
      });

      // 发送分析请求
      const {data: res} = await this.$http.post("/admin/generateChart", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      // 请求失败处理
      if (res.status !== 200) {
        this.submitting = false;
        return this.$message.error({
          message: res.msg,
          duration: 1500,
        });
      }

      // 更新图表数据
      this.chart = res.data;
      this.resultChart = res.map.genChart;

      // 渲染图表
      this.createChart();

      // 提示成功
      this.$message.success({
        message: res.msg,
        duration: 1500,
      });

      this.submitting = false;
    },

    /**
     * 使用ECharts渲染可视化图表
     *
     * 通过ECharts.init初始化图表实例，使用后端返回的配置项渲染图表
     */
    createChart() {
      const chartOption = this.resultChart;

      if (!chartOption) {
        return this.$message.error({
          message: "图表代码解析错误",
          duration: 1500,
        });
      }

      // 等待DOM更新后再初始化图表
      this.$nextTick(() => {
        const chart = echarts.init(this.$refs.chartContainer);
        chart.setOption(chartOption);
      });
    },

    /**
     * 重置表单
     *
     * 重置表单字段值并清除已上传的文件
     */
    resetForm() {
      this.$refs.form.resetFields();
      this.handleRemove();
    },

    /**
     * 移除上传的文件
     *
     * @param {Object} [file] - 要移除的文件（可选）
     * @param {File[]} [fileList] - 当前文件列表（可选）
     */
    handleRemove(file, fileList) {
      this.fileList = fileList || [];
      this.file = {};
    },
  },

  /**
   * 组件挂载（预留钩子）
   */
  mounted() {
  },
};
</script>
<style lang="less" scoped>
.intelligent_analysis {
  padding: 20px;
}

.analysis-card {
  margin-bottom: 24px;
  border-radius: 8px;

  ::v-deep .el-card__header {
    padding: 16px 20px;
    border-bottom: 2px solid var(--chai-primary);
    background: linear-gradient(135deg, var(--chai-primary-light) 0%, var(--chai-light) 100%);
  }

  ::v-deep .el-card__body {
    padding: 24px;
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--chai-text-primary);
  letter-spacing: 1px;
}

::v-deep .el-form-item {
  margin-bottom: 16px;
}

::v-deep .el-upload {
  line-height: 1;
}

::v-deep .el-upload__tip {
  margin-top: 4px;
  margin-bottom: 0;
  font-size: 12px;
}

.form-actions {
  margin-top: 8px !important;
  margin-bottom: 0;
  padding-top: 0;
}

.result-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: var(--chai-text-primary);
  font-size: 14px;
  min-height: 200px;
  padding: 16px;
  background: var(--chai-light);
  border-radius: 6px;
  border-left: 4px solid var(--chai-primary);
}

.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: var(--chai-text-secondary);
  font-size: 14px;

  i {
    font-size: 48px;
    margin-bottom: 16px;
    opacity: 0.5;
  }
}

.chart-wrapper {
  width: 100%;
  height: 500px;
}

.chartContainer {
  width: 100%;
  height: 100%;
}

.chart-card {
  ::v-deep .el-card__body {
    padding: 16px;
  }
}

@media screen and (max-width: 576px) {
  .intelligent_analysis {
    padding: 12px;
  }

  ::v-deep .el-col {
    width: 100%;
  }

  .chart-wrapper {
    height: 350px;
  }
}
</style>