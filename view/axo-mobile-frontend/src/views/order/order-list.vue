<template>
  <f7-page :class="config.pageKey" page-content with-subnavbar :infinite="true"
           @infinite="onInfinite"
           :infinite-preloader="config.hasMoreOrder"
           :ptr="true" @ptr:refresh="onPtrRefresh">
    <f7-navbar>
      <f7-subnavbar>
        <template v-if="!approvePage">
          <common-dropdownfilter v-model="filterCondition.statusFilterConditions"
                                 :backdrop-container-el="'.' + config.pageKey"
                                 simple-mode
                                 close-on-click
                                 :close-link-text="$t('common.label.confirm')"
                                 :clear-link-text="$t('common.label.clear')">
            <f7-link slot="trigger" href="javascript:void(0)">{{$t('order.label.orderStatus')}}
              <f7-icon f7="chevron_down" size="16"/>
            </f7-link>
          </common-dropdownfilter>
          <common-dropdownfilter v-model="filterCondition.productFilterConditions"
                                 :backdrop-container-el="'.' + config.pageKey"
                                 simple-mode
                                 :close-link-text="$t('common.label.confirm')"
                                 :clear-link-text="$t('common.label.clear')">
            <f7-link slot="trigger" href="javascript:void(0)">{{$t('order.label.productType')}}
              <f7-icon f7="chevron_down" size="16"/>
            </f7-link>
          </common-dropdownfilter>
          <common-dropdownfilter v-if="filterCondition.otherFilterConditions.length>0"
                                 v-model="filterCondition.otherFilterConditions"
                                 :backdrop-container-el="'.' + config.pageKey"
                                 :close-link-text="$t('common.label.confirm')"
                                 :clear-link-text="$t('common.label.clear')">
            <f7-link slot="trigger" href="javascript:void(0)">{{$t('air.label.filter')}}
              <f7-icon f7="chevron_down" size="16"/>
            </f7-link>
          </common-dropdownfilter>
        </template>
        <template v-if="approvePage">
          <f7-segmented raised>
            <f7-button :key="orderStatus.code"
                       v-for="(orderStatus, index) in orderStatusList"
                       @click="searchTaParam.statusCode=orderStatus.code; config.showFilter=false"
                       :active="searchTaParam.statusCode === orderStatus.code"
                       v-if="index<config.statusLimit">
              {{$i18nMsg(orderStatus.nameCN, orderStatus.nameEN)}}
            </f7-button>
            <f7-button class="filterTrigger"
                       @click="config.showSearchCondition=!config.showSearchCondition">
              <f7-icon f7="search" class="search"/>
            </f7-button>
          </f7-segmented>
        </template>
        <common-dropdown :open="config.showSearchCondition"
                         @onClose="config.showSearchCondition=false"
                         :backdrop-container-el="'.' + config.pageKey"
                         :close-on-click="false">
          <f7-link slot="trigger">
            <f7-icon f7="search" class="search" v-if="!approvePage"/>
            <div v-if="approvePage">&nbsp;</div>
          </f7-link>
          <f7-list inline-labels form>
            <f7-list-input :label="$t('order.label.orderNo')"
                           :placeholder="$t('order.label.orderNo')"
                           :value="searchTaParam.taNo"
                           @input="searchTaParam.taNo=$event.target.value"
                           inline-label
                           input-style="text-align: right;"
                           class="item-link"></f7-list-input>
            <f7-list-input :label="$t('order.label.travellerName')"
                           :placeholder="$t('order.label.travellerName')"
                           :value="searchTaParam.travellerName"
                           @input="searchTaParam.travellerName=$event.target.value"
                           inline-label
                           input-style="text-align: right;"
                           class="item-link"></f7-list-input>
            <li>
              <common-datepicker
                :label="$t('order.label.createDateFrom')"
                inline-label
                show-clear
                :placeholder="$t('order.label.createDateFrom')"
                name="createDateFrom"
                input-class-name="text-align-right"
                class="item-link"
                v-model="searchTaParam.createDateFrom"></common-datepicker>
            </li>
            <li>
              <common-datepicker
                :label="$t('order.label.createDateTo')"
                inline-label
                show-clear
                :placeholder="$t('order.label.createDateTo')"
                :min-date="searchTaParam.createDateFrom"
                name="createDateTo"
                input-class-name="text-align-right"
                class="item-link"
                v-model="searchTaParam.createDateTo"></common-datepicker>
            </li>
            <li style="padding: 10px;border-top: 1px solid #ddd;margin-top: -1px;">
              <f7-row>
                <f7-col>
                  <f7-button @click="clearSearchOrderCondition()" fill color="gray">{{$t('common.label.clear')}}
                  </f7-button>
                </f7-col>
                <f7-col>
                  <f7-button @click="doSearchOrderCondition()" fill>
                    {{$t('common.label.search')}}
                  </f7-button>
                </f7-col>
              </f7-row>
            </li>
          </f7-list>
        </common-dropdown>
      </f7-subnavbar>
    </f7-navbar>
    <!--loading skeleton-->
    <div class="orderList" v-if="config.searching && searchOrderList.length===0">
      <!--<div class="orderList">-->
      <f7-list class="skeleton-text margin-top no-flex">
        <f7-list-item :key="n" v-for="n in 10">
          <f7-row>
            <f7-col width="40" class="data-table-title">
              000000000000
            </f7-col>
            <f7-col width="40">
              000000
            </f7-col>
            <f7-col width="20">
            </f7-col>
          </f7-row>
          <f7-row class="text-color-gray">
            <f7-col width="60" class="red">
              000000000000000000
            </f7-col>
            <f7-col width="40" class="text-clip">
              000000000000000000000
            </f7-col>
          </f7-row>
        </f7-list-item>
      </f7-list>
    </div>
    <f7-list class="margin-top no-flex">
      <f7-list-item v-if="!config.searching && searchOrderList.length===0">
        {{$t('common.msg.noresult')}}
      </f7-list-item>
      <f7-list-item v-for="order in searchOrderList"
                    :key="order.taNo"
                    @click="gotoTaDetail(order)"
                    link>
        <f7-row>
          <f7-col width="40" class="data-table-title">
            {{order.taNo}}
          </f7-col>
          <f7-col width="40">
            <order-status-tags :order="order"/>
          </f7-col>
          <f7-col width="20">
            <products-icons
              :dom-air="order.existDomAir"
              :intl-air="order.existIntlAir || order.existIntlFlight"
              :hotel="order.existHotel"
              :train="order.existTrain"
              :other="order.existOther"
              :car-rental="order.existCarRental"
              :offline="!!order.tcUserId"
            />
          </f7-col>
        </f7-row>
        <f7-row class="text-color-gray">
          <f7-col width="60" class="red">
            {{order.createDate|date('YYYY-MM-DD HH:mm')}}
          </f7-col>
          <f7-col width="40" class="text-clip">
            {{$i18nMsg(order.passengerNameCN, order.passengerNameEN)}}
          </f7-col>
        </f7-row>
      </f7-list-item>
    </f7-list>
  </f7-page>
