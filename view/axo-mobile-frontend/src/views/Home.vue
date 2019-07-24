<template>
  <f7-page :page-content="false" class="home">
    <f7-toolbar tabbar labels class="homeToolbar">
      <f7-link :key="homeTab.name"
               v-for="homeTab in homeTabs"
               tab-link
               @click="clickHome(homeTab)"
               :href="homeTab.url"
               :route-tab-id="'index-' + homeTab.name"
               :tab-link-active="currentTab===homeTab"
               :icon-f7="homeTab.icon"
               :text="homeTab.label"></f7-link>
    </f7-toolbar>
    <f7-tabs routable>
      <f7-tab id="index-products" :tab-active="currentTab.name==='products'"/>
      <f7-tab id="index-order" :tab-active="currentTab.name==='order'"/>
      <f7-tab id="index-profile" :tab-active="currentTab.name==='profile'"/>
    </f7-tabs>
  </f7-page>
</template>

<script>
// @ is an alias to /src
import fromPairs from 'lodash/fromPairs'
import pathToRegexp from 'path-to-regexp'

export default {
  name: 'Home',
  data () {
    const homeTabs = [{
      name: 'products',
      icon: 'home_fill',
      label: this.$i18nBundle('common.label.index'),
      url: '/'
    }, {
      name: 'order',
      icon: 'document_fill',
      label: this.$i18nBundle('common.label.order'),
      url: '/order'
    }, {
      name: 'profile',
      icon: 'person',
      label: this.$i18nBundle('common.label.my'),
      url: '/profile'
    }]
    const homePathMap = fromPairs(homeTabs.map(tab => [tab.url, tab]))
    const currentTab = homePathMap[this.$f7route.path] || homeTabs[0]
    return {
      isBottom: true,
      homeTabs,
      currentTab
    }
  },
  mounted () {
    // fixme 处理tab路由history问题
    this.$f7router.on('tabMounted', (newTabEl, tabRoute) => {
      const history = this.$f7router.view.history
      if (this.checkTabHistory(history, tabRoute)) {
        this.$f7router.clearPreviousHistory()
      }
    })
  },
  methods: {
    checkTabHistory (history, tabRoute) {
      if (history && history.length > 1 && pathToRegexp(tabRoute.path).exec(this.$f7route.path)) {
        return !pathToRegexp(tabRoute.path).exec(history[0])
      }
    },
    clickHome (homeTab) {
      if (this.$f7route.path !== homeTab.path) {
        this.$f7router.refreshPage() // 继续预定的时候点击首页没反应问题
      }
    }
  }
}
</script>
