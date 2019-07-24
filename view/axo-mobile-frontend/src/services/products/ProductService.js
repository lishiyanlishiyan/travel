import store from '../../store'
import forEach from 'lodash/forEach'
import find from 'lodash/find'
import cloneDeep from 'lodash/cloneDeep'
import merge from 'lodash/merge'
import Vue from 'vue'
import { defaultPageSetting } from '../caller/ServiceCaller'
import ProfileService from '../profile/ProfileService'
import OrderConsts, { Products, ProductType } from '../../consts/OrderConsts'
import ProfileConsts from '../../consts/ProfileConsts'
import { $checkItemIndex, $coreAlert } from '../../utils/AxoUtils'
import BillingTypeProductService from './BillingTypeProductService'

const TRAVELLER_ATTR_MAP = cloneDeep(ProfileConsts.TRAVELLER_ATTR_MAP)
const USER_BASIC_TRAVELLER_MAPPING = cloneDeep(ProfileConsts.USER_BASIC_TRAVELLER_MAPPING)

function getTravellerByUserBasic (userBasic, creator) {
  const traveller = {}
  forEach(USER_BASIC_TRAVELLER_MAPPING, function (vKey, key) {
    traveller[key] = userBasic[vKey]
  })
  if (creator && userBasic.userId.match(/^T.*/)) { // 处理临时差旅人包装成的UserBasicDto
    traveller.tempUserId = traveller.userId
    traveller.creator = creator
    traveller.userId = creator.userId
    traveller.passengerType = 'T'
  }
  return traveller
}

function searchAutocompleteTravellers (param, config) {
  const currentLoginUser = store.getters.currentLoginUser || {}
  return new Promise((resolve, reject) => {
    ProfileService.getUnionTravellers(Object.assign({ pageSetting: defaultPageSetting() }, param, {
      companyCode: currentLoginUser.companyCode,
      userId: currentLoginUser.userId
    }), config).then((result) => {
      console.info(result)
      if (result.success && result.resultData && result.resultData.travellers) {
        result.resultData.travellers.forEach(traveller => {
          traveller.uuid = [traveller.userId, traveller.tempUserId || ''].join('/')
        })
        resolve(result.resultData.travellers)
      } else {
        reject(result)
      }
    }, reject)
  })
}

function validateTraveller (travellerId, valType, config) {
  const currentLoginUser = store.getters.currentLoginUser || {}
  return new Promise((resolve, reject) => {
    ProfileService.validateUserBeforeOperation(Object.assign({}, {
      userId: travellerId,
      companyCode: currentLoginUser.companyCode,
      valType
    }, {
      companyCode: currentLoginUser.companyCode,
      userId: currentLoginUser.userId
    }), config).then((result) => {
      if (result.success && result.resultData) {
        resolve(result.resultData)
      } else {
        reject(result.resultData)
      }
    }, reject)
  })
}

function loadTravellersDetails (travellers, config) {
  const travellerIds = (travellers || []).map(traveller => {
    return traveller.passengerType === 'T' ? traveller.tempUserId : traveller.userId
  })
  return loadTravellers(travellerIds, config)
}

function loadTravellers (travellerIds, config) {
  return new Promise((resolve, reject) => {
    ProfileService.loadTravellersById({ travellerIds }, config).then(result => {
      if (result && result.success && result.resultData && result.resultData.travellers) {
        if (result.resultData.travellers) {
          result.resultData.travellers.forEach(traveller => {
            traveller.uuid = [traveller.userId, traveller.tempUserId || ''].join('/')
          })
        }
        resolve(result.resultData.travellers)
      } else {
        reject(result.message)
      }
    })
  })
}

function getProducts () {
  return cloneDeep(Products)
}

function changeTravellerAttrs (traveller, type, $event) {
  console.info(traveller, type, $event.target.value)
  const v = TRAVELLER_ATTR_MAP[type]
  if (traveller[v.itemsKey]) {
    traveller[v.selectKey] = find(traveller[v.itemsKey], { id: parseInt($event.target.value) })
    console.info(traveller[v.selectKey])
  }
  this.$forceUpdate()
}

function initSelectTravellers (travellers) {
  travellers = travellers || []
  travellers.forEach(traveller => {
    traveller.selected = true
    let v = TRAVELLER_ATTR_MAP.cert
    traveller[v.selectKey] = traveller[v.itemsKey] ? traveller[v.itemsKey][0] : null
    if (traveller.userCerts && traveller.userCerts.length >= 1) {
      traveller.selectedCert = traveller.userCerts[0]
    }
  })
  return travellers
}

function selectBookTraveller (traveller, $event) {
  traveller.selected = $event.target.checked
  this.$forceUpdate()
}

function getUserIdsStr (travellers) {
  const userIds = getUserIds(travellers, false)
  return userIds.join(',')
}

function getUserIds (travellers, includeTemp = true) {
  const travellerIds = (travellers || []).filter(traveller => {
    return includeTemp || traveller.passengerType !== 'T'
  }).map(traveller => {
    return traveller.passengerType === 'T' ? traveller.tempUserId : traveller.userId
  })
  return travellerIds
}

function reCalcPolicyUser (creators, policyUserId) {
  let policyUser = null
  if (creators.length > 0) {
    let idx = policyUserId ? $checkItemIndex(creators, { userId: policyUserId }, 'userId') : 0
    policyUser = creators[idx < 0 ? 0 : idx]
  }
  return policyUser
}

