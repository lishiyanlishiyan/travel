/***
 * @author Gary.Fu
 */
import Vue from 'vue'
import axios from 'axios'
import store from '../store'
import { isArray, isPlainObject, isString } from 'lodash/lang'
import forEach from 'lodash/forEach'
import cloneDeep from 'lodash/cloneDeep'
import get from 'lodash/get'
import ProfileConsts from '../consts/ProfileConsts'
import moment from 'moment'
import AxoConsts from '../consts/AxoConsts'
import numeral from 'numeral'
import OrderConsts from '../consts/OrderConsts'

const DEFAULT_LOCALE = 'zh_CN'
const SUPPORT_LOCALE = {
  zh_CN: {
    language: 'cn',
    locale: 'zh_CN',
    label: '中文'
  },
  en_US: {
    language: 'en',
    locale: 'en_US',
    label: 'English'
  }
}

const LoadingHelper = cloneDeep(AxoConsts.COUNT_HELPER)

export function $isCordova () {
  return process.env.VUE_APP_CORDOVA_FLAG === '1' && window.cordova
}

export function $isOnlineSsoEnabled () {
  return process.env.VUE_APP_ONLINE_SSO_ENABLED === '1' && !$isCordova()
}

/**
 * 基本提示工具
 * @param msg
 * @param config
 * @returns {Promise<any>}
 */
export function $coreAlert (msg, config = {}) {
  const _config = Object.assign({ title: Vue.$i18nBundle('common.label.info') }, config)
  return new Promise((resolve, reject) => {
    if (Vue.f7) {
      Vue.f7.dialog.alert(msg, _config.title, () => {
        resolve()
      })
    } else {
      resolve()
    }
  })
}

export const $coreError = (msg, config = {}) => {
  return $coreAlert(msg, Object.assign({ title: Vue.$i18nBundle('common.label.error') }, config))
}

export const $coreWarning = (msg, config = {}) => {
  return $coreAlert(msg, Object.assign({ title: Vue.$i18nBundle('common.label.warning') }, config))
}

export function $coreConfirm (msg, config = {}) {
  const _config = Object.assign({ title: Vue.$i18nBundle('common.label.confirm') }, config)
  return new Promise((resolve, reject) => {
    if (Vue.f7) {
      Vue.f7.dialog.confirm(msg, _config.title, resolve, reject)
    } else {
      resolve()
    }
  })
}

export function $coreShowLoading (msg) {
  LoadingHelper.increment()
  if (Vue.f7) {
    if (msg) {
      Vue.f7.dialog.preloader(msg)
    } else {
      Vue.f7.preloader.show()
    }
  }
}

export function $coreHideLoading () {
  if (Vue.f7 && LoadingHelper.decrement().isHide()) {
    Vue.f7.dialog.close()
    Vue.f7.preloader.hide()
  }
}

export function $coreToast (content, config = {}) {
  if (Vue.f7) {
    Vue.f7.toast.create(Object.assign({
      text: content,
      destroyOnClose: true,
      position: 'top',
      closeTimeout: 3000
    }, config)).open()
  }
}

export function $coreShowPopup (content, title = Vue.$i18nBundle('common.label.info'), config = {}) {
  if (Vue.f7) {
    const closeText = Vue.$i18nBundle('common.label.close')
    Vue.f7.popup.create({
      content: `
            <div class="popup">
              <div class="page">
                <div class="navbar">
                  <div class="navbar-inner">
                    <div class="title">${title}</div>
                    <div class="right"><a href="#" class="link popup-close">${closeText}</a></div>
                  </div>
                </div>
                <div class="page-content">
                   <div class="block block-strong">${content}</div>
                </div>
              </div>
            </div>
          `.trim(),
      on: {
        closed () {
          this.destroy()
        }
      }
    }).open()
  }
}

export function $coreShowSheet (content, title = '', config = {}) {
  if (Vue.f7) {
    const closeText = Vue.$i18nBundle('common.label.close')
    Vue.f7.popup.create({
      content: `
          <div class="sheet-modal my-sheet">
              <div class="toolbar">
                <div class="toolbar-inner">
                  <div class="left"></div>
                  <div class="toolbar-title">${title}</div>
                  <div class="right"><a class="link sheet-close" href="#">${closeText}</a></div>
                </div>
              </div>
              <div class="sheet-modal-inner">
                 <div class="block block-strong">${content}</div>
              </div>
          </div>
          `.trim(),
      on: {
        closed () {
          this.destroy()
        }
      }
    }).open()
  }
}

/**
 * axios封装http请求
 * @param url
 * @param params
 * @returns {Promise<any>}
 */
export function $httpGet (url, params, config = {}) {
  return new Promise((resolve, reject) => {
    axios.get(url, Object.assign({
      params: params
    }, config)).then(res => {
      resolve(res.data)
    }).catch(err => {
      reject(err.data)
    })
  })
}

export function $httpPost (url, params, config = {}) {
  return new Promise((resolve, reject) => {
    axios.post(url, params, config)
      .then(res => {
        resolve(res.data)
      })
      .catch(err => {
        reject(err.data)
      })
  })
}

