<template>
  <f7-page page-content with-subnavbar>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"></f7-nav-left>
      <f7-nav-title>
        <div v-if="searchDomFlightParam">
          {{$i18nMsg(searchDomFlightParam.searchDomFlight.departPort.cityNameCN,
          searchDomFlightParam.searchDomFlight.departPort.cityNameEN)}}
          <f7-icon f7="arrow_right"/>
          {{$i18nMsg(searchDomFlightParam.searchDomFlight.arrivalPort.cityNameCN,
          searchDomFlightParam.searchDomFlight.arrivalPort.cityNameEN)}}
        </div>
        <div v-if="!searchDomFlightParam">
          {{$t('air.label.dom')}}
        </div>
      </f7-nav-title>
      <common-home/>
      <f7-subnavbar>
        <common-dropdown>
          <f7-link slot="trigger">{{domAirCurrentTab.label}}
            <f7-icon f7="chevron_down" size="16"/>
          </f7-link>
          <f7-list>
            <f7-list-item :key="domAirTab.id" v-for="domAirTab in domAirTabs" name="domAirTab" radio
                          :checked="domAirCurrentTab === domAirTab"
                          @change="domAirCurrentTab = domAirTab"
                          :title="domAirTab.label"></f7-list-item>
          </f7-list>
        </common-dropdown>
        <common-dropdown>
          <f7-link slot="trigger">
            {{config.orderBy.label || $t('air.label.sort')}}
            <f7-icon f7="chevron_down" size="16"/>
          </f7-link>
          <f7-list>
            <f7-list-item :key="sortCondition.id" v-for="sortCondition in sortConditions"
                          name="orderBy" radio
                          :checked="config.orderBy===sortCondition"
                          @change="config.orderBy=sortCondition"
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
      </f7-subnavbar>
    </f7-navbar>
    <!--loading skeleton-->
    <div class="airList" v-if="config.searching && showDomAirFlights.length===0">
      <f7-list inset class="skeleton-text margin no-flex" :key="n" v-for="n in 10">
        <f7-list-item>
          <f7-row no-gap>
            <f7-col width="80">
              <f7-row>
                <f7-col width="25" class="data-table-title">
                  00:00
                </f7-col>
                <f7-col class="text-align-center" width="25">
                  <f7-icon f7="arrow_right"/>
                </f7-col>
                <f7-col width="50" class="data-table-title">
                  00:00
                </f7-col>
              </f7-row>
              <f7-row class="text-color-gray item-subtitle">
                <f7-col width="50">
                  DepAirportName
                </f7-col>
                <f7-col width="50">
                  ArrAirportName
                </f7-col>
              </f7-row>
              <f7-row class="text-color-gray item-subtitle">
                <f7-col width="100">
                  Airways and Flight No XX0000
                </f7-col>
              </f7-row>
            </f7-col>
            <f7-col width="20">
              <f7-row>
                <f7-col class="data-table-title text-align-right">
                  0000000
                </f7-col>
              </f7-row>
            </f7-col>
          </f7-row>
        </f7-list-item>
      </f7-list>
    </div>
    <div class="airList" :key="domAirTab.id" v-if="domAirCurrentTab===domAirTab" v-for="domAirTab in domAirTabs">
      <f7-list inset class="margin no-flex"
               v-if="!domAirFlight.filtered"
               v-for="(domAirFlight, index) in showDomAirFlights"
               :key="index">
        <f7-list-item @click="$set(domAirFlight, 'showFlightCabins', !!!domAirFlight.showFlightCabins)">
          <f7-row no-gap>
            <f7-col width="80">
              <f7-row>
                <f7-col width="25" class="data-table-title">
                  {{domAirFlight.deptDate|date('HH:mm')}}
                </f7-col>
                <f7-col class="text-align-center" width="25">
                  <f7-icon f7="arrow_right"/>
                </f7-col>
                <f7-col width="50" class="data-table-title">
                  {{domAirFlight.arrivalDate|date('HH:mm')}}
                </f7-col>
              </f7-row>
              <f7-row class="text-color-gray item-subtitle">
                <f7-col width="50">
                  {{$i18nMsg(domAirFlight.deptCityNameCN, domAirFlight.deptCityNameEN)}}
                  {{domAirFlight.departTerm}}
                </f7-col>
                <f7-col width="50">
                  {{$i18nMsg(domAirFlight.arrivalCityNameCN, domAirFlight.arrivalCityNameEN)}}
                  {{domAirFlight.arrivalTerm}}
                </f7-col>
              </f7-row>
              <f7-row class="text-color-gray item-subtitle">
                <f7-col width="100">
                  {{$i18nMsg(domAirFlight.carrierNameCN, domAirFlight.carrierNameEN)}}
                  <f7-chip v-if="domAirFlight.corporateAirline==='T'" :text="$t('common.label.corp')" color="blue"/>
                  |
                  {{domAirFlight.flightCarrier}}{{domAirFlight.flightNo}}<f7-chip v-if="domAirFlight.stopNumber!=='False'" :text="$t('air.msg.stopOver')" color="blue"/>
                </f7-col>
              </f7-row>
            </f7-col>
            <f7-col width="20">
              <f7-row>
                <f7-col class="data-table-title text-color-orange text-align-right"
                        v-if="parseFloat(domAirFlight.sellPrice)===parseFloat(domAirFlight.flightCabins[0].lowestPrice)">
                  ¥{{domAirFlight.sellPrice}}
                </f7-col>
                <f7-col class="data-table-title text-color-blue text-align-right"
                        v-if="parseFloat(domAirFlight.sellPrice)!==parseFloat(domAirFlight.flightCabins[0].lowestPrice)">
                  ¥{{domAirFlight.sellPrice}}
                </f7-col>
              </f7-row>
              <f7-row class="text-color-gray text-align-right">
                <f7-col>
                  <f7-icon size="small" v-if="!domAirFlight.showFlightCabins" f7="chevron_down_round_fill"></f7-icon>
                  <f7-icon size="small" v-if="domAirFlight.showFlightCabins" f7="chevron_up_round_fill"></f7-icon>
                </f7-col>
              </f7-row>
            </f7-col>
          </f7-row>
        </f7-list-item>
        <li v-if="domAirFlight.showFlightCabins && !flightCabin.filtered"
            :key="cabinIndex"
            v-for="(flightCabin, cabinIndex) in domAirFlight.flightCabins">
          <f7-row class="item-content no-gap">
            <f7-col width="60">
              {{flightCabin.cabinType|cabinType}}
              <div style="display: inline-block; min-width: 20px;">
                {{flightCabin.cabinCode}}
              </div>
              <div style="display: inline-block;">
                <f7-chip v-if="flightCabin.airPriceType==='T'" :text="$t('common.label.corp')" color="blue"/>
                <f7-chip v-if="flightCabin.sysType==='NET'" :text="$t('air.label.Specials')" color="red"/>
                <f7-chip v-if="flightCabin.seatLeft!=='A'" :text="$t('air.label.seatShortage')" color="red"/>
                <f7-chip v-if="flightCabin.interfaceType==='BO'" :text="$t('air.label.sysType')" color="red"/>
              </div>
            </f7-col>
            <f7-col width="25" class="text-color-orange text-align-right padding-right"
                    v-if="parseFloat(flightCabin.sellPrice)===parseFloat(flightCabin.lowestPrice)">
              ¥{{flightCabin.sellPrice}}
            </f7-col>
            <f7-col width="25" class="text-color-blue text-align-right padding-right"
                    v-if="parseFloat(flightCabin.sellPrice)!==parseFloat(flightCabin.lowestPrice)">
              ¥{{flightCabin.sellPrice}}
            </f7-col>
            <f7-col width="15">
              <f7-button @click="selectOrBookDomAir(domAirFlight, flightCabin)"
                         :text="config.isBook?$t('common.label.book'):$t('common.label.select')"></f7-button>
            </f7-col>
          </f7-row>
        </li>
      </f7-list>
    </div>
    <f7-popup class="demo-popup" ref="selectSegment" id="selectSegment">
      <f7-page>
        <f7-navbar>
          <f7-nav-left>
            <f7-link popup-close="#selectSegment">
              <i class="icon icon-back"></i>
              <span>{{$t('common.label.back')}}</span>
            </f7-link>
          </f7-nav-left>
          <f7-nav-title>
            <div v-if="searchDomFlightParam">
              {{$i18nMsg(searchDomFlightParam.searchDomFlight.departPort.cityNameCN,
              searchDomFlightParam.searchDomFlight.departPort.cityNameEN)}}
              <f7-icon f7="arrow_right"/>
              {{$i18nMsg(searchDomFlightParam.searchDomFlight.arrivalPort.cityNameCN,
              searchDomFlightParam.searchDomFlight.arrivalPort.cityNameEN)}}
            </div>
            <div v-if="!searchDomFlightParam">
              {{$t('air.label.dom')}}
            </div>
          </f7-nav-title>
          <f7-nav-right>
            <f7-link popup-close="#selectSegment" @click="selectLowestSegment">{{$t('common.label.next')}}</f7-link>
          </f7-nav-right>
        </f7-navbar>
        <div class="block block-strong" style="padding: 10px;">
          <span class="text-color-orange" v-html="$t('air.msg.policy1')"></span>
          <span class="text-color-orange">
            {{selectLowestPrice.resultSegment.deptDate|date('HH:mm')}}{{$t('air.msg.policy2')}}
            {{lowestPriceTimeRange}}{{$t('air.msg.policy3')}}{{lowestPrice}}
          </span>
          <span class="text-color-orange" v-html="$t('air.msg.policy4')"></span>
        </div>
        <f7-list inset class="margin no-flex"
                 v-for="(segment, index) in lowestPriceSegments"
                 :key="index">
          <f7-list-item checkbox :checked="defaultSelect === index" @change="selectLowestPriceSegment(segment,index)">
            <div class="item-title" slot="title">
              <f7-row no-gap>
                <f7-col width="75">
                  <f7-row>
                    <f7-col width="25" class="data-table-title">
                      {{segment.resultSegment.deptDate|date('HH:mm')}}
                    </f7-col>
                    <f7-col class="text-align-center" width="25">
                      <f7-icon f7="arrow_right"/>
                    </f7-col>
                    <f7-col width="50" class="data-table-title">
                      {{segment.resultSegment.arrivalDate|date('HH:mm')}}
                    </f7-col>
                  </f7-row>
                  <f7-row class="text-color-gray item-subtitle">
                    <f7-col width="50">
                      {{$i18nMsg(segment.resultSegment.deptCityNameCN, segment.resultSegment.deptCityNameEN)}}
                      {{segment.resultSegment.departTerm}}
                    </f7-col>
                    <f7-col width="50">
                      {{$i18nMsg(segment.resultSegment.arrivalCityNameCN, segment.resultSegment.arrivalCityNameEN)}}
                      {{segment.resultSegment.arrivalTerm}}
                    </f7-col>
                  </f7-row>
                  <f7-row class="text-color-gray item-subtitle">
                    <f7-col width="100">
                      {{$i18nMsg(segment.resultSegment.carrierNameCN, segment.resultSegment.carrierNameEN)}}
                      <f7-chip v-if="segment.resultSegment.corporateAirline==='T'" :text="$t('common.label.corp')" color="blue"/>
                      &nbsp;&nbsp;|&nbsp;&nbsp;
                      {{segment.resultSegment.flightCarrier}}{{segment.resultSegment.flightNo}}
                    </f7-col>
                  </f7-row>
                </f7-col>
                <f7-col width="25">
                  <f7-row>
                    <f7-col class="data-table-title text-color-orange text-align-right">
                      ¥{{segment.flightCabin.sellPrice}}
                    </f7-col>
                  </f7-row>
                  <f7-row class="text-color-gray item-subtitle text-align-right">
                    <f7-col>
                      {{segment.flightCabin.cabinCode}}{{segment.flightCabin.cabinType|cabinType}}
                    </f7-col>
                  </f7-row>
                </f7-col>
              </f7-row>
            </div>
          </f7-list-item>
        </f7-list>
        <div class="block block-strong">
          <span class="text-color-blue" v-html="$t('air.msg.selectSegment')"></span>
        </div>
        <f7-list inset class="margin no-flex">
          <f7-list-item checkbox :checked="defaultSelect === 'A'" @change="selectLowestPriceSegment(selectLowestPrice,'A')">
            <div class="item-title" slot="title">
              <f7-row no-gap>
                <f7-col width="75">
                  <f7-row>
                    <f7-col width="25" class="data-table-title">
                      {{selectLowestPrice.resultSegment.deptDate|date('HH:mm')}}
                    </f7-col>
                    <f7-col class="text-align-center" width="25">
                      <f7-icon f7="arrow_right"/>
                    </f7-col>
                    <f7-col width="50" class="data-table-title">
                      {{selectLowestPrice.resultSegment.arrivalDate|date('HH:mm')}}
                    </f7-col>
                  </f7-row>
                  <f7-row class="text-color-gray item-subtitle">
                    <f7-col width="50">
                      {{$i18nMsg(selectLowestPrice.resultSegment.deptCityNameCN, selectLowestPrice.resultSegment.deptCityNameEN)}}
                      {{selectLowestPrice.resultSegment.departTerm}}
                    </f7-col>
                    <f7-col width="50">
                      {{$i18nMsg(selectLowestPrice.resultSegment.arrivalCityNameCN, selectLowestPrice.resultSegment.arrivalCityNameEN)}}
                      {{selectLowestPrice.resultSegment.arrivalTerm}}
                    </f7-col>
                  </f7-row>
                  <f7-row class="text-color-gray item-subtitle">
                    <f7-col width="100">
                      {{$i18nMsg(selectLowestPrice.resultSegment.carrierNameCN, selectLowestPrice.resultSegment.carrierNameEN)}}
                      <f7-chip v-if="selectLowestPrice.resultSegment.corporateAirline==='T'" :text="$t('common.label.corp')" color="blue"/>
                      &nbsp;&nbsp;|&nbsp;&nbsp;
                      {{selectLowestPrice.resultSegment.flightCarrier}}{{selectLowestPrice.resultSegment.flightNo}}
                    </f7-col>
                  </f7-row>
                </f7-col>
                <f7-col width="25">
                  <f7-row>
                    <f7-col class="data-table-title text-color-blue text-align-right">
                      ¥{{selectLowestPrice.flightCabin.sellPrice}}
                    </f7-col>
                  </f7-row>
                  <f7-row class="text-color-gray item-subtitle text-align-right">
                    <f7-col>
                      {{selectLowestPrice.flightCabin.cabinCode}}{{selectLowestPrice.flightCabin.cabinType|cabinType}}
                    </f7-col>
                  </f7-row>
                </f7-col>
              </f7-row>
            </div>
          </f7-list-item>
        </f7-list>
      </f7-page>
    </f7-popup>
  </f7-page>
