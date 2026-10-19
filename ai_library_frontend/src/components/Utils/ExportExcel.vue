<template>
  <span class="export-excel-wrapper" @click="exportExcel">
    <slot></slot>
  </span>
</template>

<script>
import * as XLSX from 'xlsx'

/**
 * 基于 xlsx 库的真正 Excel 导出组件
 *
 * 特点：生成真正的 .xlsx 格式文件，可被 EasyExcel 正确解析
 *
 * Props:
 * - data: 要导出的数据数组
 * - fields: 字段映射对象 {显示名: 字段名}
 * - header: 工作表名称
 * - name: 导出文件名（自动添加 .xlsx 后缀）
 */
export default {
  name: 'exportExcel',
  props: {
    data: {
      type: Array,
      required: true
    },
    fields: {
      type: Object,
      required: true
    },
    header: {
      type: String,
      default: '数据导出'
    },
    name: {
      type: String,
      default: 'export'
    }
  },
  methods: {
    exportExcel() {
      if (!this.data || this.data.length === 0) {
        this.$message.warning('暂无数据可导出')
        return
      }

      // 获取表头和字段映射
      const headers = Object.keys(this.fields)
      const keys = Object.values(this.fields)

      // 构建二维数组（带表头）
      const worksheetData = [headers]

      // 添加数据行
      this.data.forEach(item => {
        const row = keys.map(key => {
          const value = item[key]
          return value !== undefined && value !== null ? String(value) : ''
        })
        worksheetData.push(row)
      })

      // 创建工作簿和工作表
      const worksheet = XLSX.utils.aoa_to_sheet(worksheetData)
      const workbook = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(workbook, worksheet, this.header)

      // 生成文件名（确保以 .xlsx 结尾）
      let fileName = this.name
      if (!fileName.toLowerCase().endsWith('.xlsx')) {
        fileName += '.xlsx'
      }

      // 导出文件
      XLSX.writeFile(workbook, fileName)
    }
  }
}
</script>

<style scoped>
.export-excel-wrapper {
  cursor: pointer;
}
</style>