axios.interceptors.request.use(config => {
  if (config.loading !== false) {
    config.loading = isString(config.loading) ? config.loading : true
    $coreShowLoading(isString(config.loading) ? config.loading : undefined)
  }
  config.headers['axo_locale'] = store.getters.globalConfig.currentLocale || DEFAULT_LOCALE
  if (config.addToken !== false && store.getters.globalConfig.accessToken) {
    config.headers['Authorization'] = `Bearer ${store.getters.globalConfig.accessToken}`
  }
  return config
})
axios.interceptors.response.use(data => {
  data.config.loading && $coreHideLoading()
  return data
}, error => {
  error.config.loading && $coreHideLoading()
  if (error.response.status === 401 && !error.response.config.isLogin) {
    $doLogout()
  }
  return error.response
})

function $noop () {
}

/**
 * 初始化表单验证
 * @param name
 */
function $initFormValidate (name) {
  const $form = this.$$(name ? `form[name=${name}]` : 'form')
  $form.change((e) => this.$formValidate(e).then($noop, $noop))
  $form.on('change input', 'input, textarea, select', (e) => this.$formValidate(e).then($noop, $noop))
  $form.change()
}

function $formValidate (e) {
  return new Promise((resolve, reject) => {
    this.$nextTick(() => {
      this.$validator.validate().then(result => {
        if (this.formValidator) {
          this.$set(this.formValidator, 'isFormValid', result)
        }
        if (result) {
          resolve(result)
        } else {
          reject(result)
        }
      }, reject)
    })
  })
}

export function $isLocale (locale) {
  return store.getters.globalConfig.currentLocale === locale
}

export function $getSupportLocale (locale) {
  return SUPPORT_LOCALE[locale]
}

export function $getSupportLocales () {
  return [SUPPORT_LOCALE.zh_CN, SUPPORT_LOCALE.en_US]
}

export function $changeLocale (locale = DEFAULT_LOCALE) {
  let spLocale = $getSupportLocale(locale)
  if (spLocale) {
    store.dispatch('changeLocale', locale)
  }
}

export function $doLogout () {
  store.dispatch('logout')
  window.location.reload()
}

export function $logout () {
  $coreConfirm(Vue.$i18nBundle('login.msg.logoutConfirm')).then($doLogout)
}

export function $back (url, options) {
  if (isPlainObject(url)) {
    options = url
    url = undefined
  }
  return new Promise(resolve => {
    setTimeout(() => {
      if (this && this.$f7router) {
        this.$f7router.back(url, options)
      }
      resolve()
    })
  })
}

export function $goto (url, options) {
  return new Promise(resolve => {
    setTimeout(() => {
      if (this && this.$f7router) {
        this.$f7router.navigate(url, options)
      }
      resolve()
    })
  })
}

export function $backHomePage (url) {
  this.$coreConfirm(this.$i18nBundle('common.msg.backHomePage')).then(() => {
    this.$goto(url || '/', { force: true, ignoreCache: true, clearPreviousHistory: true })
  })
}

export function $hasRole (...roles) {
  const loginUser = store.getters.currentLoginUser
  let result = false
  if (loginUser && loginUser.userRoles) {
    const userRoles = loginUser.userRoles.map(userRole => ProfileConsts.ROLE_MAP[userRole.roleId])
    roles.forEach(role => {
      if (userRoles.indexOf(role) > -1) {
        result = true
      }
    })
  }
  return result
}

export function $gotoAxoTaPage (taNo, productType, bookSuccess) {
  if (bookSuccess) {
    store.dispatch('Order/storeTravellerTabConfig')
    store.dispatch('Order/storeSsoExternalOrderNo')
  }
  this.$goto('/', {
    force: true,
    ignoreCache: true,
    clearPreviousHistory: true
  }).then(() => {
    this.$goto(`/order/detail/${taNo}/${productType || ''}`, {})
  })
}

export function $defaultSetup (productType) {
  let defaultSetupConfig = store.getters.defaultSetupConfig
  const loginResult = store.getters.loginResult
  if (!defaultSetupConfig && loginResult) {
    store.dispatch('storeDefaultSetupConfig', loginResult)
  }
  defaultSetupConfig = store.getters.defaultSetupConfig
  if (defaultSetupConfig) {
    return cloneDeep(defaultSetupConfig[productType])
  }
}

export function $calcDefaultSetup (loginResult, remove) {
  if (loginResult) {
    const defaultSetupConfig = {
      // 产品默认信息
      domair: loginResult.domAirDefaultSetup,
      intlair: loginResult.intlAirDefaultSetup,
      hotel: loginResult.hotelDefaultSetup,
      'car-rental': loginResult.carRentalDefaultSetup,
      train: {
        trainDefaultSetup: loginResult.trainDefaultSetup,
        trainGlobalDefaultSetup: loginResult.trainGlobalDefaultSetup
      },
      others: loginResult.otherDefaultSetup,
      company: loginResult.companyDefaultSetup,
      profile: loginResult.profileDefaultSetup,
      // 公司配置信息
      companyProducts: loginResult.companyProducts,
      allProducts: loginResult.allProducts,
      companyInfo: loginResult.company
    }
    if (remove) {
      $removeAttrs(loginResult, ['domAirDefaultSetup', 'intlAirDefaultSetup', 'hotelDefaultSetup',
        'carRentalDefaultSetup', 'trainDefaultSetup', 'trainGlobalDefaultSetup', 'otherDefaultSetup',
        'companyDefaultSetup', 'profileDefaultSetup', 'companyProducts', 'allProducts', 'company'])
    }
    return defaultSetupConfig
  }
}

