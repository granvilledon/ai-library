<template>
  <div class="comment_container">
    <!-- 背景图片层 - 半透明效果 -->
    <div class="backgroundImg">
      <img alt="" src="@/assets/images/background.jpg"/>
    </div>

    <!-- 弹幕展示区 + 输入框 -->
    <div class="barrages-drop">
      <!-- 弹幕组件 - 展示读者留言 -->
      <vue-baberrage
          :barrageList="barrageList"
          :boxHeight="boxHeight"
          :boxWidth="boxWidth"
          :isShow="barrageIsShow"
          :lanesCount="lanesCount"
          :loop="barrageLoop"
          :maxWordCount="maxWordCount"
          :messageHeight="messageHeight"
          :throttleGap="throttleGap"
          style="width: 1330px; height: 750px"
      >
      </vue-baberrage>

      <!-- 留言输入框 - 居中悬浮 -->
      <div class="addMain">
        <el-input
            v-model.trim="input"
            class="input-with-select"
            placeholder="请输入内容"
            @keyup.enter.native="addContent"
        >
          <el-button slot="append" icon="el-icon-edit" @click="addContent"></el-button>
        </el-input>
      </div>
    </div>
  </div>
</template>

<script>
// 引入弹幕库和ID生成工具
import {MESSAGE_TYPE} from "vue-baberrage";

/**
 * 读者留言弹幕组件
 *
 * 功能：展示读者留言弹幕 + 发布新留言（防抖处理）
 * 特点：实时弹幕滚动、输入内容校验、5秒防抖限制、统一头像替换
 */
export default {
  data() {
    return {
      // 弹幕显示控制
      barrageIsShow: true,     // 是否显示弹幕
      messageHeight: 3,        // 消息高度倍数
      barrageLoop: false,      // 是否循环播放
      maxWordCount: 2,         // 最大词数限制
      lanesCount: 1,           // 弹幕轨道数量
      boxWidth: 1600,          // 弹幕容器宽度
      boxHeight: 200,          // 弹幕容器高度
      throttleGap: 3000,       // 弹幕间隔时间（毫秒）

      input: "",               // 输入框内容
      barrageList: [],         // 弹幕列表数组

      // 单条弹幕数据结构模板
      barrage: {
        id: "",                                              // 唯一ID（由后端生成）
        avatar: require('@/assets/images/dog.png'),         // 头像（统一使用柴犬图标）
        msg: "",                                             // 留言内容
        time: "",                                            // 发布时间
        type: MESSAGE_TYPE.NORMAL,                          // 弹幕类型：普通
        barrageStyle: "",                                    // 弹幕样式
      },
    };
  },

  methods: {
    /**
     * 发布新留言（带防抖处理）
     *
     * 流程：
     * 1. 校验输入内容（不能为空/纯数字/纯字母）
     * 2. 调用接口提交留言
     * 3. 成功后刷新弹幕列表并清空输入框
     *
     * 防抖配置：
     * - 间隔时间：5000毫秒（5秒内只能提交一次）
     * - trailing: false - 延迟期间不再触发
     *
     * 接口：POST user/addComment
     * 注意：使用 _.throttle 进行防抖，防止频繁提交
     */
    addContent: _.throttle(async function () {
      // 校验输入内容：不能为空、不能是纯数字、不能是纯字母
      if (this.input.trim() === '' || /^\d+$/.test(this.input) || /^[a-zA-Z]+$/.test(this.input)) {
        this.$message.info({
          message: "请不要输入无意义的内容",
          duration: 1000
        })
        return;
      }

      this.barrage.msg = this.input;

      const {data: res} = await this.$http.post('user/addComment', this.barrage);

      if (res.status !== 200) {
        return this.$message.error(res.msg)
      }

      // 刷新弹幕列表
      this.getCommentList();

      // 清空输入框
      this.input = "";

      this.$message.success(res.msg);
    }, 5000, {trailing: false}),

    /**
     * 获取留言列表并转换为弹幕格式
     *
     * 流程：
     * 1. 调用接口获取所有留言
     * 2. 遍历数据，统一替换为本地头像（dog.png）
     * 3. 添加空对象触发数组更新，强制刷新弹幕组件
     *
     * 接口：GET user/getCommentList
     * 响应：[{id, msg, time, ...}]
     *
     * 注意：
     * - 后端返回的头像可能是URL，前端统一替换为本地资源
     * - push空对象是为了触发Vue响应式更新，确保弹幕组件重新渲染
     */
    async getCommentList() {
      const {data: res} = await this.$http.get("user/getCommentList");

      if (res.status !== 200) {
        return this.$message.error(res.msg);
      }

      this.$message.success(res.msg);

      // 替换后端返回数据的头像为本地图片（保持视觉统一）
      this.barrageList = res.data.map(item => ({
        ...item,
        avatar: require('@/assets/images/dog.png')
      }));

      // 触发数组更新以刷新组件（Vue响应式 hack）
      this.barrageList.push({})
    },
  },

  /**
   * 组件挂载后加载留言列表
   */
  mounted() {
    this.getCommentList();
  }
};
</script>

<style lang="less" scoped>
/* 容器 - 相对定位，撑满全屏 */
.comment_container {
  position: relative;
  height: 100%;
  width: 100%;
}

/* 背景图片层 - 绝对定位覆盖全屏 */
.backgroundImg {
  position: absolute;
  height: 100%;
  width: 100%;

  img {
    height: 100%;
    width: 100%;
    opacity: 0.5; /* 半透明效果，不干扰弹幕阅读 */
  }
}

/* 弹幕展示区 - 相对定位作为子元素基准 */
.barrages-drop {
  position: relative;
}

/* 留言输入框 - 绝对定位居中悬浮 */
.addMain {
  position: absolute;
  width: 300px;
  height: 100%;
  background-color: var(--chai-light);
  top: 450px; /* 距离顶部450px */
  left: 50%; /* 水平居中技巧 */
  transform: translate(-50%, -50%); /* 完美居中 */
}
</style>