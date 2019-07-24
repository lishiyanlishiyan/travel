import TrainService from '../services/train/TrainService'

export default {
  namespaced: true,
  state: {
    dataConfig: {
      trainHotStations: [],
      cacheResultData: null
    },
    params: {
      searchTrainScheduleParams: null,
      selectedTrainSegments: [],
      firstResultData: null,
      lastResultData: null,
      bookDomFlightParam: null
    }
  },
  getters: {
    dataConfig: state => {
      return state.dataConfig
    },
    searchTrainScheduleParams: state => {
      return state.params.searchTrainScheduleParams
    },
    cacheResultData: state => {
      return state.dataConfig.cacheResultData
    },
    selectedTrainSegments: state => {
      return state.params.selectedTrainSegments
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
    storeTrainHotStations (store, airports) {
      store.dataConfig.trainHotStations = airports
    },
    storeSearchTrainScheduleParams (store, param) {
      store.params.searchTrainScheduleParams = param
    },
    storeBookDomFlightParam (store, param) {
      store.params.bookDomFlightParam = param
    },
    storeSelectedTrainSegments (store, param) {
      store.params.selectedTrainSegments = param
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
    storeTrainHotStations ({ commit, getters }) {
      if (!getters.dataConfig.trainHotStations || getters.dataConfig.trainHotStations.length === 0) {
        return TrainService.getTrainStation({
          queryStrs: 'Y'
        }, { loading: false }).then((trainStations) => {
          trainStations = trainStations || []
          trainStations = trainStations.map(trainStation => {
            const { code, nameCN, nameEN, cityCode, cityNameCN, cityNameEN } = trainStation
            return {
              code, nameCN, nameEN, cityCode, cityNameCN, cityNameEN
            }
          })
          commit('storeTrainHotStations', trainStations)
        })
      }
    },
    storeSearchTrainScheduleParams ({ commit }, param) {
      commit('storeSearchTrainScheduleParams', param)
    },
    storeBookDomFlightParam ({ commit }, param) {
      commit('storeBookDomFlightParam', param)
    },
    storeSelectedTrainSegments ({ commit }, param) {
      commit('storeSelectedTrainSegments', param || [])
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
