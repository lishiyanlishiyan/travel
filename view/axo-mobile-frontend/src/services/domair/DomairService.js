import Vue from 'vue'
import cloneDeep from 'lodash/cloneDeep'
import { isArray } from 'lodash/lang'
import store from '../../store'
import { defaultPageSetting, getCallerMethod } from '../caller/ServiceCaller'
import AirConsts from '../../consts/AirConsts'

const SERVICE_PATH = 'product/air'
const SEARCH_DOM_FLIGHT_PARAM = 'com.citsamex.app.spi.data.caller.request.air.domestic.SearchDomFlightParam'
const BOOK_DOM_FLIGHT_PARAM = 'com.citsamex.app.spi.data.caller.request.air.domestic.DomAirBookingParam'
const DOM_AIR_CITY_PARAM = 'com.citsamex.app.spi.data.caller.request.master.air.DomAirCityParam'
const DOM_AIR_CABIN_LIMIT_PARAM = 'com.citsamex.app.spi.data.caller.request.master.air.GetDomairCabinLimitParam'
const DOM_AIR_CABIN_LIMIT_BOAO_PARAM = 'com.citsamex.app.spi.data.caller.request.master.air.GetDomairCabinLimitByBoaoParam'

const basicSearchParamKey = 'Order/basicSearchParam'

function getSearchParam () {
  const globalConfig = store.getters.globalConfig
  const basicSearchParam = store.getters[basicSearchParamKey]
  const DEFAULT_SEARCH_PARAM = {
    policyUserId: basicSearchParam.policyUserId,
    companyCode: globalConfig.currentLoginUser.companyCode,
    lang: globalConfig.currentLocale,
    transitFlag: '',
    airType: 'OW',
    cabinType: 'A',
    policyApplied: false,
    searchDomFlight: {
      arrivalAirportCode: '',
      departAirportCode: '',
      deptDate: '',
      deptTime: '',
      rtFlag: false,
      returnDate: '',
      returnTime: ''
    }
  }
  return cloneDeep(DEFAULT_SEARCH_PARAM)
}

function getBookParam () {
  const globalConfig = store.getters.globalConfig
  const basicSearchParam = store.getters[basicSearchParamKey]
  const DEFAULT_BOOK_PARAM = {
    policyUserId: basicSearchParam.policyUserId,
    companyCode: globalConfig.currentLoginUser.companyCode,
    userId: globalConfig.currentLoginUser.userId,
    domAirBooking: {
      lang: globalConfig.currentLocale,
      selectedFlights: [],
      travelers: [],
      taNo: basicSearchParam.taNo,
      externalOrderNo: basicSearchParam.externalOrderNo
    }
  }
  return cloneDeep(DEFAULT_BOOK_PARAM)
}

function getDomAirCities (param, config) {
  const getDomAirCities = getCallerMethod(SERVICE_PATH, 'getDomAirCities', DOM_AIR_CITY_PARAM)
  return new Promise((resolve, reject) => {
    getDomAirCities(Object.assign({ pageSetting: defaultPageSetting() }, param), config).then((result) => {
      if (result.success && result.resultData && result.resultData.airports) {
        resolve(result.resultData.airports)
      } else {
        reject(result)
      }
    }, reject)
  })
}

function getDomAirTabs (selectShowTab) {
  const tabs = selectShowTab.split('|')
  return cloneDeep(tabs.map(id => {
    return {
      id: id,
      label: Vue.$i18nBundle(AirConsts.FLIGHT_TAB_MAP[id]),
      flightKey: AirConsts.FLIGHT_KEY_MAP[id],
      selected: false
    }
  }))
}

function getDomAirCabinTypes (cabinTypeStr) {
  const types = isArray(cabinTypeStr) ? cabinTypeStr : cabinTypeStr.split('|')
  return cloneDeep(types.map(id => {
    return { id, label: Vue.$i18nBundle(AirConsts.CABIN_TYPE_MAP[id]), selected: false }
  }))
}

export default {
  getSearchParam,
  getBookParam,
  searchDomFlight: getCallerMethod(SERVICE_PATH, 'searchDomFlight', SEARCH_DOM_FLIGHT_PARAM),
  checkDomAirSearchPolicys: getCallerMethod(SERVICE_PATH, 'checkDomAirSearchPolicys', SEARCH_DOM_FLIGHT_PARAM),
  bookDomFlight: getCallerMethod(SERVICE_PATH, 'bookDomFlight', BOOK_DOM_FLIGHT_PARAM),
  checkDomAirBookPolicys: getCallerMethod(SERVICE_PATH, 'checkDomAirBookPolicys', BOOK_DOM_FLIGHT_PARAM),
  getCabinLimit: getCallerMethod(SERVICE_PATH, 'ajaxGetDomairCabinLimit', DOM_AIR_CABIN_LIMIT_PARAM),
  getCabinLimitByBoao: getCallerMethod(SERVICE_PATH, 'ajaxGetDomairCabinLimitByBoao', DOM_AIR_CABIN_LIMIT_BOAO_PARAM),
  getDomAirCities,
  getDomAirTabs,
  getDomAirCabinTypes
}
