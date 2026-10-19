import Vue from 'vue'

/**
 * Element UI按需引入配置
 *
 * 作用：
 * - 只引入项目中实际使用的组件，减小打包体积（完整引入约300KB+，按需引入约100KB）
 * - 提升首屏加载速度
 *
 * 维护建议：
 * - 新增组件时需在此处添加引入和注册
 * - 定期清理未使用的组件
 * - 需在babel.config.js中配置babel-plugin-component插件
 */
// ==================== 表单相关组件 ====================
// ==================== 数据展示组件 ====================
// ==================== 导航组件 ====================
// ==================== 布局组件 ====================
// ==================== 反馈组件 ====================
// ==================== 全局方法组件 ====================
import {
    Alert,
    Aside,
    Avatar,
    Breadcrumb,
    BreadcrumbItem,
    Button,
    Card,
    Cascader,
    Checkbox,
    CheckboxGroup,
    Col,
    Container,
    DatePicker,
    Dialog,
    Divider,
    Form,
    FormItem,
    Header,
    Input,
    Loading,
    Main,
    Menu,
    MenuItem,
    Message,
    MessageBox,
    Option,
    Pagination,
    Popover,
    Radio,
    RadioButton,
    RadioGroup,
    Row,
    Select,
    Step,
    Steps,
    Submenu,
    Switch,
    Table,
    TableColumn,
    TabPane,
    Tabs,
    Tag,
    Tooltip,
    Tree,
    Upload,
} from 'element-ui'

/**
 * 全局注册所有引入的Element UI组件
 *
 * Vue.use()的作用：
 * - 调用组件的install方法
 * - 全局注册组件，可在任何模板中使用<el-xxx>标签
 */
Vue.use(Button)
Vue.use(Form)
Vue.use(FormItem)
Vue.use(Input)
Vue.use(Container)
Vue.use(Header)
Vue.use(Aside)
Vue.use(Main)
Vue.use(Menu)
Vue.use(Submenu)
Vue.use(MenuItem)
Vue.use(Breadcrumb)
Vue.use(BreadcrumbItem)
Vue.use(Card)
Vue.use(Row)
Vue.use(Col)
Vue.use(Table)
Vue.use(TableColumn)
Vue.use(Switch)
Vue.use(Tooltip)
Vue.use(Pagination)
Vue.use(Dialog)
Vue.use(Tag)
Vue.use(Tree)
Vue.use(Select)
Vue.use(Option)
Vue.use(Cascader)
Vue.use(Alert)
Vue.use(Tabs)
Vue.use(TabPane)
Vue.use(Steps)
Vue.use(Step)
Vue.use(CheckboxGroup)
Vue.use(Checkbox)
Vue.use(Upload)
Vue.use(Avatar)
Vue.use(Loading)
Vue.use(Divider)
Vue.use(DatePicker)
Vue.use(RadioGroup)
Vue.use(RadioButton)
Vue.use(Radio)
Vue.use(Popover)

/**
 * 挂载全局方法到Vue原型
 *
 * 作用：在组件内通过this.$message、this.$confirm直接调用，无需import
 *
 * 使用示例：
 * this.$message.success('操作成功')
 * this.$message.error('操作失败')
 * this.$confirm('确认删除？', '提示')
 *
 * 区别：
 * - Message: 轻量提示，自动消失
 * - MessageBox: 模态对话框，需要用户交互
 */
Vue.prototype.$message = Message
Vue.prototype.$confirm = MessageBox.confirm