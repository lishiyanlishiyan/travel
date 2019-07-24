import store from '../../store'
import { defaultPageSetting, baseTaNoCompanyParam, getCallerMethod } from '../caller/ServiceCaller'

const SERVICE_PATH = 'ta/product'
const AIR_SERVICE_PATH = 'product/air'
const GET_TA_LIST_PARAM = 'com.citsamex.app.spi.data.caller.request.order.GetTAListParam'
const GET_APPROVE_TA_LIST_PARAM = 'com.citsamex.app.spi.data.caller.request.order.GetTAListParam'
const SUBMIT_PAGE_CONTENT_PARAM = 'com.citsamex.app.spi.data.caller.request.order.submit.SubmitPageContentParam'
const SELECT_SERVICE_PATH = 'admin/select'
const GET_SELECT_DATAS_PARAM = 'com.citsamex.app.spi.data.caller.request.admin.GetSelectDatasParam'
const GET_SELECT_DATA_DETAILS_PARAM = 'com.citsamex.app.spi.data.caller.request.admin.GetSelectDataDetailParam'
const SUBMIT_ORDER_PARAM = 'com.citsamex.app.spi.data.caller.request.order.submit.SubmitTAParam'
const APPROVE_ORDER_PARAM = 'com.citsamex.app.spi.data.caller.request.order.approval.TAApprovalParam'
const CANCEL_ORDER_PARAM = 'com.citsamex.app.spi.data.caller.request.order.CancelTAParam'
const GET_INTLAIR_OPTION_PARAM = 'com.citsamex.app.spi.data.caller.request.air.international.GetIntlAirOptionParam'
const SELECT_OPTION_PARAM = 'com.citsamex.app.spi.data.caller.request.air.international.SelectIntlAirOptionParam'
const CANCEL_INTL_ORDER_PARAM = 'com.citsamex.app.spi.data.caller.request.air.international.CancelIntlAirOrderParam'
const CANCEL_DOMAIR_ORDER_PARAM = 'com.citsamex.app.spi.data.caller.request.air.domestic.InternalCancelDomSegmentParam'
let getCompanySelectDatas = getCallerMethod(SELECT_SERVICE_PATH, 'getSelectDatas', GET_SELECT_DATAS_PARAM)
let getCompanySelectDataDetails = getCallerMethod(SELECT_SERVICE_PATH, 'getSelectDataDetail', GET_SELECT_DATA_DETAILS_PARAM)
let getCompanySelectDataCount = getCallerMethod(SELECT_SERVICE_PATH, 'getSelectDataCounts', GET_SELECT_DATAS_PARAM)
function getSubmitDatas (param, config) {
  const currentLoginUser = store.getters.currentLoginUser || {}
  let queryParam = Object.assign({}, { pageSetting: defaultPageSetting(10) }, param, { companyCode: currentLoginUser.companyCode, activeFlag: 1, delFlag: 0 })
  return new Promise((resolve, reject) => {
    getCompanySelectDatas(queryParam, config).then(function (data) {
      if (data && data.success) {
        resolve(data.resultData.selectDatas)
      } else {
        reject(data)
      }
    }, reject)
  })
};
function getSubmitDataDetails (param, config) {
  const currentLoginUser = store.getters.currentLoginUser || {}
  let queryParam = Object.assign({}, { pageSetting: defaultPageSetting(10) }, param, { companyCode: currentLoginUser.companyCode })
  return new Promise((resolve, reject) => {
    getCompanySelectDataDetails(queryParam).then(function (data) {
      if (data && data.success) {
        resolve(data.resultData.selectData)
      } else {
        reject(data)
      }
    }, reject)
  })
};
function getSubmitDateCount (param, config) {
  const currentLoginUser = store.getters.currentLoginUser || {}
  let queryParam = Object.assign({}, param, { companyCode: currentLoginUser.companyCode, activeFlag: 1, delFlag: 0 })
  return new Promise((resolve, reject) => {
    getCompanySelectDataCount(queryParam).then(function (data) {
      if (data && data.success) {
        resolve(data.resultData.selectDatas)
      } else {
        reject(data)
      }
    }, reject)
  })
};
function isCustomControlSaveDataItemVoStart (controlName) {
  return controlName && controlName.indexOf('customControlSaveDataItemVo') >= 0
};
function hanlderControlName (controlName) {
  return controlName ? controlName.substring(controlName.lastIndexOf('.') + 1) : ''
};
function getCabinOptions (selectCabinStr, self) {
  let options = []
  if (selectCabinStr) {
    if (selectCabinStr.indexOf('A') >= 0) {
      options.push(createCabinOption('', self.$t('air.label.cabinA')))
    }
    if (selectCabinStr.indexOf('Y') >= 0) {
      options.push(createCabinOption('Economy', self.$t('air.label.cabinY')))
    }
    if (selectCabinStr.indexOf('C') >= 0) {
      options.push(createCabinOption('Business', self.$t('air.label.cabinC')))
    }
    if (selectCabinStr.indexOf('F') >= 0) {
      options.push(createCabinOption('First', self.$t('air.label.cabinF')))
    }
  }
  return options
};
function showOptionRuleInfo (option, self) {
  let result = ''
  if (option && option.airlineList) {
    let index = 0
    for (let airline of option.airlineList) {
      if (airline && airline.intlAirRuleDto) {
        let title = self.$t('air.label.segment') + (index + 1)
        let temp = self.$i18nMsg(airline.intlAirRuleDto.ruleCN, airline.intlAirRuleDto.ruleEN)
        if (temp.indexOf(title) < 0) {
          result += self.$t('air.label.segment') + (index + 1) + ':<br />'
        }
        let reg1 = new RegExp('\\n', 'gm')
        result += temp.replace(reg1, '<br/>')
        let lastFlag = index === option.airlineList.length - 1
        if (!lastFlag) {
          result += '<br />'
        }
      }
      index++
    }
  }
  return result
};
function createCabinOption (value, title) {
  return {
    'value': value,
    'title': title
  }
};
function showIntlDuration (dateStr, type) {
  if (dateStr) {
    let dateArr = dateStr.split(':')
    if (type === 'H') {
      return dateArr[0]
    } else {
      return dateArr[1]
    }
  }
  return dateStr
};
function getPhsicalCabinName (physicalCabin, self) {
  let result = physicalCabin
  let cabinList = self.cabinList
  if (cabinList) {
    for (let cabinValue of cabinList) {
      if (physicalCabin === cabinValue.cabinCode.toUpperCase()) {
        result = self.$i18nMsg(cabinValue.cabinNameCn, cabinValue.cabinNameEn)
        break
      }
    }
  }
  return result
};
function showFlightDuration (duration) {
  let result = ''
  if (duration) {
    result = showIntlDuration(duration, 'H') + 'h' + showIntlDuration(duration, 'M') + 'm'
  }
  return result
};
function hasVisaInfo (option) {
  let result = false
  if (option && option.visaList) {
    for (let visaInfo of option.visaList) {
      if (visaInfo.visaRemark) {
        result = true
        break
      }
    }
  }
  return result
};
function showPackageVisaInfo (option, self) {
  let result = ''
  if (option && option.visaList) {
    result += '<pre>'
    let index = 0
    for (let visaInfo of option.visaList) {
      if (visaInfo.visaRemark) {
        if (visaInfo.visaType === '1') {
          result += self.$t('air.label.visaDestination') + ':<br />'
        } else {
          result += self.$t('air.label.visaTransit') + ':<br />'
        }
        result += visaInfo.visaRemark
        let lastFlag = index === option.visaList.length - 1
        if (!lastFlag) {
          result += '<br />'
        }
      }
      index++
    }
    result += '</pre>'
  }
  return result
};
function showOptionView (intlAir, order, qcMode) {
  if (qcMode) {
    return intlAir.emailFlag === 'Y' && order.statusCode !== '1' &&
      order.historyFlag !== 1 && !order.tcUserId
  }
  return intlAir.emailFlag === 'Y' && order.historyFlag !== 1 && !order.tcUserId
};
function getSelectedOption (intlAir) {
  if (intlAir.packageType === 'NORMAL' && intlAir.status === 3) {
    return intlAir
  }
  let optionList = intlAir.optionList
  if (optionList) {
    for (let option of optionList) {
      if (option.status === 3) {
        return option
      }
    }
  }
  return null
};
function initDomAirSegment (domAirList) {
  let domSegmentList = []
  if (domAirList && domAirList.length > 0) {
    for (let domAir of domAirList) {
      domSegmentList = domSegmentList.concat(domAir.domAirSegments)
    }
    domSegmentList = domSegmentList.sort((a, b) => new Date(a.deptDate).getTime() - new Date(b.deptDate).getTime())
  }
  return domSegmentList
};

