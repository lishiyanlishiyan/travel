<template>
  <div>
    <f7-list>
      <li>
        <common-autocomplete
          class="item-link"
          input-class-name="text-align-right"
          :label="$t('order.label.externalOrder')"
          :inline-label="true"
          :page-title="$t('order.label.selectExternalOrder')"
          :placeholder="$t('order.label.selectExternalOrder')"
          value-property="externalOrderNo"
          text-property="externalOrderNo"
          :text-property-fun="textPropertyFun"
          v-model="travellerTab.selectedExternalOrder"
          @change="selectExternalOrder($event)"
          :close-on-select="true"
          :request-source-on-open="true"
          input-events="search"
          :opened="toSelectExternalOrder"
          @closed="toSelectExternalOrder=false"
          auto-focus
          :autocompleteConfig="selectExternalOrderConfig"/>
      </li>
    </f7-list>
    <f7-list media-list>
      <f7-list-item
        swipeout
        :key="traveller.uuid"
        link
        v-for="traveller in travellerTab.selectedTravellers"
        :subtitle="traveller.userEmail">
        <div class="item-title" slot="title">
          {{$i18nMsg(traveller.userNameCN, traveller.userNameEN)}}
          <f7-chip v-if="traveller.tempUserId" :text="$t('profile.label.guest')" color="blue"/>
        </div>
        <f7-swipeout-actions right>
          <f7-swipeout-button @click="removeExternalOrder(traveller)" delete>
            {{$t('common.label.delete')}}
          </f7-swipeout-button>
        </f7-swipeout-actions>
      </f7-list-item>
    </f7-list>
  </div>
</template>

<script>
import ExternalOrderService from '../../../services/order/ExternalOrderService'
import ProductService from '../../../services/products/ProductService'
import { ProductType } from '../../../consts/OrderConsts'

export default {
  name: 'external-order-select',
  model: {
    event: 'change'
  },
  props: {
    value: {
      type: Object
    },
    productType: {
      type: String,
      default: ProductType.DomAir
    }
  },
  data () {
    const currentLoginUser = this.$store.getters.currentLoginUser
    const { value, productType } = this.$props
    const selectExternalOrderConfig = {
      keyWordKey: 'filterStr',
      searchMethod: ExternalOrderService.getAutoExternalOrders({
        companyCode: currentLoginUser.companyCode,
        needDetails: true,
        creatorId: currentLoginUser.userId,
        existAir: [ProductType.DomAir, ProductType.IntlAir].indexOf(productType) > -1,
        existHotel: productType === ProductType.Hotel,
        existTrain: productType === ProductType.Train
      })
    }
    return {
      travellerTab: value,
      selectExternalOrderConfig,
      toSelectExternalOrder: false,
      currentLoginUser
    }
  },
  watch: {
    value: {
      handler (v) {
        this.travellerTab = v
      },
      deep: true
    }
  },
  mounted () {
  },
  methods: {
    textPropertyFun (item) {
      if (item.data) {
        const data = item.data
        return `
          <div class="item-title-row">
            <div class="item-title">
            ${data.externalOrderNo}
            </div>
          </div>
          <div class="item-subtitle">
              ${this.$i18nMsg(data.traveller.nameCN, data.traveller.nameEN)}
              ${data.traveller.email ? `(${data.traveller.email})` : ''}
          </div>
        `
      } else {
        return `<div class="item-title">${item.text}</div>`
      }
    },
    internalSelectExternalOrder (externalOrder) {
      this.travellerTab.selectedExternalOrder = externalOrder
      const loginTraveller = ProductService.getTravellerByUserBasic(this.currentLoginUser)
      this.travellerTab.selectedTravellers = []
      this.travellerTab.selectedCreators = [loginTraveller]
      if (externalOrder && externalOrder.traveller) {
        const externalTraveller = ProductService.getTravellerByUserBasic(externalOrder.traveller, this.currentLoginUser)
        this.travellerTab.selectedTravellers.push(externalTraveller)
        if (this.currentLoginUser.userId !== externalTraveller.userId) {
          this.travellerTab.selectedCreators.push(externalTraveller)
          this.travellerTab.policyUserId = externalTraveller.userId
        }
      }
      this.onChange()
    },
    selectExternalOrder (externalOrder) {
      if (externalOrder) {
        if (!this.travellerTab.selectedExternalOrder || externalOrder.externalOrderNo !== this.travellerTab.selectedExternalOrder.externalOrderNo) {
          ProductService.validateTraveller(externalOrder.travellerId, 1).then(() => {
            this.internalSelectExternalOrder(externalOrder)
          }, this.validateTravellerError)
        } else {
          this.internalSelectExternalOrder(externalOrder)
        }
      }
    },
    removeExternalOrder () {
      this.internalSelectExternalOrder()
    },
    validateTravellerError (data) {
      return this.$coreError(this.$i18nMsg(data.msgCN, data.msgEN))
    },
    onChange () {
      this.$emit('change', this.travellerTab)
    }
  }
}
</script>

<style scoped>

</style>
