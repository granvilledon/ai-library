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
                v-for="item in searchs"
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
              @keyup.enter.native="getStatementList(true)"
          >
            <el-button
                slot="append"
                icon="el-icon-search"
                @click="getStatementList(true)"
            ></el-button>
          </el-input>
          <el-button size="mini" type="primary" @click="showAddDialog()">
            <i class="el-icon-plus"></i> 添加借阅证
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
              name="借阅证管理"
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
        <el-table-column label="借阅证编号" prop="cardNumber" sortable>
        </el-table-column>
        <el-table-column label="用户名" prop="username"></el-table-column>
        <el-table-column label="借阅规则" prop="ruleNumber" sortable></el-table-column>
        <el-table-column label="状态" prop="status" sortable></el-table-column>
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
                  @click="showEditDialog(scope.row.userId)"
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
                  @click="removeUserById(scope.row.userId)"
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
      <!-- 修改借阅证的对话框 -->
      <el-dialog
          :visible.sync="editDialogVisible"
          title="修改借阅证"
          width="50%"
          @close="editDialogClosed"
      >
        <el-form
            ref="editFormRef"
            :model="editForm"
            :rules="editFormRules"
            label-width="120px"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="editForm.username"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="editForm.password" type="password"></el-input>
          </el-form-item>
          <el-form-item label="规则" prop="ruleNumber">
            <el-select v-model="editForm.ruleNumber" placeholder="请选择">
              <el-option
                  v-for="item in bookRuleIdLists"
                  :key="item.ruleId"
                  :label="item.bookRuleId"
                  :value="item.bookRuleId"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="userStatus">
            <el-radio-group v-model="editForm.userStatus">
              <el-radio label="可用">可用</el-radio>
              <el-radio label="挂失">挂失</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="editDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="updateStatement">确 定</el-button>
        </span>
      </el-dialog>
      <!-- 添加借阅证的对话框 -->
      <el-dialog
          :visible.sync="addDialogVisible"
          title="添加借阅证"
          width="50%"
          @close="addDialogClosed"
      >
        <el-form
            ref="addFormRef"
            :model="addForm"
            :rules="addFormRules"
            label-width="120px"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="addForm.username"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="addForm.password"></el-input>
          </el-form-item>
          <el-form-item label="规则" prop="ruleNumber">
            <el-select v-model="addForm.ruleNumber" placeholder="请选择">
              <el-option
                  v-for="item in bookRuleIdLists"
                  :key="item.ruleId"
                  :label="item.bookRuleId"
                  :value="item.bookRuleId"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="userStatus">
            <el-radio-group v-model="addForm.userStatus">
              <el-radio label="可用">可用</el-radio>
              <el-radio label="禁用">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="addDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="addStatement">添加借阅证</el-button>
        </span>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
/**
 * 借阅证管理组件
 *
 * 功能：管理图书馆借阅证信息（增删改查）
 * 特点：支持分页查询、表单验证、数据导出（Excel/PDF）、全屏查看
 *
 * 配置：
 * - 默认每页显示：5条记录
 * - 默认排序：按借阅证编号升序
 *
 * 接口列表：
 * - GET admin/getStatement/{id} - 获取单个借阅证信息
 * - POST admin/getStatementList - 分页查询借阅证列表
 * - POST admin/addStatement - 添加借阅证
 * - PUT admin/updateStatement - 修改借阅证
 * - DELETE admin/deleteStatement/{id} - 删除借阅证
 * - GET user/getRuleList - 获取借阅规则列表
 */
