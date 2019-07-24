<template>
  <f7-page page-content>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"/>
      <f7-nav-title>{{$t('common.label.order')}}</f7-nav-title>
    </f7-navbar>
    <f7-list v-if="submitPage.apprLineControlList" inline-labels>
      <f7-list-item :title="submitPage.approvalConfig.apprETA==='Y'?$t('order.label.approveMode'):$t('order.label.CopyMode')"
                    smart-select :smart-select-params="{openIn: 'sheet',closeOnSelect: true}">
        <select name="currentApprMode" v-model="currentApprMode">
          <option :value="'0'">{{$i18nBundle('order.label.submitDefaultLine')}}</option>
          <option :key="index" v-for="(apprLine,index) in submitPage.apprLineControlList" :value="apprLine.id">
            {{$i18nMsg(apprLine.controlTitleCN,apprLine.controlTitleEN)}}
          </option>
        </select>
      </f7-list-item>
    </f7-list>
    <f7-list inline-labels no-hairlines-md>
      <!--eslint-disable-next-line-->
      <template v-for="(customControl,index) in submitPage.customControlList"  v-if="calSubmitPageItemShowFlag(customControl)" >
        <f7-list-input v-if="customControl.controlType==='text'"  @input="changeSubmitInputVal($event.target,customControl)"
          :key="index">
          <div slot="label">
            <span class="text-color-red" v-if="customControl.controlRequired==='1'">*</span>
            <span>{{$i18nMsg(customControl.controlTitleCN,customControl.controlTitleEN)}}</span>
          </div>
        </f7-list-input>
        <f7-list-item v-if="customControl.controlType==='radio'"
                       :key="index"
                      smart-select :smart-select-params="{openIn: 'sheet',closeOnSelect: true}">
          <div slot="title">
            <span class="text-color-red" v-if="customControl.controlRequired==='1'">*</span>
            <span>{{$i18nMsg(customControl.controlTitleCN,customControl.controlTitleEN)}}</span>
          </div>
          <select @change="changeSubmitInputVal($event.target,customControl)">
            <option value="">{{$i18nMsg(customControl.controlRemarkCN,customControl.controlRemarkEN)}}</option>
          </select>
        </f7-list-item>
        <f7-list-input  v-if="customControl.controlType==='textarea'" :key="index" type="textarea" @input="changeSubmitInputVal($event.target,customControl)">
          <div slot="label">
            <span class="text-color-red" v-if="customControl.controlRequired==='1'">*</span>
            <span>{{$i18nMsg(customControl.controlTitleCN,customControl.controlTitleEN)}}</span>
          </div>
        </f7-list-input>
        <li :key="index" v-if="customControl.controlType==='select'" >
          <submit-select :value="customControl" :superObj="superIds"
                         :taNeedDataVo="tamasterOrderSubmitNeedDataVo"
                         :customNeedDataVo="customControlSaveDataItemVo"
                         :apprBuffer="approverBuffer"
                         :approvers="approverList"
                         :compControlDatas="companyControlDatas"
                         :compSelectDatas="companySelectDatas"
                         :currentMode="currentApprMode"
                         :submitPageData="submitPage"
                         :key="index"
                          @dataChange="changeData"/>
        </li>
        <f7-list-item :key="index" v-if="customControl.controlType==='label'">
          <div slot="title">{{$i18nMsg(customControl.controlTitleCN,customControl.controlTitleEN)}}</div>
          <template v-if="customControl.controlRemarkCN">
            <div class="text-color-red">{{$i18nMsg(customControl.controlRemarkCN,customControl.controlRemarkEN)}}</div>
          </template>
        </f7-list-item>
      </template>
    </f7-list>
    <f7-block-title v-if="approverList && approverList.length>0">{{submitPage.approvalConfig.apprETA==='Y'?$i18nBundle('order.label.approvers'):$i18nBundle('order.label.copyto')}}</f7-block-title>
    <f7-list>
      <f7-list-item v-for="(approver, index) in approverList" :key="index" >
        <template>
          {{$i18nMsg(approver.approver.nameCN,approver.approver.nameEN)+'('+approver.approver.email+')'}}
        </template>
      </f7-list-item>
    </f7-list>
    <f7-block v-show="initFlag">
      <f7-row>
        <f7-col>
          <f7-button fill large  @click="doSubmitOrder">
            {{$t('order.label.submitPage')}}
          </f7-button>
        </f7-col>
      </f7-row>
    </f7-block>
  </f7-page>
