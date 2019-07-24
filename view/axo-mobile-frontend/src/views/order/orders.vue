<template>
  <f7-page :page-content="false">
    <f7-navbar>
      <f7-toolbar tabbar>
        <f7-link :key="orderTab.id"
                 :id="`order-tab-${orderTab.id}`"
                 v-for="orderTab in orderTabs"
                 :tab-link="'#index-order-' + orderTab.id"
                 :tab-link-active="currentOrderTab===orderTab"
                 :text="orderTab.label"></f7-link>
      </f7-toolbar>
    </f7-navbar>
    <f7-tabs animated>
      <f7-page-content
        @tabShow="currentOrderTab=orderTabMap.list"
        tab id="index-order-list" :tab-active="currentOrderTab===orderTabMap.list">
        <order-list :init-page="currentOrderTab===orderTabMap.list" :current-tab="orderTabMap.list"/>
      </f7-page-content>
      <f7-page-content
        v-if="$hasRole('APPROVER')"
        @tabShow="currentOrderTab=orderTabMap.approve"
        :tab-active="currentOrderTab===orderTabMap.approve"
        tab id="index-order-approve">
        <order-list approve-page :init-page="currentOrderTab===orderTabMap.approve" :current-tab="orderTabMap.approve"/>
      </f7-page-content>
    </f7-tabs>
  </f7-page>
</template>

<script>
import OrderList from './order-list'
import find from 'lodash/find'
import fromPairs from 'lodash/fromPairs'
import OrderConsts from '../../consts/OrderConsts'

console.info('into order')
export default {
  name: 'order',
  components: { OrderList },
  data () {
    let orderTabs = [{
      id: 'list',
      label: this.$i18nBundle('order.label.myOrder')
    }]
    if (this.$hasRole('APPROVER')) {
      orderTabs.push({
        id: 'approve',
        label: this.$i18nBundle('order.label.approveOrder')
      })
    }
    const orderTabMap = fromPairs(orderTabs.map(orderTab => [orderTab.id, orderTab]))
    return {
      config: {},
      orderTabs,
      orderTabMap,
      currentOrderTab: null
    }
  },
  mounted () {
    let currentOrderTab = this.orderTabs[0]
    const orderSearchStatus = this.$store.getters[OrderConsts.ORDER_SEARCH_STATUS_KEY]
    if (orderSearchStatus && orderSearchStatus.currentOrderTab && this.$isPreloadRoute()) {
      currentOrderTab = find(this.orderTabs, { id: orderSearchStatus.currentOrderTab.id }) || currentOrderTab
    }
    this.currentOrderTab = currentOrderTab
    if (this.currentOrderTab && this.currentOrderTab !== this.orderTabs[0]) { // 修复默认选中后面的tab显示不正常的问题
      this.$$(`#order-tab-${this.currentOrderTab.id}`).click()
    }
  },
  methods: {}
}
</script>

<style scoped>

</style>
