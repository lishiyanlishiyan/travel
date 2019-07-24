import { baseTaNoCompanyParam, defaultPageSetting, getCallerMethod } from '../caller/ServiceCaller'
import store from '../../store'
import OrderConsts from '../../consts/OrderConsts'
import cloneDeep from 'lodash/cloneDeep'

const SERVICE_PATH = 'product/other'
const GET_OTHER_DEFAULT = 'com.citsamex.app.spi.data.caller.request.master.BaseCompanyParam'
const SAVE_OTHER_ORDER = 'com.citsamex.app.spi.data.caller.request.other.SaveOtherOrderParam'
const GET_OTHER_ORDERS = 'com.citsamex.app.spi.data.caller.request.order.BaseTaNoCompanyParam'
const DELETE_OTHER_ORDER = 'com.citsamex.app.spi.data.caller.request.other.DeleteOtherOrderParam'

function getOtherSearchParam () {
  const globalConfig = store.getters.globalConfig
  const basicSearchParam = store.getters[OrderConsts.BASIC_SEARCH_PARAM_KEY]

  const DEFAULT_SEARCH_PARAM = {
    companyCode: globalConfig.currentLoginUser.companyCode,
    userId: globalConfig.currentLoginUser.userId,
    policyApplied: false,
    policyUserId: basicSearchParam.policyUserId,
    taNo: basicSearchParam.taNo,
    externalOrderNo: basicSearchParam.externalOrderNo,
    newOthers: {
      userId: globalConfig.currentLoginUser.userId,
      companyCode: globalConfig.currentLoginUser.companyCode,
      comment: '',
      typeId: '',
      orderDate: '',
      City: '',
      toCity: '',
      deptCity: '',
      endDate: '',
      remark: '',
      cityCode: '',
      countryCode: '',
      price: '',
      deptNation: 'CN',
      needAmexService: 0,
      otherPsgrs: []
    }
  }
  return cloneDeep(DEFAULT_SEARCH_PARAM)
}

export default {
  getOtherDefaultSetup: getCallerMethod(SERVICE_PATH, 'getOtherDefaultSetup', GET_OTHER_DEFAULT),
  saveOtherOrder: getCallerMethod(SERVICE_PATH, 'saveOtherOrder', SAVE_OTHER_ORDER),
  getOtherOrders: getCallerMethod(SERVICE_PATH, 'getOtherOrders', GET_OTHER_ORDERS),
  deleteOtherOrder: getCallerMethod(SERVICE_PATH, 'deleteOtherOrder', DELETE_OTHER_ORDER),
  getOtherSearchParam
}
