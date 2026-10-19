<template>
  <div class="search_container">
    <el-card shadow="always">
      <!-- 搜索内容和导出区域 -->
      <div class="search-header">
        <div class="search-left">
          <el-button size="mini" type="primary" @click="showAddDialog()">
            <i class="el-icon-plus"></i> 添加管理员
          </el-button>
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
              name="图书管理员"
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
          :default-sort="{ prop: 'bookAdminId', order: 'ascending' }"
          border
          element-loading-background="rgba(0, 0, 0, 0.8)"
          element-loading-spinner="el-icon-loading"
          element-loading-text="拼命加载中"
          stripe
          style="width: 100%"
      >
        <el-table-column label="ID" prop="bookAdminId" sortable>
        </el-table-column>
        <el-table-column label="用户名" prop="username"></el-table-column>
        <el-table-column label="姓名" prop="bookAdminName"></el-table-column>
        <el-table-column label="邮箱" prop="email"></el-table-column>
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
                  @click="showEditDialog(scope.row.bookAdminId)"
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
                  @click="removeUserById(scope.row.bookAdminId)"
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
      <!-- 修改公告的对话框 -->
      <el-dialog
          :visible.sync="editDialogVisible"
          title="修改管理员"
          width="50%"
          @close="editDialogClosed"
      >
        <el-form
            ref="editFormRef"
            :model="editForm"
            :rules="editFormRules"
            label-width="120px"
        >
          <el-form-item label="姓名" prop="bookAdminName">
            <el-input v-model="editForm.bookAdminName"></el-input>
          </el-form-item>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="editForm.username"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
                v-model="editForm.password"
                show-password
                type="password"
            ></el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="editForm.email"></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="editDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="updateBookAdmin">确 定</el-button>
        </span>
      </el-dialog>
      <!-- 添加管理员的对话框 -->
      <el-dialog
          :visible.sync="addDialogVisible"
          title="添加管理员"
          width="50%"
          @close="addDialogClosed"
      >
        <el-form
            ref="addFormRef"
            :model="addForm"
            :rules="addFormRules"
            label-width="120px"
        >
          <el-form-item label="姓名" prop="bookAdminName">
            <el-input v-model="addForm.bookAdminName"></el-input>
          </el-form-item>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="addForm.username"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="addForm.password"></el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="addForm.email"></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="addDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="addBookAdmin">添加管理员</el-button>
        </span>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
