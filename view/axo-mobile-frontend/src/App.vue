<template>
  <!-- App -->
  <f7-app :params="f7params">
    <!-- Statusbar -->
    <f7-statusbar></f7-statusbar>
    <!-- Main View -->
    <f7-view id="main-view" url="/" main></f7-view>
  </f7-app>
</template>

<script>
// Import Routes
import Vue from 'vue'
import moment from 'moment'
import routes from './routes'
import { initF7DefaultConfig, calcF7Language } from './utils/F7Utils'
import { $coreHideLoading } from './utils/AxoUtils'

const INDEX_FIX = ['/android_asset/www/index.html', '/index.html']

export default {
  data () {
    let self = this
    return {
      // Framework7 parameters here
      f7params: {
        id: 'com.citsgbt.axo-mobile', // App bundle ID
        name: process.env.VUE_APP_APP_NAME, // App name
        theme: 'ios', // ios,md,auto
        // App routes
        routes: routes,
        view: {
          pushState: true,
          pushStateSeparator: '#!',
          iosSwipeBack: self.$isCordova(),
          preloadPreviousPage: true,
          routesBeforeEnter: function (to, from, resolve, reject) {
            console.info('route to.........', to.path)
            if (INDEX_FIX.indexOf(to.path) > -1) {
              this.navigate('/', { pushState: true, reloadCurrent: true })
              reject()
              return
            }
            if ((self.isBeforeLogin() || self.isSessionTimeout()) && (to.path !== '/login' && !to.path.startsWith('/sso'))) {
              if (self.isSessionTimeout()) {
                self.$coreAlert(self.$t('login.msg.sessionTimeout')).then(() => {
                  self.$doLogout()
                  reject()
                })
              } else {
                this.navigate('/login', { pushState: true, reloadCurrent: true })
                reject()
              }
            } else {
              $coreHideLoading()
              resolve()
            }
          }
        }
      }
    }
  },
  methods: {
    isBeforeLogin () {
      const globalConfig = this.$store.getters.globalConfig
      return !globalConfig.isLoginIn
    },
    isSessionTimeout () {
      const sessionInfo = this.$store.getters.sessionInfo
      return !this.isBeforeLogin() && (!sessionInfo.expires || moment(sessionInfo.expires).isBefore(moment()) || sessionInfo.forceExpire)
    }
  },
  mounted () {
    this.$f7ready(f7 => {
      Vue.f7 = f7
      initF7DefaultConfig(f7)
      calcF7Language(f7, this.$store.getters.globalConfig.currentLocale)
    })
  }
}
</script>
