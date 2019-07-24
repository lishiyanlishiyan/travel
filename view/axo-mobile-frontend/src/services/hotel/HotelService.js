import { baseKeyWordsParam, defaultPageSetting, getCallerMethod } from '../caller/ServiceCaller'
import moment from 'moment'
import store from '../../store'
import cloneDeep from 'lodash/cloneDeep'
import numeral from 'numeral'
import Vue from 'vue'
import HotelConsts from '../../consts/HotelConsts'
import { $checkItemIndex } from '../../utils/AxoUtils'
import OrderConsts from '../../consts/OrderConsts'

const SERVICE_PATH = 'product/hotel'
const GET_HOTEL_MASTERS_PARAM = 'com.citsamex.app.spi.data.caller.request.hotel.GetHotelMastersParam'
const SEARCH_ROOM_PRICES_PARAM = 'com.citsamex.app.spi.data.caller.request.hotel.SearchRoomPricesParam'
const ROOM_BOOKING_PARAM = 'com.citsamex.app.spi.data.caller.request.hotel.RoomBookingParam'
const HOTEL_CITY_BUDGET_PARAM = 'com.citsamex.app.spi.data.caller.request.hotel.HotelCityBudgetParam'
const GUARANTEE_INFO_PARAM = 'com.citsamex.app.spi.data.caller.request.hotel.GuaranteeInfoParam'
const HOTEL_ROOM_RATE_DESCRIPTION_PARAM = 'com.citsamex.app.spi.data.caller.request.hotel.HotelRoomRateDescriptionParam'
const ELONG_VALIDATE_CREDIT_CARD_PARAM = 'com.citsamex.app.spi.data.caller.request.hotel.ElongValidateCreditCardParam'
const LOCAITON_INPUT_TIPS_PARAM = 'com.citsamex.app.spi.data.caller.request.map.LocaitonInputTipsParam'
const GET_CITY_LOCATION_PARAM = 'com.citsamex.app.spi.data.caller.request.map.GetCityLocationParam'
const CANCEL_HOTEL_ORDER_PARAM = 'com.citsamex.app.spi.data.caller.request.hotel.CancelHotelOrderParam'

function getHotelSearchParam () {
  const globalConfig = store.getters.globalConfig
  const basicSearchParam = store.getters[OrderConsts.BASIC_SEARCH_PARAM_KEY]
  const travellerIds = (basicSearchParam.bookTravellers || []).map(traveller => {
    return traveller.passengerType === 'T' ? traveller.tempUserId : traveller.userId
  })
  const DEFAULT_SEARCH_PARAM = {
    policyUserId: basicSearchParam.policyUserId,
    companyCode: globalConfig.currentLoginUser.companyCode,
    policyApplied: false,
    taNo: basicSearchParam.taNo,
    externalOrderNo: basicSearchParam.externalOrderNo,
    hotelType: 'D',
    pageSetting: defaultPageSetting(),
    domCity: null,
    intlCity: null,
    today: false,
    travellerIds,
    hotelQueryParam: {
      companyCode: globalConfig.currentLoginUser.companyCode,
      hotelName: '',
      roomCount: 1,
      guestCount: 1,
      checkinDate: '',
      checkoutDate: '',
      cityCode: '',
      countryCode: '',
      corporateAlwaysFirst: true,
      onlyQianTaoHotelPrepay: false,
      location: null,
      distance: null
    }
  }
  return cloneDeep(DEFAULT_SEARCH_PARAM)
}