</template>

<script>
import { mapGetters } from 'vuex'
import filter from 'lodash/filter'
import includes from 'lodash/includes'
import cloneDeep from 'lodash/cloneDeep'
import merge from 'lodash/merge'
import concat from 'lodash/concat'
import OrderService from '../../services/order/OrderService'
import ProductsIcons from '../../components/products/products-icons'
import OrderConsts, { ProductType, SearchProductMap } from '../../consts/OrderConsts'
import { calcConditionFilter } from '../../components/common/utils/common-utils'
import OrderStatusTags from './order-status-tags'

const APPROVE_STATUS_MAP = {
  N: '2,3',
  A: '4',
  R: '5'
}

export default {
  name: 'order-list',
  components: { OrderStatusTags, ProductsIcons },
  props: {
    approvePage: {
      type: Boolean,
      default: false
    },
    initPage: {
      type: Boolean,
      default: false
    },
    currentTab: {
      type: Object
    }
  },
  data () {
    const companyDefaultSetup = this.$defaultSetup(ProductType.Company)
    const companyProducts = this.$defaultSetup(ProductType.CompanyProducts)
    const { approvePage, currentTab } = this.$props
    const configOrderStatusList = approvePage ? companyDefaultSetup.approveOrderStatusList : companyDefaultSetup.companyOrderStatusList
    const pageKey = approvePage ? 'orderApprovePage' : 'orderPage'
    const statusLimit = approvePage ? 4 : 3
    const orderStatusList = [...filter(cloneDeep(configOrderStatusList), orderStatus => {
      orderStatus.selected = false
      orderStatus.code = approvePage ? APPROVE_STATUS_MAP[orderStatus.code] : orderStatus.code
      return !includes(['0', '3'], orderStatus.code)
    })]
    if (approvePage) {
      orderStatusList.push({
        code: '',
        nameCN: this.$t('common.label.all'),
        nameEN: this.$t('common.label.all'),
        selected: false
      })
    }
    const searchTaParam = OrderService.getSearchTaParam()
    const orderSearchStatus = this.$store.getters[OrderConsts.ORDER_SEARCH_STATUS_KEY]
    let filterCondition
    if (orderSearchStatus && orderSearchStatus.searchTaParam &&
      orderSearchStatus.currentOrderTab && orderSearchStatus.currentOrderTab.id === currentTab.id && this.$isPreloadRoute()) {
      merge(searchTaParam, orderSearchStatus.searchTaParam)
      filterCondition = orderSearchStatus.filterCondition
    } else {
      const statusFilterItems = orderStatusList.map((item, index) => {
        return {
          id: item.code,
          label: this.$i18nMsg(item.nameCN, item.nameEN),
          selected: approvePage && index === 0
        }
      })
      approvePage && (searchTaParam.statusCode = orderStatusList[0].code)
      const productFilterItems = companyProducts.map(item => {
        return { id: item.productId, label: this.$i18nMsg(item.prodNameCN, item.prodNameEN), selected: false }
      })
      const otherFilterConditions = approvePage ? [] : [{
        id: 'onHoldFilter',
        label: this.$t('order.label.onhold'),
        singleSelect: true,
        items: [{
          id: 'Y',
          label: this.$t('common.label.yes'),
          selected: false
        }, {
          id: 'N',
          label: this.$t('common.label.no'),
          selected: false
        }]
      }, {
        id: 'createModeFilter',
        label: this.$t('order.label.createChannel'),
        singleSelect: true,
        items: [{
          id: '1',
          label: this.$t('order.label.online'),
          selected: false
        }, {
          id: '2',
          label: this.$t('order.label.offline'),
          selected: false
        }]
      }]
      filterCondition = {
        productFilterConditions: [{
          id: 'productFilter',
          // label: this.$t('order.label.productType'),
          items: productFilterItems
        }],
        statusFilterConditions: [{
          id: 'statusFilter',
          // label: this.$t('order.label.orderStatus'),
          singleSelect: true,
          items: statusFilterItems
        }],
        otherFilterConditions
      }
    }
    return {
      config: {
        searching: false,
        hasMoreOrder: false,
        showFilter: false,
        pageKey,
        statusLimit,
        showSearchCondition: false
      },
      orderStatusList,
      searchTaParam,
      searchOrderList: [],
      filterCondition
    }
  },
  computed: {
    ...mapGetters(['loginResult', 'currentLoginUser'])
  },
  watch: {
    'searchTaParam.statusCode': function (value) {
      const { approvePage } = this.$props
      if (approvePage) {
        this.doSearchOrders(1)
      }
    },
    filterCondition: {
      handler: function (condition) {
        let productFilters = calcConditionFilter(condition.productFilterConditions, 'productFilter')
        let statusFilters = calcConditionFilter(condition.statusFilterConditions, 'statusFilter')
        let onHoldFilters = calcConditionFilter(condition.otherFilterConditions, 'onHoldFilter')
        let createModeFilters = calcConditionFilter(condition.otherFilterConditions, 'createModeFilter')
        console.info('condition change...', productFilters, statusFilters, onHoldFilters, createModeFilters)
        this.searchTaParam.statusCode = statusFilters.length > 0 ? statusFilters[0] : ''
        this.searchTaParam.issueNow = onHoldFilters.length > 0 ? onHoldFilters[0] : ''
        this.searchTaParam.createByMode = createModeFilters.length > 0 ? createModeFilters[0] : ''
        const allProducts = this.$defaultSetup(ProductType.AllProducts)
        allProducts.forEach(product => {
          const key = SearchProductMap[product.productType]
          if (key) {
            if (productFilters.indexOf(product.id) > -1) {
              this.searchTaParam[key] = true
            } else {
              delete this.searchTaParam[key]
            }
          }
        })
        this.doSearchOrders(1)
      },
      deep: true
    },
    initPage (v) {
      if (v && this.searchOrderList.length === 0) {
        this.doSearchOrders()
      }
    }
  },
  mounted () {
    const { initPage } = this.$props
    if (initPage) {
      this.doSearchOrders()
    }
  },
  methods: {
    onInfinite () {
      if (!this.config.searching && this.config.hasMoreOrder) {
        console.info('infinite.......................................')
        this.doSearchOrders(this.searchTaParam.pageSetting.pageNumber + 1, true)
      }
    },
    onPtrRefresh (event, done) {
      if (!this.config.searching) {
        console.info('ptr refresh .......................................')
        this.doSearchOrders(1).then(done)
      }
    },
    gotoTaDetail (order) {
      const { currentTab } = this.$props
      const searchTaParam = cloneDeep(this.searchTaParam)
      searchTaParam.pageSetting = undefined
      this.$store.dispatch('Order/storeOrderSearchStatus', {
        currentOrderTab: currentTab,
        searchTaParam,
        filterCondition: this.filterCondition
      })
      this.$goto(`/order/detail/${order.taNo}`)
    },
    clearSearchOrderCondition () {
      ['taNo', 'travellerName', 'createDateFrom', 'createDateTo'].forEach(key => {
        this.searchTaParam[key] = ''
      })
    },
    doSearchOrderCondition () {
      this.config.showSearchCondition = true
      this.$nextTick(() => {
        this.config.showSearchCondition = false
      })
      this.doSearchOrders(1)
    },
    doSearchOrders (pageNumber, append) {
      if (this.config.searching) {
        return
      }
      this.config.searching = true
      merge(this.searchTaParam, {
        userId: this.currentLoginUser.userId,
        companyCode: this.currentLoginUser.companyCode,
        pageSetting: {
          pageNumber
        }
      })
      this.searchTaParam.systemKey = this.searchTaParam.statusCode === '1' ? 'CONNECT' : ''
      const { approvePage } = this.$props
      const orderMethod = approvePage ? 'getApproveTAList' : 'getTAList'
      if (!append) {
        this.$$('.page-content').scrollTo(0, 0)
      }
      const preload = this.$isPreloadRoute()
      return OrderService[orderMethod](this.searchTaParam, { loading: !preload && !append }).then(data => {
        this.config.searching = false
        if (data && data.success && data.resultData) {
          Object.assign(this.searchTaParam.pageSetting, data.resultData.pageSetting || {})
          this.config.hasMoreOrder = data.resultData.pageSetting.pageNumber < data.resultData.pageSetting.pageCount
          if (!append) {
            this.searchOrderList = data.resultData.taList || []
          } else {
            this.searchOrderList = concat(this.searchOrderList, data.resultData.taList || [])
          }
        }
      })
    }
  }
}
</script>

<style scoped>

</style>