</template>

<script>
import { mapGetters } from 'vuex'
import uniq from 'lodash/uniq'
import uniqBy from 'lodash/uniqBy'
import merge from 'lodash/merge'
import cloneDeep from 'lodash/cloneDeep'
import orderBy from 'lodash/orderBy'
import moment from 'moment'
import DomairService from '../../../services/domair/DomairService'
import { ProductType } from '../../../consts/OrderConsts'
import PolicyControlService from '../../../services/policy/PolicyControlService'

const searchParamsKey = 'DomAir/searchDomFlightParams'
const selectedSegmentsKey = 'DomAir/selectedDomAirSegments'
const firstResultDataKey = 'DomAir/firstResultData'
export default {
  name: 'domair-search-result',
  data () {
    const domAirDefaultSetup = this.$defaultSetup(ProductType.DomAir)
    const domAirTabs = DomairService.getDomAirTabs(domAirDefaultSetup.selectShowTab)
    const lowestPriceTimeRange = domAirDefaultSetup.lowestPriceTimeRange
    const lowestPriceByCabinType = domAirDefaultSetup.lowestPriceByCabinType
    const lowestPriceSelect = domAirDefaultSetup.lowestPriceSelect
    const sortConditions = [{
      id: 'sellPrice asc',
      label: this.$t('air.label.priceAsc')
    }, {
      id: 'sellPrice desc',
      label: this.$t('air.label.priceDesc')
    }, {
      id: 'deptDate asc',
      label: this.$t('air.label.dateAsc')
    }, {
      id: 'deptDate desc',
      label: this.$t('air.label.dateDesc')
    }]
    return {
      config: {
        searching: false,
        orderBy: domAirDefaultSetup.defaultSort === 'deptTime' ? sortConditions[2] : sortConditions[0],
        isBook: false
      },
      domAirTabs,
      lowestPriceTimeRange,
      lowestPriceByCabinType,
      lowestPriceSelect,
      sortConditions,
      domAirCurrentTab: domAirTabs[domAirTabs.length - 1],
      showDomAirFlights: [],
      resultData: {},
      lowestSegment: {},
      lowestPriceSegments: [],
      selectLowestPrice: {
        resultSegment: {},
        flightCabin: {}
      },
      selectSegment: {
        resultSegment: {},
        flightCabin: {}
      },
      lowestPrice: '',
      defaultSelect: 'A',
      filterConditions: [],
      searchDomFlightParam: null,
      searchDomFlightParams: []
    }
  },
  computed: {
    ...mapGetters([searchParamsKey, selectedSegmentsKey, firstResultDataKey])
  },
  watch: {
    filterConditions: {
      handler: function (v) {
        let showDomAirFlights = this.showDomAirFlights
        let cabinFilters = v[0].items.filter(item => {
          return item.selected === true
        }).map(item => item.id)
        let airlineFilters = v[1].items.filter(item => {
          return item.selected === true
        }).map(item => item.id)
        showDomAirFlights.forEach(flight => {
          this.filterFlightCabins(flight, cabinFilters)
          flight.filtered = flight.filtered || (airlineFilters.length !== 0 && airlineFilters.indexOf(flight.flightCarrier) < 0)
        })
        this.showDomAirFlights = showDomAirFlights
        this.doSortFlights()
        this.$forceUpdate()
      },
      deep: true
    },
    'config.orderBy': function () {
      this.doSortFlights()
    },
    domAirCurrentTab () {
      this.calcFilterCondition()
    }
  },
  methods: {
    filterFlightCabins (flight, cabinTypes) {
      flight.filtered = true
      flight.flightCabins.forEach(flightCabin => {
        flightCabin.filtered = cabinTypes.length > 0 && cabinTypes.indexOf(flightCabin.cabinType) === -1
        if (!flightCabin.filtered) {
          flight.filtered = false
        }
      })
    },
    calcFilterCondition () {
      let cabinTypes = []
      const airlines = []
      this.showDomAirFlights = this.resultData[this.domAirCurrentTab.flightKey]
      if (this.showDomAirFlights) {
        this.showDomAirFlights.forEach((flight) => {
          cabinTypes = cabinTypes.concat(flight.flightCabins.map(cabin => cabin.cabinType))
          airlines.push({
            id: flight.flightCarrier,
            label: this.$i18nMsg(flight.carrierNameCN, flight.carrierNameEN),
            selected: false
          })
        })
        this.filterConditions = [{
          label: this.$t('air.label.cabinType'),
          items: DomairService.getDomAirCabinTypes(uniq(cabinTypes))
        }, {
          label: this.$t('air.label.airline'),
          items: uniqBy(airlines, 'id')
        }]
        this.doSortFlights()
      }
    },
    selectOrBookDomAir (flight, flightCabin) {
      const searchParam = this.searchDomFlightParam
      if (this.lowestPriceTimeRange !== -1 && searchParam.searchDomFlight.deptTime === '') {
        const islowestPriceFlag = this.getIslowestPriceFlag(flight, flightCabin)
        if (islowestPriceFlag) {
          this.selectOrBookDomAirNext(flight, flightCabin, true)
        } else {
          this.defaultSelect = 'A'
          this.selectLowestPrice.resultSegment = cloneDeep(flight)
          this.selectLowestPrice.flightCabin = cloneDeep(flightCabin)
          this.selectSegment = cloneDeep(this.selectLowestPrice)
          this.$refs.selectSegment.open()
        }
      } else {
        this.selectOrBookDomAirNext(flight, flightCabin, false)
      }
    },
    getIslowestPriceFlag (flight, flightCabin) {
      this.lowestPriceSegments = []
      let lowestPrice = 1000000
      let policyUserBarnoFlag = '0'
      if (this.$store.getters['Order/basicSearchParam'].policyUser.custNo && this.$store.getters['Order/basicSearchParam'].policyUser.custNo.length >= 6) {
        if (this.lowestPriceByCabinType.indexOf(this.$store.getters['Order/basicSearchParam'].policyUser.custNo.substring(0, 6)) !== -1) {
          policyUserBarnoFlag = '1'
        }
      }
      const maxtime = moment(flight.deptDate).add(this.lowestPriceTimeRange, 'h')
      const mintime = moment(flight.deptDate).add(-this.lowestPriceTimeRange, 'h')
      this.showDomAirFlights.forEach(resultSegment => {
        if (moment(resultSegment.deptDate).isBetween(mintime, maxtime, 'minutes', '[]')) {
          resultSegment.flightCabins.forEach(cabin => {
            if (this.lowestPriceByCabinType === 'N' || (this.lowestPriceByCabinType !== 'Y' && policyUserBarnoFlag === '0') || ((this.lowestPriceByCabinType === 'Y' || policyUserBarnoFlag === '1') && cabin.cabinType === flightCabin.cabinType)) {
              if (this.lowestPriceSelect === '3' || (this.lowestPriceSelect === '2' && cabin.interfaceType !== 'BO') || (this.lowestPriceSelect === '1' && cabin.interfaceType !== '9C' && cabin.interfaceType !== 'BO')) {
                let selectSegment = {
                  resultSegment: {},
                  flightCabin: {}
                }
                selectSegment.resultSegment = cloneDeep(resultSegment)
                selectSegment.flightCabin = cloneDeep(cabin)
                let sellPrice = parseFloat(cabin.sellPrice)
                if (sellPrice < lowestPrice) {
                  lowestPrice = sellPrice
                  this.lowestPriceSegments = []
                  this.lowestPriceSegments.push(selectSegment)
                } else if (sellPrice === lowestPrice) {
                  this.lowestPriceSegments.push(selectSegment)
                }
              }
            }
          })
        }
      })
      this.lowestPrice = this.lowestPriceSegments[0].flightCabin.sellPrice
      if (lowestPrice < parseFloat(flightCabin.sellPrice) && lowestPrice !== 1000000) {
        return false
      } else {
        return true
      }
    },
    selectLowestPriceSegment (selectSegment, defaultSelect) {
      this.selectSegment = cloneDeep(selectSegment)
      this.defaultSelect = defaultSelect
    },
    selectLowestSegment () {
      this.$refs.selectSegment.close()
      if (this.defaultSelect === 'A') {
        this.selectOrBookDomAirNext(this.selectSegment.resultSegment, this.selectSegment.flightCabin, false)
      } else {
        this.selectOrBookDomAirNext(this.selectSegment.resultSegment, this.selectSegment.flightCabin, true)
      }
    },
    selectOrBookDomAirNext (flight, flightCabin, lowestFlag) {
      const flights = cloneDeep(this.selectedDomAirSegments)
      let calcFlight = null
      if (lowestFlag) {
        calcFlight = cloneDeep(flight)
        flightCabin = cloneDeep(flightCabin)
        delete calcFlight.flightCabins
        calcFlight.selectedCabin = flightCabin
        flights.push(calcFlight)
      } else {
        calcFlight = this.processFlightAndLLF(flight, flightCabin)
        flights.push(calcFlight)
      }
      const bookParam = this.processBookParam(flights)
      PolicyControlService.doPolicyControl(DomairService.checkDomAirBookPolicys, bookParam, (resultData, reasonCodes, handlers) => {
        console.info(resultData, reasonCodes, handlers)
        calcFlight.resultHandlers = handlers
        if (this.config.isBook) {
          bookParam.domAirBooking.selectedFlights[bookParam.domAirBooking.selectedFlights.length - 1] = calcFlight
          this.$store.dispatch('DomAir/cacheResultData', bookParam)
          // this.$store.dispatch('DomAir/storeBookDomFlightParam', bookParam)
          this.$goto('/domair/toBookDomAir')
        } else {
          this.selectedDomAirSegments[0] = calcFlight
          this.$store.dispatch('DomAir/storeSelectedDomAirSegments', this.selectedDomAirSegments)
          this.$goto('/domair/doSearchLastDomAir')
        }
      }, { showReasonCode: false })
    },
    switchDomAirPorts (searchParam) {
      const searchDomFlight = searchParam.searchDomFlight
      const tmpPort = searchDomFlight.departPort
      searchDomFlight.departPort = searchDomFlight.arrivalPort
      searchDomFlight.arrivalPort = tmpPort
      searchDomFlight.departAirportCode = searchDomFlight.departPort.code
      searchDomFlight.arrivalAirportCode = searchDomFlight.arrivalPort.code
      searchDomFlight.rtFlag = true
    },
    processFlightAndLLF (flight, flightCabin) {
      flight = cloneDeep(flight)
      flightCabin = cloneDeep(flightCabin)
      delete flight.flightCabins
      flight.selectedCabin = flightCabin
      const searchParam = this.searchDomFlightParam
      let policyUserBarnoFlag = '0'
      if (this.$store.getters['Order/basicSearchParam'].policyUser.custNo && this.$store.getters['Order/basicSearchParam'].policyUser.custNo.length >= 6) {
        if (this.lowestPriceByCabinType.indexOf(this.$store.getters['Order/basicSearchParam'].policyUser.custNo.substring(0, 6)) !== -1) {
          policyUserBarnoFlag = '1'
        }
      }
      let lowestPrice = 1000000
      if (this.lowestPriceTimeRange !== -1 && searchParam.searchDomFlight.deptTime === '') {
        const maxtime = moment(flight.deptDate).add(this.lowestPriceTimeRange, 'h')
        const mintime = moment(flight.deptDate).add(-this.lowestPriceTimeRange, 'h')
        this.showDomAirFlights.forEach(resultSegment => {
          if (moment(resultSegment.deptDate).isBetween(mintime, maxtime, 'minutes', '[]')) {
            lowestPrice = this.processLlfFlightCabin(resultSegment, lowestPrice, flightCabin, policyUserBarnoFlag)
          }
        })
      } else {
        this.showDomAirFlights.forEach(resultSegment => {
          lowestPrice = this.processLlfFlightCabin(resultSegment, lowestPrice, flightCabin, policyUserBarnoFlag)
        })
      }
      if (this.lowestSegment && (parseFloat(this.lowestSegment.selectedCabin.sellPrice) < parseFloat(flight.selectedCabin.sellPrice))) {
        flight.llfFlightNo = this.lowestSegment.flightCarrier + this.lowestSegment.flightNo
        flight.llfPrice = this.lowestSegment.selectedCabin.sellPrice
        flight.llfDeptTime = moment(this.lowestSegment.deptDate).format('YYYY-MM-DD HH:mm')
        flight.llfArrivalTime = moment(this.lowestSegment.arrivalDate).format('YYYY-MM-DD HH:mm')
        flight.llfDeptPort = this.lowestSegment.deptCityCode
        flight.llfArrPort = this.lowestSegment.arrivalCityCode
        flight.llfDeptTerminal = this.lowestSegment.departTerm
        flight.llfArrTerminal = this.lowestSegment.arrivalTerm
        flight.llfCabin = this.lowestSegment.selectedCabin
      }
      return flight
    },

    processLlfFlightCabin (resultSegment, lowestPrice, flightCabin, policyUserBarnoFlag) {
      resultSegment.flightCabins.forEach(cabin => {
        if (this.lowestPriceByCabinType === 'N' || (this.lowestPriceByCabinType !== 'Y' && policyUserBarnoFlag === '0') || ((this.lowestPriceByCabinType === 'Y' || policyUserBarnoFlag === '1') && cabin.cabinType === flightCabin.cabinType)) {
          if (this.lowestPriceSelect === '3' || (this.lowestPriceSelect === '2' && cabin.interfaceType !== 'BO') || (this.lowestPriceSelect === '1' && cabin.interfaceType !== '9C' && cabin.interfaceType !== 'BO')) {
            let sellPrice = parseFloat(cabin.sellPrice)
            if (sellPrice < lowestPrice) {
              lowestPrice = sellPrice
              this.lowestSegment = resultSegment
              this.lowestSegment.selectedCabin = cabin
            }
          }
        }
      })
      return lowestPrice
    },
    processBookParam (flights) {
      let bookParam = DomairService.getBookParam()
      let searchParam = this.searchDomFlightParam
      merge(bookParam.domAirBooking, {
        airType: searchParam.airType,
        selectedFlights: flights
      })
      return bookParam
    },
    doSearchDomAir () {
      const searchParam = this.searchDomFlightParam
      searchParam.policyApplied = true
      this.config.searching = true
      DomairService.searchDomFlight(searchParam).then(data => {
        this.config.searching = false
        this.resultData = data.resultData
        this.$store.dispatch('DomAir/firstResultData', this.resultData)
        this.calcFilterCondition()
        this.calcResultFlights(searchParam)
      })
    },
    calcResultFlights () {
      const searchParam = this.searchDomFlightParam
      this.config.isBook = searchParam.airType === 'OW' ||
        (searchParam.airType === 'RT' && this.selectedDomAirSegments.length > 0)
      this.lowestSegment = cloneDeep(this.resultData.lowestFlights[0])
      if (this.lowestSegment && this.lowestSegment.flightCabins) {
        this.lowestSegment.selectedCabin = this.lowestSegment.flightCabins[0]
        delete this.lowestSegment.flightCabins
      }
    },
    doSortFlights () {
      if (this.showDomAirFlights) {
        const orders = this.config.orderBy.id.split(/\s+/)
        this.showDomAirFlights.forEach(flight => {
          for (let i = 0; i < flight.flightCabins.length; i++) {
            if (!flight.flightCabins[i].filtered) {
              flight.sellPrice = parseFloat(flight.flightCabins[i].sellPrice)
              break
            }
          }
        })
        this.$nextTick(() => {
          this.showDomAirFlights = orderBy(this.showDomAirFlights, [orders[0]], [orders[1]])
        })
      }
    }
  },
  mounted () {
    if (this[searchParamsKey]) {
      this.$store.dispatch('DomAir/storeSelectedDomAirSegments')
      this.searchDomFlightParams = cloneDeep(this[searchParamsKey])
      this.selectedDomAirSegments = cloneDeep(this[selectedSegmentsKey]) || []
      this.searchDomFlightParam = this.searchDomFlightParams[0]
      if (this[firstResultDataKey]) {
        this.resultData = cloneDeep(this[firstResultDataKey])
        this.calcFilterCondition()
        this.calcResultFlights(this.searchDomFlightParam)
      } else {
        this.doSearchDomAir()
      }
    } else {
      this.$back('/search/domair', { force: true })
    }
  }
}
</script>

<style scoped>

</style>
