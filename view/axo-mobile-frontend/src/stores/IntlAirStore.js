import MasterDataService from '../services/master/MasterDataService'

export default {
  namespaced: true,
  state: {
    dataConfig: {
      intlAirHotCitys: [],
      selectedSegment: [],
      selectedOption: null,
      allCabin: [],
      firstCurrentTab: null,
      lastCurrentTab: null
    },
    params: {
      intlAirSearchParam: null,
      cacheResultData: null,
      intlAirBookingParam: null
    }
  },
  getters: {
    dataConfig: state => {
      return state.dataConfig
    },
    intlAirSearchParam: state => {
      return state.params.intlAirSearchParam
    },
    cacheResultData: state => {
      return state.params.cacheResultData
    },
    selectedOption: state => {
      return state.dataConfig.selectedOption
    },
    selectedSegment: state => {
      return state.dataConfig.selectedSegment
    },
    intlAirBookingParam: state => {
      return state.params.intlAirBookingParam
    },
    allCabin: state => {
      return state.dataConfig.allCabin
    },
    firstCurrentTab: state => {
      return state.dataConfig.firstCurrentTab
    },
    lastCurrentTab: state => {
      return state.dataConfig.lastCurrentTab
    }
  },
  mutations: {
    storeIntlAirHotCitys (store, citys) {
      store.dataConfig.intlAirHotCitys = citys
    },
    storeIntlAirSearchParam (store, param) {
      store.params.intlAirSearchParam = param
    },
    storeIntlAirBookingParam (store, param) {
      store.params.intlAirBookingParam = param
    },
    selectedSegment (store, selectedSegment) {
      store.dataConfig.selectedSegment = selectedSegment
    },
    cacheResultData (store, resultData) {
      store.params.cacheResultData = resultData
    },
    selectedOption (store, selectedOption) {
      store.dataConfig.selectedOption = selectedOption
    },
    allCabin (store, allCabin) {
      store.dataConfig.allCabin = allCabin
    },
    firstCurrentTab (store, firstCurrentTab) {
      store.dataConfig.firstCurrentTab = firstCurrentTab
    },
    lastCurrentTab (store, lastCurrentTab) {
      store.dataConfig.lastCurrentTab = lastCurrentTab
    }
  },
  actions: {
    storeIntlAirHotCitys ({ commit, getters }) {
      if (!getters.dataConfig.intlAirHotCitys || getters.dataConfig.intlAirHotCitys.length === 0) {
        return MasterDataService.searchAirCityList({
          cityType: 'I',
          selectPageParam: '0'
        }, { loading: false }).then((citys) => {
          citys = citys || []
          citys = citys.map(city => {
            const { code, nameCN, nameEN, ctryCode, ctryNameCN, ctryNameEN } = city
            return {
              code, nameCN, nameEN, ctryCode, ctryNameCN, ctryNameEN
            }
          })
          commit('storeIntlAirHotCitys', citys)
        })
      }
    },
    storeIntlAirSearchParam ({ commit }, params) {
      commit('storeIntlAirSearchParam', params)
    },
    storeIntlAirBookingParam ({ commit }, param) {
      commit('storeIntlAirBookingParam', param)
    },
    selectedSegment ({ commit }, param) {
      commit('selectedSegment', param)
    },
    cacheResultData ({ commit }, param) {
      commit('cacheResultData', param)
    },
    selectedOption ({ commit }, param) {
      commit('selectedOption', param)
    },
    allCabin ({ commit }, param) {
      commit('allCabin', param)
    },
    firstCurrentTab ({ commit }, param) {
      commit('firstCurrentTab', param)
    },
    lastCurrentTab ({ commit }, param) {
      commit('lastCurrentTab', param)
    }
  }
}
