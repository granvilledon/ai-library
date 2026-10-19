<template>
  <div class="search_container">
    <el-card shadow="always">
      <!-- 搜索内容和导出区域 -->
      <div class="search-header">
        <div class="search-left">
          <el-button size="mini" type="primary" @click="showAddDialog()">
            <i class="el-icon-plus"></i> 添加分类
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
              name="书籍类型"
          >
            <el-button class="el-icon-printer" size="mini" type="primary"
            >导出Excel
            </el-button>
          </export-excel>
        </div>
      </div>
      <!-- 表格区域 -->
      <el-table id="pdfDom" v-loading="loading" :data="tableData" :default-sort="{prop: 'typeId', order: 'ascending'}"
                border
                element-loading-background="rgba(0, 0, 0, 0.8)"
                element-loading-spinner="el-icon-loading"
                element-loading-text="拼命加载中"
                stripe
                style="width: 100%">
        <el-table-column label="ID" prop="typeId" sortable></el-table-column>
        <el-table-column label="分类名" prop="typeName"></el-table-column>
        <el-table-column label="描述" prop="typeContent"></el-table-column>
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
                  @click="showEditDialog(scope.row.typeId)"
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
                  @click="removeUserById(scope.row.typeId)"
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
          title="修改分类"
          width="50%"
          @close="editDialogClosed"
      >
        <el-form
            ref="editFormRef"
            :model="editForm"
            :rules="editFormRules"
            label-width="120px"
        >
          <el-form-item label="分类名" prop="typeName">
            <el-input v-model="editForm.typeName"></el-input>
          </el-form-item>
          <el-form-item label="分类描述" prop="typeContent">
            <el-input v-model="editForm.typeContent" type="textarea"></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="editDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="updateBookType"
          >确 定</el-button
          >
        </span>
      </el-dialog>
      <!-- 添加规则的对话框 -->
      <el-dialog
          :visible.sync="addDialogVisible"
          title="添加分类"
          width="50%"
          @close="addDialogClosed"
      >
        <el-form
            ref="addFormRef"
            :model="addForm"
            :rules="addFormRules"
            label-width="120px"
        >
          <el-form-item label="分类名" prop="typeName">
            <el-input v-model="addForm.typeName"></el-input>
          </el-form-item>
          <el-form-item label="分类描述" prop="typeContent">
            <el-input v-model="addForm.typeContent" type="textarea"></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="addDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="addBookType">添加分类</el-button>
        </span>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
/**
 * 书籍类型管理组件
 *
 * 功能：管理图书馆书籍分类信息（增删改查）
 * 特点：支持分页查询、表单验证、数据导出（Excel/PDF）、全屏查看
 *
 * 配置：
 * - 默认每页显示：5条记录
 * - 默认排序：按分类ID升序
 *
 * 接口列表：
 * - GET admin/getBookType/{id} - 获取单个分类信息
 * - POST admin/getBookTypePage - 分页查询分类列表
 * - POST admin/addBookType - 添加分类
 * - PUT admin/updateBookType - 修改分类
 * - DELETE admin/deleteBookType/{id} - 删除分类
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
        typeName: "",      // 分类名称
        typeContent: "",   // 分类描述
      },
      // 修改表单验证规则
      editFormRules: {
        typeName: [
          {required: true, message: "请输入分类名", trigger: "blur"},
        ],
        typeContent: [
          {required: true, message: "请输入分类描述", trigger: "blur"},
        ],
      },
      // 添加对话框显示状态
      addDialogVisible: false,
      // 添加表单数据
      addForm: {
        typeName: "",      // 分类名称
        typeContent: "",   // 分类描述
      },
      // 添加表单验证规则
      addFormRules: {
        typeName: [
          {required: true, message: "请输入分类名", trigger: "blur"},
        ],
        typeContent: [
          {required: true, message: "请输入分类描述", trigger: "blur"},
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
      title: "书籍类型",
      json_fields: {
        ID: "typeId",
        分类名: "typeName",
        描述: "typeContent",
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
      this.getBookTypeList();
    },

    /**
     * 当前页码改变时触发
     *
     * @param {number} val - 新的页码
     */
    handleCurrentChange(val) {
      this.queryInfo.pageNum = val;
      this.getBookTypeList();
    },

    /**
     * 显示修改对话框并加载分类数据
     *
     * @param {number} id - 分类ID
     */
    async showEditDialog(id) {
      this.editDialogVisible = true;
      const {data: res} = await this.$http.get("admin/getBookType/" + id);

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.editForm = res.data;
    },

    /**
     * 修改对话框关闭时重置表单
     */
    editDialogClosed() {
      this.$refs.editFormRef.resetFields();
      this.editForm.checkList = ["五山校区", "大学城校区", "国际校区"];
    },

    /**
     * 删除书籍分类
     *
     * 弹出确认对话框，确认后发送删除请求
     * 删除成功后重置页码并重新加载列表
     *
     * @param {number} id - 分类ID
     */
    async removeUserById(id) {
      // 弹出确认对话框
      const confirmResult = await this.$confirm(
          "此操作将永久删除该书籍类型, 是否继续?",
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
      const {data: res} = await this.$http.delete("admin/deleteBookType/" + id);

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.$message.success(res.msg);

      // 重置页码防止越界
      this.queryInfo.pageNum = 1;
      this.queryInfo.pageSize = 5;
      this.getBookTypeList();
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
     * 获取书籍分类列表
     *
     * 通过POST请求向后端发送查询参数，获取分类列表
     * 请求成功后更新表格数据和总记录数
     */
    async getBookTypeList() {
      this.loading = true;
      this.tableData = [];

      const {data: res} = await this.$http.post(
          "admin/getBookTypePage",
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
     * 添加书籍分类
     *
     * 先进行表单验证，验证通过后发送POST请求添加分类
     * 添加成功后刷新列表并关闭对话框
     */
    async addBookType() {
      this.$refs.addFormRef.validate(async (valid) => {
        if (!valid) {
          return;
        }

        const {data: res} = await this.$http.post(
            "admin/addBookType",
            this.addForm
        );

        if (res.status !== 200) {
          return this.$message.error(res.msg);
        }

        this.$message.success({
          message: res.msg,
          duration: 1500,
        });

        this.getBookTypeList();
        this.addDialogVisible = false;
      });
    },

    /**
     * 修改书籍分类
     *
     * 通过POST请求向后端发送更新后的分类信息
     * 修改成功后刷新列表并关闭对话框
     */
    async updateBookType() {
      const {data: res} = await this.$http.put(
          "admin/updateBookType",
          this.editForm
      );

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.$message.success({
        message: res.msg,
        duration: 1500,
      });

      this.getBookTypeList();
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
   * 组件创建后自动加载书籍分类列表
   */
  created() {
    this.getBookTypeList();
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