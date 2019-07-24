import cn from './messages_cn'
import en from './messages_en'
import store from '../store'

export function $i18nMsg (cn, en) {
  const globalConfig = store.getters.globalConfig
  return globalConfig.currentLocale === 'zh_CN' ? cn : (en || cn)
}

function install (Vue, options = {}) {
  Vue.i18n.add('cn', cn)
  Vue.i18n.add('en', en)
  Vue.$i18nBundle = Vue.i18n.translate
  Vue.$i18nMsg = $i18nMsg
  Object.assign(Vue.prototype, { $i18nMsg, $i18nBundle: Vue.$i18nBundle })
}

export default Object.assign({
  install
})
