<template>
  <div class="search_container">
    <el-card shadow="always">
      <!-- 搜索内容和导出区域 -->
      <div class="search-header">
        <div class="search-left">
          <span class="search-label">条件搜索:</span>
          <el-select
              v-model="queryInfo.condition"
              class="search-select"
              filterable
              placeholder="请选择"
          >
            <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            >
            </el-option>
          </el-select>
          <el-input
              v-model="queryInfo.query"
              class="search-input"
              placeholder="请输入内容"
              @keyup.enter.native="searchViolationByPage(true)"
          >
            <el-button
                slot="append"
                icon="el-icon-search"
                @click="searchViolationByPage(true)"
            ></el-button>
          </el-input>
        </div>
        <div class="search-right">
          <el-button class="el-icon-full-screen" size="mini" type="success" @click="fullScreen"
          >全屏
          </el-button>
          <el-button
              class="el-icon-printer"
              size="mini"
              type="primary"
              @click="downLoad"
          >导出PDF
          </el-button>
          <export-excel
              :data="tableData"
              :fields="json_fields"
              :header="title"
              class="export-excel-wrapper"
              name="图书违章表格"
          >
            <el-button class="el-icon-printer" size="mini" type="primary"
            >导出Excel
            </el-button>
          </export-excel>
        </div>
      </div>
      <!-- 表格区域 -->
      <el-table id="pdfDom" v-loading="loading" :data="tableData"
                :default-sort="{prop: 'cardNumber', order: 'ascending'}" border
                element-loading-background="rgba(0, 0, 0, 0.8)"
                element-loading-spinner="el-icon-loading"
                element-loading-text="拼命加载中"
                stripe
                style="width: 100%">
        <el-table-column label="借阅证号" prop="cardNumber" sortable></el-table-column>
        <el-table-column label="图书编号" prop="bookNumber" sortable></el-table-column>
        <el-table-column label="借阅日期" prop="borrowDate" sortable></el-table-column>
        <el-table-column label="截止日期" prop="closeDate" sortable></el-table-column>
        <el-table-column label="归还日期" prop="returnDate" sortable></el-table-column>
        <el-table-column label="违章信息" prop="violationMessage">
        </el-table-column>
        <el-table-column label="处理人" prop="violationAdmin">
        </el-table-column>
        <template slot="empty">
          <span style="color: #8B7355;">暂无数据</span>
        </template>
      </el-table>
      <!-- 分页查询区域 -->
      <el-pagination
          :current-page="this.queryInfo.pageNum"
          :page-size="this.queryInfo.pageSize"
          :page-sizes="[1, 2, 3, 4, 5]"
          :total="this.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      >
      </el-pagination>
    </el-card>
  </div>
</template>

<script>
/**
 * 用户违章信息管理组件
 *
 * 功能：展示当前用户的图书违章记录列表
 * 特点：支持条件搜索、分页查询、Excel/PDF导出、全屏查看
 *
 * 配置：
 * - 默认每页显示：5条记录
 * - 默认排序：按借阅证号升序
 * - 搜索条件：图书编号、借阅日期、截止日期、归还日期、违章信息
 * - 自动过滤违章信息为空的记录
 */
export default {
  data() {
    return {
      // 搜索条件选项列表
      options: [
        {
          value: "book_number",
          label: "图书编号",
        },
        {
          value: "borrow_date",
          label: "借阅日期",
        },
        {
          value: "close_date",
          label: "截止日期",
        },
        {
          value: "return_date",
          label: "归还日期",
        },
        {
          value: "violation_message",
          label: "违章信息",
          tableData: [], returnDate: "",
          violationMessage: "",
          violationAdmin: "",
        },
      ],
      // 查询参数对象
      queryInfo: {
        pageNum: 1,           // 当前页码
        pageSize: 5,          // 每页条数
        condition: "",       // 搜索条件类型
        query: "",           // 搜索关键词
        cardNumber: window.sessionStorage.getItem("cardNumber"), // 当前用户借阅卡号
      },
      // 总记录数
      total: 0,
      // 表格标题（用于导出）
      title: "图书违章表格",
      // Excel导出字段映射
      json_fields: {
        借阅证号: "cardNumber",
        图书编号: "bookNumber",
        借阅日期: "borrowDate",
        截止日期: "closeDate",
        归还日期: "returnDate",
        违章信息: "violationMessage",
        处理人: "violationAdmin",
      },
      loading: true,

    };
  },
  methods: {
    /**
     * 每页条数改变时触发
     *
     * @param {number} val - 新的每页条数
     */
    handleSizeChange(val) {
      this.queryInfo.pageSize = val;
      this.searchViolationByPage();
    },

    /**
     * 当前页码改变时触发
     *
     * @param {number} val - 新的页码
     */
    handleCurrentChange(val) {
      this.queryInfo.pageNum = val;
      this.searchViolationByPage();
    },

    /**
     * 分页查询违章记录
     *
     * 通过POST请求向后端发送查询参数，获取违章记录列表
     * 请求成功后过滤出违章信息不为空的记录并更新表格数据
     *
     * 接口信息：
     * - 地址：POST user/getViolationRecords
     * - 参数：{pageNum, pageSize, condition, query, cardNumber}
     * - 响应：{records: [...], total: number}
     */
    async searchViolationByPage(isSearch = false) {
      this.loading = true;
      // 只有在搜索时才重置到第一页，分页时保持当前页码
      if (isSearch) {
        this.queryInfo.pageNum = 1;
      }
      const {data: res} = await this.$http.post(
          "user/getViolationRecords",
          this.queryInfo
      );

      this.tableData = [];

      if (res.status !== 200) {
        this.total = 0;
        this.loading = false;
        return this.$message.error(res.msg);
      }

      this.$message.success({
        message: res.msg,
        duration: 1000,
      });

      // 过滤出违章信息不为空的记录
      const filteredData = res.data.records.filter(
          item => item.violationMessage && item.violationMessage.trim() !== ''
      );

      this.tableData = filteredData;
      this.total = filteredData.length;
      this.loading = false;
    },

    /**
     * 导出PDF文件
     *
     * 调用全局混入的getPdf方法生成并下载PDF文件
     * @param {string} title - PDF文件名称
     */
    downLoad() {
      this.getPdf(this.title);
    },

    /**
     * 切换全屏模式
     *
     * 通过HTML5全屏API实现页面全屏/退出全屏的切换
     * document.fullscreenElement: 判断当前是否为全屏模式
     * document.documentElement.requestFullscreen(): 进入全屏
     * document.exitFullscreen(): 退出全屏
     */
    fullScreen() {
      let full = document.fullscreenElement;

      if (!full) {
        document.documentElement.requestFullscreen();
      } else {
        document.exitFullscreen();
      }
    }
  },

  /**
   * 组件创建后自动加载违章记录
   */
  created() {
    this.searchViolationByPage();
  },
};
</script>

<style scoped>
.search-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  overflow-x: auto;
}

.search-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
  white-space: nowrap;
}

.search-label {
  white-space: nowrap;
  font-size: 14px;
  color: #606266;
}

.search-select {
  width: 140px;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  min-width: 200px;
  max-width: 300px;
}

.search-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  white-space: nowrap;
}

@media screen and (max-width: 576px) {
  .search-right {
    justify-content: flex-start;
  }

  .search-right .el-button {
    flex-shrink: 0;
  }
}
</style>