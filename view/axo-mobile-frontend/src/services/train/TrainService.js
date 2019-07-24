import Vue from 'vue'
import cloneDeep from 'lodash/cloneDeep'
import { isArray } from 'lodash/lang'
import store from '../../store'
import { defaultPageSetting, getCallerMethod } from '../caller/ServiceCaller'
import TrainConsts from '../../consts/TrainConsts'

const SERVICE_PATH = 'product/train'
const SEARCH_TRAIN_SCHEDULE_PARAM = 'com.citsamex.app.spi.data.caller.request.train.SearchTrainScheduleParam'
const GET_TRAIN_NUMBER_INFO_PARAM = 'com.citsamex.app.spi.data.caller.request.train.GetTrainNumberInfoParam'
const BOOK_TRAIN_SEAT_POLICY_PARAM = 'com.citsamex.app.spi.data.caller.request.train.BookTrainSeatPolicyParam'
const BOOK_TRAIN_SEAT_PARAM = 'com.citsamex.app.spi.data.caller.request.train.BookTrainSeatParam'
const GET_TRAIN_STATION_PARAM = 'com.citsamex.app.spi.data.caller.request.train.GetTrainStationParam'

const basicSearchParamKey = 'Order/basicSearchParam'

function getSearchParam () {
  const globalConfig = store.getters.globalConfig
  const basicSearchParam = store.getters[basicSearchParamKey]
  const DEFAULT_SEARCH_PARAM = {
    policyUserId: basicSearchParam.policyUserId,
    companyCode: globalConfig.currentLoginUser.companyCode,
    lang: globalConfig.currentLocale,
    policyApplied: false,
    trainRouteType: 'O',
    searchParam: {
      tano: basicSearchParam.taNo,
      departStationCode: '',
      arrivalStationCode: '',
      departDate: ''
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

function getTrainStation (param, config) {
  const getTrainStation = getCallerMethod(SERVICE_PATH, 'getTrainStation', GET_TRAIN_STATION_PARAM)
  return new Promise((resolve, reject) => {
    getTrainStation(Object.assign({ pageSetting: defaultPageSetting() }, param), config).then((result) => {
      if (result.success && result.resultData && result.resultData.trainStations) {
        resolve(result.resultData.trainStations)
      } else {
        reject(result)
      }
    }, reject)
  })
}

function getTrainTypes () {
  const types = ['G/C', 'D', 'Z', 'T', 'K', 'O']
  return cloneDeep(types.map(id => {
    return { id, label: Vue.$i18nBundle(TrainConsts.TRAIN_TYPE_MAP[id]), selected: false }
  }))
}

function getTrainStationTypes () {
  const types = ['1', '3', '4', '2']
  return cloneDeep(types.map(id => {
    return { id, label: Vue.$i18nBundle(TrainConsts.TRAIN_STATION_TYPE_MAP[id]), selected: false }
  }))
}

function getTrainSeatType (id) {
  return TrainConsts.TRAIN_SEAT_TYPE_MAP[id] || ''
}

export default {
  getSearchParam,
  getBookParam,
  checkTrainSearchPolicys: getCallerMethod(SERVICE_PATH, 'checkTrainSearchPolicys', SEARCH_TRAIN_SCHEDULE_PARAM),
  searchTrainSchedule: getCallerMethod(SERVICE_PATH, 'searchTrainSchedule', SEARCH_TRAIN_SCHEDULE_PARAM),
  getTrainNumberInfo: getCallerMethod(SERVICE_PATH, 'getTrainNumberInfo', GET_TRAIN_NUMBER_INFO_PARAM),
  bookTrainSeatPolicy: getCallerMethod(SERVICE_PATH, 'bookTrainSeatPolicy', BOOK_TRAIN_SEAT_POLICY_PARAM),
  bookTrainSeat: getCallerMethod(SERVICE_PATH, 'bookTrainSeat', BOOK_TRAIN_SEAT_PARAM),
  getTrainStation,
  getTrainTypes,
  getTrainStationTypes,
  getTrainSeatType
}
