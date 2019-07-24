import { $httpGet, $httpPost } from '../../utils/AxoUtils'
import store from '../../store'
import moment from 'moment'
import axios from 'axios'
import AxoConsts from '../../consts/AxoConsts'

const LOGIN_PARAM = 'com.citsamex.app.spi.data.caller.request.profile.LoginParam'

function baseAxiosPost (url, params, config = {}) {
  const baseAxios = axios.create({
    baseURL: process.env.VUE_APP_API_GATEWAY
  })
  return new Promise((resolve, reject) => {
    baseAxios.post(url, params, config).then(res => {
      resolve(res.data)
    }).catch(err => {
      reject(err.data)
    })
  })
}

const LoginService = {
  login: (param = {}, config = {}) => {
    return $httpPost(`/login`, Object.assign({ '@class': LOGIN_PARAM }, param), Object.assign({
      addToken: false,
      isLogin: true
    }, config))
  },
  loginByToken: (token, config = {}) => {
    return $httpPost(`/loginByToken`, null, Object.assign({
      addToken: false,
      isLogin: true,
      headers: {
        Authorization: `Bearer ${token}`
      }
    }, config))
  },
  ssoLoginConfig (companyCode, config = {}) {
    return baseAxiosPost(`/ssoLoginConfig/${companyCode}`, null, config)
  },
  onlineSso (config = { loading: true }) {
    if (store.getters.globalConfig.accessToken) {
      Object.assign(config, {
        headers: {
          Authorization: `Bearer ${store.getters.globalConfig.accessToken}`
        }
      })
    }
    return baseAxiosPost(`/onlineSso`, null, config)
  },
  checkAndLoadDefaultSetup () {
    const sessionInfo = store.getters.sessionInfo
    const config = process.env.VUE_APP_LOAD_DEFAULT_INTERVAL || '30m'
    const timeConfig = config.match(AxoConsts.TIME_CONFIG_REGEXP)
    if (!sessionInfo.lastLoadDefaultDate || moment(sessionInfo.lastLoadDefaultDate).add(timeConfig[1], timeConfig[2] || 'm').isBefore(moment())) {
      store.dispatch('storeNewsCounts', { cnLoaded: false, enLoaded: false })
      $httpGet(`/loadDefaultSetup`, null, {
        loading: false
      }).then(data => {
        if (data && data.resultData) {
          store.dispatch('storeDefaultSetupConfig', data.resultData)
        }
      })
    }
  },
  gotoOnlineSso () {
    this.$coreShowLoading()
    LoginService.onlineSso().then(data => {
      if (data && data.resultData && data.resultData.enabled && data.resultData.url) {
        window.location.href = data.resultData.url
      }
    })
  }
}
export default LoginService
