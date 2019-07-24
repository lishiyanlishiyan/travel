import cloneDeep from 'lodash/cloneDeep'
import merge from 'lodash/merge'
import HotelService from './HotelService'
import CompanyService from '../company/CompanyService'
import store from '../../store'

function autoHotelList (param, config) {
  return new Promise((resolve, reject) => {
    HotelService.getHotelMasterList(param, config).then((result) => {
      let hotels = []
      if (result.success && result.resultData) {
        hotels = result.resultData.hotelMasters || []
      }
      resolve(hotels)
    }, reject)
  })
}

function getAutoHotelList (searchParam, searchConfig) {
  const _searchParam = cloneDeep(searchParam)
  delete _searchParam.domCity
  delete _searchParam.intlCity
  delete _searchParam.hotelType
  merge(_searchParam, {
    policyApplied: true,
    hotelQueryParam: {
      corporateAlwaysFirst: false,
      guestCount: 1,
      minPrice: 0,
      queryOldDate: true,
      resultType: 3,
      roomCount: 1
    }
  })
  return (param, config) => {
    _searchParam.hotelQueryParam.hotelName = param.keyWords
    return autoHotelList(_searchParam, Object.assign(config, searchConfig))
  }
}

function getAutoHotelAddressList (searchParam, city, enableCompanyAddress) {
  const globalConfig = store.getters.globalConfig
  const calcParam = {
    city: city.nameCN,
    cityCode: city.code,
    countryCode: city.ctryCode,
    companyCode: searchParam.companyCode,
    enableCompanyAddr: enableCompanyAddress,
    language: globalConfig.currentLocale
  }
  const autoHotelAddressMethod = (param, config) => {
    const external = !!param.keyWords // 判断是请求公司地址数据还是外部接口数据
    const searchMethod = external ? HotelService.locationInputTips : CompanyService.getAvailableCompanyCityAddresses
    return new Promise((resolve, reject) => {
      const internalSearch = function () {
        if (!external && !enableCompanyAddress) {
          resolve()
        } else {
          searchMethod(Object.assign(calcParam, param), config).then((result) => {
            let items = []
            if (result.success && result.resultData) {
              if (result.resultData.companyCityAddressList) { // 公司配置的地址搜索
                items = result.resultData.companyCityAddressList
              } else if (result.resultData.tips) { // 外部接口搜索返回值，通常国内是高德地图接口、国际是here或者google地图接口
                items = result.resultData.tips.map(tip => {
                  return {
                    id: tip.id,
                    nameCn: tip.name,
                    nameEn: tip.name,
                    addressCn: tip.address,
                    location: tip.location
                  }
                })
              }
            }
            resolve(items)
          }, reject)
        }
      }
      if (city.ctryCode !== 'CN' && !calcParam.location && external) { // 国外接口需要城市中心坐标点
        HotelService.getCityLocation({
          countryCode: city.ctryCode,
          cityName: city.nameEN
        }, { loading: false }).then(data => {
          // 城市location暂存起来
          calcParam.location = data && data.resultData ? data.resultData.location : undefined
          internalSearch()
        }, reject)
      } else {
        internalSearch()
      }
    })
  }
  return autoHotelAddressMethod
}

export default {
  autoHotelList,
  getAutoHotelList,
  getAutoHotelAddressList
}