function getRoomPricesParam (searchParam) {
  return cloneDeep({
    policyUserId: searchParam.policyUserId,
    companyCode: searchParam.companyCode,
    policyApplied: false,
    roomQueryBasic: {
      checkInDate: moment(searchParam.hotelQueryParam.checkinDate).toDate(),
      checkOutDate: moment(searchParam.hotelQueryParam.checkoutDate).toDate(),
      cityCode: searchParam.hotelQueryParam.cityCode,
      ctryCode: searchParam.hotelQueryParam.countryCode,
      roomCount: searchParam.hotelQueryParam.roomCount,
      guestCount: searchParam.hotelQueryParam.guestCount,
      minRate: searchParam.hotelQueryParam.minPrice,
      maxRate: searchParam.hotelQueryParam.maxPrice,
      queryByCityBudget: !!searchParam.hotelQueryParam.queryByCityBudget,
      onlyQianTaoHotelPrepay: searchParam.hotelQueryParam.onlyQianTaoHotelPrepay,
      onlyHotelSelfPrepay: searchParam.hotelQueryParam.onlyHotelSelfPrepay
    }
  })
}

function getHotelRoomBookingParam (searchParam) {
  const currentLoginUser = store.getters.currentLoginUser || {}
  return cloneDeep({
    policyUserId: searchParam.policyUserId,
    userId: currentLoginUser.userId,
    taNo: searchParam.taNo || '',
    externalOrderNo: searchParam.externalOrderNo || '',
    companyCode: searchParam.companyCode,
    policyApplied: false,
    syncFlag: true,
    confirmDoubleBooking: false,
    roomBookingBasic: {
      checkInDate: moment(searchParam.hotelQueryParam.checkinDate).toDate(),
      checkOutDate: moment(searchParam.hotelQueryParam.checkoutDate).toDate(),
      cityCode: searchParam.hotelQueryParam.cityCode,
      ctryCode: searchParam.hotelQueryParam.countryCode,
      roomCount: 1,
      roomCountShow: searchParam.hotelQueryParam.roomCount,
      guestCount: searchParam.hotelQueryParam.guestCount,
      travellerRoomList: [],
      hotelName: ''
    }
  })
}

function getHotelTypes (searchRegion) {
  return cloneDeep(HotelConsts.HOTEL_TYPE_LIST.map((item, index) => {
    const result = {
      id: item.id,
      label: Vue.$i18nBundle(item.label),
      enabled: (index === 0 && searchRegion !== 2) || (index === 1 && searchRegion !== 1)
    }
    return result
  })).filter(item => {
    return item.enabled
  })
}

function getHotelTabs (tabConfig = {}, favoriteEnabled) {
  const result = []
  HotelConsts.HOTEL_TAB_LIST.forEach(item => {
    if (tabConfig[`${item.id}TabEnable`] || (favoriteEnabled && item.id === 'favorite')) {
      result.push({
        id: item.id,
        label: Vue.$i18nBundle(item.label),
        resultType: item.resultType,
        selected: false,
        hasMoreHotel: false,
        conditionChange: false,
        items: []
      })
    }
  })
  return result
}

function getHotelFilterItems (itemList) {
  const result = []
  itemList.forEach(item => {
    const _item = cloneDeep(item)
    _item.selected = false
    _item.label = Vue.$i18nBundle(item.label)
    result.push(_item)
  })
  return result
}

function getHotelPrices (cityBudget) {
  const priceItems = getHotelFilterItems(HotelConsts.HOTEL_PRICES_LIST)
  if (!cityBudget) {
    priceItems.pop()
  }
  return priceItems
}

function isNonGDS (interfaceIdOrKey) {
  if (!interfaceIdOrKey) {
    return true
  } else if (isNaN(interfaceIdOrKey)) { // key
    return /^ng.*/ig.test(interfaceIdOrKey)
  } else {
    return [3, 8, 19, 20, 22].indexOf(parseInt(interfaceIdOrKey)) < 0
  }
}

function formatBudget (budget) {
  return !budget || budget < 0 ? this.$i18nBundle('common.label.unlimited') : numeral(budget).format('0,0.00')
}

