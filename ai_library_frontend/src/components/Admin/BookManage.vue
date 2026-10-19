<template>
  <div class="search_container">
    <el-card shadow="always">
      <!-- 搜索内容和导出区域 -->
      <div class="search-header">
        <div class="search-left">
          <span class="search-label">条件搜索:</span>
          <el-select v-model="queryInfo.condition" class="search-select" filterable placeholder="请选择">
            <el-option v-for="item in searchs" :key="item.value" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
          <el-input
              v-model="queryInfo.query"
              class="search-input"
              placeholder="请输入内容"
              @keyup.enter.native="getBookList(true)"
          >
            <el-button slot="append" icon="el-icon-search" @click="getBookList(true)"></el-button>
          </el-input>
          <el-button size="mini" type="primary" @click="showAddDialog()"
          ><i class="el-icon-plus"></i> 添加书籍
          </el-button>
          <el-upload ref="upload" :auto-upload="false" :file-list="fileList"
                     :on-change="handleChange" :on-preview="handlePreview"
                     :on-remove="handleRemove"
                     action="https://jsonplaceholder.typicode.com/posts/" class="upload-demo" name="file">
            <el-button slot="trigger" size="mini" title="从Excel批量导入图书" type="primary">选取文件</el-button>
            <el-button size="mini" type="success" @click="submitUpload">上传</el-button>
          </el-upload>
        </div>
        <div class="search-right">
          <el-button size="mini" type="warning" @click="removeBatch()"
          ><i class="el-icon-delete"></i> 批量删除
          </el-button>
          <el-button class="el-icon-full-screen" size="mini" type="success" @click="fullScreen"
          >全屏
          </el-button>
          <el-button class="el-icon-printer" size="mini" type="primary" @click="downLoad"
          >导出PDF
          </el-button>
          <export-excel :data="tableData" :fields="json_fields" :header="title" class="export-excel-wrapper"
                        name="书籍管理.xlsx">
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
          :default-sort="{ prop: 'bookNumber', order: 'ascending' }"
          border
          element-loading-background="rgba(0, 0, 0, 0.8)"
          element-loading-spinner="el-icon-loading"
          element-loading-text="拼命加载中"
          height="520"
          stripe
          style="width: 100%"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="图书编号" prop="bookNumber" sortable>
        </el-table-column>
        <el-table-column label="书名" prop="bookName" width="80px">
        </el-table-column>
        <el-table-column label="作者" prop="bookAuthor" width="80px">
        </el-table-column>
        <el-table-column label="图书馆" prop="bookLibrary" width="80px">
        </el-table-column>
        <el-table-column label="分类" prop="bookType" width="80px">
        </el-table-column>
        <el-table-column label="位置" prop="bookLocation" sortable width="80px">
        </el-table-column>
        <el-table-column label="状态" prop="bookStatus" sortable width="80px">
        </el-table-column>
        <el-table-column label="描述" prop="bookDescription" width="400px">
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <!-- 修改按钮 -->
            <el-tooltip :enterable="false" content="修改" effect="dark" placement="top">
              <el-button icon="el-icon-edit" size="mini" type="primary"
                         @click="showEditDialog(scope.row.bookId)"></el-button>
            </el-tooltip>

            <!-- 删除按钮 -->
            <el-tooltip :enterable="false" content="删除" effect="dark" placement="top">
              <el-button icon="el-icon-delete" size="mini" type="danger"
                         @click="removeUserById(scope.row.bookId)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
        <template slot="empty">
          <span style="color: #8B7355;">暂无数据</span>
        </template>
      </el-table>
      <!-- 分页查询区域 -->
      <el-pagination :current-page="queryInfo.pageNum" :page-size="queryInfo.pageSize"
                     :page-sizes="[1, 2, 3, 4, 5]" :total="this.total" layout="total, sizes, prev, pager, next, jumper"
                     @size-change="handleSizeChange" @current-change="handleCurrentChange">
      </el-pagination>
      <!-- 修改规则的对话框 -->
      <el-dialog :visible.sync="editDialogVisible" title="修改书籍" width="50%" @close="editDialogClosed">
        <el-form ref="editFormRef" :model="editForm" :rules="editFormRules" label-width="120px">
          <el-form-item label="书名" prop="bookName">
            <el-input v-model="editForm.bookName"></el-input>
          </el-form-item>
          <el-form-item label="作者" prop="bookAuthor">
            <el-input v-model="editForm.bookAuthor"></el-input>
          </el-form-item>
          <el-form-item label="图书馆">
            <el-radio-group v-model="editForm.bookLibrary">
              <el-radio-button label="五山校区"></el-radio-button>
              <el-radio-button label="大学城校区"></el-radio-button>
              <el-radio-button label="国际校区"></el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="editForm.bookType" placeholder="请选择">
              <el-option v-for="item in types" :key="item.typeId" :label="item.typeName" :value="item.typeId">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="位置" prop="bookLocation">
            <el-input v-model="editForm.bookLocation"></el-input>
          </el-form-item>
          <el-form-item label="状态" prop="bookStatus">
            <el-radio-group v-model="editForm.bookStatus">
              <el-radio label="已借出">已借出</el-radio>
              <el-radio label="未借出">未借出</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="书籍简介" prop="bookDescription">
            <el-input v-model="editForm.bookDescription" type="textarea"></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="editDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="updateBook">确 定</el-button>
        </span>
      </el-dialog>
      <!-- 添加书籍的对话框 -->
      <el-dialog :visible.sync="addDialogVisible" title="添加书籍" width="50%" @close="addDialogClosed">
        <el-form ref="addFormRef" :model="addForm" :rules="addFormRules" label-width="120px">
          <el-form-item label="书名" prop="bookName">
            <el-input v-model="addForm.bookName"></el-input>
          </el-form-item>
          <el-form-item label="作者" prop="bookAuthor">
            <el-input v-model="addForm.bookAuthor"></el-input>
          </el-form-item>
          <el-form-item label="图书馆">
            <el-radio-group v-model="addForm.bookLibrary">
              <el-radio-button label="五山校区"></el-radio-button>
              <el-radio-button label="大学城校区"></el-radio-button>
              <el-radio-button label="国际校区"></el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="addForm.bookTypeNumber" placeholder="请选择">
              <el-option v-for="item in types" :key="item.typeId" :label="item.typeName" :value="item.typeId">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="位置" prop="bookLocation">
            <el-input v-model="addForm.bookLocation"></el-input>
          </el-form-item>
          <el-form-item label="状态" prop="bookStatus">
            <el-radio-group v-model="addForm.bookStatus">
              <el-radio label="已借出">已借出</el-radio>
              <el-radio label="未借出">未借出</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="书籍简介" prop="bookDescription">
            <el-input v-model="addForm.bookDescription" type="textarea"></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="addDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="addBook">添加书籍</el-button>
        </span>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
