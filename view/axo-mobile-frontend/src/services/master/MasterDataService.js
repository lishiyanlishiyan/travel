import { baseKeyWordsParam, getCallerMethod } from '../caller/ServiceCaller'

const SERVICE_PATH = 'master/city'
const SEARCH_CITY_LIST_PARAM = 'com.citsamex.app.spi.data.caller.request.master.city.SearchCityListParam'

function searchAirportList (param, config) {
  const searchAirportList = getCallerMethod(SERVICE_PATH, 'searchAirportList', SEARCH_CITY_LIST_PARAM)
  return new Promise((resolve, reject) => {
    searchAirportList(param, config).then((result) => {
      if (result.success && result.resultData && result.resultData.airports) {
        resolve(result.resultData.airports)
      } else {
        reject(result)
      }
    }, reject)
  })
}

function searchAirCityList (param, config) {
  const searchAirCityList = getCallerMethod(SERVICE_PATH, 'searchAirCityList', SEARCH_CITY_LIST_PARAM)
  return new Promise((resolve, reject) => {
    searchAirCityList(param, config).then((result) => {
      if (result.success && result.resultData && result.resultData.cityList) {
        resolve(result.resultData.cityList)
      } else {
        reject(result)
      }
    }, reject)
  })
}

function searchCityList (param, config) {
  const searchMethod = getCallerMethod(SERVICE_PATH, 'searchCityList', SEARCH_CITY_LIST_PARAM)
  return new Promise((resolve, reject) => {
    searchMethod(param, config).then((result) => {
      if (result.success && result.resultData && result.resultData.cityList) {
        resolve(result.resultData.cityList)
      } else {
        reject(result)
      }
    }, reject)
  })
}

function searchCountryList (param, config) {
  const searchMethod = getCallerMethod(SERVICE_PATH, 'searchCountryList', baseKeyWordsParam())
  return new Promise((resolve, reject) => {
    searchMethod(param, config).then((result) => {
      if (result.success && result.resultData && result.resultData.countryList) {
        resolve(result.resultData.countryList)
      } else {
        reject(result)
      }
    }, reject)
  })
}

function searchAllCountryList (param, config) {
  const searchMethod = getCallerMethod(SERVICE_PATH, 'searchAllCountryList')
  return new Promise((resolve, reject) => {
    searchMethod(param, config).then((result) => {
      if (result.success && result.resultData && result.resultData.countryList) {
        resolve(result.resultData.countryList)
      } else {
        reject(result)
      }
    }, reject)
  })
}

function getAutoSearchCityList (type) {
  return (param, config) => {
    return searchCityList(Object.assign({ cityType: type }, param || {}), config)
  }
}

export default {
  searchAirportList,
  searchCityList,
  getAutoSearchCityList,
  searchCountryList,
  searchAllCountryList,
  searchAirCityList
}