function calcCityPolicy (cityBudget, hotelDefaultSetup) {
  let cityPolicyStr = ''
  const $i18nBundle = Vue.$i18nBundle
  const $i18nMsg = Vue.$i18nMsg
  if (cityBudget || hotelDefaultSetup.hotelCityBudgetPriceDesc || hotelDefaultSetup.hotelCityPolicy) {
    if (cityBudget) {
      cityPolicyStr += `${$i18nBundle('hotel.label.cityPolicyTitle1')}(${cityBudget.currency}): `
      cityPolicyStr += cityBudget.budget === cityBudget.corpBudget ? `${formatBudget(cityBudget.budget)}`
        : `${$i18nBundle('common.label.nonCorp')}(${formatBudget(cityBudget.budget)}), ${$i18nBundle('common.label.corp')}(${formatBudget(cityBudget.corpBudget)})`
      cityPolicyStr += `(${$i18nMsg(hotelDefaultSetup.hotelCityBudgetPriceDescCN, hotelDefaultSetup.hotelCityBudgetPriceDescEN) || $i18nBundle('hotel.label.cityPolicyTitle2')})<br>`
    }
    if (hotelDefaultSetup.hotelCityPolicy) {
      cityPolicyStr += `${$i18nMsg(hotelDefaultSetup.hotelCityPolicyCN, hotelDefaultSetup.hotelCityPolicyEN)}`
    }
  }
  return cityPolicyStr
}

function arrivalParser (config, late) {
  const timeOptions = cloneDeep(HotelConsts.HOTEL_TIME_OPTIONS)
  let _config = config
  if (_config.arrivalEarlyTimeStr && _config.arrivalLateTimeStr) {
    let earlyIdx = $checkItemIndex(timeOptions, _config.arrivalEarlyTimeStr)
    let lateIdx = $checkItemIndex(timeOptions, _config.arrivalLateTimeStr)
    if (_config.today) { // 当天的最晚到店和最早到店时间需要特殊处理
      const current = new Date().getHours()
      if (earlyIdx <= current) {
        earlyIdx = current + 1
      }
      if (lateIdx <= earlyIdx) {
        lateIdx = earlyIdx === timeOptions.length - 1 ? earlyIdx : (earlyIdx + 1)
      }
    }
    if (earlyIdx >= lateIdx) {
      !late && (lateIdx = earlyIdx === timeOptions.length - 1 ? earlyIdx : earlyIdx + 1)
      earlyIdx = lateIdx - 1
    }
    _config.arrivalEarlyTimeStr = timeOptions[earlyIdx]
    _config.arrivalLateTimeStr = timeOptions[lateIdx]
    console.info(_config.arrivalEarlyTimeStr, _config.arrivalLateTimeStr)
  }
}

function getSortConditions (type, recommendEnable) {
  return cloneDeep([{
    id: 'bookingTimes desc',
    label: Vue.$i18nBundle('hotel.label.defaultSort'),
    enabled: type === '3'
  }, {
    id: 'recommendLevel desc',
    label: Vue.$i18nBundle('hotel.label.recommendationSort'),
    enabled: type === '1' && recommendEnable
  }, {
    id: 'price asc',
    label: Vue.$i18nBundle('hotel.label.priceAsc'),
    enabled: true
  }, {
    id: 'price desc',
    label: Vue.$i18nBundle('hotel.label.priceDesc'),
    enabled: true
  }, {
    id: 'stars asc',
    label: Vue.$i18nBundle('hotel.label.starsAsc'),
    enabled: true
  }, {
    id: 'stars desc',
    label: Vue.$i18nBundle('hotel.label.starsDesc'),
    enabled: true
  }, {
    id: 'distance asc',
    label: Vue.$i18nBundle('hotel.label.distanceAsc'),
    enabled: false
  }, {
    id: 'distance desc',
    label: Vue.$i18nBundle('hotel.label.distanceDesc'),
    enabled: false
  }])
}

function isQiantaoPrepay (hotel, hotelDefaultSetup) {
  return ((hotel.qiantaoCode || ((hotel.abacusCode || hotel.huazhuCode) && hotel.corporate)) &&
    hotelDefaultSetup.qiantaoHotelPrepayEnable) && hotel.ctryCode === 'CN'
}

/**
 * 判断当前时间是每天的最后一个小时以内
 * @param checkInDate
 * @returns {boolean}
 */