function getSearchTaParam () {
  return {
    statusCode: '',
    pageSetting: defaultPageSetting(),
    adminFlag: false,
    taNo: '',
    travellerName: '',
    createDateFrom: '',
    createDateTo: '',
    issueNow: '',
    createByMode: ''
  }
}

export default {
  getTAList: getCallerMethod(SERVICE_PATH, 'getTAList', GET_TA_LIST_PARAM),
  getApproveTAList: getCallerMethod(SERVICE_PATH, 'getApproveTAList', GET_APPROVE_TA_LIST_PARAM),
  getTADetailForAuth: getCallerMethod(SERVICE_PATH, 'getTADetailForAuth', baseTaNoCompanyParam()),
  searchOrderTimeline: getCallerMethod(SERVICE_PATH, 'searchOrderTimeline', GET_APPROVE_TA_LIST_PARAM),
  toSubmitOrder: getCallerMethod(SERVICE_PATH, 'getSubmitPageContent', SUBMIT_PAGE_CONTENT_PARAM),
  submitOrder: getCallerMethod(SERVICE_PATH, 'submitTA', SUBMIT_ORDER_PARAM),
  internalGetTAContinueBook: getCallerMethod(SERVICE_PATH, 'internalGetTAContinueBook', baseTaNoCompanyParam()),
  internalGetTABasic: getCallerMethod(SERVICE_PATH, 'internalGetTABasic', baseTaNoCompanyParam()),
  getSubmitDatas,
  getSubmitDateCount,
  getSubmitDataDetails,
  hanlderControlName,
  isCustomControlSaveDataItemVoStart,
  approveTa: getCallerMethod(SERVICE_PATH, 'approveTA', APPROVE_ORDER_PARAM),
  rejectTa: getCallerMethod(SERVICE_PATH, 'rejectTA', APPROVE_ORDER_PARAM),
  cancelTa: getCallerMethod(SERVICE_PATH, 'cancelTA', CANCEL_ORDER_PARAM),
  getCabinOptions,
  getIntlOptions: getCallerMethod(AIR_SERVICE_PATH, 'searchOptionListInfo', GET_INTLAIR_OPTION_PARAM),
  submitSelectOption: getCallerMethod(AIR_SERVICE_PATH, 'selectInterOption', SELECT_OPTION_PARAM),
  cancelIntlOrder: getCallerMethod(AIR_SERVICE_PATH, 'cancelIntlAirOrder', CANCEL_INTL_ORDER_PARAM),
  eidtIntlAirOrder: getCallerMethod(AIR_SERVICE_PATH, 'searchTaOptionListInfo', GET_INTLAIR_OPTION_PARAM),
  showOptionRuleInfo,
  showIntlDuration,
  getPhsicalCabinName,
  showFlightDuration,
  showPackageVisaInfo,
  hasVisaInfo,
  showOptionView,
  getSelectedOption,
  initDomAirSegment,
  getSearchTaParam,
  cancelDomAirOrder: getCallerMethod(AIR_SERVICE_PATH, 'internalCancelFlightSegments', CANCEL_DOMAIR_ORDER_PARAM)
}
