<template>
  <f7-page page-content>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')" back-link-url="/order"/>
      <f7-nav-title>{{$t('common.label.order')}}: {{order.taNo}}-{{$t('common.label.train')}}</f7-nav-title>
    </f7-navbar>
    <div>
      {{$t('common.label.train')}}
    </div>
  </f7-page>
</template>
<script>

import OrderProductButton from './order-product-button'
import { mapGetters } from 'vuex'
import OrderService from '../../services/order/OrderService'

export default {
  name: 'order-detail-train',
  components: { OrderProductButton },
  computed: {
    ...mapGetters(['Order/orderDetail', 'currentLoginUser'])
  },
  data () {
    let order = {}
    let productBtn = []
    let showPsgrFlag = false
    return { order, productBtn, showPsgrFlag }
  },
  methods: {},
  mounted () {
    let self = this
    self.order = self['Order/orderDetail']
    if (self.order && self.order.trainList) {

    } else {
      self.$back()
    }
  }
}
</script>

<style scoped>

</style>
