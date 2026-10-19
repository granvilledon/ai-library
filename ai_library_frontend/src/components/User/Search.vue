<template>
  <div class="search_container">
    <el-card shadow="always">
      <!-- 搜索操作区：条件筛选 + 关键词搜索 + 导出/全屏按钮 -->
      <div class="search-header">
        <div class="search-left">
          <span class="search-label">条件搜索:</span>
          <el-select v-model="queryInfo.condition" class="search-select" filterable placeholder="请选择">
            <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
          <el-input
              v-model="queryInfo.query"
              class="search-input"
              placeholder="请输入内容"
              @keyup.enter.native="searchBookByPage(true)"
          >
            <el-button slot="append" icon="el-icon-search" @click="searchBookByPage(true)">
            </el-button>
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
              name="图书查询表格"
          >
            <el-button class="el-icon-printer" size="mini" type="primary"
            >导出Excel
            </el-button>
          </export-excel>
        </div>
      </div>

      <!-- 图书列表表格 - 固定高度520px / 支持排序 / 分页 / 加载状态 -->
      <el-table
          id="pdfDom"
          v-loading="loading"
          :data="tableData"
          :default-sort="{ prop: 'bookNumber', order: 'ascending' }"
          border
          element-loading-background="rgba(0, 0, 0, 0.8)"
          element-loading-spinner="el-icon-loading"
          element-loading-text="拼命加载中"
          height="520"
          stripe
          style="width: 100%; font-size: 14px"
      >
        <el-table-column label="图书编号" prop="bookNumber" sortable></el-table-column>
        <el-table-column label="图书名称" prop="bookName"></el-table-column>
        <el-table-column label="作者" prop="bookAuthor"></el-table-column>
        <el-table-column label="图书馆" prop="bookLibrary"></el-table-column>
        <el-table-column label="分类" prop="bookType"></el-table-column>
        <el-table-column label="位置" prop="bookLocation" sortable></el-table-column>
        <el-table-column label="状态" prop="bookStatus" sortable></el-table-column>
        <el-table-column label="描述" prop="bookDescription" width="600px"></el-table-column>
      </el-table>

      <!-- 分页组件 -->
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
 * 读者端图书查询组件
 *
 * 功能：多条件检索馆藏图书 + 数据导出（Excel/PDF）+ 全屏显示
 * 特点：支持5种搜索维度、分页加载、结果可导出、只读展示
 *
 * 配置：
 * - 默认每页显示：5条记录
 * - 默认排序：按图书编号升序
 * - 表格固定高度：520px
 * - 搜索条件：图书编号、图书名称、作者、位置、描述
 */
export default {
  data() {
    return {
      // 搜索条件选项列表（后端字段名 -> 前端显示文本）
      options: [
        {value: "book_number", label: "图书编号"},
        {value: "book_name", label: "图书名称"},
        {value: "book_author", label: "作者"},
        {value: "book_location", label: "位置"},
        {value: "book_description", label: "描述"},
      ],
      // 表格数据列表
      tableData: [],
      // 查询参数对象
      queryInfo: {
        pageNum: 1,           // 当前页码
        pageSize: 5,          // 每页条数
        condition: "",       // 搜索条件类型
        query: "",           // 搜索关键词
      },
      // 总记录数
      total: 0,
      title: "图书查询表格",
      json_fields: {
        图书编号: "bookNumber",
        图书名称: "bookName",
        作者: "bookAuthor",
        图书馆: "bookLibrary",
        分类: "bookType",
        位置: "bookLocation",
        状态: "bookStatus",
        描述: "bookDescription",
      },
      loading: true,
    };
  },

  /**
   * 组件创建后自动加载图书列表
   */
  created() {
    this.searchBookByPage();
  },

  methods: {
    /**
     * 每页条数改变时触发
     *
     * @param {number} val - 新的每页条数
     */
    handleSizeChange(val) {
      this.queryInfo.pageSize = val;
      this.searchBookByPage();
    },

    /**
     * 当前页码改变时触发
     *
     * @param {number} val - 新的页码
     */
    handleCurrentChange(val) {
      this.queryInfo.pageNum = val;
      this.searchBookByPage();
    },

    /**
     * 分页搜索图书列表
     *
     * 通过POST请求向后端发送查询参数，获取图书列表
     * 请求成功后更新表格数据和总记录数
     *
     * 接口信息：
     * - 地址：POST user/searchBookPage
     * - 参数：{pageNum, pageSize, condition, query}
     * - 响应：{records: [...], total: number}
     *
     * 注意：
     * - condition为空时查询全部图书
     * - bookDescription列宽度设为600px以展示完整描述
     */
    async searchBookByPage(isSearch = false) {
      this.loading = true;
      if (isSearch) {
        this.queryInfo.pageNum = 1;
      }
      const {data: res} = await this.$http.post(
          "user/searchBookPage",
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

      this.loading = false;
      this.tableData = res.data.records || [];
      this.total = parseInt(res.data.total) || 0;
    },

    /**
     * 导出PDF文件
     *
     * 调用全局混入的getPdf方法生成并下载PDF文件
     *
     * 依赖：@/components/Utils/htmlToPdf.js
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
    },
  },
};
</script>

<style lang="css" scoped>
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