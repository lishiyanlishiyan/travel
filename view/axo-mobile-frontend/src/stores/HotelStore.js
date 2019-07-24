import MasterDataService from '../services/master/MasterDataService'

export default {
  namespaced: true,
  state: {
    dataConfig: {
      domHotelHotCities: [],
      intlHotelHotCities: [],
      cacheResultData: null,
      hotelSearchParam: null,
      hotelDetailParam: null,
      hotelRoomDetailParam: null
    },
    params: {
      hotelSearchStatus: null
    }
  },
  getters: {
    dataConfig: state => {
      return state.dataConfig
    },
    domHotelHotCities: state => {
      return state.dataConfig.domHotelHotCities
    },
    intlHotelHotCities: state => {
      return state.dataConfig.intlHotelHotCities
    },
    hotelSearchParam: state => {
      return state.dataConfig.hotelSearchParam
    },
    hotelDetailParam: state => {
      return state.dataConfig.hotelDetailParam
    },
    hotelRoomDetailParam: state => {
      return state.dataConfig.hotelRoomDetailParam
    },
    hotelSearchStatus: state => {
      return state.params.hotelSearchStatus
    }
  },
  mutations: {
    storeDomHotelHotCities (store, data) {
      store.dataConfig.domHotelHotCities = data
    },
    storeIntlHotelHotCities (store, data) {
      store.dataConfig.intlHotelHotCities = data
    },
    storeHotelSearchParam (store, data) {
      store.dataConfig.hotelSearchParam = data
    },
    storeHotelDetailParam (store, data) {
      store.dataConfig.hotelDetailParam = data
    },
    storeHotelRoomDetailParam (store, data) {
      store.dataConfig.hotelRoomDetailParam = data
    },
    storeHotelSearchStatus (store, data) {
      store.params.hotelSearchStatus = data
    },
    cacheResultData (store, resultData) {
      store.dataConfig.cacheResultData = resultData
    }
  },
  actions: {
    storeHotelHotCities ({ commit, getters }, hotelType) {
      if (getters.domHotelHotCities.length === 0 || getters.intlHotelHotCities.length === 0) {
        return MasterDataService.searchCityList({
          cityType: hotelType,
          selectPageParam: '0'
        }, { loading: false }).then((cities) => {
          cities = cities || []
          cities = cities.map(city => {
            const { code, nameCN, nameEN, ctryCode, ctryNameCN, ctryNameEN } = city
            return {
              code, nameCN, nameEN, ctryCode, ctryNameCN, ctryNameEN
            }
          })
          commit(hotelType === 'D' ? 'storeDomHotelHotCities' : 'storeIntlHotelHotCities', cities)
        })
      }
    },
    storeHotelSearchParam ({ commit }, param) {
      commit('storeHotelSearchParam', param)
    },
    storeHotelDetailParam ({ commit }, param) {
      commit('storeHotelDetailParam', param)
    },
    storeHotelRoomDetailParam ({ commit }, param) {
      commit('storeHotelRoomDetailParam', param)
    },
    storeHotelSearchStatus ({ commit }, param) {
      commit('storeHotelSearchStatus', param)
    },
    cacheResultData ({ commit }, param) {
      commit('cacheResultData', param)
    }
  }
}
