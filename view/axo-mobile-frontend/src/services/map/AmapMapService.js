import VueAMap, { lazyAMapApiLoaderInstance } from 'vue-amap'

/**
 * 地图搜索工具
 *
 * @param city
 * @param address
 * @returns {Promise<any>}
 */
function getPointByAddress (city, address) {
  return new Promise((resolve, reject) => {
    if (city && address && window.AMap) {
      window.AMap.service(['AMap.PlaceSearch'], () => {
        const placeSearch = new window.AMap.PlaceSearch({ // 构造地点查询类
          city: city // 城市
        })
        // 关键字查询
        placeSearch.search(address, function (status, result) {
          if (status === 'complete') {
            resolve(result.poiList.pois[0].location)
          } else {
            resolve()
          }
        })
      })
    } else {
      resolve()
    }
  })
}

function getPointsByHotels (city, hotelInfos) {
  const results = []
  return new Promise((resolve, reject) => {
    if (city && window.AMap) {
      let total = hotelInfos.length
      hotelInfos.forEach(hotelInfo => {
        const hotel = hotelInfo.hotel
        let address = hotel.addressCN.replace(/（/, '(')
        address = address.substring(0, address.indexOf('(') <= 0 ? address.length : address.indexOf('('))
        getPointByAddress(city, address).then(position => {
          if (position) {
            hotel.position = [position.lng, position.lat]
            results.push(hotelInfo)
          }
          if (--total === 0) {
            resolve(results)
          }
        })
      })
    } else {
      resolve(results)
    }
  })
}

function initAmapService () {
  VueAMap.initAMapApiLoader({
    key: process.env.VUE_APP_AMAP_KEY,
    plugin: ['AMap.PlaceSearch', 'AMap.Scale', 'AMap.OverView', 'AMap.ToolBar'],
    v: process.env.VUE_APP_AMAP_VERSION,
    uiVersion: process.env.VUE_APP_AMAP_UI_VERSION
  })
  return lazyAMapApiLoaderInstance.load()
}

export default {
  getPointByAddress,
  getPointsByHotels,
  initAmapService
}
