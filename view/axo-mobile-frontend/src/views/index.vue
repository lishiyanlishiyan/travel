<template>
  <f7-page login-screen class="home">
    <f7-login-screen-title>{{$t('common.label.indexTitle')}}</f7-login-screen-title>
    <f7-block>
      <f7-list>
        <f7-list-item link="/news" v-if="totalNewsCount>0">
          <div slot="title">
            {{$t('common.label.newsLabel')}}
            <f7-badge color="purple">{{totalNewsCount}}</f7-badge>
          </div>
        </f7-list-item>
        <f7-list-item link @click="gotoOnlineSso()" v-if="$isOnlineSsoEnabled()">
          <div slot="title">
            {{$t('common.label.desktopVersion')}}
          </div>
        </f7-list-item>
      </f7-list>
      <f7-list form :key="product.id" v-for="product in supportedProducts">
        <f7-list-item class="button button-fill button-large button-raised"
                      :class="product.productType"
                      :disabled="product.disabled"
                      :link="'/book-travellers/' + product.productType"
                      :title="$i18nMsg(product.nameCN, product.nameEN)"/>
      </f7-list>
    </f7-block>
  </f7-page>
</template>

<script>
import ProductService from '../services/products/ProductService'
import { mapGetters } from 'vuex'
import find from 'lodash/find'
import cloneDeep from 'lodash/cloneDeep'
import { ProductType } from '../consts/OrderConsts'
import LoginService from '../services/login/LoginService'
import AdminNewsService from '../services/news/AdminNewsService'
import OrderService from '../services/order/OrderService'

export default {
  name: 'index',
  data () {
    return {
      supportedProducts: [],
      totalNewsCount: 0
    }
  },
  computed: {
    ...mapGetters(['loginResult', 'newsCounts'])
  },
  mounted () {
    this.loadIndexNews()
    LoginService.checkAndLoadDefaultSetup()
    const taNo = this.$f7route.params.taNo
    this.$store.dispatch('Order/storeContinueOrderNo')
    if (taNo) {
      OrderService.internalGetTABasic({ taNo }).then(data => {
        console.info(data && data.success && data.resultData && data.resultData.taDetail)
        if (data && data.success && data.resultData && data.resultData.taDetail) {
          this.$store.dispatch('Order/storeContinueOrderNo', taNo)
          const order = data.resultData.taDetail
          let supportedProducts = this.processProducts()
          supportedProducts.forEach(product => {
            if ((product.productType === ProductType.IntlAir && (order.existIntlAir || order.existIntlFlight)) ||
              (product.productType === ProductType.Others && order.existOther)) {
              product.disabled = true
            }
          })
          this.supportedProducts = supportedProducts
        }
      })
    } else {
      this.supportedProducts = this.processProducts()
    }
  },
  methods: {
    gotoOnlineSso: LoginService.gotoOnlineSso,
    processProducts () {
      let companyProducts = cloneDeep(this.$defaultSetup(ProductType.CompanyProducts))
      let supportedProducts = []
      if (companyProducts) {
        supportedProducts = ProductService.getProducts().filter(product => {
          return !!find(companyProducts, { productId: product.id })
        })
        supportedProducts.forEach(product => {
          product.disabled = false
        })
      }
      return supportedProducts
    },
    loadIndexNews () {
      const newsParam = AdminNewsService.getNewsListParam(1)
      const lang = newsParam.language
      this.totalNewsCount = this.newsCounts[lang]
      if (!this.newsCounts[`${lang}Loaded`]) {
        AdminNewsService.getNewsList(newsParam, { loading: false }).then(data => {
          if (data && data.success && data.resultData && data.resultData.pageSetting) {
            this.totalNewsCount = data.resultData.pageSetting.totalCount
            this.storeNewsCounts(lang, this.totalNewsCount)
          }
        })
      }
    },
    storeNewsCounts (lang, totalCount) {
      const newsCounts = {}
      newsCounts[`${lang}Loaded`] = true
      newsCounts[`${lang}`] = totalCount
      this.$store.dispatch('storeNewsCounts', newsCounts)
    }
  }
}
</script>

<style scoped>

</style>
