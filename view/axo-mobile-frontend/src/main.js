// Vue
import Vue from 'vue'
// 国际化库
import vuexI18n from 'vuex-i18n'
// Framework7 UI框架
import Framework7 from 'framework7/framework7.esm.bundle.js'
import Framework7Vue from 'framework7-vue/framework7-vue.esm.bundle.js'
// import './assets/css/framework7.ios.css'
import './assets/css/v4/framework7.bundle.css'
import 'framework7-icons/css/framework7-icons.css'
import 'material-design-icons/iconfont/material-icons.css'
// 表单验证
import VeeValidate from 'vee-validate'
import validationEn from 'vee-validate/dist/locale/en'
import validationCn from 'vee-validate/dist/locale/zh_CN'
import VueAMap from 'vue-amap'
// HTTP请求工具
import axios from 'axios'
import VueLocalStorage from 'vue-localstorage'
// 自定义样式
import './assets/css/AppStyles.css'
// 自定义控件，以插件方式引入
import CommonPlugin from './components/common/common-plugin'
// APP定义，以及一些工具
import App from './App.vue'
import store from './store'
import AxoUtils, { $changeLocale, $isCordova } from './utils/AxoUtils'
import AxoFilters from './utils/AxoFilters'
import AxoMessages from './utils/AxoMessages'

Vue.use(VeeValidate, {
  events: '',
  dictionary: {
    en: validationEn,
    cn: validationCn
  },
  delay: 100
})

// Init F7-Vue Plugin
Framework7.use(Framework7Vue)

Vue.use(CommonPlugin)
Vue.use(vuexI18n.plugin, store)
Vue.use(VueLocalStorage)
Vue.use(AxoUtils)
Vue.use(AxoFilters)
Vue.use(AxoMessages)
Vue.use(VueAMap)
Vue.config.productionTip = false
let gateway = process.env.VUE_APP_API_GATEWAY
if (!gateway) {
  gateway = `${window.location.protocol}//${window.location.hostname}${window.location.port ? ':' + window.location.port : ''}`
}
axios.defaults.baseURL = gateway + process.env.VUE_APP_API_BASE_URL

$changeLocale(store.getters.globalConfig.currentLocale)

function initVueApp () {
  new Vue({
    store,
    render: h => h(App)
  }).$mount('#app')
}

if ($isCordova()) {
  document.addEventListener('deviceready', function () {
    initVueApp()
  })
} else {
  initVueApp()
}
