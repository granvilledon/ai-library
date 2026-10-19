<template>
  <div class="search_container">
    <el-card shadow="always">
      <!-- 搜索内容和导出区域 -->
      <div class="search-header">
        <div class="search-left">
          <el-button size="mini" type="primary" @click="showAddDialog()">
            <i class="el-icon-plus"></i> 添加规则
          </el-button>
        </div>
        <div class="search-right">
          <el-button class="el-icon-full-screen" size="mini" type="success" @click="fullScreen"
          >全屏
          </el-button>
          <el-button class="el-icon-printer" size="mini" type="primary" @click="downLoad">导出PDF
          </el-button>
          <export-excel
              :data="tableData"
              :fields="json_fields"
              :header="title"
              class="export-excel-wrapper"
              name="借阅规则"
          >
            <el-button class="el-icon-printer" size="mini" type="primary"
            >导出Excel
            </el-button>
          </export-excel>
        </div>
      </div>
      <!-- 表格区域 -->
      <el-table id="pdfDom" v-loading="loading" :data="tableData"
                :default-sort="{prop: 'bookRuleId', order: 'ascending'}" border
                element-loading-background="rgba(0, 0, 0, 0.8)"
                element-loading-spinner="el-icon-loading"
                element-loading-text="拼命加载中"
                stripe
                style="width: 100%">
        <el-table-column label="ID" prop="bookRuleId" sortable></el-table-column>
        <el-table-column label="限制天数" prop="bookDays" sortable></el-table-column>
        <el-table-column label="限制本数" prop="bookLimitNumber" sortable>
        </el-table-column>
        <el-table-column label="限制图书馆" prop="bookLimitLibrary">
        </el-table-column>
        <el-table-column label="逾期费用" prop="bookOverdueFee" sortable>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <!-- 修改按钮 -->
            <el-tooltip
                :enterable="false"
                content="修改"
                effect="dark"
                placement="top"
            >
              <el-button
                  icon="el-icon-edit"
                  size="mini"
                  type="primary"
                  @click="showEditDialog(scope.row.ruleId)"
              ></el-button
              >
            </el-tooltip>

            <!-- 删除按钮 -->
            <el-tooltip
                :enterable="false"
                content="删除"
                effect="dark"
                placement="top"
            >
              <el-button
                  icon="el-icon-delete"
                  size="mini"
                  type="danger"
                  @click="removeUserById(scope.row.ruleId)"
              ></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
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
      <!-- 修改规则的对话框 -->
      <el-dialog
          :visible.sync="editDialogVisible"
          title="修改规则"
          width="50%"
          @close="editDialogClosed"
      >
        <el-form
            ref="editFormRef"
            :model="editForm"
            :rules="editFormRules"
            label-width="120px"
        >
          <el-form-item label="限制天数" prop="bookDays">
            <el-input v-model="editForm.bookDays"></el-input>
          </el-form-item>
          <el-form-item label="限制数量" prop="bookLimitNumber">
            <el-input v-model="editForm.bookLimitNumber"></el-input>
          </el-form-item>
          <el-form-item label="限制图书馆">
            <el-checkbox-group v-model="editForm.checkList">
              <el-checkbox label="五山校区"></el-checkbox>
              <el-checkbox label="大学城校区"></el-checkbox>
              <el-checkbox label="国际校区"></el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="逾期每天费用" prop="bookOverdueFee">
            <el-input v-model="editForm.bookOverdueFee"></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="editDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="updateRule"
          >确 定</el-button
          >
        </span>
      </el-dialog>
      <!-- 添加规则的对话框 -->
      <el-dialog
          :visible.sync="addDialogVisible"
          title="添加规则"
          width="50%"
          @close="addDialogClosed"
      >
        <el-form
            ref="addFormRef"
            :model="addForm"
            :rules="addFormRules"
            label-width="120px"
        >
          <el-form-item label="限制天数" prop="bookDays">
            <el-input v-model="addForm.bookDays"></el-input>
          </el-form-item>
          <el-form-item label="限制数量" prop="bookLimitNumber">
            <el-input v-model="addForm.bookLimitNumber"></el-input>
          </el-form-item>
          <el-form-item label="限制图书馆">
            <el-checkbox-group v-model="addForm.checkList">
              <el-checkbox label="五山校区"></el-checkbox>
              <el-checkbox label="大学城校区"></el-checkbox>
              <el-checkbox label="国际校区"></el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="逾期每天费用" prop="bookOverdueFee">
            <el-input v-model="addForm.bookOverdueFee"></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="addDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="addRule">添加规则</el-button>
        </span>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
/**
 * 借阅规则管理组件
 *
 * 功能：管理图书馆借阅规则信息（增删改查）
 * 特点：支持分页查询、表单验证、多选图书馆、数据导出（Excel/PDF）、全屏查看
 *
 * 配置：
 * - 默认每页显示：5条记录
 * - 默认排序：按规则ID升序
 * - 可选图书馆：五山校区、大学城校区、国际校区
 *
 * 规则字段说明：
 * - bookDays：借阅限制天数
 * - bookLimitNumber：借阅限制本数
 * - bookLimitLibrary：限制借阅的图书馆（逗号分隔）
 * - bookOverdueFee：逾期每天费用
 *
 * 接口列表：
 * - GET admin/getRule/{id} - 获取单个规则信息
 * - POST admin/getRuleListPage - 分页查询规则列表
 * - POST admin/addRule - 添加规则
 * - PUT admin/updateRule - 修改规则
 * - DELETE admin/deleteRule/{id} - 删除规则
 */
