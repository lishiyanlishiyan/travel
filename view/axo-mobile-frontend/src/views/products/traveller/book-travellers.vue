<template>
  <f7-page>
    <f7-navbar :back-link="$t('common.label.back')">
      <f7-nav-title>
        <template v-if="continueBookFlag">
          {{$t('order.label.continueBook')}}({{continueOrderNo}})
        </template>
        <template v-else>
          <span v-if="currentTravellerTab">
            {{$t(currentTravellerTab.label)}}
          </span>
          <span v-if="!currentTravellerTab">
            {{$t('common.label.selectTraveller')}}
          </span>
        </template>
      </f7-nav-title>
      <common-home/>
      <f7-subnavbar v-if="!continueBookFlag && travellerTabs.filter(tab=>tab.enabled).length>1">
        <f7-segmented raised>
          <f7-button :key="travellerTab.id" @click="currentTravellerTab=travellerTab"
                     :active="currentTravellerTab===travellerTab" v-for="travellerTab in travellerTabs">
            {{$t(travellerTab.label)}}
          </f7-button>
        </f7-segmented>
      </f7-subnavbar>
    </f7-navbar>
    <template v-if="!continueBookFlag && currentTravellerTab">
      <traveller-select v-if="travellerTabConfig.internalTab && travellerTabConfig.internalTab.enabled"
                        v-show="currentTravellerTab === travellerTabConfig.internalTab"
                        v-model="travellerTabConfig.internalTab"/>
      <external-order-select v-if="travellerTabConfig.externalTab && travellerTabConfig.externalTab.enabled"
                             :product-type="currentProductType"
                             v-show="currentTravellerTab === travellerTabConfig.externalTab"
                             v-model="travellerTabConfig.externalTab"/>
      <f7-list v-if="selectedCreators.length>1">
        <f7-list-item :title="$t('common.label.policyTraveller')" smart-select
                      :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
          <select
            v-if="selectedCreators"
            v-validate="'required'"
            :data-vv-name="$t('common.label.policyTraveller')"
            name="policyUserId" v-model="currentTravellerTab.policyUserId">
            <option :key="selectedCreator.uuid" :value="selectedCreator.userId"
                    :selected="selectedCreator.userId===currentTravellerTab.policyUserId"
                    v-for="(selectedCreator) in selectedCreators">
              {{$i18nMsg(selectedCreator.userNameCN, selectedCreator.userNameEN)}}
            </option>
          </select>
        </f7-list-item>
      </f7-list>
    </template>
    <continue-travellers v-if="continueBookFlag" :value="basicSearchParam"/>
    <f7-list v-if="errorMessage">
      <li>
        <common-form-errors :value="errorMessage"/>
      </li>
    </f7-list>
    <f7-block v-if="config.bookButton">
      <f7-row>
        <f7-col>
          <f7-button large raised fill @click="toBookProduct(false)" :disabled="!!errorMessage">
            {{config.searchOnlyButton ? $t('common.label.searchAndBook') : $t('common.label.next')}}
          </f7-button>
        </f7-col>
      </f7-row>
    </f7-block>
    <f7-block v-if="config.searchOnlyButton">
      <f7-row class="margin-bottom" v-if="currentTravellerTab.id==='externalTab'">
        <f7-col class="text-align-center">{{$t('common.msg.searchOnly')}}</f7-col>
      </f7-row>
      <f7-row>
        <f7-col>
          <f7-button large raised fill color="purple" @click="toBookProduct(true)">
            {{$t('common.label.searchOnly')}}
          </f7-button>
        </f7-col>
      </f7-row>
    </f7-block>
  </f7-page>
</template>

<script>
import { mapGetters } from 'vuex'
import TravellerSelect from './traveller-select'
import { isEmpty } from 'lodash'
import ProductService from '../../../services/products/ProductService'
import OrderConsts, { ProductType } from '../../../consts/OrderConsts'
import ExternalOrderSelect from './external-order-select'
import OrderService from '../../../services/order/OrderService'
import ContinueTravellers from './continue-travellers'
import ExternalOrderService from '../../../services/order/ExternalOrderService'

const travellerTabConfigKey = 'Order/travellerTabConfig'
const continueOrderNoKey = 'Order/continueOrderNo'
const ssoExternalOrderNoKey = 'Order/ssoExternalOrderNo'