/**
 * 图书管理员管理组件
 *
 * 功能：管理图书馆图书管理员信息（增删改查）
 * 特点：支持分页查询、表单验证、数据导出（Excel/PDF）、全屏查看
 *
 * 配置：
 * - 默认每页显示：5条记录
 * - 默认排序：按管理员ID升序
 *
 * 接口列表：
 * - GET admin/getBookAdmin/{id} - 获取单个管理员信息
 * - POST admin/getBookAdminList - 分页查询管理员列表
 * - POST admin/addBookAdmin - 添加管理员
 * - PUT admin/updateBookAdmin - 修改管理员
 * - DELETE admin/deleteBookAdmin/{id} - 删除管理员
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
        username: "",       // 用户名
        password: "",       // 密码
        email: "",          // 邮箱
        bookAdminName: "",  // 姓名
      },
      // 修改表单验证规则
      editFormRules: {
        username: [
          {required: true, message: "请输入用户名", trigger: "blur"},
          {min: 5, max: 30, message: "长度在5到30个字符", trigger: "blur"},
        ],
        password: [
          {required: true, message: "请输入密码", trigger: "blur"},
          {min: 6, max: 50, message: "长度在6到50个字符", trigger: "blur"},
        ],
        email: [
          {required: true, message: "请输入正确的邮箱", trigger: "blur", type: "email"},
          {min: 6, max: 50, message: "长度在6到50个字符", trigger: "blur"},
        ],
        bookAdminName: [
          {required: true, message: "请输入姓名", trigger: "blur"},
        ],
      },
      // 添加对话框显示状态
      addDialogVisible: false,
      // 添加表单数据
      addForm: {
        username: "",       // 用户名
        password: "",       // 密码
        email: "",          // 邮箱
        bookAdminName: "",  // 姓名
      },
      // 添加表单验证规则
      addFormRules: {
        username: [
          {required: true, message: "请输入用户名", trigger: "blur"},
          {min: 5, max: 30, message: "长度在5到30个字符", trigger: "blur"},
        ],
        password: [
          {required: true, message: "请输入密码", trigger: "blur"},
          {min: 6, max: 50, message: "长度在6到50个字符", trigger: "blur"},
        ],
        email: [
          {required: true, message: "请输入正确的邮箱", trigger: "blur", type: "email"},
          {min: 6, max: 50, message: "长度在6到50个字符", trigger: "blur"},
        ],
        bookAdminName: [
          {required: true, message: "请输入姓名", trigger: "blur"},
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
      title: "图书管理员",
      json_fields: {
        ID: "bookAdminId",
        用户名: "username",
        姓名: "bookAdminName",
        邮箱: "email",
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
      this.getBookAdminList();
    },

    /**
     * 当前页码改变时触发
     *
     * @param {number} val - 新的页码
     */
    handleCurrentChange(val) {
      this.queryInfo.pageNum = val;
      this.getBookAdminList();
    },

    /**
     * 显示修改对话框并加载管理员数据
     *
     * @param {number} id - 管理员ID
     */
    async showEditDialog(id) {
      const {data: res} = await this.$http.get("admin/getBookAdmin/" + id);

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
    },

    /**
     * 删除管理员
     *
     * 弹出确认对话框，确认后发送删除请求
     * 删除成功后重置页码并重新加载列表
     *
     * @param {number} id - 管理员ID
     */
    async removeUserById(id) {
      // 弹出确认对话框
      const confirmResult = await this.$confirm(
          "此操作将永久删除该图书管理员, 是否继续?",
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
          "admin/deleteBookAdmin/" + id
      );

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.$message.success({
        message: res.msg,
        duration: 1000,
      });

      // 重置页码防止越界
      this.queryInfo.pageNum = 1;
      this.queryInfo.pageSize = 5;
      this.getBookAdminList();
    },

    /**
     * 添加对话框关闭时重置表单
     */
    addDialogClosed() {
      this.$refs.addFormRef.resetFields();
    },

    /**
     * 显示添加对话框
     */
    showAddDialog() {
      this.addDialogVisible = true;
    },

    /**
     * 获取图书管理员列表
     *
     * 通过POST请求向后端发送查询参数，获取管理员列表
     * 请求成功后更新表格数据和总记录数
     */
    async getBookAdminList() {
      this.loading = true;
      this.tableData = [];

      const {data: res} = await this.$http.post(
          "admin/getBookAdminList",
          this.queryInfo
      );

      if (res.status !== 200) {
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
     * 添加图书管理员
     *
     * 通过POST请求向后端发送管理员信息，完成添加操作
     * 添加成功后刷新列表并关闭对话框
     */
    async addBookAdmin() {
      const {data: res} = await this.$http.post(
          "admin/addBookAdmin",
          this.addForm
      );

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.$message.success({
        message: res.msg,
        duration: 1500,
      });

      this.getBookAdminList();
      this.addDialogVisible = false;
    },

    /**
     * 修改图书管理员
     *
     * 通过PUT请求向后端发送更新后的管理员信息
     * 修改成功后刷新列表并关闭对话框
     */
    async updateBookAdmin() {
      const {data: res} = await this.$http.put(
          "admin/updateBookAdmin",
          this.editForm
      );

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.$message.success({
        message: res.msg,
        duration: 1500,
      });

      this.getBookAdminList();
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
   * 组件创建后自动加载管理员列表
   */
  created() {
    this.getBookAdminList();
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