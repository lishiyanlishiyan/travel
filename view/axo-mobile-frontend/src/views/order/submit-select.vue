<template>
      <common-autocomplete
        class="item-link"
        input-class-name="text-align-right"
        :inline-label="true"
        :page-title="$i18nMsg(customControl.controlTitleCN,customControl.controlTitleEN)"
        :placeholder="$i18nMsg(customControl.controlTitleCN,customControl.controlTitleEN)"
        :text-property="$i18nMsg('foreShowCN', 'foreShowEN')"
        :text-property-fun="textPropertyFun"
        @change="selectDataValue($event)"
        value-property="dataValue"
        :close-on-select="true"
        :request-source-on-open="true"
        input-events="search"
        clear-button
        :autocompleteConfig="selectCustomControlConfig">
        <div slot="label">
          <span class="text-color-red" v-if="customControl.controlRequired==='1'">*</span>
          <span>{{$i18nMsg(customControl.controlTitleCN,customControl.controlTitleEN)}}</span>
          <f7-link  @click="showRemark(customControl)">
            <f7-icon size="20" material="help_outline"
                     v-if="customControl.controlRemarkCN && customControl.controlExtend1 && customControl.controlExtend1==='Y'" >
            </f7-icon>
          </f7-link>
        </div>
      </common-autocomplete>
