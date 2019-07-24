const DEFAULT_TRAVELLER_TAB_CONFIG = {
  internalTab: null,
  externalTab: null,
  currentTravellerTabId: ''
}
export default {
  namespaced: true,
  state: {
    dataConfig: {
      orderTimelineList: [],
      travellerTabConfig: Object.assign({}, DEFAULT_TRAVELLER_TAB_CONFIG),
      basicSearchParam: {
        policyUserId: '',
        policyUser: null,
        bookTravellers: [],
        externalOrderNo: '',
        taNo: ''
      },
      searchStatus: {
        searchOnlyFlag: false
      }
    },
    params: {
      continueOrderNo: null,
      ssoExternalOrderNo: '',
      externalOrderAutoInput: null,
      orderSearchStatus: null,
      orderDetail: {},
      submitPageData: {}
    }
  },
  getters: {
    dataConfig: state => {
      return state.dataConfig
    },
    orderTimelineList: state => {
      return state.dataConfig.orderTimelineList
    },
    bookTravellers: state => {
      return state.dataConfig.basicSearchParam.bookTravellers
    },
    travellerTabConfig: state => {
      return state.dataConfig.travellerTabConfig
    },
    basicSearchParam: state => {
      return state.dataConfig.basicSearchParam
    },
    searchStatus: state => {
      return state.dataConfig.searchStatus
    },
    continueOrderNo: state => {
      return state.params.continueOrderNo
    },
    ssoExternalOrderNo: state => {
      return state.params.ssoExternalOrderNo
    },
    externalOrderAutoInput: state => {
      return state.params.externalOrderAutoInput
    },
    orderDetail: state => {
      return state.params.orderDetail
    },
    submitPageData: state => {
      return state.params.submitPageData
    },
    orderSearchStatus: state => {
      return state.params.orderSearchStatus
    }
  },
  mutations: {
    storeOrderTimelineList (store, orderTimelineList) {
      store.dataConfig.orderTimelineList = orderTimelineList
    },
    storeContinueOrderNo (store, orderNo) {
      store.params.continueOrderNo = orderNo
    },
    storeSsoExternalOrderNo (store, orderNo) {
      store.params.ssoExternalOrderNo = orderNo
    },
    storeTravellerTabConfig (store, data) {
      if (!data) {
        data = Object.assign({}, DEFAULT_TRAVELLER_TAB_CONFIG)
      }
      store.dataConfig.travellerTabConfig = data
    },
    storeBasicSearchParam (store, data) {
      store.dataConfig.basicSearchParam = data
    },
    storeSearchStatus (store, data) {
      store.dataConfig.searchStatus = data
    },
    storeExternalOrderAutoInput (store, data) {
      store.params.externalOrderAutoInput = data
    },
    storeOrderDetail (store, data) {
      store.params.orderDetail = data
    },
    storeSubmitPageData (store, data) {
      store.params.submitPageData = data
    },
    storeOrderSearchStatus (store, data) {
      store.params.orderSearchStatus = data
    }
  },
  actions: {
    storeOrderTimelineList ({ commit, getters }, data) {
      commit('storeOrderTimelineList', data)
    },
    storeContinueOrderNo ({ commit, getters }, data) {
      commit('storeContinueOrderNo', data)
    },
    storeSsoExternalOrderNo ({ commit, getters }, data) {
      commit('storeSsoExternalOrderNo', data)
    },
    storeTravellerTabConfig ({ commit, getters }, data) {
      commit('storeTravellerTabConfig', data)
    },
    storeBasicSearchParam ({ commit, getters }, data) {
      commit('storeBasicSearchParam', data)
    },
    storeSearchStatus ({ commit, getters }, data) {
      commit('storeSearchStatus', data)
    },
    storeExternalOrderAutoInput ({ commit, getters }, data) {
      commit('storeExternalOrderAutoInput', data)
    },
    storeOrderDetail ({ commit, getters }, data) {
      commit('storeOrderDetail', data)
    },
    storeSubmitPageData ({ commit, getters }, data) {
      commit('storeSubmitPageData', data)
    },
    storeOrderSearchStatus ({ commit, getters }, data) {
      commit('storeOrderSearchStatus', data)
    }
  }
}
