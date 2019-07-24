<template>
  <f7-page page-content with-subnavbar>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"
                   @back-click="$store.dispatch('Train/storeSelectedTrainSegments')"></f7-nav-left>
      <f7-nav-title>
        <div v-if="searchTrainScheduleParam">
          {{$i18nMsg(searchTrainScheduleParam.searchParam.departStation.nameCN,
          searchTrainScheduleParam.searchParam.departStation.nameEN)}}
          <f7-icon f7="arrow_right"/>
          {{$i18nMsg(searchTrainScheduleParam.searchParam.arrivalStation.nameCN,
          searchTrainScheduleParam.searchParam.arrivalStation.nameEN)}}
        </div>
        <div v-if="!searchTrainScheduleParam">
          {{$t('train.label.train')}}
        </div>
      </f7-nav-title>
      <common-home/>
      <f7-subnavbar>
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
    <div class="airList" v-if="config.searching && trainSchedules.length===0">
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
    <div class="airList">
      <f7-list inset class="margin no-flex"
               v-if="!trainSchedule.filtered"
               v-for="(trainSchedule, index) in trainSchedules"
               :key="index">
        <f7-list-item @click="$set(trainSchedule, 'showTrainSeats', !!!trainSchedule.showTrainSeats)">
          <f7-row no-gap>
            <f7-col width="80">
              <f7-row>
                <f7-col width="20">
                  {{trainSchedule.trainNo}}
                </f7-col>
                <f7-col width="20" class="data-table-title">
                  {{trainSchedule.fromTime}}
                </f7-col>
                <f7-col class="text-align-center" width="20">
                  <f7-icon f7="arrow_right"/>
                </f7-col>
                <f7-col width="20" class="data-table-title">
                  {{trainSchedule.toTime}}
                </f7-col>
              </f7-row>
              <f7-row class="text-color-gray item-subtitle">
                <f7-col width="20">
                  {{$i18nMsg(trainSchedule.tripTimeCN, trainSchedule.tripTimeEN)}}
                </f7-col>
                <f7-col width="30">
                  {{$i18nMsg(trainSchedule.departureStationNameCN,trainSchedule.departureStationNameEN)}}
                  <f7-chip v-if="trainSchedule.stationType === '1' || trainSchedule.stationType === '4'" :text="$t('train.label.stationType1')"
                           color="blue"/>
                </f7-col>
                <f7-col width="30">
                  {{$i18nMsg(trainSchedule.arrivalStationNameCN,trainSchedule.arrivalStationNameEN)}}
                  <f7-chip v-if="trainSchedule.stationType === '3' || trainSchedule.stationType === '4'" :text="$t('train.label.stationType3')"
                           color="blue"/>
                </f7-col>
              </f7-row>
            </f7-col>
            <f7-col width="20">
              <f7-row class="text-color-gray text-align-right">
                <f7-col>
                  <f7-icon size="small" v-if="!trainSchedule.showTrainSeats"
                           f7="chevron_down_round_fill"></f7-icon>
                  <f7-icon size="small" v-if="trainSchedule.showTrainSeats"
                           f7="chevron_up_round_fill"></f7-icon>
                </f7-col>
              </f7-row>
            </f7-col>
          </f7-row>
        </f7-list-item>
        <li v-if="trainSchedule.showTrainSeats"
            :key="seatIndex"
            v-for="(trainSeatPrice, seatIndex) in trainSchedule.seatPrices">
          <f7-row class="item-content no-gap">
            <f7-col width="60">
              {{trainSeatPrice.nameCN}}
              <div style="display: inline-block; min-width: 20px;">
              </div>
              <div style="display: inline-block;">
                余{{trainSeatPrice.yupiao}}张
              </div>
            </f7-col>
            <f7-col width="25" class="text-color-orange text-align-right padding-right">
              ¥{{trainSeatPrice.price}}
            </f7-col>
            <f7-col width="15">
              <f7-button @click="toBookTrainSeat(trainSchedule, trainSeatPrice)"
                         :text="$t('common.label.book')"></f7-button>
            </f7-col>
          </f7-row>
        </li>
      </f7-list>
    </div>
  </f7-page>
