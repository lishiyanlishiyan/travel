<template>
  <f7-page :page-content="config.mapMode">
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"
                   back-link-force></f7-nav-left>
      <f7-nav-title>
        <span v-if="config.city">
          {{$i18nMsg(config.city.nameCN, config.city.nameEN)}}
        </span>
        <span v-if="searchParam.hotelQueryParam">
          {{searchParam.hotelQueryParam.checkinDate|date('MM/DD')}}-{{searchParam.hotelQueryParam.checkoutDate|date('MM/DD')}}
          <f7-chip v-if="searchParam.today" :text="$t('hotel.label.today')" color="red"/>
        </span>
      </f7-nav-title>
      <f7-nav-right>
        <f7-link :icon-material="config.mapMode?'format_list_bulleted':'map'"
                 @click="config.mapMode=!config.mapMode; calcMapPositions()"/>
      </f7-nav-right>
      <f7-subnavbar>
        <common-dropdown>
          <f7-link slot="trigger">{{currentTab.label}}
            <f7-icon f7="chevron_down" size="16"/>
          </f7-link>
          <f7-list>
            <f7-list-item :key="hotelTab.id" v-for="hotelTab in hotelTabs" name="hotelTab" radio
                          :checked="currentTab === hotelTab"
                          @change="currentTab = hotelTab"
                          :title="hotelTab.label"></f7-list-item>
          </f7-list>
        </common-dropdown>
        <common-dropdown>
          <f7-link slot="trigger">
            {{currentTab.orderBy.label || $t('air.label.sort')}}
            <f7-icon f7="chevron_down" size="16"/>
          </f7-link>
          <f7-list>
            <f7-list-item :key="sortCondition.id" v-for="sortCondition in currentTab.sortConditions"
                          name="orderBy" radio
                          v-show="sortCondition.enabled"
                          :checked="currentTab.orderBy===sortCondition"
                          @change="currentTab.orderBy=sortCondition; currentTab.conditionChange = true"
                          :title="sortCondition.label"></f7-list-item>
          </f7-list>
        </common-dropdown>
        <common-dropdownfilter v-model="filterConditions"
                               :close-link-text="$t('common.label.confirm')"
                               :clear-link-text="$t('common.label.clear')">
          <f7-link slot="trigger" href="javascript:void(0)">{{$t('air.label.filter')}}
            <f7-icon f7="chevron_down" size="16"/>
          </f7-link>
        </common-dropdownfilter>
        <common-dropdown :open="config.showFilterCondition" :close-on-click="false">
          <f7-link slot="trigger">
            <f7-icon f7="search" class="search"/>
          </f7-link>
          <f7-list>
            <li>
              <hotel-name-filter :value="searchParam"/>
            </li>
            <li>
              <hotel-address-filter :value="searchParam" :city="config.city"
                                    :company-address-enable="hotelDefaultSetup.enableCompanyAddressSelect"/>
            </li>
            <!--<f7-list-button @click="doSearchHotelLocation()">
              {{$t('common.label.search')}}
            </f7-list-button>-->
            <li style="padding: 10px;border-top: 1px solid #ddd;">
              <f7-button @click="doSearchHotelLocation()" fill>{{$t('common.label.search')}}</f7-button>
            </li>
          </f7-list>
        </common-dropdown>
      </f7-subnavbar>
    </f7-navbar>
    <f7-tabs v-show="!config.mapMode">
      <f7-tab :key="hotelTab.id" :tab-active="currentTab===hotelTab" v-for="hotelTab in hotelTabs">
        <f7-page :infinite="true"
                 @infinite="onInfinite"
                 :infinite-preloader="currentTab.hasMoreHotel"
                 :ptr="true" @ptr:refresh="onPtrRefresh">
          <hotel-city-policy v-model="cityPolicyStr"/>
          <!--loading skeleton-->
          <f7-list media-list class="skeleton-text" v-if="config.searching && currentTab.items.length===0">
            <f7-list-item
              :key="n"
              v-for="n in 10"
              title="Hotel Name Hotel Name"
              subtitle="Hotel Stars"
              text="Hotel Address Hotel Address Hotel Address Hotel Address">
              <f7-skeleton-block style="width: 85px; height: 85px;" slot="media"></f7-skeleton-block>
            </f7-list-item>
          </f7-list>
          <f7-list media-list>
            <f7-list-item v-if="!config.searching && currentTab && currentTab.items.length===0">
              {{$t('common.msg.noresult')}}
            </f7-list-item>
            <f7-list-item link :key="item.id" v-for="(item, index) in currentTab.items"
                          @click="doSearchRoom(item)"
                          :text="$i18nMsg(item.addressCN, item.addressEN)">
              <div slot="title">
                <f7-badge color="blue">{{index + 1}}</f7-badge>
                {{$i18nMsg(item.nameCN, item.nameEN)}}
              </div>
              <span slot="subtitle">
                <f7-icon :key="n" size="small" color="orange" v-for="n in item.stars||0" f7="star_fill"/>
                <f7-chip v-if="item.corporate" :text="$t('common.label.corp')" color="blue"/>
                <f7-chip v-if="isQiantaoPrepay(item, hotelDefaultSetup)" :text="$t('hotel.label.prepay')"
                         color="purple"/>
              </span>
              <img slot="media" :src="`${axoDomain}/${item.picture||''}`"
                   width="85" height="85"/>
              <div slot="footer">
                <f7-row>
                  <f7-col v-if="item.distance">
                    <span v-if="item.distance<1">
                      {{$t('hotel.msg.distanceMetre', [$number(item.distance * 1000, 0)])}}
                    </span>
                    <span v-if="item.distance>=1">
                      {{$t('hotel.msg.distanceKilometre', [$number(item.distance, 2)])}}
                    </span>
                  </f7-col>
                  <f7-col class="text-color-orange text-align-right">
                    <strong>
                  <span v-if="$isLocale('en_US')">
                    {{$t('hotel.label.atLeast')}}
                  </span>
                      {{item.currency}}
                      <span class="data-table-title">
                    {{item.minRate|number(0)}}
                  </span>
                      <span v-if="$isLocale('zh_CN')">
                    {{$t('hotel.label.atLeast')}}
                  </span>
                    </strong>
                  </f7-col>
                </f7-row>
              </div>
            </f7-list-item>
          </f7-list>
        </f7-page>
      </f7-tab>
    </f7-tabs>
    <el-amap vid="HotelMap" :events="hotelMap.mapEvents"
             v-if="config.mapMode || hotelMap.map" v-show="config.mapMode"
             :plugin="hotelMap.plugin"
             :amap-manager="hotelMap.amapManager" :lang="$i18nMsg('zh_cn', 'en')">
      <el-amap-info-window
        v-if="hotelMap.current"
        :position="hotelMap.current.position"
        :visible="hotelMap.current.showInfo"
        :auto-move="false"
        :close-when-click-map="true"
        :offset="[0, -25]">
        <div style="width: 250px;">
          <a @click="doSearchRoom(hotelMap.current.hotel)">
            <f7-row>
              <f7-col>
                <f7-chip color="blue" :text="hotelMap.current.index"></f7-chip>
                {{$i18nMsg(hotelMap.current.hotel.nameCN, hotelMap.current.hotel.nameEN)}}
              </f7-col>
            </f7-row>
            <f7-row>
              <f7-col>
                <f7-icon :key="n" size="small" color="orange" v-for="n in hotelMap.current.hotel.stars||0"
                         f7="star_fill"/>
                <f7-chip v-if="hotelMap.current.hotel.corporate" :text="$t('common.label.corp')" color="blue"/>
              </f7-col>
            </f7-row>
            <f7-row v-if="hotelMap.current.hotel.distance">
              <f7-col>
                  <span v-if="hotelMap.current.hotel.distance<1">
                      {{$t('hotel.msg.distanceMetre', [$number(hotelMap.current.hotel.distance * 1000, 0)])}}
                    </span>
                <span v-if="hotelMap.current.hotel.distance>=1">
                      {{$t('hotel.msg.distanceKilometre', [$number(hotelMap.current.hotel.distance, 2)])}}
                    </span>
              </f7-col>
            </f7-row>
            <f7-row>
              <f7-col>
                <div class="text-color-orange text-align-right">
                  <strong>
                  <span v-if="$isLocale('en_US')">
                    {{$t('hotel.label.atLeast')}}
                  </span>
                    {{hotelMap.current.hotel.currency}}
                    <span class="data-table-title">
                    {{hotelMap.current.hotel.minRate|number(0)}}
                  </span>
                    <span v-if="$isLocale('zh_CN')">
                    {{$t('hotel.label.atLeast')}}
                  </span>
                  </strong>
                </div>
              </f7-col>
            </f7-row>
          </a>
        </div>
      </el-amap-info-window>
    </el-amap>
    <common-fab :position="config.fabPosition" :buttons="fabButtons"/>
  </f7-page>
