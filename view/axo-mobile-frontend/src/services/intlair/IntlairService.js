import Vue from 'vue'
import cloneDeep from 'lodash/cloneDeep'
import { isArray } from 'lodash/lang'
import store from '../../store'
import { defaultPageSetting, getCallerMethod } from '../caller/ServiceCaller'
import AirConsts from '../../consts/AirConsts'
import MasterDataService from '../../services/master/MasterDataService'
import OrderConsts from '../../consts/OrderConsts'

const SERVICE_PATH = 'product/air'
const INTLAIR_SRARCH_PARAM = 'com.citsamex.app.spi.data.caller.request.air.international.IntlAirSearchParam'
const INTLAIR_BOOK_PARAM = 'com.citsamex.app.spi.data.caller.request.air.international.IntlAirBookingParam'

function getSearchParam () {
  const globalConfig = store.getters.globalConfig
  const policyUserId = store.getters['Order/basicSearchParam'].policyUserId
  const DEFAULT_SEARCH_PARAM = {
    policyUserId: policyUserId,
    companyCode: globalConfig.currentLoginUser.companyCode,
    language: globalConfig.currentLocale,
    policyApplied: false,
    intlAirSearchDto: {
      owFromCity: '',
      owToCity: '',
      owFromDate: '',
      owFromTime: '',
      owSeatType: '',
      owNoStopFlag: '',
      rtFromCity: '',
      rtToCity: '',
      rtFromDate: '',
      rtReturnDate: '',
      rtFromTime: '',
      rtToTime: '',
      rtSeatType: '',
      rtNoStopFlag: '',
      interFlightType: 'OW'
    },
    selectOption: null
  }
  return cloneDeep(DEFAULT_SEARCH_PARAM)
}

function getBookParam () {
  const basicSearchParam = store.getters[OrderConsts.BASIC_SEARCH_PARAM_KEY]
  const globalConfig = store.getters.globalConfig
  const DEFAULT_BOOK_PARAM = {
    policyUserId: globalConfig.currentLoginUser.userId,
    companyCode: globalConfig.currentLoginUser.companyCode,
    userId: globalConfig.currentLoginUser.userId,
    language: globalConfig.currentLocale,
    optionFlag: 'Y',
    packageType: 'NORMAL',
    intlAirBookingDto: {
      taNo: basicSearchParam.taNo,
      externalOrderNo: basicSearchParam.externalOrderNo,
      bookOption: {},
      llfOption: {},
      travelers: []
    }
  }
  return cloneDeep(DEFAULT_BOOK_PARAM)
}

function getIntlAirCitys (param, config) {
  param.cityType = 'I'
  return MasterDataService.searchAirCityList(param, config)
}

function getIntlAirTabs (selectShowTab) {
  const tabs = selectShowTab.split('|')
  return cloneDeep(tabs.map(id => {
    return {
      id: id,
      label: Vue.$i18nBundle(AirConsts.FLIGHT_TAB_MAP[id]),
      selected: false
    }
  }))
}

function getIntlAirCabinTypes (cabinTypeStr) {
  const types = isArray(cabinTypeStr) ? cabinTypeStr : cabinTypeStr.split('|')
  return cloneDeep(types.map(id => {
    return { id, label: Vue.$i18nBundle(AirConsts.CABIN_TYPE_MAP[id]), selected: false }
  }))
}

function getIntlAirAlliance (alliance) {
  return Vue.$i18nBundle(AirConsts.FLIGHT_ALLIANCE_MAP[alliance])
}

export default {
  getSearchParam,
  getBookParam,
  searchIntlAir: getCallerMethod(SERVICE_PATH, 'searchInterFlightFormat', INTLAIR_SRARCH_PARAM),
  checkIntlAirSearchPolicys: getCallerMethod(SERVICE_PATH, 'checkIntlAirSearchPolicys', INTLAIR_SRARCH_PARAM),
  doSearchIntlAirAllCabin: getCallerMethod(SERVICE_PATH, 'searchInterFlightAllCabin', INTLAIR_SRARCH_PARAM),
  bookInterFlight: getCallerMethod(SERVICE_PATH, 'bookInterFlight', INTLAIR_BOOK_PARAM),
  checkIntlAirBookPolicys: getCallerMethod(SERVICE_PATH, 'checkIntlAirBookPolicys', INTLAIR_BOOK_PARAM),
  getInterRuleData: getCallerMethod(SERVICE_PATH, 'getInterRuleData', INTLAIR_SRARCH_PARAM),
  checkIntlAV: getCallerMethod(SERVICE_PATH, 'checkIntlAV', INTLAIR_SRARCH_PARAM),
  checkOptionPricing: getCallerMethod(SERVICE_PATH, 'checkOptionPricing', INTLAIR_SRARCH_PARAM),
  getIntlAirCitys,
  getIntlAirTabs,
  getIntlAirCabinTypes,
  getIntlAirAlliance
}