function validateProductBillingTypeByIds (productType, travellerIds, companyCode) {
  const param = {
    companyCode: companyCode,
    productType: productType,
    userIds: travellerIds
  }
  return new Promise((resolve, reject) => {
    if (!travellerIds || travellerIds.length === 0) {
      $coreAlert(Vue.$i18nBundle('common.msg.selectTraveller'))
      reject(Vue.$i18nBundle('common.msg.selectTraveller'))
    } else {
      if (productType !== ProductType.Others) {
        BillingTypeProductService.validateProductBillingType(param).then((data) => {
          if (data.resultData) {
            if (data.resultData.allPassed) {
              resolve(data.resultData)
            } else {
              $coreAlert(data.message || '')
              reject(data.resultData)
            }
          } else {
            reject(data.resultData)
          }
        })
      } else {
        resolve()
      }
    }
  })
}

function validateTaTravellerBillingType (productType, taTravellers, companyCode) {
  const travellerIds = taTravellers.map(traveller => traveller.travelerId)
  return this.validateProductBillingTypeByIds(productType, travellerIds, companyCode)
}

function validateBookTravellerBillingType (productType, selectedTravellers, companyCode) {
  const travellerIds = getUserIds(selectedTravellers)
  return this.validateProductBillingTypeByIds(productType, travellerIds, companyCode)
}

function processExternalOrderAutoInput (self, currentSearchParam, productType) {
  const externalOrderAutoInput = self.$store.getters[OrderConsts.EXTERNAL_ORDER_AUTO_INPUT_KEY]
  if (externalOrderAutoInput) {
    let currentDomAirOrder = externalOrderAutoInput.currentDomAirOrder
    let currentIntlAirOrder = externalOrderAutoInput.currentIntlAirOrder
    let currentHotelOrder = externalOrderAutoInput.currentHotelOrder
    if (productType === ProductType.DomAir && currentDomAirOrder &&
      currentDomAirOrder.domAirOrderList && currentDomAirOrder.domAirOrderList.length > 0) {
      if (currentDomAirOrder.type === 'MD') { // 移动端暂不支持联程，转换成单程
        currentDomAirOrder.type = 'OW'
      }
      const domAirOrder = currentDomAirOrder.domAirOrderList[0]
      const rtDomAirOrder = currentDomAirOrder.domAirOrderList[1]
      if (domAirOrder) {
        const searchDomFlightParam = {
          airType: currentDomAirOrder.type,
          searchDomFlight: {
            departPort: domAirOrder.fromAirportEntity,
            arrivalPort: domAirOrder.toAirportEntity,
            arrivalAirportCode: domAirOrder.fromAirportEntity ? domAirOrder.fromAirportEntity.code : '',
            departAirportCode: domAirOrder.toAirportEntity ? domAirOrder.toAirportEntity.code : '',
            deptDate: domAirOrder.date,
            rtFlag: false,
            returnDate: ''
          }
        }
        if (currentDomAirOrder.type === 'RT' && rtDomAirOrder) {
          merge(searchDomFlightParam.searchDomFlight, {
            rtFlag: true,
            returnDate: rtDomAirOrder.date
          })
        }
        merge(currentSearchParam, searchDomFlightParam)
        console.info('external dom....', searchDomFlightParam)
      }
    }
    if (productType === ProductType.IntlAir && currentIntlAirOrder &&
      currentIntlAirOrder.intlAirOrderList && currentIntlAirOrder.intlAirOrderList.length > 0) {
      if (currentIntlAirOrder.type === 'MD') { // 移动端暂不支持联程，转换成单程
        currentIntlAirOrder.type = 'OW'
      }
      const intlAirSearchParam = {
        intlAirSearchDto: {
          interFlightType: currentIntlAirOrder.type
        }
      }
      const owIntlAirOrder = currentIntlAirOrder.intlAirOrderList[0]
      const rtIntlAirOrder = currentIntlAirOrder.intlAirOrderList[1]
      if (owIntlAirOrder) {
        merge(intlAirSearchParam.intlAirSearchDto, {
          departPort: owIntlAirOrder.fromCityEntity,
          arrivalPort: owIntlAirOrder.toCityEntity,
          owFromCity: owIntlAirOrder.fromCityEntity ? owIntlAirOrder.fromCityEntity.code : '',
          owToCity: owIntlAirOrder.toCityEntity ? owIntlAirOrder.toCityEntity.code : '',
          owFromDate: owIntlAirOrder.date
        })
        if (currentIntlAirOrder.type === 'RT' && rtIntlAirOrder) {
          merge(intlAirSearchParam.intlAirSearchDto, {
            rtReturnDate: rtIntlAirOrder.date
          })
        }
        merge(currentSearchParam, intlAirSearchParam)
      }
    }
    if (productType === ProductType.Hotel && currentHotelOrder && currentHotelOrder.cityEntity) {
      const isCN = (!currentHotelOrder.country || currentHotelOrder.country === 'CN')
      const searchParam = {
        hotelType: isCN ? 'D' : 'I',
        hotelQueryParam: {
          checkinDate: currentHotelOrder.checkinDate,
          checkoutDate: currentHotelOrder.checkoutDate
        }
      }
      searchParam[isCN ? 'domCity' : 'intlCity'] = currentHotelOrder.cityEntity
      merge(currentSearchParam, searchParam)
    }
  }
}

export default {
  getTravellerByUserBasic,
  searchAutocompleteTravellers,
  validateTraveller,
  loadTravellers,
  loadTravellersDetails,
  getProducts,
  changeTravellerAttrs,
  initSelectTravellers,
  selectBookTraveller,
  getUserIdsStr,
  getUserIds,
  reCalcPolicyUser,
  validateTaTravellerBillingType,
  validateBookTravellerBillingType,
  validateProductBillingTypeByIds,
  processExternalOrderAutoInput
}
