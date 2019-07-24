import MasterDataService from '../services/master/MasterDataService'

export default {
  namespaced: true,
  state: {
    dataConfig: {
      domAirHotAirports: [],
      cacheResultData: null
    },
    params: {
      searchDomFlightParams: null,
      selectedDomAirSegments: [],
      firstResultData: null,
      lastResultData: null,
      bookDomFlightParam: null
    }
  },
  getters: {
    dataConfig: state => {
      return state.dataConfig
    },
    searchDomFlightParams: state => {
      return state.params.searchDomFlightParams
    },
    cacheResultData: state => {
      return state.dataConfig.cacheResultData
    },
    selectedDomAirSegments: state => {
      return state.params.selectedDomAirSegments
    },
    firstResultData: state => {
      return state.params.firstResultData
    },
    lastResultData: state => {
      return state.params.lastResultData
    },
    bookDomFlightParam: state => {
      return state.params.bookDomFlightParam
    }
  },
  mutations: {
    storeDomAirHotAirports (store, airports) {
      store.dataConfig.domAirHotAirports = airports
    },
    storeSearchDomFlightParams (store, param) {
      store.params.searchDomFlightParams = param
    },
    storeBookDomFlightParam (store, param) {
      store.params.bookDomFlightParam = param
    },
    storeSelectedDomAirSegments (store, param) {
      store.params.selectedDomAirSegments = param
    },
    firstResultData (store, param) {
      store.params.firstResultData = param
    },
    lastResultData (store, param) {
      store.params.lastResultData = param
    },
    cacheResultData (store, resultData) {
      store.dataConfig.cacheResultData = resultData
    }
  },
  actions: {
    storeDomAirHotAirports ({ commit, getters }) {
      if (!getters.dataConfig.domAirHotAirports || getters.dataConfig.domAirHotAirports.length === 0) {
        return MasterDataService.searchAirportList({
          cityType: 'D',
          selectPageParam: '0'
        }, { loading: false }).then((airports) => {
          airports = airports || []
          airports = airports.map(airport => {
            const { code, nameCN, nameEN, cityCode, cityNameCN, cityNameEN } = airport
            return {
              code, nameCN, nameEN, cityCode, cityNameCN, cityNameEN
            }
          })
          commit('storeDomAirHotAirports', airports)
        })
      }
    },
    storeSearchDomFlightParams ({ commit }, params) {
      commit('storeSearchDomFlightParams', params)
    },
    storeBookDomFlightParam ({ commit }, param) {
      commit('storeBookDomFlightParam', param)
    },
    storeSelectedDomAirSegments ({ commit }, param) {
      commit('storeSelectedDomAirSegments', param || [])
    },
    firstResultData ({ commit }, param) {
      commit('firstResultData', param)
    },
    lastResultData ({ commit }, param) {
      commit('lastResultData', param)
    },
    cacheResultData ({ commit }, param) {
      commit('cacheResultData', param)
    }
  }
}
