<template>
  <div class="search_container">
    <el-card shadow="always">
      <!-- 操作按钮区：发布新公告 -->
      <div class="search-header">
        <div class="search-left">
          <el-button size="mini" type="primary" @click="showAddDialog()"
          ><i class="el-icon-s-promotion"></i> 发布新公告
          </el-button>
        </div>
        <div class="search-right">
          <!-- 如果有导出/全屏按钮可以在这里添加 -->
        </div>
      </div>

      <!-- 公告列表表格 - 支持分页/加载状态 -->
      <el-table
          v-loading="loading"
          :data="tableData"
          border
          element-loading-background="rgba(0, 0, 0, 0.8)"
          element-loading-spinner="el-icon-loading"
          element-loading-text="拼命加载中"
          stripe
          style="width: 100%"
      >
        <el-table-column label="ID" prop="noticeId"></el-table-column>
        <el-table-column label="标题" prop="noticeTitle"></el-table-column>
        <el-table-column label="公告" prop="noticeContent"></el-table-column>
        <el-table-column label="发布日期" prop="createTime"></el-table-column>

        <!-- 操作列：编辑/删除 -->
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-tooltip :enterable="false" content="修改" effect="dark" placement="top">
              <el-button
                  icon="el-icon-edit"
                  size="mini"
                  type="primary"
                  @click="showEditDialog(scope.row.noticeId)"
              ></el-button>
            </el-tooltip>

            <el-tooltip :enterable="false" content="删除" effect="dark" placement="top">
              <el-button
                  icon="el-icon-delete"
                  size="mini"
                  type="danger"
                  @click="removeUserById(scope.row.noticeId)"
              ></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
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

      <!-- 编辑对话框 -->
      <el-dialog
          :visible.sync="editDialogVisible"
          title="修改公告"
          width="50%"
          @close="editDialogClosed"
      >
        <el-form
            ref="editFormRef"
            :model="editForm"
            :rules="editFormRules"
            label-width="120px"
        >
          <el-form-item label="公告标题" prop="noticeTitle">
            <el-input v-model="editForm.noticeTitle"></el-input>
          </el-form-item>
          <el-form-item label="公告内容" prop="noticeContent">
            <el-input v-model="editForm.noticeContent" type="textarea"></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="editDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="editNoticeById">确 定</el-button>
        </span>
      </el-dialog>

      <!-- 添加对话框 -->
      <el-dialog
          :visible.sync="addDialogVisible"
          title="添加公告"
          width="50%"
          @close="addDialogClosed"
      >
        <el-form
            ref="addFormRef"
            :model="addForm"
            :rules="addFormRules"
            label-width="120px"
        >
          <el-form-item label="公告标题" prop="noticeTitle">
            <el-input v-model="addForm.noticeTitle"></el-input>
          </el-form-item>
          <el-form-item label="公告内容" prop="noticeContent">
            <el-input v-model="addForm.noticeContent" type="textarea"></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="addDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="addNotice">添加公告</el-button>
        </span>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
/**
 * 公告管理组件
 *
 * 功能：CRUD操作（增删改查）+ 分页加载
 * 特点：富文本内容展示、管理员ID自动关联、二次确认删除
 */