</template>
<script>
import OrderService from '../../services/order/OrderService'
import { $coreShowPopup } from '../../utils/AxoUtils'
export default {
  name: 'submit-select',
  props: {
    superObj: {
      type: Object
    },
    value: {
      type: Object
    },
    taNeedDataVo: {
      type: Object
    },
    customNeedDataVo: {
      type: Object
    },
    apprBuffer: {
      type: Object
    },
    approvers: {
      type: Array
    },
    compControlDatas: {
      type: Object
    },
    compSelectDatas: {
      type: Object
    },
    currentMode: {
      type: String
    },
    submitPageData: {
      type: Object
    }
  },
  data () {
    let self = this
    const { superObj, value, taNeedDataVo, customNeedDataVo, apprBuffer, approvers, compControlDatas, compSelectDatas, currentMode, submitPageData } = self.$props
    let selectCustomControlConfig = {
      keyWordKey: 'queryStr',
      searchMethod: function (query, config) {
        query['selectTypeId'] = self.customControl.id
        query['orderBy'] = self.$i18nMsg(self.customControl.selectSortCN, self.customControl.selectSortEN)
        if (self.customControl.superId && self.superIds[self.customControl.superId]) {
          query['superId'] = self.superIds[self.customControl.superId].dataId
        }
        return OrderService.getSubmitDatas(query, config)
      }
    }
    return { customControl: value,
      superIds: superObj,
      selectCustomControlConfig,
      tamasterOrderSubmitNeedDataVo: taNeedDataVo,
      customControlSaveDataItemVo: customNeedDataVo,
      approverBuffer: apprBuffer,
      approverList: approvers,
      companyControlDatas: compControlDatas,
      companySelectDatas: compSelectDatas,
      currentApprMode: currentMode,
      submitPage: submitPageData }
  },
  watch: {
    approvers: {
      handler (v) {
        this.approverList = v || []
      },
      deep: true
    }
  },
  methods: {
    textPropertyFun (item) {
      return `<div class="item-title">${item.data.dataValue}</div><div class="item-subtitle">${this.$i18nMsg(item.data.foreShowCN, item.data.foreShowEN)}</div>`
    },
    selectDataValue (data) {
      let self = this
      if (data) {
        this.pickDataValue(self.customControl, data)
      } else {
        this.removeDataValue(self.customControl)
      }
    },
    pickDataValue (customControl, selectData) {
      let self = this
      let controlName = customControl.controlName
      let superId = self.customControl.id
      let dataId = selectData.id
      let dataValue = customControl.template === 'custom_create_control_dataValue.ftl' ? (selectData.dataValue + '__' + selectData.foreShowCN + '$' + selectData.dataValue + '__' + selectData.foreShowEN) : selectData.dataValue
      let paramName = OrderService.hanlderControlName(controlName)
      if (OrderService.isCustomControlSaveDataItemVoStart(controlName)) {
        self.customControlSaveDataItemVo[paramName] = dataValue
        self.customControlSaveDataItemVo[paramName + 'ID'] = selectData.id
      } else {
        self.tamasterOrderSubmitNeedDataVo[paramName] = dataValue
        self.tamasterOrderSubmitNeedDataVo[paramName + 'ID'] = selectData.id
      }
      self.superIds[superId] = dataId
      if (self.companyControlDatas[dataId]) {
        self.superIds[superId] = { superId: superId, dataId: selectData.id }
        if (self.currentApprMode === customControl.id) {
          self.loadSelectDataDetail(customControl, selectData)
        }
      } else {
        OrderService.getSubmitDateCount({ superId: dataId }).then(data => {
          for (let childData of data) {
            let controlId = childData.controlId
            let currentSelectDatas = self.companySelectDatas[controlId]
            if (currentSelectDatas) {
              // if (!currentSelectDatas[childData.id]) {
              currentSelectDatas[dataId] = true
              // }
            } else {
              self.companySelectDatas[controlId] = {}
              self.companySelectDatas[controlId][dataId] = true
            }
          }
          if (self.currentApprMode === customControl.id) {
            self.loadSelectDataDetail(customControl, selectData)
          }
          self.companyControlDatas[dataId] = data
          self.superIds[superId] = { superId: superId, dataId: selectData.id }
          self.$emit('dataChange', self.$data)
        })
      }
    },
    removeDataValue (customControl) {
      let self = this
      let currentCustomControlId = customControl.id
      if (self.superIds) {
        let currentSuperObj = self.superIds[currentCustomControlId]
        if (currentSuperObj && currentSuperObj.superId) {
          self.superIds[currentCustomControlId] = null
        }
      }
      if (customControl.id === self.currentApprMode) {
        let apprCustomControl = self.findApprCustomControl(customControl)
        if (apprCustomControl) {
          self.approverList = []
        }
      }
      let controlName = customControl.controlName
      let paramName = OrderService.hanlderControlName(controlName)
      if (OrderService.isCustomControlSaveDataItemVoStart(controlName)) {
        self.customControlSaveDataItemVo[paramName] = null
        self.customControlSaveDataItemVo[paramName + 'ID'] = null
      } else {
        self.tamasterOrderSubmitNeedDataVo[paramName] = null
        self.tamasterOrderSubmitNeedDataVo[paramName + 'ID'] = null
      }
      self.$emit('dataChange', self.$data)
    },
    findApprCustomControl (customControl) {
      let result = null
      while (!result && customControl) {
        if (customControl.forAppr === 'Y') {
          result = customControl
        } else {
          customControl = self.superIds[customControl.id]
        }
      }
      return result
    },
    showRemark (customControl) {
      let self = this
      $coreShowPopup(self.$i18nMsg(customControl.controlRemarkCN, customControl.controlRemarkEN))
    },
    loadSelectDataDetail (customControl, selectData) {
      let self = this
      if (customControl.forAppr === 'Y') {
        if (self.approverBuffer.hasOwnProperty(selectData.id)) {
          self.approverList = self.approverBuffer[selectData.id]
          self.customControlSaveDataItemVo['approveTypeChoose'] = selectData.id
          self.$emit('dataChange', self.$data)
        } else {
          let approvalConfig = self.submitPage.approvalConfig
          let queryParam = {
            selectDataId: selectData.id
          }
          if (approvalConfig) {
            queryParam['isEnableCountryRegion'] = approvalConfig.isEnableCountryRegion
            queryParam['countryRegionConfig'] = approvalConfig.countryRegionConfig
          }
          OrderService.getSubmitDataDetails(queryParam).then(data => {
            self.approverList = data.approvers
            self.approverBuffer[selectData.id] = data.approvers
            self.customControlSaveDataItemVo['approveTypeChoose'] = selectData.id
            self.$emit('dataChange', self.$data)
          }, () => {
            self.approverList = []
            self.customControlSaveDataItemVo['approveTypeChoose'] = 0
            self.$emit('dataChange', self.$data)
          })
        }
      }
    }
  },
  mounted () {

  }
}
</script>

<style scoped>

</style>
