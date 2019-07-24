import Vue from 'vue'
import Vuex from 'vuex'
import createPersistedState from 'vuex-persistedstate'
import { $calcDefaultSetup, $getSupportLocale } from './utils/AxoUtils'
import DomAirStore from './stores/DomAirStore'
import IntlAirStore from './stores/IntlAirStore'
import TrainStore from './stores/TrainStore'
import OthersStore from './stores/OthersStore'
import HotelStore from './stores/HotelStore'
import OrderStore from './stores/OrderStore'
import get from 'lodash/get'
import set from 'lodash/set'
import merge from 'lodash/merge'
import { calcF7Language } from './utils/F7Utils'
import { Validator } from 'vee-validate'
import moment from 'moment'
import AxoConsts from './consts/AxoConsts'

Vue.use(Vuex)

const STORAGE_KEY = 'axo-mobile'
const F7ROUTER_STORAGE_KEY = 'f7router-view_main-history'
/**
 * 需要保存到LocalStorage的store数据
 * @type {string[]}
 */
const STORAGE_SAVE_KEYS = ['globalConfig', 'DomAir.dataConfig', 'IntlAir.dataConfig', 'Train.dataConfig', 'Order.dataConfig', 'Hotel.dataConfig']

export function $localeChangePlugin (store) {
  store.subscribe(mutation => {
    if (mutation.type === 'changeLocale') {
      let locale = mutation.payload
      let spLocale = $getSupportLocale(locale)
      if (spLocale) {
        Vue.i18n.set(spLocale.language)
        if (Vue.f7) {
          Vue.f7.language = locale
          calcF7Language(Vue.f7, locale)
        }
        Validator.localize(spLocale.language)
        moment.locale(spLocale.locale.toLowerCase())
      }
    }
  })
}

export default new Vuex.Store({
  modules: {
    DomAir: DomAirStore,
    IntlAir: IntlAirStore,
    Train: TrainStore,
    Hotel: HotelStore,
    Order: OrderStore,
    Others: OthersStore
  },
  state: {
    globalConfig: {
      currentLocale: 'zh_CN',
      isLoginIn: false,
      loginResult: {},
      accessToken: '',
      sessionInfo: {
        created: null,
        expires: null,
        forceExpire: false,
        lastLoadDefaultDate: null
      },
      defaultSetupConfig: null,
      newsCounts: {
        cnLoaded: false,
        enLoaded: false,
        cn: 0,
        en: 0
      }
    },
    params: {
      dropDownOpening: false
    }
  },
  mutations: {
    changeLocale (state, locale) {
      state.globalConfig.currentLocale = locale
    },
    userLogin (state, user) {
      state.globalConfig.currentLoginUser = user
      state.globalConfig.isLoginIn = !!user
    },
    loginResult (state, loginResult) {
      state.globalConfig.loginResult = loginResult
    },
    accessToken (state, accessToken) {
      state.globalConfig.accessToken = accessToken
    },
    sessionInfo (state, sessionInfo) {
      state.globalConfig.sessionInfo = sessionInfo
    },
    storeDefaultSetupConfig (state, loginResult) {
      state.globalConfig.defaultSetupConfig = $calcDefaultSetup(loginResult, true)
      state.globalConfig.sessionInfo.lastLoadDefaultDate = moment()
      if (loginResult && loginResult.loginResponse && loginResult.loginResponse.userBasic) {
        state.globalConfig.currentLoginUser = loginResult.loginResponse.userBasic
      }
    },
    forceSessionExpire (state) {
      state.globalConfig.sessionInfo.forceExpire = true
    },
    storeNewsCounts (state, data) {
      state.globalConfig.newsCounts = merge(state.globalConfig.newsCounts, data)
    },
    storeDropDownOpening (state, data) {
      state.params.dropDownOpening = data
    }
  },
  getters: {
    globalConfig: state => state.globalConfig,
    loginResult: state => state.globalConfig.loginResult,
    currentLoginUser: state => state.globalConfig.currentLoginUser,
    sessionInfo: state => state.globalConfig.sessionInfo,
    defaultSetupConfig: state => state.globalConfig.defaultSetupConfig,
    newsCounts: state => state.globalConfig.newsCounts,
    dropDownOpening: state => state.params.dropDownOpening
  },
  actions: {
    changeLocale ({ commit, state }, locale) {
      let spLocale = $getSupportLocale(locale)
      if (spLocale) {
        commit('changeLocale', locale)
      }
    },
    userLogin: ({ commit }, user) => commit('userLogin', user),
    logout: ({ commit }) => {
      window.localStorage.removeItem(STORAGE_KEY)
      window.localStorage.removeItem(F7ROUTER_STORAGE_KEY)
    },
    forceSessionExpire: ({ commit }) => {
      commit('forceSessionExpire')
    },
    loginResult: ({ commit }, loginResult) => {
      commit('userLogin', loginResult ? loginResult.loginResponse.userBasic : {})
      commit('loginResult', loginResult)
      commit('accessToken', loginResult ? loginResult.accessToken : undefined)
      const config = process.env.VUE_APP_SESSION_TIMEOUT || '1d'
      const timeoutConfig = config.match(AxoConsts.TIME_CONFIG_REGEXP)
      commit('sessionInfo', {
        created: moment(),
        expires: moment().add(timeoutConfig[1], timeoutConfig[2] || 'd'),
        forceExpire: false,
        lastLoadDefaultDate: moment()
      })
      commit('storeDefaultSetupConfig', loginResult)
    },
    storeDefaultSetupConfig ({ commit }, loginResult) {
      commit('storeDefaultSetupConfig', loginResult)
    },
    storeDropDownOpening ({ commit }, data) {
      commit('storeDropDownOpening', data)
    },
    storeNewsCounts ({ commit }, data) {
      commit('storeNewsCounts', data)
    }
  },
  plugins: [createPersistedState({
    key: STORAGE_KEY,
    reducer (val) {
      const saveObj = {}
      STORAGE_SAVE_KEYS.forEach(data => set(saveObj, data, get(val, data)))
      return saveObj
    }
  }), $localeChangePlugin]
})