</template>

<script>
import { mapGetters } from 'vuex'
import uniqBy from 'lodash/uniqBy'
import merge from 'lodash/merge'
import cloneDeep from 'lodash/cloneDeep'
import orderBy from 'lodash/orderBy'
import TrainService from '../../../services/train/TrainService'
import { ProductType } from '../../../consts/OrderConsts'
import PolicyControlService from '../../../services/policy/PolicyControlService'

const searchParamsKey = 'Train/searchTrainScheduleParams'
const selectedSegmentsKey = 'Train/selectedTrainSegments'
const lastResultDataKey = 'Train/lastResultData'
export default {
  name: 'train-search-last-result',
  data () {
    const trainDefaultSetup = this.$defaultSetup(ProductType.Train)
    const companyDefaultSetup = this.$defaultSetup(ProductType.Company)
    this.$processBookEnable(trainDefaultSetup)
    const sortConditions = [{
      id: 'departureDate asc',
      label: this.$t('train.label.depTime')
    }, {
      id: 'departureDate desc',
      label: this.$t('train.label.depTime')
    }, {
      id: 'arrivalDate asc',
      label: this.$t('train.label.arrTime')
    }, {
      id: 'arrivalDate desc',
      label: this.$t('train.label.arrTime')
    }, {
      id: 'tripTime asc',
      label: this.$t('train.label.useTime')
    }, {
      id: 'tripTime desc',
      label: this.$t('train.label.useTime')
    }]
    return {
      config: {
        searching: false,
        orderBy: sortConditions[0]
      },
      sortConditions,
      trainDefaultSetup,
      companyDefaultSetup,
      trainSchedules: [],
      resultData: {},
      defaultSelect: 'A',
      filterConditions: [],
      searchTrainScheduleParam: null,
      selectedTrainSegments: [],
      searchTrainScheduleParams: []
    }
  },
  computed: {
    ...mapGetters([searchParamsKey, selectedSegmentsKey, lastResultDataKey])
  },
  watch: {
    filterConditions: {
      handler: function (v) {
        let trainSchedules = this.trainSchedules
        let trainTypeFilters = v[0].items.filter(item => {
          return item.selected === true
        }).map(item => item.id)
        let stationTypeFilters = v[1].items.filter(item => {
          return item.selected === true
        }).map(item => item.id)
        let deptStationsFilters = v[1].items.filter(item => {
          return item.selected === true
        }).map(item => item.id)
        let arrStationsFilters = v[1].items.filter(item => {
          return item.selected === true
        }).map(item => item.id)
        let ticketingStatusFilters = v[1].items.filter(item => {
          return item.selected === true
        }).map(item => item.id)
        trainSchedules.forEach(trainSchedule => {
          trainSchedule.filtered = trainSchedule.filtered || (trainTypeFilters.length !== 0 && trainTypeFilters.indexOf(trainSchedule.trainType.type) < 0)
          trainSchedule.filtered = trainSchedule.filtered || (stationTypeFilters.length !== 0 && stationTypeFilters.indexOf(trainSchedule.stationType) < 0)
          trainSchedule.filtered = trainSchedule.filtered || (deptStationsFilters.length !== 0 && deptStationsFilters.indexOf(trainSchedule.departureStationCode) < 0)
          trainSchedule.filtered = trainSchedule.filtered || (arrStationsFilters.length !== 0 && arrStationsFilters.indexOf(trainSchedule.arrivalStationCode) < 0)
          trainSchedule.filtered = trainSchedule.filtered || (ticketingStatusFilters.length !== 0 && ticketingStatusFilters.indexOf(trainSchedule.flag) < 0)
        })
        this.trainSchedules = trainSchedules
        this.doSortFlights()
        this.$forceUpdate()
      },
      deep: true
    },
    'config.orderBy': function () {
      this.doSortFlights()
    }
  },
  methods: {
    calcFilterCondition () {
      const deptStations = []
      const arrStations = []
      this.trainSchedules = this.resultData['trainSchedules']
      if (this.trainSchedules) {
        this.trainSchedules.forEach((trainSchedule) => {
          deptStations.push({
            id: trainSchedule.departureStationCode,
            label: this.$i18nMsg(trainSchedule.departureStationNameCN, trainSchedule.departureStationNameEN),
            selected: false
          })
          arrStations.push({
            id: trainSchedule.arrivalStationCode,
            label: this.$i18nMsg(trainSchedule.arrivalStationNameCN, trainSchedule.arrivalStationNameEN),
            selected: false
          })
        })
        this.filterConditions = [{
          label: this.$t('train.label.trainType'),
          items: TrainService.getTrainTypes()
        }, {
          label: this.$t('train.label.stationType'),
          items: TrainService.getTrainStationTypes()
        }, {
          label: this.$t('train.label.from'),
          items: uniqBy(deptStations, 'id')
        }, {
          label: this.$t('train.label.to'),
          items: uniqBy(arrStations, 'id')
        }, {
          label: this.$t('train.label.ticketingStatus'),
          items: [{
            id: '1',
            label: this.$t('train.label.availableOnly'),
            selected: false
          }]
        }]
        this.doSortFlights()
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
      if (this.validateBeforeBook(bookParam)) {
        PolicyControlService.doPolicyControl(TrainService.checkDomAirBookPolicys, bookParam, (resultData, reasonCodes, handlers) => {
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
      }
    },
    validateBeforeBook (bookParam) {
      if (!this.domAirDefaultSetup.bookEnable || (this.companyDefaultSetup.bookEnableNeedExternalOrder && !bookParam.domAirBooking.externalOrderNo)) {
        this.$coreError(this.$t('common.msg.bookDisabled'))
        return false
      }
      return true
    },
    processBookParam (flights) {
      let bookParam = TrainService.getBookParam()
      let searchParam = this.searchDomFlightParam
      merge(bookParam.domAirBooking, {
        airType: searchParam.airType,
        selectedFlights: flights
      })
      return bookParam
    },
    doSearchTrain () {
      const searchParam = this.searchTrainScheduleParam
      searchParam.policyApplied = true
      this.config.searching = true
      TrainService.searchTrainSchedule(searchParam).then(data => {
        this.config.searching = false
        this.resultData = data.resultData
        this.trainSchedulesFilters()
        this.$store.dispatch('Train/lastResultData', this.resultData)
        this.calcFilterCondition()
      })
    },
    trainSchedulesFilters () {
      this.resultData['trainSchedules'].forEach((trainSchedule) => {
        trainSchedule.flag = false
        trainSchedule.seatPrices.forEach((trainSeatPrice) => {
          let seatCode = TrainService.getTrainSeatType(trainSeatPrice.nameCN)
          if (seatCode !== '') {
            trainSeatPrice.seatCode = seatCode
            if (trainSeatPrice.yupiao !== 0) {
              trainSchedule.flag = true
            }
          } else {
            trainSeatPrice.filtered = true
          }
        })
      })
    },
    doSortFlights () {
      if (this.trainSchedules) {
        const orders = this.config.orderBy.id.split(/\s+/)
        this.$nextTick(() => {
          this.trainSchedules = orderBy(this.trainSchedules, [orders[0]], [orders[1]])
        })
      }
    }
  },
  mounted () {
    if (this[searchParamsKey]) {
      this.searchTrainScheduleParams = cloneDeep(this[searchParamsKey])
      this.selectedTrainSegments = cloneDeep(this[selectedSegmentsKey]) || []
      this.searchTrainScheduleParam = this.searchTrainScheduleParams[this.selectedTrainSegments.length]
      console.info(this.searchTrainScheduleParams)
      if (this[lastResultDataKey]) {
        this.resultData = cloneDeep(this[lastResultDataKey])
        this.calcFilterCondition()
      } else {
        this.doSearchTrain()
      }
    } else {
      this.$back('/search/train', { force: true })
    }
  }
}
</script>

<style scoped>

</style>
