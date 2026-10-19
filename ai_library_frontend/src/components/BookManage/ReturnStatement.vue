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
              @keyup.enter.native="getRuturnStatement(true)"
          >
            <el-button
                slot="append"
                icon="el-icon-search"
                @click="getRuturnStatement(true)"
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
              name="还书报表"
          >
            <el-button class="el-icon-printer" size="mini" type="primary"
            >导出Excel
            </el-button>
          </export-excel>
        </div>
      </div>
      <!-- 表格区域 -->
      <el-table
          id="pdfDom"
          v-loading="loading"
          :data="tableData"
          :default-sort="{ prop: 'cardNumber', order: 'ascending' }"
          border
          element-loading-background="rgba(0, 0, 0, 0.8)"
          element-loading-spinner="el-icon-loading"
          element-loading-text="拼命加载中"
          stripe
          style="width: 100%"
      >
        <el-table-column label="借阅证编号" prop="cardNumber" sortable>
        </el-table-column>
        <el-table-column label="图书编号" prop="bookNumber" sortable></el-table-column>
        <el-table-column label="借阅日期" prop="borrowDate" sortable></el-table-column>
        <el-table-column label="截止日期" prop="closeDate" sortable></el-table-column>
        <template slot="empty">
          <span style="color: #8B7355;">暂无数据</span>
        </template>
      </el-table>
      <!-- 分页查询区域 -->
      <el-pagination
          :current-page="queryInfo.pageNum"
          :page-size="queryInfo.pageSize"
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
 * 还书报表管理组件
 *
 * 功能：展示图书馆所有还书记录报表
 * 特点：支持条件搜索、分页查询、Excel/PDF导出、全屏查看
 *
 * 配置：
 * - 默认每页显示：5条记录
 * - 默认排序：按借阅证编号升序
 * - 搜索条件：借阅证号、图书编号、借阅日期、截止日期
 */
export default {
  data() {
    return {
      // 搜索条件选项列表
      options: [
        {value: "card_number", label: "借阅证号"},
        {value: "book_number", label: "图书编号"},
        {value: "borrow_date", label: "借阅日期"},
        {value: "close_date", label: "截止日期"},
      ],
      // 表格数据列表
      tableData: [],
      // 查询参数对象
      queryInfo: {
        pageNum: 1,    // 当前页码
        pageSize: 5,   // 每页条数
        condition: "", // 搜索条件类型
        query: "",     // 搜索关键词
      },
      // 总记录数
      total: 0,
      title: "借书报表",
      json_fields: {
        借阅证编号: "cardNumber",
        图书编号: "bookNumber",
        借阅日期: "borrowDate",
        截止日期: "closeDate",
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
      this.getRuturnStatement();
    },

    /**
     * 当前页码改变时触发
     *
     * @param {number} val - 新的页码
     */
    handleCurrentChange(val) {
      this.queryInfo.pageNum = val;
      this.getRuturnStatement();
    },

    /**
     * 获取还书报表数据
     *
     * 通过POST请求向后端发送查询参数，获取还书报表列表
     * 请求成功后更新表格数据和总记录数
     *
     * 接口信息：
     * - 地址：POST bookadmin/getReturnRecords
     * - 参数：{pageNum, pageSize, condition, query}
     * - 响应：{records: [...], total: number}
     */
    async getRuturnStatement(isSearch = false) {
      this.loading = true;
      if (isSearch) {
        this.queryInfo.pageNum = 1;
      }
      const {data: res} = await this.$http.post(
          "bookadmin/getReturnRecords",
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

      this.tableData = res.data.records || [];
      this.total = parseInt(res.data.total) || 0;
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
   * 组件创建后自动加载还书报表数据
   */
  created() {
    this.getRuturnStatement();
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