export function $checkItemIndex (arr, target, key) {
  let result = -1
  if (arr && arr.length > 0 && target) {
    if (!key) {
      for (let i = 0; i < arr.length; i++) {
        if (arr[i] === target) {
          return i
        }
      }
    } else {
      let arrKeys = isArray(key) ? key : [key]
      forEach(arr, function (item, idx) {
        let _eq = true
        for (let i = 0; i < arrKeys.length; i++) {
          let _key = arrKeys[i]
          if (target[_key] !== item[_key]) {
            _eq = false
            break
          }
        }
        if (_eq) {
          result = idx
        }
      })
    }
  }
  return result
}

function $date (date, format) {
  if (date && moment(date).isValid()) {
    return moment(date).format(format)
  }
  return date
}

function $number (value, size) {
  const digits = []
  for (let i = 0; i < size; i++) {
    digits.push('0')
  }
  return numeral(value).format('0,0.' + digits.join(''))
}

function $refreshValue (key, defaultValue) {
  return new Promise((resolve, reject) => {
    let tp = get(this, key)
    let idx = key.indexOf('.')
    let nonRoot = idx > -1
    let rootKey
    defaultValue = isArray(tp) && !defaultValue ? [] : defaultValue
    if (nonRoot) {
      rootKey = key.substring(0, idx)
      key = key.substring(idx + 1, key.length)
      this.$set(this[rootKey], key, defaultValue)
    } else {
      this[key] = defaultValue
    }
    this.$nextTick(() => {
      if (nonRoot) {
        this.$set(this[rootKey], key, tp)
      } else {
        this[key] = tp
      }
      resolve(tp)
    })
  })
}

function $setSmartSelectValue (refName, value, valueKey = true) {
  let key = isString(valueKey) ? valueKey : (valueKey ? refName : '')
  const idx = key.indexOf('.')
  const nonRoot = idx > -1
  if (nonRoot) {
    let rootKey = key.substring(0, idx)
    key = key.substring(idx + 1, key.length)
    this.$set(this[rootKey], key, value)
  } else {
    this[key] = value
  }
  this.$refs[refName].f7SmartSelect.setValue(value)
}

function $removeAttrs (obj, keys = []) {
  keys.forEach(key => {
    delete obj[key]
  })
  return obj
}

function $copyAttrsByKeys (target, source, keys = [], remove) {
  keys.forEach(key => {
    target[key] = source[key]
  })
  if (remove) {
    $removeAttrs(source, keys)
  }
  return target
}

function $cardConfusion (cardNo, first = 4, last = 4) {
  return cardNo.replace(new RegExp(`(\\w{${first}}).*(\\w{${last}})$`), '$1****$2')
}

function $basicSearchParam () {
  return store.getters[OrderConsts.BASIC_SEARCH_PARAM_KEY]
}

function $processBookEnable (defaultSetup) {
  if (defaultSetup && $isSearchOnly()) {
    defaultSetup.bookEnable = false
  }
}

function $isSearchOnly () {
  const searchStatus = store.getters[OrderConsts.SEARCH_STATUS_KEY]
  return searchStatus && searchStatus.searchOnlyFlag
}

function $isPreloadRoute () {
  console.info(this.$f7router, this.$f7router.currentRoute.path, this.$f7route.path)
  return this.$f7router && this.$f7route && this.$f7router.currentRoute.path && this.$f7route.path && this.$f7router.currentRoute.path !== this.$f7route.path
}

const output = {
  $isCordova,
  $isOnlineSsoEnabled,
  $httpGet,
  $httpPost,
  $formValidate,
  $initFormValidate,
  $isLocale,
  $getSupportLocale,
  $getSupportLocales,
  $changeLocale,
  $coreShowLoading,
  $coreHideLoading,
  $coreAlert,
  $coreWarning,
  $coreError,
  $coreConfirm,
  $coreShowPopup,
  $coreShowSheet,
  $coreToast,
  $logout,
  $doLogout,
  $noop,
  $back,
  $goto,
  $backHomePage,
  $hasRole,
  $defaultSetup,
  $calcDefaultSetup,
  $checkItemIndex,
  $date,
  $number,
  $refreshValue,
  $gotoAxoTaPage,
  $removeAttrs,
  $copyAttrsByKeys,
  $cardConfusion,
  $basicSearchParam,
  $processBookEnable,
  $isSearchOnly,
  $isPreloadRoute,
  $setSmartSelectValue
}

function install (Vue, options = {}) {
  Object.assign(Vue.prototype, output)
}

export default Object.assign({
  install
}, output)