function checkDayAndHour (checkInDate, start = '23:00:00', end = '23:59:59') {
  const newTime = moment()
  const today = newTime.format('YYYY-MM-DD')
  return moment(checkInDate).isSame(newTime, 'd') &&
    newTime.isAfter(moment(`${today} ${start}`)) && newTime.isBefore(moment(`${today} ${end}`))
}

function isBehindTheTimes (checkInDate, cpOption, cpNumeric) {
  let cpFlag = false
  if (cpOption && cpNumeric) {
    let cpTime = moment(checkInDate)
    if (cpOption === 'P') { // 12PM特殊处理
      cpTime = cpTime.set('hour', parseInt(cpNumeric === 12 ? 0 : cpNumeric) + 12)
    } else if (cpOption === 'H') {
      cpTime = cpTime.add(12 - parseInt(cpNumeric), 'h')
    } else if (cpOption === 'N') {
      cpTime = moment()
    } else if (cpOption === 'D') {
      cpTime = cpTime.subtract(parseInt(cpNumeric), 'd')
      cpTime = cpTime.set('hour', 12)
    } else if (cpOption === 'M') {
      cpTime = cpTime.subtract(parseInt(cpNumeric), 'M')
      cpTime = cpTime.set('hour', 12)
    } else {
      cpTime = cpTime.set('hour', 12)
    }
    if (cpTime.isBefore(moment())) {
      cpFlag = true
    }
  }
  return cpFlag
}

export default {
  getHotelMasterList: getCallerMethod(SERVICE_PATH, 'getHotelMasterList', GET_HOTEL_MASTERS_PARAM),
  checkHotelSearchPolicies: getCallerMethod(SERVICE_PATH, 'checkHotelSearchPolicies', GET_HOTEL_MASTERS_PARAM),
  searchRoomPrices: getCallerMethod(SERVICE_PATH, 'searchRoomPrices', SEARCH_ROOM_PRICES_PARAM),
  bookGDSRoom: getCallerMethod(SERVICE_PATH, 'bookGDSRoom', ROOM_BOOKING_PARAM),
  bookHotelRequest: getCallerMethod(SERVICE_PATH, 'bookHotelRequest', ROOM_BOOKING_PARAM),
  loadHotelCityBudget: getCallerMethod(SERVICE_PATH, 'loadHotelCityBudget', HOTEL_CITY_BUDGET_PARAM),
  validateGuaranteeInfo: getCallerMethod(SERVICE_PATH, 'validateGuaranteeInfo', GUARANTEE_INFO_PARAM),
  showHotelRoomDetail: getCallerMethod(SERVICE_PATH, 'showHotelRoomDetail', HOTEL_ROOM_RATE_DESCRIPTION_PARAM),
  elongValidateCreditCard: getCallerMethod(SERVICE_PATH, 'elongValidateCreditCard', ELONG_VALIDATE_CREDIT_CARD_PARAM),
  getExchangeRates: getCallerMethod(SERVICE_PATH, 'getExchangeRates', baseKeyWordsParam()),
  locationInputTips: getCallerMethod(SERVICE_PATH, 'locationInputTips', LOCAITON_INPUT_TIPS_PARAM),
  getCityLocation: getCallerMethod(SERVICE_PATH, 'getCityLocation', GET_CITY_LOCATION_PARAM),
  cancelHotelOrder: getCallerMethod(SERVICE_PATH, 'cancelHotelOrder', CANCEL_HOTEL_ORDER_PARAM),
  getHotelTypes,
  getHotelTabs,
  getHotelStars: () => {
    return getHotelFilterItems(HotelConsts.HOTEL_STARS_LIST)
  },
  getHotelPrices,
  getHotelSearchParam,
  getRoomPricesParam,
  getHotelRoomBookingParam,
  isNonGDS,
  formatBudget,
  calcCityPolicy,
  arrivalParser,
  getSortConditions,
  isQiantaoPrepay,
  checkDayAndHour,
  isBehindTheTimes
}
