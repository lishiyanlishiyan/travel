<template>
  <f7-page :page-content="false">
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"/>
      <f7-nav-title title="查询服务参数类型"/>
    </f7-navbar>
    <f7-list form class="page-content">
      <f7-list-input :value="checkParam.path" @input="checkParam.path=$event.target.value" label="服务路径"
                     placeholder="Service Path" v-validate="'required'"/>
      <f7-list-input :value="checkParam.name" @input="checkParam.name=$event.target.value" label="服务名称"
                     placeholder="Service Name" v-validate="'required'"/>
      <f7-list-input :value="checkParam.result" label="参数类型" placeholder="Param Class Type" type="textarea"/>
      <f7-button large fill @click="checkParams" :disabled="!formValidator.isFormValid" text="查询"/>
    </f7-list>
  </f7-page>
</template>
<script>
import { checkParams } from '../services/caller/ServiceCaller'

export default {
  data () {
    return {
      checkParam: {
        path: '',
        name: '',
        result: ''
      },
      formValidator: {}
    }
  },
  mounted () {
    this.$initFormValidate()
  },
  methods: {
    checkParams () {
      checkParams(this.checkParam.path, this.checkParam.name).then(data => {
        this.checkParam.result = data && data.success ? data.resultData : data.message
      })
    }
  }
}
</script>