/**
 * 图书管理组件
 *
 * 功能：管理图书馆图书信息（增删改查）+ Excel批量导入
 * 特点：支持分页查询、条件搜索、表单验证、数据导出（Excel/PDF）、全屏查看、批量删除
 *
 * 配置：
 * - 默认每页显示：5条记录
 * - 默认排序：按图书编号升序
 * - 搜索条件：图书编号、书名、作者、图书馆、位置、状态、描述
 * - 支持校区：五山校区、大学城校区、国际校区
 *
 * 接口列表：
 * - POST admin/getBookList - 分页查询图书列表
 * - GET admin/getType - 获取图书分类列表
 * - GET admin/getBookInfo/:id - 获取单个图书信息
 * - POST admin/addBook - 添加图书
 * - PUT admin/updateBook - 修改图书
 * - DELETE admin/deleteBook/:id - 删除单个图书
 * - DELETE admin/deleteBookBatch - 批量删除图书
 * - POST admin/uploadBooks - Excel批量导入图书
 */
export default {
  data() {
    return {
      tableData: [],

      editDialogVisible: false,
      editForm: {
        bookName: "",
        bookAuthor: "",
        bookLibrary: "五山校区",
        bookType: "",
        bookLocation: "",
        bookStatus: "",
        bookDescription: "",
      },
      editFormRules: {
        bookName: [
          {required: true, message: "图书名称不能为空", trigger: "blur"},
        ],
        bookAuthor: [
          {required: true, message: "图书作者不能为空", trigger: "blur"},
        ],
        bookLocation: [
          {required: true, message: "图书位置不能为空", trigger: "blur"},
        ],
        bookDescription: [
          {required: true, message: "图书介绍不能为空", trigger: "blur"},
        ],
      },
      addDialogVisible: false,
      addForm: {
        bookName: "",
        bookAuthor: "",
        bookLibrary: "五山校区",
        bookTypeNumber: "",
        bookLocation: "",
        bookStatus: "",
        bookDescription: "",
      },
      addFormRules: {
        bookName: [
          {required: true, message: "图书名称不能为空", trigger: "blur"},
        ],
        bookAuthor: [
          {required: true, message: "图书作者不能为空", trigger: "blur"},
        ],
        bookLocation: [
          {required: true, message: "图书位置不能为空", trigger: "blur"},
        ],
        bookDescription: [
          {required: true, message: "图书介绍不能为空", trigger: "blur"},
        ],
      },
      searchs: [
        {
          value: "book_number",
          label: "图书编号",
        },
        {
          value: "book_name",
          label: "书名",
        },
        {
          value: "book_author",
          label: "作者",
        },
        {
          value: "book_library",
          label: "图书馆",
        },
        {
          value: "book_location",
          label: "位置",
        },
        {
          value: "book_status",
          label: "状态",
        },
        {
          value: "book_description",
          label: "描述",
        },
      ],
      queryInfo: {
        pageNum: 1,
        pageSize: 5,
        condition: "",
        query: "",
      },
      total: 0,
      types: [
        {
          typeId: "童话",
          typeName: "童话",
        },
        {
          typeId: "文学",
          typeName: "文学",
        },
        {
          typeId: "散文",
          typeName: "散文",
        },
      ],
      title: "书籍管理",
      json_fields: {
        '图书编号': 'bookNumber',
        '书名': 'bookName',
        '作者': 'bookAuthor',
        '图书馆': 'bookLibrary',
        '分类': 'bookType',
        '位置': 'bookLocation',
        '状态': 'bookStatus',
        '描述': 'bookDescription',
      },
      loading: true,
      multipleSelection: [],
      fileList: []
    };
  },
  methods: {
    handleSizeChange(val) {
      this.queryInfo.pageSize = val;
      this.getBookList();
    },
    handleCurrentChange(val) {
      this.queryInfo.pageNum = val;
      this.getBookList();
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    //让修改图书的对话框可见,并从数据库中回显数据
    async showEditDialog(id) {
      const {data: res1} = await this.$http.get("admin/getTypeList");
      if (res1.status !== 200) {
        return this.$message.error(res1.msg);
      }
      this.types = res1.data;
      this.editDialogVisible = true;
      const {data: res} = await this.$http.get(
          "admin/getBookInfo/" + id
      );
      // console.log(res);
      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }
      this.editForm = res.data;
    },
    //监听修改对话框的关闭，一旦对话框关闭，就重置表单，回显数据
    editDialogClosed() {
      this.$refs.editFormRef.resetFields();
    },
    //删除单个图书
    async removeUserById(id) {
      //弹框，询问用户是否删除数据
      const confirmResult = await this.$confirm(
          "此操作将永久删除该书籍, 是否继续?",
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          }
      ).catch((error) => {
        return error;
      });
      //如果用户确认删除，则返回值为字符串confirm
      //如果用户取消删除，则返回值为字符串cancel
      // console.log(confirmResult);
      if (confirmResult !== "confirm") {
        return this.$message.info("已经取消删除");
      }
      //如果用户确认删除，那么下一步就是发送axios请求，检查响应状态码是否成功,成功则返回删除成功，否则返回删除失败
      const {data: res} = await this.$http.delete("admin/deleteBook/" + id);
      // console.log(res);
      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }
      this.$message.success(res.msg);
      // 防止删除出现数据显示错误
      this.queryInfo.pageNum = 1;
      this.queryInfo.pageSize = 5;
      this.getBookList();
    },
    //监听添加公告对话框的关闭，一旦对话框关闭，就重置表单
    addDialogClosed() {
      this.$refs.addFormRef.resetFields();
    },
    //当用户点击发送新公告时，让添加对话框的visible改为true
    async showAddDialog() {
      const {data: res} = await this.$http.get("admin/getTypeList");
      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }
      this.types = res.data;
      this.addDialogVisible = true;
    },
    async getBookList(isSearch = false) {
      this.loading = true;
      this.tableData = [];
      // 只有在搜索时才重置到第一页，分页时保持当前页码
      if (isSearch) {
        this.queryInfo.pageNum = 1;
      }
      const {data: res} = await this.$http.post(
          "admin/getBookList",
          this.queryInfo
      );
      // console.log(res);
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
    async addBook() {
      const {data: res} = await this.$http.post(
          "admin/addBook",
          this.addForm
      );
      // console.log(res);
      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }
      this.$message.success(res.msg);
      this.getBookList(true);
      this.addDialogVisible = false;
    },
    async updateBook() {
      this.$refs.editFormRef.validate(async (valid) => {
        if (!valid) {
          return;
        }
        const {data: res} = await this.$http.put(
            "admin/updateBook",
            this.editForm
        );
        // console.log(res);
        if (res.status !== 200) {
          return this.$message.error(res.msg);
        }
        this.$message.success(res.msg);
        this.getBookList(true);
        this.editDialogVisible = false;
      });
    },
    downLoad() {
      this.getPdf(this.title); //参数是下载的pdf文件名
    },
    fullScreen() {
      // Dom对象的一个属性: 可以用来判断当前是否为全屏模式(trueORfalse)
      let full = document.fullscreenElement;
      // 切换为全屏模式
      if (!full) {
        // 文档根节点的方法requestFullscreen实现全屏模式
        document.documentElement.requestFullscreen();
      } else {
        // 退出全屏模式
        document.exitFullscreen();
      }
    },
    // 批量删除图书
    async removeBatch() {
      //弹框，询问用户是否删除数据
      const confirmResult = await this.$confirm(
          "此操作将永久删除这些书籍, 是否继续?",
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          }
      ).catch((error) => {
        return error;
      });
      //如果用户确认删除，则返回值为字符串confirm
      //如果用户取消删除，则返回值为字符串cancel
      // console.log(confirmResult);
      if (confirmResult !== "confirm") {
        return this.$message.info("已经取消删除");
      }
      // 判断multipleSelection数组是否为空，为空的话进行提示
      if (this.multipleSelection.length == 0) {
        return this.$message.error({
          message: "选中项为空，无法进行批量删除",
          duration: 1000
        });
      }
      //如果用户确认删除，那么下一步就是发送axios请求，检查响应状态码是否成功,成功则返回删除成功，否则返回删除失败
      // const { data: res } = await this.$http.get("admin/delete_book/" + id);
      const {data: res} = await this.$http.delete("admin/deleteBookBatch", {
        data: this.multipleSelection
      });
      // console.log(res);
      if (res.status !== 200) {
        return this.$message.error({
          message: res.msg,
          duration: 1000
        });
      }
      this.$message.success({
        message: res.msg,
        duration: 1000
      });
      // 防止删除出现数据显示错误
      this.queryInfo.pageNum = 1;
      this.queryInfo.pageSize = 5;
      this.getBookList(true);
    },
    async submitUpload() {
      if (this.fileList.length === 0) {
        return this.$message.error({
          message: "请先选择要上传的Excel文件",
          duration: 1500
        });
      }

      // 构建FormData对象
      const formData = new FormData();
      formData.append("file", this.fileList[0].raw);

      try {
        const {data: res} = await this.$http.post("admin/uploadBooks", formData, {
          headers: {
            "Content-Type": "multipart/form-data"
          }
        });

        this.fileList = [];
        if (res.status === 200) {
          this.$message.success({
            duration: 1500,
            message: res.msg || "Excel批量导入图书成功"
          });
          this.getBookList(true);
        } else {
          this.$message.error({
            duration: 1500,
            message: res.msg || "导入失败"
          });
        }
      } catch (error) {
        this.$message.error({
          duration: 1500,
          message: "上传失败：" + (error.message || "网络错误")
        });
      }
    },
    handleRemove(file, fileList) {
      this.fileList = fileList;
    },
    handleChange(file, fileList) {
      this.fileList = fileList;
    },
    handlePreview(file) {
      console.log(file);
    },

  },
  created() {
    this.getBookList();
  },
  computed: {
    headers() {
      return {
        "Authorization": "Bearer " + window.sessionStorage.getItem('token')
      };
    }
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