export default {
  data() {
    return {
      // 表格数据列表
      tableData: [],
      // 修改对话框显示状态
      editDialogVisible: false,
      // 修改表单数据
      editForm: {
        bookDays: "",                            // 借阅限制天数
        bookLimitNumber: "",                     // 借阅限制本数
        checkList: ["五山校区", "大学城校区", "国际校区"], // 选中的图书馆
        bookOverdueFee: 0,                        // 逾期每天费用
        bookLimitLibrary: "",                    // 限制图书馆（逗号分隔）
      },
      // 修改表单验证规则
      editFormRules: {
        bookLimitDays: [
          {required: true, message: "请输入限制天数", trigger: "blur"},
        ],
        bookLimitNumber: [
          {required: true, message: "请输入限制数量", trigger: "blur"},
        ],
        bookOverdueFee: [
          {required: true, message: "请输入正确的逾期每天费用", trigger: "blur"},
        ],
      },
      // 添加对话框显示状态
      addDialogVisible: false,
      // 添加表单数据
      addForm: {
        bookDays: "",                            // 借阅限制天数
        bookLimitNumber: "",                     // 借阅限制本数
        checkList: ["五山校区", "大学城校区", "国际校区"], // 选中的图书馆
        bookOverdueFee: 0,                        // 逾期每天费用
        bookLimitLibrary: "",                    // 限制图书馆（逗号分隔）
      },
// 添加表单验证规则
      addFormRules: {
        bookLimitDays: [
          {required: true, message: "请输入限制天数", trigger: "blur"},
        ],
        bookLimitNumber: [
          {required: true, message: "请输入限制数量", trigger: "blur"},
        ],
        bookOverdueFee: [
          {required: true, message: "请输入正确的逾期每天费用", trigger: "blur"},
        ],
      },
      // 查询参数对象
      queryInfo: {
        pageNum: 1,    // 当前页码
        pageSize: 5,   // 每页条数
      },
      // 总记录数
      total: 0,
      // 表格标题（用于导出）
      title: "借阅规则",
      // Excel导出字段映射
      json_fields: {
        规则编号: "bookRuleId",
        限制天数: "bookDays",
        限制本数: "bookLimitNumber",
        限制图书馆: "bookLimitLibrary",
        逾期费用: "bookOverdueFee",
      },
      // 加载状态标识
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
      this.getRuleList();
    },

    /**
     * 当前页码改变时触发
     *
     * @param {number} val - 新的页码
     */
    handleCurrentChange(val) {
      this.queryInfo.pageNum = val;
      this.getRuleList();
    },

    /**
     * 显示修改对话框并加载规则数据
     *
     * @param {number} id - 规则ID
     */
    async showEditDialog(id) {
      const {data: res} = await this.$http.get("admin/getRule/" + id);

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.editForm = res.data;
      this.editDialogVisible = true;
    },

    /**
     * 修改对话框关闭时重置表单
     */
    editDialogClosed() {
      this.$refs.editFormRef.resetFields();
      this.editForm.checkList = ["五山校区", "大学城校区", "国际校区"];
    },

    /**
     * 删除借阅规则
     *
     * 弹出确认对话框，确认后发送删除请求
     * 删除成功后重新加载列表
     *
     * @param {number} id - 规则ID
     */
    async removeUserById(id) {
      // 弹出确认对话框
      const confirmResult = await this.$confirm(
          "此操作将永久删除该规则, 是否继续?",
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          }
      ).catch((error) => error);

      // 用户取消删除
      if (confirmResult !== "confirm") {
        return this.$message.info("已经取消删除");
      }

      // 发送删除请求
      const {data: res} = await this.$http.delete("admin/deleteRule/" + id);

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.$message.success({
        message: res.msg,
        duration: 1500,
      });

      this.getRuleList();
    },

    /**
     * 添加对话框关闭时重置表单
     */
    addDialogClosed() {
      this.$refs.addFormRef.resetFields();
      this.addForm.checkList = ["五山校区", "大学城校区", "国际校区"];
    },

    /**
     * 显示添加对话框
     */
    showAddDialog() {
      this.addDialogVisible = true;
    },

    /**
     * 获取借阅规则列表
     *
     * 通过POST请求向后端发送查询参数，获取规则列表
     * 请求成功后更新表格数据和总记录数
     */
    async getRuleList() {
      this.loading = true;
      this.tableData = [];

      const {data: res} = await this.$http.post(
          "admin/getRuleListPage",
          this.queryInfo
      );

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
     * 添加借阅规则
     *
     * 将选中的图书馆列表转换为逗号分隔的字符串
     * 通过POST请求向后端发送规则信息，完成添加操作
     * 添加成功后刷新列表并关闭对话框
     */
    async addRule() {
      // 将选中的图书馆转换为逗号分隔字符串
      const libraryList = this.addForm.checkList.join(',');
      this.addForm.bookLimitLibrary = libraryList;

      // 发送添加请求
      const {data: res} = await this.$http.post(
          "admin/addRule",
          this.addForm
      );

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.$message.success({
        message: res.msg,
        duration: 1500,
      });

      this.addDialogVisible = false;
      this.getRuleList();
    },

    /**
     * 修改借阅规则
     *
     * 通过PUT请求向后端发送更新后的规则信息
     * 修改成功后刷新列表并关闭对话框
     */
    async updateRule() {
      const {data: res} = await this.$http.put(
          "admin/updateRule",
          this.editForm
      );

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.$message.success({
        message: res.msg,
        duration: 1500,
      });

      this.getRuleList();
      this.editDialogVisible = false;
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
   * 组件创建后自动加载借阅规则列表
   */
  created() {
    this.getRuleList();
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
