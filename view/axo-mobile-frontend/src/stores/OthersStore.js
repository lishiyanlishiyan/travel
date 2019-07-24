import MasterDataService from '../services/master/MasterDataService'

export default {
  namespaced: true,
  state: {
    dataConfig: {
      othersHotCities: []
    }
  },
  getters: {
    dataConfig: state => {
      return state.dataConfig
    },
    othersHotCities: state => {
      return state.dataConfig.othersHotCities
    }
  },
  mutations: {
    storeOthersAllHotCities (store, data) {
      store.dataConfig.othersHotCities = data
    }
  },
  actions: {
    storeOthersHotCities ({ commit, getters }, othersType) {
      if (getters.othersHotCities.length === 0 && othersType === 'I') {
        return MasterDataService.searchCityList({
          cityType: othersType,
          selectPageParam: '0'
        }, { loading: false }).then((cities) => {
          cities = cities || []
          cities = cities.map(city => {
            const { code, nameCN, nameEN, ctryCode, ctryNameCN, ctryNameEN } = city
            return {
              code, nameCN, nameEN, ctryCode, ctryNameCN, ctryNameEN
            }
          })
          commit('storeOthersAllHotCities', cities)
        })
      } else {
        getters.othersHotCities.length = 0
        return MasterDataService.searchCityList({
          cityType: othersType,
          selectPageParam: '0'
        }, { loading: false }).then((cities) => {
          cities = cities || []
          cities = cities.map(city => {
            const { code, nameCN, nameEN, ctryCode, ctryNameCN, ctryNameEN } = city
            return {
              code, nameCN, nameEN, ctryCode, ctryNameCN, ctryNameEN
            }
          })
          commit('storeOthersAllHotCities', cities)
        })
      }
    }
  }
}
