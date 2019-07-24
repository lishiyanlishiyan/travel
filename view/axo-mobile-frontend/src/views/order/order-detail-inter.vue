<template>
  <f7-page page-content>
    <template v-if="order && order.taNo">
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')" back-link-url="/order"/>
      <f7-nav-title>{{$t('common.label.order')}}: {{order.taNo}}-{{$t('air.label.intl')}}</f7-nav-title>
    </f7-navbar>
    <f7-list  :key="productOrder.id" v-for="(productOrder,i) in order.newIntlAirList">
      <div class="orderDetail">
        <div v-if="productOrder.packageType==='NORMAL' && productOrder.lockFlag !==1">
          <order-detail-intlp2p :intl-air="productOrder" :cabins="cabinOptions" :order="order"/>
        </div>
        <div v-else-if="productOrder.packageType==='REQUEST' && productOrder.optionList && getSelectedOption(productOrder)">
          <order-detail-intlp2p :intl-air="getSelectedOption(productOrder)" :cabins="cabinOptions" :order="order"/>
        </div>
        <div v-else>
          <order-detail-intloption :intl-option="productOrder" :cabins="cabinOptions" :order="order"/>
        </div>
        <order-product-button :current-btns="productBtn[i]" :product-order="productOrder" :ref-name="'intlAirAction'+i"/>
      </div>
    </f7-list>
    </template>
  </f7-page>
</template>
<script>
import OrderDetailIntloption from './order-detail-intloption'
import { mapGetters } from 'vuex'
import { ProductType } from '../../consts/OrderConsts'
import OrderDetailIntlp2p from './order-detail-intlp2p'
import OrderProductButton from './order-product-button'
import OrderService from '../../services/order/OrderService'
export default {
  name: 'order-detail-inter',
  components: { OrderProductButton, OrderDetailIntlp2p, OrderDetailIntloption },
  data () {
    let order = {}
    let productBtn = []
    const intlDefaultSetup = this.$defaultSetup(ProductType.IntlAir)
    const cabinOptions = OrderService.getCabinOptions(intlDefaultSetup.selectableCabinStr, this)
    return { order, cabinOptions, productBtn }
  },
  computed: {
    ...mapGetters(['Order/orderDetail', 'currentLoginUser'])
  },
  methods: {
    getSelectedOption (intlAir) {
      return OrderService.getSelectedOption(intlAir)
    },
    initIntlBtn (order, intlAir) {
      let self = this
      let intlBtns = []
      if (order.orderButton.currentCreatorFlag && order.statusCode < 2 && order.historyFlag !== 1 && intlAir.lockFlag !== 1) {
        intlBtns.push({
          title: self.$t('common.label.delete'),
          click: self.deleteIntlAir,
          color: 'red'
        })
      }
      if ((intlAir.packageType === 'REQUEST' || intlAir.packageType === 'NORMAL') && intlAir.emailFlag === 'Y' && !order.tcUserId && intlAir.optionList) {
        if (self.getSelectedOption(intlAir)) {
          intlBtns.push({
            title: self.$t('air.label.viewOption'),
            click: self.viewAllIntlOption,
            color: 'blue'
          })
        } else {
          intlBtns.push({
            title: self.$t('air.label.selectOption'),
            click: self.toSelectOption,
            color: 'blue'
          })
        }
      }
      if (self.showOptionView(intlAir) && intlAir.status === 2 && intlAir.optionList && intlAir.optionList.length > 0 && order.orderButton.currentCreatorFlag) {
        intlBtns.push({
          title: self.$t('air.label.selectOption'),
          click: self.toSelectOption,
          color: 'blue'
        })
      }
      return intlBtns
    },
    viewAllIntlOption (option) {
      let self = this
      self.$goto('/order/selectOption/' + option.taNo + '/' + option.id + '/0')
    },
    showOptionView (intlAir) {
      OrderService.showOptionView(intlAir, this.order, this.intlQCMode)
    },
    toSelectOption (option) {
      let self = this
      self.$goto('/order/selectOption/' + option.taNo + '/' + option.id + '/1')
    },
    deleteIntlAir (intlAir) {
      let self = this
      self.$coreConfirm(self.$t('order.msg.cancel')).then(function () {
        let { userId, companyCode } = self.currentLoginUser
        let param = {
          taNo: intlAir.taNo,
          companyCode,
          userId
        }
        OrderService.cancelIntlOrder(param).then(function (data) {
          if (data && data.success) {
            window.location.reload()
          } else if (data) {
            self.$coreError(data.message)
          }
        })
      })
    }
  },
  mounted () {
    let self = this
    self.order = self['Order/orderDetail']
    if (self.order && self.order.newIntlAirList) {
      for (let i = 0; i < self.order.newIntlAirList.length; i++) {
        let intlAir = self.order.newIntlAirList[i]
        self.productBtn[i] = self.initIntlBtn(self.order, intlAir)
      }
    } else {
      self.$back()
    }
  }
}
</script>

<style scoped>

</style>