</template>

<script>
import OrderService from '../../services/order/OrderService'
import submitSelect from './submit-select'
import { $coreError } from '../../utils/AxoUtils'
import PolicyService from '../../services/policy/PolicyControlService'
import trim from 'lodash/trim'
import { mapGetters } from 'vuex'
export default {
  name: 'order-submit',
  data () {
    const currentTaNo = this.$f7route.params.taNo
    const companyCode = ''
    let submitPage = {}
    let superIds = {}
    let tamasterOrderSubmitNeedDataVo = {}
    let customControlSaveDataItemVo = {}
    let currentApprMode = 0
    let companySelectDatas = {}
    let approverBuffer = {}
    let approverList = []
    let companyControlDatas = {}
    let dynamicApprFlag = false
    let initFlag = false
    let validatorInfos = {}
    return { currentTaNo,
      companyCode,
      submitPage,
      superIds,
      customControlSaveDataItemVo,
      initFlag,
      validatorInfos,
      tamasterOrderSubmitNeedDataVo,
      currentApprMode,
      companySelectDatas,
      approverBuffer,
      approverList,
      companyControlDatas,
      dynamicApprFlag }
  },
  watch: {
    currentApprMode: {
      handler (newValue, oldValue) {
        this.changeCurrentMode(newValue, oldValue)
      },
      deep: true
    }
  },
  methods: {
    calSubmitPageItemShowFlag (customControl) {
      let self = this
      let idShowFlag = self.showItemById(customControl)
      let itemShowFlag = self.showSubmitPageItem(customControl)
      let resultFlag = false
      if (itemShowFlag === 1) {
        resultFlag = true
      } else if (itemShowFlag === 2 && idShowFlag) {
        resultFlag = true
      }
      if (resultFlag) {
        let custValidators = customControl.validators
        let addFlag = true
        if (!custValidators) {
          custValidators = []
        } else {
          for (let custValidtor of custValidators) {
            if (custValidtor.validFuncName === 'validateNullValue') {
              addFlag = false
              break
            }
          }
        }
        if (addFlag && customControl.controlRequired === '1') {
          let notNullValidator = {
            validFuncName: 'validateNullValue',
            validAlertCN: customControl.controlTitleCN + '不能为空！',
            validAlertEN: customControl.controlTitleEN + ' is required!',
            validRegex: ''
          }
          custValidators.push(notNullValidator)
          customControl.validators = custValidators
        }
        self.validatorInfos[customControl.controlName] = customControl
      }
      if (!resultFlag) {
        if (self.validatorInfos[customControl.controlName]) {
          self.validatorInfos[customControl.controlName] = null
        }
        if (self.isCustomControlSaveDataItemVoStart(customControl.controlName)) {
          self.customControlSaveDataItemVo[self.hanlderControlName(customControl.controlName)] = ''
          self.customControlSaveDataItemVo[self.hanlderControlName(customControl.controlName + 'Name')] = ''
          self.customControlSaveDataItemVo[self.hanlderControlName(customControl.controlName + 'ID')] = ''
        } else {
          self.tamasterOrderSubmitNeedDataVo[self.hanlderControlName(customControl.controlName)] = ''
          self.tamasterOrderSubmitNeedDataVo[self.hanlderControlName(customControl.controlName + 'Name')] = ''
          self.tamasterOrderSubmitNeedDataVo[self.hanlderControlName(customControl.controlName + 'ID')] = ''
        }
      }
      return resultFlag
    },
    isCustomControlSaveDataItemVoStart (controlName) {
      return OrderService.isCustomControlSaveDataItemVoStart(controlName)
    },
    hanlderControlName (controlName) {
      return OrderService.hanlderControlName(controlName)
    },
    changeSubmitInputVal (target, customControl) {
      const self = this
      if (self.isCustomControlSaveDataItemVoStart(customControl.controlName)) {
        self.customControlSaveDataItemVo[self.hanlderControlName(customControl.controlName)] = target.value
      } else {
        self.tamasterOrderSubmitNeedDataVo[self.hanlderControlName(customControl.controlName)] = target.value
      }
    },
    doSubmitOrder () {
      let self = this
      let validateResult = self.validateSubmitForm()
      if (validateResult) {
        $coreError(validateResult)
      } else {
        let submitParam = {
          syncFlag: true,
          submitTAData: {}
        }
        const orderDetail = self['Order/orderDetail']
        submitParam.submitTAData.taNo = self.currentTaNo
        submitParam.submitTAData.companyCode = self.companyCode
        submitParam.submitTAData.tamasterOrderSubmitNeedDataVo = Object.assign({}, self.tamasterOrderSubmitNeedDataVo)
        submitParam.submitTAData.customControlSaveDataItemVo = Object.assign({}, self.customControlSaveDataItemVo)
        submitParam.submitTAData.userId = orderDetail.polValue
        submitParam.submitTAData.approvalConfig = self.submitPage.approvalConfig
        submitParam.submitTAData.customControlSaveDataItemVo.selectTypes = self.currentApprMode
        submitParam.submitTAData.customControlSaveDataItemVo.selectItems = self.pickSelectItems()
        OrderService.submitOrder(submitParam).then(function (data) {
          if (data && data.success) {
            let resultData = data.resultData
            if (resultData.policyControlResult) {
              PolicyService.handlePolicyControl(resultData.policyControlResult, function (resultData, reasonCodes) {
                submitParam.policyApplied = true
                if (reasonCodes) {
                  submitParam.submitTAData.approvalConfig.reasonCode = reasonCodes.join(',')
                }
                OrderService.submitOrder(submitParam).then(function (data) {
                  if (data && data.success) {
                    self.$back({ force: true })
                  } else if (data) {
                    $coreError(data.message)
                  }
                })
              })
            } else {
              self.$back({ force: true })
            }
          } else if (data) {
            $coreError(data.message)
          }
        })
      }
    },
    pickSelectItems () {
      let self = this
      let result = ''
      let reg = new RegExp('ID$')
      for (let p in self.customControlSaveDataItemVo) {
        if (reg.test(p) && self.customControlSaveDataItemVo[p]) {
          result += ',' + self.customControlSaveDataItemVo[p]
        }
      }
      for (let p in self.tamasterOrderSubmitNeedDataVo) {
        if (reg.test(p) && self.tamasterOrderSubmitNeedDataVo[p]) {
          result += ',' + self.tamasterOrderSubmitNeedDataVo[p]
        }
      }
      return result ? result.substring(1) : result
    },
    showSubmitPageItem (customControl) {
      let self = this
      let showFlag = 2
      if (self.submitPage) {
        let singleApprHidden = self.submitPage.singleApprHidden
        let showHideCustomControl = self.submitPage.showHideCustomControl
        let customControlLevel = self.submitPage.customControlLevel
        let customControlLevelStepByStep = self.submitPage.customControlLevelStepByStep
        if (singleApprHidden) {
          let forAppr = customControl.forAppr
          if (forAppr === 'Y') {
            if (!self.dynamicApprFlag) {
              if (showHideCustomControl === 'Y') {
                if ((self.checkApprModeType(customControl.id) && self.currentApprMode === customControl.id) || !self.checkApprModeType(customControl.id)) {
                  if (customControlLevel && customControlLevelStepByStep) {
                    let superId = customControl.superId
                    if (superId && !self.hasSuperId(superId, customControl, customControlLevelStepByStep)) {
                      showFlag = 0
                    } else if (superId && self.hasSuperId(superId, customControl, customControlLevelStepByStep)) {
                      showFlag = 1
                    }
                  } else {
                    showFlag = 0
                  }
                } else {
                  showFlag = 0
                }
              }
            } else {
              if (self.checkApprModeType(customControl.id)) {
                if (self.currentApprMode === customControl.id) {
                  if (!customControl.superId || (customControl.superId && self.hasSuperId(customControl.superId, customControl, customControlLevelStepByStep))) {
                    showFlag = 1
                  } else {
                    showFlag = 0
                  }
                } else {
                  showFlag = 0
                }
              }
            }
          } else {
            if (customControlLevel && customControlLevelStepByStep) {
              let superId = customControl.superId
              if (superId && !self.hasSuperId(superId, customControl, customControlLevelStepByStep)) {
                showFlag = 0
              } else if (superId && self.hasSuperId(superId, customControl, customControlLevelStepByStep)) {
                showFlag = 1
              }
            }
          }
        }
        // eslint-disable-next-line
        if (showFlag !== 0 && customControl.hideCondition && eval(customControl.hideCondition)) {
          showFlag = 0
        }
      }
      return showFlag
    },
    showItemById (customControl) {
      let self = this
      let showHideCustomControl = self.submitPage.showHideCustomControl
      let showFlag = true
      if (showHideCustomControl) {
        if (/[YN]/.test(showHideCustomControl)) {
          if (showHideCustomControl === 'Y' && self.checkApprModeType(customControl.id)) {
            showFlag = customControl.id === self.currentApprMode
          }
        } else {
          let controlArr = showHideCustomControl.split(',')
          // eslint-disable-next-line
          loop:
          for (let control of controlArr) {
            if (control) {
              let hasEq = control.indexOf('=') >= 0
              let splitControls = control.split('=')
              let currentApprMode = hasEq ? splitControls[0] : '0'
              if (self.currentApprMode === currentApprMode) {
                let controlStr = hasEq ? splitControls[1] : splitControls[0]
                let controlIdArr = controlStr.split('|')
                for (let controlId of controlIdArr) {
                  if (controlId === customControl.id) {
                    showFlag = false
                    // eslint-disable-next-line
                    break loop
                  }
                }
              }
            }
          }
        }
      }
      return showFlag
    },
    checkApprModeType (controlId) {
      let self = this
      let resultFlag = false
      let apprLineControlList = self.submitPage.apprLineControlList
      if (apprLineControlList) {
        for (let apprLineControl of apprLineControlList) {
          if (apprLineControl.id === controlId) {
            resultFlag = true
            break
          }
        }
      }
      return resultFlag
    },
    hasSuperId (superId, customControl, customControlLevelStepByStep) {
      let self = this
      let currentId = customControl.id
      let superIdArr = self.superIds
      let resultFlag = self.hasSuperIdNoChange(superId, customControl, customControlLevelStepByStep)
      if (!resultFlag) {
        if (superIdArr) {
          let currentObj = superIdArr[currentId]
          if (currentObj && currentObj.superId) {
            superIdArr[currentId] = {}
            if (OrderService.isCustomControlSaveDataItemVoStart(customControl.controlName)) {
              self.customControlSaveDataItemVo[OrderService.hanlderControlName(customControl.controlName)] = ''
              self.customControlSaveDataItemVo[OrderService.hanlderControlName(customControl.controlName + 'Name')] = ''
              self.customControlSaveDataItemVo[OrderService.hanlderControlName(customControl.controlName + 'ID')] = ''
            } else {
              self.tamasterOrderSubmitNeedDataVo[OrderService.hanlderControlName(customControl.controlName)] = ''
              self.tamasterOrderSubmitNeedDataVo[OrderService.hanlderControlName(customControl.controlName + 'Name')] = ''
              self.tamasterOrderSubmitNeedDataVo[OrderService.hanlderControlName(customControl.controlName + 'ID')] = ''
            }
          }
        }
      }
      return resultFlag
    },
    hasSuperIdNoChange (superId, customControl, customControlLevelStepByStep) {
      let self = this
      let resultFlag = false
      let superIdArr = self.superIds
      if (superIdArr) {
        let superObj = superIdArr[superId]
        if (superObj) {
          let parentDataId = superObj.dataId
          if (customControlLevelStepByStep) {
            let currentDatas = self.companySelectDatas[customControl.id]
            // for (let p in currentDatas) {
            //   let currentData = currentDatas[p]
            //   if (parentDataId === currentData.superId) {
            //     resultFlag = true
            //     break
            //   }
            // }
            resultFlag = currentDatas && currentDatas[parentDataId]
          } else {
            resultFlag = customControl.superId === superId
          }
        }
      }
      return resultFlag
    },
    changeCurrentMode (controlId, oldMode) {
      let self = this
      if (controlId !== oldMode) {
        if (controlId === '0') {
          self.dynamicApprFlag = false
          self.approverList = self.submitPage.approverList
        } else {
          self.dynamicApprFlag = true
          self.approverList = []
        }
        self.currentApprMode = controlId
        self.customControlSaveDataItemVo['approveTypeChoose'] = 0
        let selectObj = self.superIds[controlId]
        if (selectObj) {
          let resultObj = self.removeDynamicValue(selectObj.dataId)
          let paramDelFlag = resultObj['delFlag']
          if (paramDelFlag) {
            delete self.superIds[controlId]
          }
        }
        self.$forceUpdate()
      }
    },
    removeDynamicValue (dataId) {
      let result = { 'delFlag': false }
      let tamasterOrderSubmitNeedDataVo = self.tamasterOrderSubmitNeedDataVo
      let customControlSaveDataItemVo = self.customControlSaveDataItemVo
      let paramName = ''
      let parentObj = null
      for (let p in tamasterOrderSubmitNeedDataVo) {
        if (tamasterOrderSubmitNeedDataVo[p] === dataId) {
          paramName = p
          parentObj = tamasterOrderSubmitNeedDataVo
          break
        }
      }
      if (!paramName) {
        for (let p in customControlSaveDataItemVo) {
          if (customControlSaveDataItemVo[p] === dataId) {
            paramName = p
            parentObj = customControlSaveDataItemVo
            break
          }
        }
      }
      if (paramName) {
        let paramNameOri = paramName.replace('ID', '')
        let paramNameOriName = paramNameOri + 'Name'
        delete parentObj[paramName]
        delete parentObj[paramNameOri]
        delete parentObj[paramNameOriName]
        result['delFlag'] = true
      }
      return result
    },
    processToSubmit (data) {
      let self = this
      self.submitPage = data.resultData.submitOrderPageData
      if (!self.submitPage.approvalConfig.defaultEnable &&
        self.submitPage.approvalConfig.dynamicEnable &&
        self.submitPage.apprLineControlList &&
        self.submitPage.apprLineControlList.length > 0) {
        let apprLine = self.submitPage.apprLineControlList[0]
        self.currentApprMode = apprLine.id
        self.approverList = []
        self.dynamicApprFlag = true
      } else {
        self.currentApprMode = '0'
        self.dynamicApprFlag = false
        self.approverList = self.submitPage.approverList
      }
      self.initFlag = true
    },
    validateSubmitForm () {
      let self = this
      let submitErrorMsg = ''
      let resultFlag = true
      let validatorInfos = self.validatorInfos
      if (validatorInfos) {
        let validatorArr = self.handlerValidatorsSort(validatorInfos)
        for (let customControl of validatorArr) {
          if (customControl) {
            let customValue = ''
            let controlName = customControl.controlName
            if (OrderService.isCustomControlSaveDataItemVoStart(controlName)) {
              customValue = self.customControlSaveDataItemVo[OrderService.hanlderControlName(controlName)]
            } else {
              customValue = self.tamasterOrderSubmitNeedDataVo[OrderService.hanlderControlName(controlName)]
            }
            if (customControl.controlRequired === '1' && !customValue) {
              submitErrorMsg = self.$i18nMsg(customControl.controlTitleCN, customControl.controlTitleEN) + self.$t('order.msg.submitNotnull')
              resultFlag = false
              break
            }
            let validators = customControl.validators
            if (validators) {
              for (let validator of validators) {
                if (self[validator.validFuncName]) {
                  customValue = customValue === undefined ? '' : customValue
                  resultFlag = self[validator.validFuncName](customValue, validator.validRegex)
                  if (!resultFlag) {
                    submitErrorMsg = self.$i18nMsg(validator.validAlertCN, validator.validAlertEN)
                    resultFlag = false
                    break
                  }
                } else {
                  if (console) {
                    console.debug(validator.validFuncName + '无此校验方法!')
                  }
                }
              }
              if (!resultFlag) {
                break
              }
            }
          }
        }
        if (resultFlag && self.submitPage.approvalConfig.apprETA !== 'N' && !self.externalApprovalFlag) {
          resultFlag = self.approverList && self.approverList.length !== 0
          if (!resultFlag) {
            let title = ''
            if (self.submitPage.approvalConfig.apprETA === 'Y') {
              title = self.$t('order.label.approvers')
            } else if (self.submitPage.approvalConfig.apprETA === 'R') {
              title = self.$t('order.label.copyto')
            }
            submitErrorMsg = title + self.$t('order.msg.submitNotnull')
          }
        }
      }
      return submitErrorMsg
    },
    handlerValidatorsSort (validatorInfos) {
      let validatorArr = []
      for (let p in validatorInfos) {
        validatorArr.push(validatorInfos[p])
      }
      validatorArr.sort((a, b) => {
        let as = a ? a.sequence : 0
        let bs = b ? b.sequence : 0
        return as - bs
      })
      return validatorArr
    },
    validateMaxLengthValue (value, regExp) {
      if (value) {
        value = value.replace(/[\r\n]/g, ' ')
      }
      try {
        regExp = trim(regExp)
        if (regExp === '') {
          return false
        } else {
          regExp = '0,' + regExp
        }
        return new RegExp('^.{' + regExp + '}$').test(value)
      } catch (e) {
        // 错误的正则表达式
        if (console) {
          console.debug(':validateMaxLengthValue表达式错误')
        }
      }
      return true
    },
    validateMinLengthValue (value, regExp) {
      if (value) {
        value = value.replace(/[\r\n]/g, ' ')
      }
      try {
        regExp = trim(regExp)
        if (regExp === '') {
          return false
        } else {
          regExp += ','
        }
        return new RegExp('^.{' + regExp + '}$').test(value)
      } catch (e) {
      // 错误的正则表达式
        if (console) {
          console.debug(':validateMinLengthValue表达式错误')
        }
      }
      return true
    },
    validateLengthValue (value, regExp) {
      if (value) {
        value = value.replace(/[\r\n]/g, ' ')
      }
      try {
        regExp = trim(regExp)
        if (regExp === '') {
          return false
        } else if (regExp.indexOf(',') === -1) {
          regExp += ','
        }
        return new RegExp('^.{' + regExp + '}$').test(value)
      } catch (e) {
      // 错误的正则表达式
        if (console) {
          console.debug(':validateLengthValue表达式错误')
        }
      }
      return true
    },
    validateNullValue (value) {
      return !!value
    },
    validateRegExpValue (value, regExp) {
      if (value) {
        value = value.replace(/[\r\n]/g, ' ')
      }
      try {
        if (regExp.indexOf('/') === 0) {
          // eslint-disable-next-line
          regExp = eval(regExp)
        }
        return new RegExp(regExp).test(value)
      } catch (e) {
      // 错误的正则表达式
        if (console) {
          console.debug(':正则表达式错误')
        }
      }
      return false
    },
    changeData (data) {
      let self = this
      self.companyControlDatas = data.companyControlDatas
      self.companySelectDatas = data.companySelectDatas
      self.tamasterOrderSubmitNeedDataVo = data.tamasterOrderSubmitNeedDataVo
      self.customControlSaveDataItemVo = data.customControlSaveDataItemVo
      self.approverBuffer = data.approverBuffer
      self.approverList = data.approverList
      self.superIds = data.superIds
      self.$forceUpdate()
    }
  },
  components: {
    submitSelect
  },
  computed: {
    ...mapGetters(['Order/orderDetail', 'Order/submitPageData'])
  },
  mounted () {
    let self = this
    const orderDetail = this['Order/orderDetail']
    if (orderDetail && orderDetail.companyCode) {
      self.companyCode = orderDetail.companyCode
      const submitPageData = this['Order/submitPageData']
      self.processToSubmit(submitPageData)
    } else {
      self.$back()
    }
  }
}
</script>

<style scoped>

</style>