</template>

<script>
import moment from 'moment'
import { mapGetters } from 'vuex'
import merge from 'lodash/merge'
import cloneDeep from 'lodash/cloneDeep'
import concat from 'lodash/concat'
import startsWith from 'lodash/startsWith'
import VueAMap from 'vue-amap'
import { ProductType } from '../../../consts/OrderConsts'
import HotelService from '../../../services/hotel/HotelService'
import HotelCityPolicy from '../../../components/products/hotel/hotel-city-policy'
import AmapMapService from '../../../services/map/AmapMapService'
import HotelNameFilter from './hotel-name-filter'
import HotelAddressFilter from './hotel-address-filter'
import { calcConditionFilter } from '../../../components/common/utils/common-utils'

const searchParamKey = 'Hotel/hotelSearchParam'

export default {
  name: 'hotel-search-result',
  components: { HotelNameFilter, HotelAddressFilter, HotelCityPolicy },
  data () {
    const self = this
    const hotelDefaultSetup = this.$defaultSetup(ProductType.Hotel)
    const companyDefaultSetup = this.$defaultSetup(ProductType.Company)
    this.$processBookEnable(hotelDefaultSetup)
    console.info(hotelDefaultSetup)
    // hotelDefaultSetup.qiantaoHotelPrepayEnable = true
    // hotelDefaultSetup.customerFavoriteTabIsOnOff = true
    const searchStatus = cloneDeep(this.$store.getters['Hotel/hotelSearchStatus'])
    let searchParam = searchStatus ? searchStatus.searchParam : undefined
    let city = searchStatus ? searchStatus.config.city : undefined
    let hotelTabs = searchStatus ? searchStatus.hotelTabs : []
    let currentTab = searchStatus ? searchStatus.currentTab : undefined
    let filterConditions = searchStatus ? searchStatus.filterConditions : []
    let cityBudget = searchStatus ? searchStatus.cityBudget : undefined
    let cityPolicyStr = searchStatus ? searchStatus.cityPolicyStr : ''
    if (!searchStatus) {
      searchParam = cloneDeep(this.$store.getters[searchParamKey])
      searchParam.policyApplied = true
      city = searchParam.hotelType === 'D' ? searchParam.domCity : searchParam.intlCity
      searchParam.domCity = searchParam.intlCity = null
      const tabConfig = searchParam.hotelType === 'D' ? hotelDefaultSetup.tabConfig : hotelDefaultSetup.tabConfigIntl
      hotelTabs = HotelService.getHotelTabs(tabConfig, hotelDefaultSetup.customerFavoriteTabIsOnOff)
      currentTab = hotelTabs[0]
      filterConditions = [{
        id: 'starFilter',
        label: this.$i18nBundle('hotel.label.stars'),
        items: HotelService.getHotelStars()
      }]
      searchParam.hotelQueryParam.onlyQianTaoHotelPrepay = hotelDefaultSetup.qiantaoHotelPrepayEnable && searchParam.hotelType === 'D'
      hotelTabs.forEach(hotelTab => {
        hotelTab.sortConditions = HotelService.getSortConditions(hotelTab.resultType, hotelDefaultSetup.recommendFlagEnable)
        hotelTab.orderBy = hotelTab.sortConditions.filter(item => item.enabled)[0]
      })
    } else {
      currentTab.items = []
    }
    const config = searchStatus ? searchStatus.config : {
      searching: false,
      city,
      mapMode: false,
      showFilterCondition: false,
      fabPosition: 'right-bottom'
    }
    const fabButtons = this.calcFabButtons(hotelDefaultSetup, searchParam.hotelQueryParam.checkinDate)
    return {
      isBackPage: !!searchStatus,
      config,
      hotelMap: {
        amapManager: new VueAMap.AMapManager(),
        hotelMarkers: [],
        current: null,
        map: null,
        mapEvents: {
          init (map) {
            self.hotelMap.map = map
            self.calcMapMarkers()
          }
        },
        plugin: [{
          pName: 'Scale'
        }, {
          pName: 'ToolBar',
          position: 'LB',
          useNative: true
        }]
      },
      axoDomain: process.env.VUE_APP_AXO_DOMAIN,
      searchParam,
      resultData: null,
      hotelTabs,
      currentTab,
      filterConditions,
      cityBudget,
      cityPolicyStr,
      hotelDefaultSetup,
      companyDefaultSetup,
      fabButtons
    }
  },
  computed: {
    ...mapGetters(['currentLoginUser'])
  },
  watch: {
    currentTab (tab) {
      console.info(tab, tab.conditionChange)
      if (tab.items.length === 0 || tab.conditionChange) {
        this.doSearchHotel(1)
      }
      this.calcMapPositions()
    },
    'currentTab.orderBy': function (orderBy) {
      console.info('order by change.......', orderBy, this.currentTab.conditionChange)
      if (this.currentTab.conditionChange) {
        this.doSearchHotel(1)
      }
    },
    filterConditions: {
      handler: function (v) {
        console.info('filter condition change ....')
        let starFilters = calcConditionFilter(v, 'starFilter')
        let priceFilters = calcConditionFilter(v, 'priceFilter')
        let prepayFilters = calcConditionFilter(v, 'prepayFilter')
        this.searchParam.hotelQueryParam.stars = starFilters.join(',')
        this.searchParam.hotelQueryParam.queryByCityBudget = priceFilters.length && priceFilters[0] === 'cityBudget'
        this.searchParam.hotelQueryParam.onlyQianTaoHotelPrepay = prepayFilters.length === 0 || prepayFilters.indexOf('prepay') > -1
        this.searchParam.hotelQueryParam.onlyHotelSelfPrepay = prepayFilters.length === 0 || prepayFilters.indexOf('selfpay') > -1
        const prices = priceFilters.length && priceFilters[0] !== 'cityBudget' ? priceFilters[0].split('-') : []
        this.searchParam.hotelQueryParam.minPrice = prices[0]
        this.searchParam.hotelQueryParam.maxPrice = prices[1]
        this.hotelTabs.forEach(tab => {
          tab.conditionChange = true
        })
        this.doSearchHotel(1)
      },
      deep: true
    },
    'searchParam.hotelQueryParam': {
      handler () {
        console.info('searchParam change//////////////////////')
        this.hotelTabs.forEach(tab => {
          tab.sortConditions.forEach(item => {
            if (startsWith(item.id, 'distance')) {
              item.enabled = !!this.searchParam.hotelQueryParam.location
            }
          })
          tab.conditionChange = true
        })
      },
      deep: true
    }
  },
  mounted () {
    this.doSearchHotel(1, this.isBackPage, true)
    if (!this.isBackPage) {
      this.calcFilterCondition()
    }
  },
  methods: {
    newPosition (position, hotel, index) {
      const self = this
      const point = { hotel, position, index, showInfo: false }
      return Object.assign(point, {
        click () {
          self.hotelMap.current = point
          self.hotelMap.current.showInfo = false
          self.$nextTick(() => {
            self.hotelMap.current.showInfo = true
            self.hotelMap.map.setCenter(position)
          })
          console.info('click................', hotel)
        }
      })
    },
    calcMapPositions () {
      if (this.config.mapMode) {
        AmapMapService.initAmapService().then(() => {
          const points = []
          const hotelInfos = []
          this.currentTab.items.forEach((hotel, index) => {
            if (hotel.position || (hotel.longitude && hotel.latitude)) {
              const position = hotel.position ? hotel.position : [hotel.longitude, hotel.latitude]
              points.push(this.newPosition(position, hotel, index + 1))
            } else if (this.config.city.ctryCode === 'CN' && hotel.addressCN) {
              hotelInfos.push({
                hotel,
                index: index + 1
              })
            }
          })
          this.hotelMap.hotelMarkers = points
          if (hotelInfos.length > 0) {
            AmapMapService.getPointsByHotels(this.config.city.nameCN, hotelInfos).then(results => {
              this.hotelMap.hotelMarkers = points.concat(results.map(hotelInfo =>
                this.newPosition(hotelInfo.hotel.position, hotelInfo.hotel, hotelInfo.index)))
              this.calcMapMarkers()
            })
          }
          this.calcMapMarkers()
        }, error => {
          console.info(error)
        })
      }
    },
    calcMapMarkers () {
      if (window.AMapUI && window.AMap) {
        window.AMapUI.loadUI(['overlay/SimpleMarker'], SimpleMarker => {
          const map = this.hotelMap.amapManager.getMap()
          map.remove(map.getAllOverlays())
          let center
          if (this.searchParam.hotelQueryParam.location) {
            const location = this.searchParam.hotelQueryParam.location
            const position = [location.lng, location.lat]
            const marker = new SimpleMarker({
              iconStyle: 'green',
              iconLabel: {
                innerHTML: 'A',
                style: {
                  color: '#fff'
                }
              },
              position
            })
            marker.setMap(map)
            center = position
          }
          this.hotelMap.hotelMarkers.forEach(hotelMarker => {
            try {
              const opts = this.$isCordova() ? { iconStyle: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png' } : {}
              const marker = new SimpleMarker(Object.assign(opts, {
                iconLabel: {
                  innerHTML: hotelMarker.index,
                  style: {
                    color: '#fff'
                  }
                },
                position: hotelMarker.position
              }))
              marker.setMap(map)
              marker.on('click', hotelMarker.click)
              center = center || hotelMarker.position
            } catch (e) {
              console.error('position format error', hotelMarker.position)
            }
          })
          if (center) {
            this.$nextTick(() => {
              map.setCenter(center)
              map.setFitView()
            })
          } else { // 获取城市中心
            AmapMapService.getPointByAddress(this.config.city.nameCN, this.config.city.nameCN).then(position => {
              if (position) {
                map.setCenter([position.lng, position.lat])
                map.setFitView()
              }
            })
          }
        })
      }
    },
    calcFabButtons (hotelDefaultSetup, checkinDate) {
      const fabButtons = []
      const self = this
      if (hotelDefaultSetup.allowNonCorporateRequest) {
        if (!hotelDefaultSetup.oldDateRequestOnly && moment(checkinDate).diff(moment().startOf('d'), 'd') >= 0) {
          fabButtons.push({
            label: self.$t('hotel.label.fillNoCorpRequest'),
            click () {
              self.toBookRequestHotel(null, 1)
            },
            f7: 'arrow_right'
          })
        }
        if (hotelDefaultSetup.oldDateRequestEnable) {
          fabButtons.push({
            label: self.$t('hotel.label.fillHotelRecord'),
            click () {
              self.toBookRequestHotel(null, 3)
            },
            f7: 'arrow_right'
          })
        }
      }
      return fabButtons
    },
    calcSortCondition (orderBy) {
      const orders = orderBy.id.split(/\s+/)
      const field = orders[0]
      const sortType = orders[1]
      const sortCondition = {
        rateSort: field === 'price' ? sortType : '',
        starSort: field === 'stars' ? sortType : '',
        distanceSort: field === 'distance' ? sortType : '',
        orderByRateFirstly: field === 'price',
        orderByStarFirstly: field === 'stars',
        orderByDistanceFirstly: field === 'distance',
        orderByBookingTimesFirstly: field === 'bookingTimes',
        orderByRecommendLevelSortFirstly: field === 'recommendLevel'
      }
      Object.assign(this.searchParam.hotelQueryParam, sortCondition)
    },
    calcFilterCondition () {
      const budgetParam = {
        companyCode: this.searchParam.companyCode,
        userId: this.searchParam.policyUserId,
        ctryCode: this.config.city.ctryCode,
        cityCode: this.config.city.code
      }
      HotelService.loadHotelCityBudget(budgetParam, { loading: false }).then((data) => {
        if (data && data.resultData && data.resultData.cityBudget) {
          this.cityBudget = data.resultData.cityBudget
        }
        const priceFilter = {
          id: 'priceFilter',
          singleSelect: true,
          label: this.$i18nBundle('hotel.label.price'),
          items: HotelService.getHotelPrices(!!this.cityBudget)
        }
        this.filterConditions.push(priceFilter)
        this.checkAndAddPrepayFilter()
        this.cityPolicyStr = HotelService.calcCityPolicy(this.cityBudget, this.hotelDefaultSetup)
      })
    },
    checkAndAddPrepayFilter () {
      const hotelDefaultSetup = this.hotelDefaultSetup
      if (hotelDefaultSetup.qiantaoHotelPrepayEnable) {
        this.filterConditions.push({
          id: 'prepayFilter',
          label: this.$i18nBundle('hotel.label.bookMode'),
          items: [{
            id: 'prepay',
            label: this.$i18nBundle('hotel.label.prepay'),
            selected: this.searchParam.hotelType === 'D'
          }, {
            id: 'selfpay',
            label: this.$i18nBundle('hotel.label.selfpay'),
            selected: this.searchParam.hotelType === 'D'
          }]
        })
      }
    },
    onInfinite () {
      if (!this.config.searching && this.currentTab.hasMoreHotel) {
        console.info('infinite.......................................')
        this.doSearchHotel(this.searchParam.pageSetting.pageNumber + 1, true)
      }
    },
    onPtrRefresh (event, done) {
      if (!this.config.searching) {
        console.info('ptr refresh .......................................')
        this.doSearchHotel(1).then(done)
      }
    },
    doSearchHotelLocation () {
      const isDistanceSort = startsWith(this.currentTab.orderBy.id, 'distance')
      if (this.searchParam.hotelQueryParam.location) {
        if (!isDistanceSort) {
          this.currentTab.orderBy = this.currentTab.sortConditions.filter(item => startsWith(item.id, 'distance'))[0]
        }
      } else if (isDistanceSort) {
        this.currentTab.orderBy = this.currentTab.sortConditions.filter(item => item.enabled)[0]
      }
      this.config.showFilterCondition = true
      this.$nextTick(() => {
        this.config.showFilterCondition = false
      })
      this.doSearchHotel(1)
    },
    doSearchHotel (pageNumber, append, mounted) {
      if (this.config.searching) {
        return
      }
      this.calcSortCondition(this.currentTab.orderBy)
      this.config.searching = true
      merge(this.searchParam, {
        hotelQueryParam: {
          resultType: this.currentTab.resultType
        },
        pageSetting: {
          pageNumber
        }
      })
      console.info(this.currentTab.label, ' search................ ', this.searchParam.hotelQueryParam)
      this.currentTab.conditionChange = false
      if (!append) {
        this.$$('.page-content').scrollTo(0, 0)
      }
      return HotelService.getHotelMasterList(this.searchParam, { loading: !append }).then(data => {
        this.config.searching = false
        Object.assign(this.searchParam.pageSetting, data.resultData.pageSetting || {})
        this.currentTab.hasMoreHotel = data.resultData.pageSetting.pageNumber < data.resultData.pageSetting.pageCount
        if (!append) {
          this.currentTab.items = data.resultData.hotelMasters || []
        } else {
          this.currentTab.items = concat(this.currentTab.items, data.resultData.hotelMasters || [])
        }
        this.calcMapPositions()
        if (!append && mounted && this.currentTab.resultType === '1' && this.currentTab.items.length === 0) {
          const otherTab = this.hotelTabs.filter(hotelTab => hotelTab.id === 'other')[0] // 其他酒店Tab开启状态
          if (otherTab) {
            this.currentTab = otherTab
          }
        }
      })
    },
    storeHotelSearchStatus () {
      this.$store.dispatch('Hotel/storeHotelSearchStatus', {
        searchParam: this.searchParam,
        config: this.config,
        hotelTabs: this.hotelTabs,
        currentTab: this.currentTab,
        filterConditions: this.filterConditions,
        cityPolicyStr: this.cityPolicyStr,
        cityBudget: this.cityBudget
      })
    },
    doSearchRoom (hotel) {
      console.info(hotel)
      const searchRoomPricesParam = HotelService.getRoomPricesParam(this.searchParam)
      Object.assign(searchRoomPricesParam.roomQueryBasic, {
        hotelId: hotel.id,
        onlyCorporateRoom: this.currentTab.resultType === '1',
        onlyAmexCorporateRoom: this.currentTab.resultType === '2',
        corporateHotel: hotel.corporate === 1,
        amexCorporateHotel: hotel.amexCorporate === 1
      })
      HotelService.searchRoomPrices(searchRoomPricesParam).then(data => {
        if (data && data.resultData && data.resultData.hotelRooms && data.resultData.hotelRooms.length) {
          this.$store.dispatch('Hotel/storeHotelDetailParam', {
            searchParam: this.searchParam,
            city: this.config.city,
            hotel,
            hotelRooms: data.resultData.hotelRooms,
            lowestRoom: data.resultData.lowestRoom,
            cityPolicyStr: this.cityPolicyStr,
            cityBudget: this.cityBudget,
            searchRoomPricesParam
          })
          this.storeHotelSearchStatus()
          this.$goto('/hotel/hotelDetail')
        } else {
          this.$coreError(this.$t('hotel.msg.noRoomSearch'), { title: this.$t('hotel.msg.noRoomTitle') })
        }
        console.info(data)
      })
    },
    validateBeforeBook (hotelRoom) {
      if (!this.hotelDefaultSetup.bookEnable || (this.companyDefaultSetup.bookEnableNeedExternalOrder && !this.searchParam.externalOrderNo)) {
        this.$coreError(this.$t('common.msg.bookDisabled'))
        return false
      }
      return true
    },
    toBookRequestHotel (requestRoom, requestHotelType) { // 是否是补单
      const hotelRoom = {
        currencyCode: 'CNY',
        averagePrice: undefined,
        totalPrice: 0
      }
      if (this.config.city.countryCode !== 'CN') {
        if (this.cityBudget && this.cityBudget.currency) {
          hotelRoom.currencyCode = this.cityBudget.currency
        } else {
          hotelRoom.currencyCode = null
        }
      }
      requestHotelType = requestRoom ? 2 : requestHotelType
      const hotelRoomDetailParam = Object.assign({}, cloneDeep({
        searchParam: this.searchParam,
        city: this.config.city,
        cityPolicyStr: this.cityPolicyStr,
        cityBudget: this.cityBudget,
        requestHotelType,
        hotelRoom,
        hotel: null
      }))
      if (this.validateBeforeBook(hotelRoom)) {
        this.$store.dispatch('Hotel/storeHotelRoomDetailParam', hotelRoomDetailParam)
        this.storeHotelSearchStatus()
        this.$goto('/hotel/toBookHotel')
      }
    },
    isQiantaoPrepay: HotelService.isQiantaoPrepay
  }
}
</script>

<style scoped>

</style>