export default {
  data() {
    return {
      tableData: [],  // 表格数据数组

      // 编辑对话框相关
      editDialogVisible: false,
      editForm: {
        noticeTitle: "",
        noticeContent: "",
      },
      // 编辑表单验证规则
      editFormRules: {
        noticeTitle: [
          {required: true, message: "请输入公告标题", trigger: "blur"},
          {min: 6, max: 30, message: "长度在6到30个字符", trigger: "blur"},
        ],
        noticeContent: [
          {required: true, message: "请输入公告内容", trigger: "blur"},
        ],
      },

      // 添加对话框相关
      addDialogVisible: false,
      addForm: {
        noticeTitle: "",
        noticeContent: "",
        noticeAdminId: 0  // 发布公告的管理员ID
      },
      // 添加表单验证规则
      addFormRules: {
        noticeTitle: [
          {required: true, message: "请输入公告标题", trigger: "blur"},
          {min: 6, max: 30, message: "长度在6到30个字符", trigger: "blur"},
        ],
        noticeContent: [
          {required: true, message: "请输入公告内容", trigger: "blur"},
        ],
      },

      // 分页查询参数
      queryInfo: {
        pageNum: 1,
        pageSize: 5,
      },
      total: 0,     // 总记录数
      loading: true // 表格加载状态
    };
  },

  methods: {
    /**
     * 分页 - 每页条数变化
     */
    handleSizeChange(val) {
      this.queryInfo.pageSize = val;
      this.getNoticeList();
    },

    /**
     * 分页 - 当前页变化
     */
    handleCurrentChange(val) {
      this.queryInfo.pageNum = val;
      this.getNoticeList();
    },

    /**
     * 打开编辑对话框并加载数据
     *
     * 接口：GET bookadmin/getNotice/:id
     */
    async showEditDialog(id) {
      this.loading = true;
      const {data: res} = await this.$http.get('bookadmin/getNotice/' + id);
      if (res.status !== 200) {
        return this.$message.error(res.msg)
      }

      this.editForm = res.data;
      this.editDialogVisible = true;
    },

    /**
     * 关闭编辑对话框时重置表单
     */
    editDialogClosed() {
      this.$refs.editFormRef.resetFields();
    },

    /**
     * 更新公告信息
     *
     * 接口：PUT bookadmin/updateNotice/:id
     * 注意：只提交标题和内容，ID从URL路径获取
     */
    async editNoticeById() {
      const {data: res} = await this.$http.put('bookadmin/updateNotice/' + this.editForm.noticeId, {
        noticeTitle: this.editForm.noticeTitle,
        noticeContent: this.editForm.noticeContent
      })

      if (res.status !== 200) {
        return this.$message.error(res.msg)
      }

      this.editDialogVisible = false;
      this.getNoticeList();
      this.$message.success(res.msg)
    },

    /**
     * 删除公告（带二次确认）
     *
     * 流程：确认 → 调用删除接口 → 刷新列表
     * 接口：DELETE bookadmin/deleteNotice/:id
     */
    async removeUserById(id) {
      const confirmResult = await this.$confirm(
          "此操作将永久删除该公告, 是否继续?",
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          }
      ).catch((error) => error);

      if (confirmResult !== "confirm") {
        return this.$message.info("已经取消删除");
      }

      const {data: res} = await this.$http.delete('bookadmin/deleteNotice/' + id)
      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.$message.success({
        message: res.msg,
        duration: 1500,
      });

      this.getNoticeList();
    },

    /**
     * 关闭添加对话框时重置表单
     */
    addDialogClosed() {
      this.$refs.addFormRef.resetFields();
    },

    /**
     * 打开添加对话框
     */
    showAddDialog() {
      this.addDialogVisible = true;
    },

    /**
     * 获取公告列表（分页）
     *
     * 接口：POST bookadmin/getNoticeList
     * 参数：{pageNum, pageSize}
     * 响应：{records: [...], total: number}
     */
    async getNoticeList() {
      this.loading = true;
      const {data: res} = await this.$http.post('bookadmin/getNoticeList', this.queryInfo)

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
     * 添加新公告
     *
     * 流程：
     * 1. 从sessionStorage获取当前管理员ID
     * 2. 调用添加接口
     * 3. 成功后刷新列表
     *
     * 接口：POST bookadmin/addNotice
     */
    async addNotice() {
      this.addDialogVisible = false;

      // 设置操作员ID（从sessionStorage获取）
      this.addForm.noticeAdminId = parseInt(window.sessionStorage.getItem('bookAdminId'))

      const {data: res} = await this.$http.post('bookadmin/addNotice', this.addForm)

      if (res.status !== 200) {
        return this.$message.error(res.msg)
      }

      this.$message.success({
        message: res.msg,
        duration: 1000
      })

      this.getNoticeList();
    }
  },

  created() {
    this.getNoticeList(); // 组件初始化时加载列表
  }
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