export default {
  name: 'book-travellers',
  components: { ContinueTravellers, ExternalOrderSelect, TravellerSelect },
  data () {
    const currentProductType = this.$f7route.params.productType
    const companyDefaultSetup = this.$defaultSetup(ProductType.Company)
    const continueOrderNo = this.$store.getters[continueOrderNoKey]
    const travellerTabConfig = this.$store.getters[travellerTabConfigKey]
    console.info(companyDefaultSetup)
    return {
      config: {
        bookButton: true,
        searchOnlyButton: false
      },
      selectedCreators: [],
      basicSearchParam: {
        policyUserId: '',
        policyUser: null,
        bookTravellers: [],
        externalOrderNo: '',
        taNo: ''
      },
      travellerTabs: [],
      currentTravellerTab: null,
      companyDefaultSetup,
      travellerTabConfig,
      currentProductType,
      errorMessage: '',
      continueBookFlag: !!continueOrderNo,
      continueOrderNo
    }
  },
  computed: {
    ...mapGetters(['currentLoginUser', travellerTabConfigKey, OrderConsts.SEARCH_STATUS_KEY])
  },
  watch: {
    basicSearchParam: {
      handler (v) {
        this.validateTravellerForm()
        this.$store.dispatch('Order/storeBasicSearchParam', v)
      },
      deep: true
    },
    currentTravellerTab: {
      handler (tab) {
        if (!this.continueBookFlag) {
          this.changeTravellerTab(tab)
        }
      },
      deep: true
    },
    travellerTabConfig: {
      handler (config) {
        if (!this.continueBookFlag) {
          console.info('save tab config.................', config)
          this.$store.dispatch('Order/storeTravellerTabConfig', config)
        }
      },
      deep: true
    }
  },
  methods: {
    changeTravellerTab (tab) {
      this.travellerTabConfig.currentTravellerTabId = tab.id
      this.selectedCreators = tab.selectedCreators
      this.basicSearchParam.bookTravellers = tab.selectedTravellers
      this.basicSearchParam.policyUserId = tab.policyUserId
      this.basicSearchParam.externalOrderNo = tab.selectedExternalOrder ? tab.selectedExternalOrder.externalOrderNo : ''
      this.calcPolicyUser(this.selectedCreators)
      this.$refreshValue('selectedCreators')
      this.config.bookButton = this.checkSearchButtonStatus(this.currentProductType)
      this.config.searchOnlyButton = this.checkSearchButtonStatus(this.currentProductType, true)
    },
    toBookProduct (searchOnly) {
      const ssoExternalOrderNo = this.$store.getters[ssoExternalOrderNoKey]
      if (!this.basicSearchParam.externalOrderNo && ssoExternalOrderNo) {
        this.basicSearchParam.externalOrderNo = ssoExternalOrderNo
      }
      this[OrderConsts.SEARCH_STATUS_KEY].searchOnlyFlag = searchOnly
      if (!searchOnly) {
        ProductService.validateBookTravellerBillingType(this.currentProductType, this.basicSearchParam.bookTravellers, this.currentLoginUser.companyCode).then(() => {
          this.checkExternalOrderAutoInput()
        })
      } else {
        this.checkExternalOrderAutoInput()
      }
    },
    checkExternalOrderAutoInput () {
      const externalOrderAutoInputKey = 'Order/storeExternalOrderAutoInput'
      this.$store.dispatch(externalOrderAutoInputKey)
      const externalOrderNo = this.calcExternalOrderNo()
      if (this.companyDefaultSetup.externalOrderEnable && this.companyDefaultSetup.externalOrderAutoInput && externalOrderNo) {
        ExternalOrderService.getExternalOrderAutoInput({
          companyCode: this.currentLoginUser.companyCode,
          taNo: this.continueOrderNo,
          externalOrderNo: externalOrderNo
        }).then(data => {
          if (data && data.success && data.resultData && !isEmpty(data.resultData.autoInputDto)) {
            this.$store.dispatch(externalOrderAutoInputKey, data.resultData.autoInputDto)
          }
          this.internalToBookProduct()
        })
      } else {
        this.internalToBookProduct()
      }
    },
    calcExternalOrderNo () {
      if (this.continueBookFlag) {
        return this.basicSearchParam.externalOrderNo
      } else if (this.currentTravellerTab && this.currentTravellerTab === this.travellerTabConfig.externalTab &&
        this.currentTravellerTab.selectedExternalOrder) {
        return this.currentTravellerTab.selectedExternalOrder.externalOrderNo
      }
    },
    internalToBookProduct () {
      const productType = this.$f7route.params.productType
      this.$goto(`/search/${productType}`)
    },
    newExternalTab (currentLoginTraveller) {
      return {
        id: 'externalTab',
        label: 'order.label.externalBook',
        selectedExternalOrder: null,
        selectedTravellers: [],
        selectedCreators: [currentLoginTraveller],
        policyUserId: currentLoginTraveller.userId
      }
    },
    calcBookTravellers () {
      const travellerTabConfig = this[travellerTabConfigKey] || this.travellerTabConfig
      const currentLoginTraveller = ProductService.getTravellerByUserBasic(this.currentLoginUser)
      let externalTab = travellerTabConfig.externalTab || this.newExternalTab(currentLoginTraveller)
      const internalTab = travellerTabConfig.internalTab || {
        id: 'internalTab',
        label: 'order.label.internalBook',
        selectedTravellers: [currentLoginTraveller],
        selectedCreators: [currentLoginTraveller],
        policyUserId: currentLoginTraveller.userId
      }
      if (!ExternalOrderService.checkExternalOrderProduct(externalTab.selectedExternalOrder, this.currentProductType)) {
        externalTab = this.newExternalTab(currentLoginTraveller)
      }
      if (this.companyDefaultSetup.externalOrderEnable && this.companyDefaultSetup.externalOrderSelectType === '1') {
        travellerTabConfig.externalTab = externalTab
        this.travellerTabs.push(externalTab)
        this.selectedExternalOrder = externalTab.selectedExternalOrder
      }
      if (!this.companyDefaultSetup.externalOrderEnable || this.companyDefaultSetup.externalOrderSelectTabs !== '1') {
        travellerTabConfig.internalTab = internalTab
        this.travellerTabs.push(internalTab)
      }
      this.calcExternalEnabledProducts(externalTab, internalTab)
      this.travellerTabConfig = travellerTabConfig
      this.calcCurrentTravellerTab()
    },
    calcPolicyUser (creators) {
      const policyUserId = this.currentTravellerTab.policyUserId
      const policyUser = ProductService.reCalcPolicyUser(creators, policyUserId)
      this.basicSearchParam.policyUser = policyUser
      this.basicSearchParam.policyUserId = this.basicSearchParam.policyUserId = policyUser ? policyUser.userId : null
    },
    calcExternalEnabledProducts (externalTab, internalTab) {
      const customExternalEnabledProducts = this.companyDefaultSetup.customExternalEnabledProducts
        ? this.companyDefaultSetup.customExternalEnabledProducts.split(',') : null
      if (this.currentProductType) {
        if (this.travellerTabs.length === 2 && customExternalEnabledProducts) {
          externalTab.enabled = this.$checkItemIndex(customExternalEnabledProducts, this.currentProductType) > -1
          internalTab.enabled = !externalTab.enabled
        } else {
          externalTab.enabled = internalTab.enabled = true
        }
      }
    },
    calcCurrentTravellerTab () {
      let currentTabId
      let lastTabId = this.travellerTabConfig.currentTravellerTabId
      this.travellerTabs.filter(tab => tab.enabled).forEach((tab, index) => {
        if (index === 0 || (lastTabId && lastTabId === tab.id)) {
          currentTabId = tab.id
        }
      })
      this.currentTravellerTab = this.travellerTabConfig[currentTabId]
    },
    validateTravellerForm () {
      let errorMessage
      if (!this.continueBookFlag) {
        if (!this.basicSearchParam.externalOrderNo && this.currentTravellerTab === this.travellerTabConfig.externalTab) {
          errorMessage = this.$t('order.msg.selectExternalOrder')
        } else if (this.basicSearchParam.bookTravellers.length === 0) {
          errorMessage = this.$t('common.msg.selectTraveller')
        }
      }
      this.errorMessage = errorMessage
      return !errorMessage
    },
    loadTaContinueBook () {
      OrderService.internalGetTAContinueBook({ taNo: this.continueOrderNo }).then(data => {
        console.info(data)
        if (data && data.success && data.resultData) {
          const result = data.resultData
          Object.assign(this.basicSearchParam, {
            policyUserId: result.policyUser.userId,
            policyUser: result.policyUser,
            bookTravellers: result.travellers,
            taNo: this.continueOrderNo,
            externalOrderNo: result.taDetail ? result.taDetail.externalLinkNo : ''
          })
        }
      })
    },
    checkSearchButtonStatus (productType, searchOnly) {
      const defaultSetup = this.$defaultSetup(productType)
      if (productType === ProductType.Others) {
        return !searchOnly
      }
      if (searchOnly) {
        return this.currentTravellerTab === this.travellerTabConfig.externalTab || !defaultSetup.bookEnable || defaultSetup.searchOnlyEnable
      } else {
        return defaultSetup.bookEnable
      }
    }
  },
  mounted () {
    if (!this.continueBookFlag) {
      this.calcBookTravellers()
    } else {
      this.loadTaContinueBook()
    }
  }
}
</script>

<style scoped>

</style>