export default {
  data() {
    return {
      tableData: [],

      editDialogVisible: false,
      editForm: {
        username: "",
        password: "",
        ruleNumber: "",
        status: "",
        userStatus: "",
      },
      editFormRules: {},
      addDialogVisible: false,
      addForm: {
        username: "",
        password: "",
        ruleNumber: "",
        userStatus: "",
      },
      addFormRules: {},
      searchs: [
        {
          value: "card_number",
          label: "借阅证编号",
        },
        {
          value: "username",
          label: "用户名",
        },
        {
          value: "rule_number",
          label: "借阅规则",
        },
        {
          value: "status",
          label: "状态",
        },
      ],
      bookRuleIdLists: [
        {
          ruleId: "18",
          bookRuleId: "18",
        },
        {
          ruleId: "357",
          bookRuleId: "357",
        },
      ],
      queryInfo: {
        pageNum: 1,
        pageSize: 5,
        condition: "",
        query: "",
      },
      total: 0,
      title: "借阅证管理",
      json_fields: {
        借阅证编号: "cardNumber",
        用户名: "username",
        借阅规则: "ruleNumber",
        状态: "status",
      },
      loading: true
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
      this.getStatementList();
    },

    /**
     * 当前页码改变时触发
     *
     * @param {number} val - 新的页码
     */
    handleCurrentChange(val) {
      this.queryInfo.pageNum = val;
      this.getStatementList();
    },
    /**
     * 显示修改对话框并回显数据
     *
     * 流程：
     * 1. 获取借阅规则列表（用于下拉选择）
     * 2. 根据用户ID获取用户信息
     * 3. 设置修改对话框可见
     *
     * @param {number} id - 用户ID
     */
    async showEditDialog(id) {
      const {data: res} = await this.$http.get("admin/getUserInfo/" + id);
      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      const {data: res2} = await this.$http.get("user/getRuleList");
      this.bookRuleIdLists = res2.data;

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
     * 删除用户
     *
     * 流程：
     * 1. 弹出确认对话框
     * 2. 用户确认后发送删除请求
     * 3. 删除成功后重置页码并重新加载列表
     *
     * @param {number} id - 用户ID
     */
    async removeUserById(id) {
      // 弹出确认对话框
      const confirmResult = await this.$confirm(
          "此操作将永久删除该用户, 是否继续?",
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
      const {data: res} = await this.$http.delete(
          "admin/deleteUser/" + id
      );

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.$message.success(res.msg);
      // 防止删除后页码越界导致数据显示异常
      this.queryInfo.pageNum = 1;
      this.queryInfo.pageSize = 5;
      this.getUserList();
    },
    /**
     * 添加对话框关闭时重置表单
     */
    addDialogClosed() {
      this.$refs.addFormRef.resetFields();
    },
    /**
     * 显示添加对话框并加载借阅规则
     *
     * 流程：
     * 1. 获取借阅规则列表
     * 2. 设置添加对话框可见
     */
    async showAddDialog() {
      const {data: res} = await this.$http.get("user/getRuleList");
      this.bookRuleIdLists = res.data;
      this.addDialogVisible = true;
    },
    /**
     * 获取借阅证列表
     *
     * 通过POST请求向后端发送查询参数，获取借阅证列表
     * 请求成功后更新表格数据和总记录数
     */
    async getStatementList(isSearch = false) {
      this.loading = true;
      this.tableData = [];
      if (isSearch) {
        this.queryInfo.pageNum = 1;
      }

      const {data: res} = await this.$http.post(
          "admin/getStatementList",
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
     * 获取用户列表
     *
     * 通过POST请求向后端发送查询参数，获取用户列表
     * 请求成功后更新表格数据和总记录数
     */
    async getUserList() {
      this.loading = true;
      this.tableData = [];

      const {data: res} = await this.$http.post(
          "admin/getUserList",
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
     * 添加借阅证
     *
     * 通过POST请求向后端发送借阅证信息，完成添加操作
     * 添加成功后刷新列表并关闭对话框
     */
    async addStatement() {
      const {data: res} = await this.$http.post(
          "admin/addStatement",
          this.addForm
      );

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.$message.success(res.msg);
      this.addDialogVisible = false;
      this.getStatementList(true);
    },
    /**
     * 修改借阅证
     *
     * 通过POST请求向后端发送更新后的借阅证信息
     * 修改成功后刷新列表并关闭对话框
     */
    async updateStatement() {
      const {data: res} = await this.$http.put(
          "admin/updateStatement",
          this.editForm
      );

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.$message.success(res.msg);
      this.getStatementList(true);
      this.editDialogVisible = false;
    },
    /**
     * 导出PDF文件
     *
     * 调用全局混入的getPdf方法生成并下载PDF文件
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
   * 组件创建后自动加载借阅证列表
   */
  created() {
    this.getStatementList();
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