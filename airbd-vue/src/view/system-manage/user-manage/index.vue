<template>
  <div>
    <Card>
      <Form style="width: 600px">
        <FormItem>
          <Row :gutter="16">
            <Col span="8">
            <Input type="text" v-model="searchParam.userName" placeholder="登录名"/>
            </Col>
            <Col span="8">
            <Input type="text" v-model="searchParam.mobile" placeholder="手机"/>
            </Col>
            <Col span="8">
            <ButtonGroup>
              <Button type="primary" icon="logo-google" @click="handleSearch">搜索</Button>
              <Button icon="ios-backspace-outline" @click="handleReset">重置</Button>
            </ButtonGroup>
            </Col>
          </Row>
        </FormItem>
      </Form>
      <div>
        <ButtonGroup>
          <Button icon="md-add" @click="modalParam.showModal=true,modalParam.operate=1,modalParam.title='新增'">新增</Button>
          <Button icon="md-create" @click="handleEdit">修改</Button>
          <Button icon="md-close" @click="handleDelete">删除</Button>
        </ButtonGroup>
      </div>
      <Table ref="tables" :loading="loading" :data="tableData" :columns="columns" highlight-row
             @on-row-dblclick="handleDbClick" @on-current-change="handleSelected"/>
      <div style="overflow: hidden">
        <Button @click="exportExcel" style="margin: 10px 0px;">导出Csv</Button>
        <div style="float: right;margin: 10px 0px;">
          <Page :total="total" :current="searchParam.pageIndex" :page-size="searchParam.pageSize" show-sizer show-total @on-change="handlePage" @on-page-size-change='handlePageSize'></Page>
        </div>
      </div>
    </Card>
    <MyForm :modalParam.sync="modalParam" @refreshTable="handleSearch"/>
  </div>
</template>

<script>
import {format} from '@/libs/tools'
import Tables from '_c/tables'
import * as apis from '@/api/data'
import MyForm from '@/view/system-manage/user-manage/form'

export default {
  components: {
    Tables,
    MyForm
  },
  data () {
    return {
      tableData: [],
      total: 0,
      loading: true,
      selection: [],
      searchParam: {
        pageIndex: 1,
        pageSize: 10,
        userName: '',
        mobile: ''
      },
      modalParam: {
        showModal: false,
        operate: 0,
        title: '',
        id: 0
      },
      columns: [
        {type: 'selection', width: 60, key: 'id'},
        {title: '登录名称', key: 'userName'},
        {title: '昵称', key: 'nickName', editable: true},
        {title: '用户类型', key: 'userTypeZh'},
        {title: '手机号', key: 'mobile'},
        {title: '性别', key: 'sexZh'},
        {title: '邮箱', key: 'email'},
        {title: '状态', key: 'statusZh'},
        {title: '证件', key: 'cardNo'},
        {title: '创建时间', key: 'createTime', sortable: true, render: (h, param) => { return h('div', format(param.row.createTime)) }}
      ]
    }
  },
  methods: {
    handleSearch () {
      apis.listPageSysUser(this.searchParam).then(res => {
        this.tableData = res.data.data
        this.total = res.data.total
        this.loading = false
      })
    },
    handleReset () {
      this.searchParam = {}
    },
    handleSelected (currentRow) {
      currentRow._checked === true ? this.selection.splice(this.selection.indexOf(currentRow), 1) : this.selection.push(currentRow)
      let id = currentRow.id
      this.tableData.forEach(function (row) {
        if (row.id === id) {
          row._checked = !row._checked
        }
      })
    },
    handleDbClick (value) {
      this.modalParam.id = value.id
      this.modalParam.operate = 0
      this.modalParam.title = '详情'
      this.modalParam.showModal = true
    },
    handleEdit () {
      if (this.selection.length <= 0) {
        this.$Message.error('请选择需要操作的数据！')
        return
      }
      if (this.selection.length > 1) {
        this.$Message.error('只能选择一条数据！')
        return
      }
      this.modalParam.id = this.selection[0].id
      this.modalParam.operate = 2
      this.modalParam.title = '修改'
      this.modalParam.showModal = true
    },
    handleDelete () {
      if (this.selection.length <= 0) {
        this.$Message.error('请选择需要操作的数据！')
        return
      }
      this.$Modal.confirm({
        title: '确定删除吗？',
        onOk: () => {
          let arr = []
          this.selection.forEach(function (row) {
            arr.push(row.id)
          })
          if (arr.length > 0) {
            apis.deleteSysUserByIds(arr).then(res => {
              this.$Message.info(res.data.message)
              this.handleSearch()
            }).catch(err => {
              this.$Message.error(err.message)
            })
          }
        }
      })
    },
    exportExcel () {
      this.$refs.tables.exportCsv({
        filename: `table-${(new Date()).valueOf()}.csv`
      })
    },
    handlePage (value) {
      this.searchParam.pageIndex = value
      this.handleSearch()
    },
    handlePageSize (value) {
      this.searchParam.pageSize = value
      this.handleSearch()
    }
  },
  mounted () {
    this.handleSearch()
  }
}
</script>
