<template>
  <f7-page page-content with-subnavbar>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"
                   @back-click="backIntlairSearch"></f7-nav-left>
      <f7-nav-title>
        <div v-if="intlAirSearchParam && selectedSegment">
          &nbsp;&nbsp;{{$t('air.label.return')}}&nbsp;&nbsp;
          {{$i18nMsg(intlAirSearchParam.intlAirSearchDto.arrivalPort.nameCN,
          intlAirSearchParam.intlAirSearchDto.arrivalPort.nameEN)}}
          <f7-icon f7="arrow_right"/>
          {{$i18nMsg(intlAirSearchParam.intlAirSearchDto.departPort.nameCN,
          intlAirSearchParam.intlAirSearchDto.departPort.nameEN)}}
        </div>
        <div v-if="intlAirSearchParam && !selectedSegment">
          {{$i18nMsg(intlAirSearchParam.intlAirSearchDto.departPort.nameCN,
          intlAirSearchParam.intlAirSearchDto.departPort.nameEN)}}
          <f7-icon f7="arrow_right"/>
          {{$i18nMsg(intlAirSearchParam.intlAirSearchDto.arrivalPort.nameCN,
          intlAirSearchParam.intlAirSearchDto.arrivalPort.nameEN)}}
        </div>
        <div v-if="!intlAirSearchParam && !selectedSegment">
          {{$t('air.label.intl')}}
        </div>
      </f7-nav-title>
      <common-home/>
      <f7-subnavbar>
        <common-dropdown>
          <f7-link slot="trigger">{{intlAirCurrentTab.label}}
            <f7-icon f7="chevron_down" size="16"/>
          </f7-link>
          <f7-list>
            <f7-list-item :key="intlAirTab.id" v-for="intlAirTab in intlAirTabs" name="intlAirTab" radio
                          :checked="intlAirCurrentTab === intlAirTab"
                          @change="intlAirCurrentTab = intlAirTab"
                          :title="intlAirTab.label"></f7-list-item>
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
    <div class="airList" v-if="config.searching && showIntlAirFlights.length===0">
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
    <!--eslint-disable-next-line-->
    <div class="airList" :key="intlAirTab.id" v-if="intlAirCurrentTab.id===intlAirTab.id" v-for="intlAirTab in intlAirTabs">
      <!--eslint-disable-next-line-->
      <f7-list inset class="margin no-flex" v-if="!option.filtered"
               v-for="(option, index) in showIntlAirFlights"
               :key="index">
        <f7-list-item @click="doSearchIntlAirCabin(option)">
          <f7-row no-gap>
            <f7-col width="80">
              <f7-row>
                <f7-col width="25" class="data-table-title">
                  {{option.airlineList[option.airlineList.length-1].deptTime}}
                </f7-col>
                <f7-col class="text-align-center" width="25">
                  <f7-icon f7="arrow_right"/>
                </f7-col>
                <f7-col width="50" class="data-table-title">
                  {{option.airlineList[option.airlineList.length-1].arrTime}}
                </f7-col>
              </f7-row>
              <f7-row class="text-color-gray item-subtitle">
                <f7-col width="50">
                  {{$i18nMsg(option.airlineList[option.airlineList.length-1].deptAirportCN,
                  option.airlineList[option.airlineList.length-1].deptAirportEN)}}
                  {{option.airlineList[option.airlineList.length-1].deptTerm}}
                </f7-col>
                <f7-col width="50">
                  {{$i18nMsg(option.airlineList[option.airlineList.length-1].arrAirportCN,
                  option.airlineList[option.airlineList.length-1].arrAirportEN)}}
                  {{option.airlineList[option.airlineList.length-1].arrTerm}}
                </f7-col>
              </f7-row>
              <f7-row class="text-color-gray item-subtitle">
                <f7-col width="100">
                  {{$i18nMsg(option.airlineList[option.airlineList.length-1].airwayNameCN,
                  option.airlineList[option.airlineList.length-1].airwayNameEN)}}
                  <f7-chip v-if="option.negotiatedFlag==='true'" :text="$t('common.label.corp')" color="blue"/>
                  <span :key="index" v-for="(flight, index) in option.airlineList[option.airlineList.length-1].flightList">
                    | {{flight.airwayCode}}{{flight.flightNo}}
                  </span>
                </f7-col>
              </f7-row>
            </f7-col>
            <f7-col width="20">
              <f7-row>
                <f7-col class="data-table-title text-color-orange text-align-right"
                        v-if="parseFloat(option.containTaxPrice)===parseFloat(resultData.lowestValue)">
                  ¥{{option.containTaxPrice}}
                </f7-col>
                <f7-col class="data-table-title text-color-blue text-align-right"
                        v-if="parseFloat(option.containTaxPrice)!==parseFloat(resultData.lowestValue)">
                  ¥{{option.containTaxPrice}}
                </f7-col>
              </f7-row>
            </f7-col>
          </f7-row>
        </f7-list-item>
      </f7-list>
    </div>
  </f7-page>
</template>

<script>
import { mapGetters } from 'vuex'
import uniqBy from 'lodash/uniqBy'
import cloneDeep from 'lodash/cloneDeep'
import orderBy from 'lodash/orderBy'
import IntlairService from '../../../services/intlair/IntlairService'
import { ProductType } from '../../../consts/OrderConsts'

const searchParamsKey = 'IntlAir/intlAirSearchParam'
const selectedSegmentKey = 'IntlAir/selectedSegment'
const cacheResultDataKey = 'IntlAir/cacheResultData'
const firstCurrentTabKey = 'IntlAir/firstCurrentTab'
const lastCurrentTabKey = 'IntlAir/lastCurrentTab'
export default {
  name: 'intlair-search-last-result',
  data () {
    const intlAirDefaultSetup = this.$defaultSetup(ProductType.IntlAir)
    const intlAirTabs = IntlairService.getIntlAirTabs(intlAirDefaultSetup.defaultSelectShowTab ? intlAirDefaultSetup.defaultSelectShowTab : '1|2|3')
    const sortConditions = [{
      id: 'containTaxPrice asc',
      label: this.$t('air.label.priceAsc')
    }, {
      id: 'containTaxPrice desc',
      label: this.$t('air.label.priceDesc')
    }, {
      id: 'deptTime asc',
      label: this.$t('air.label.dateAsc')
    }, {
      id: 'deptTime desc',
      label: this.$t('air.label.dateDesc')
    }, {
      id: 'durations asc',
      label: this.$t('air.label.durationAsc')
    }, {
      id: 'durations desc',
      label: this.$t('air.label.durationDesc')
    }]
    return {
      config: {
        searching: false,
        orderBy: intlAirDefaultSetup.defaultSortType === 'deptTime' ? sortConditions[2] : sortConditions[0],
        isBook: false
      },
      intlAirTabs,
      sortConditions,
      intlAirCurrentTab: intlAirTabs[intlAirTabs.length - 1],
      resultSegments: {},
      showIntlAirFlights: [],
      resultData: {},
      filterConditions: [],
      intlAirSearchParam: null,
      selectedSegment: null
    }
  },
  computed: {
    ...mapGetters([searchParamsKey, selectedSegmentKey, cacheResultDataKey, firstCurrentTabKey, lastCurrentTabKey])
  },
  watch: {
    filterConditions: {
      handler: function (v) {
        let showIntlAirFlights = this.showIntlAirFlights
        let allianceFilters = []
        let airlineFilters = []
        let stopFlagFilters = []
        let stopAirPortFilters = []
        let deptAirPortFilters = []
        let arrAirPortFilters = []
        v.forEach(m => {
          if (m.id === 'alliance') {
            allianceFilters = m.items.filter(item => {
              return item.selected === true
            }).map(item => item.id)
          }
          if (m.id === 'airline') {
            airlineFilters = m.items.filter(item => {
              return item.selected === true
            }).map(item => item.id)
          }
          if (m.id === 'stopFlag') {
            stopFlagFilters = m.items.filter(item => {
              return item.selected === true
            }).map(item => item.id)
          }
          if (m.id === 'stopAirPort') {
            stopAirPortFilters = m.items.filter(item => {
              return item.selected === true
            }).map(item => item.id)
          }
          if (m.id === 'deptAirPort') {
            deptAirPortFilters = m.items.filter(item => {
              return item.selected === true
            }).map(item => item.id)
          }
          if (m.id === 'arrAirPort') {
            arrAirPortFilters = m.items.filter(item => {
              return item.selected === true
            }).map(item => item.id)
          }
        })
        showIntlAirFlights.forEach(option => {
          this.filterFlight(option, allianceFilters, airlineFilters, stopFlagFilters, stopAirPortFilters, deptAirPortFilters, arrAirPortFilters)
        })
        this.showIntlAirFlights = showIntlAirFlights
        this.doSortFlights()
        this.$forceUpdate()
      },
      deep: true
    },
    'config.orderBy': function () {
      this.doSortFlights()
    },
    intlAirCurrentTab () {
      this.calcFilterCondition()
    }
  },
  methods: {
    doSortFlights () {
      if (this.showIntlAirFlights) {
        const orders = this.config.orderBy.id.split(/\s+/)
        this.showIntlAirFlights.forEach(option => {
          if (!option.filtered) {
            if (option.airlineList[option.airlineList.length - 1].flightList.length > 1) {
              option.deptTime = option.airlineList[option.airlineList.length - 1].flightList[0].deptTime + option.airlineList[option.airlineList.length - 1].flightList[1].arrTime
            } else {
              option.deptTime = option.airlineList[option.airlineList.length - 1].flightList[0].deptTime + option.airlineList[option.airlineList.length - 1].flightList[0].arrTime
            }
            option.durations = option.airlineList[option.airlineList.length - 1].durationTimeL
          }
        })
        this.$nextTick(() => {
          this.showIntlAirFlights = orderBy(this.showIntlAirFlights, [orders[0]], [orders[1]])
        })
      }
    },
    filterFlight (option, alliances, airlines, stopFlags, stopAirPorts, deptAirPorts, arrAirPorts) {
      option.filtered = false
      option.airlineList[option.airlineList.length - 1].flightList.forEach((flight) => {
        if (airlines.length > 0 && airlines.indexOf(flight.airwayCode) < 0) {
          option.filtered = true
        }
        if (alliances.length > 0 && alliances.indexOf(flight.alliance) < 0) {
          option.filtered = true
        }
      })
      if (stopFlags.length > 0 && ((stopFlags.indexOf('0') > -1 && stopFlags.indexOf('1') < 0 && option.airlineList[option.airlineList.length - 1].stopList.length > 0) || (stopFlags.indexOf('1') > -1 && stopFlags.indexOf('0') < 0 && option.airlineList[option.airlineList.length - 1].stopList.length === 0))) {
        option.filtered = true
      }
      if (stopAirPorts.length > 0 && (option.airlineList[option.airlineList.length - 1].stopList.length === 0 || stopAirPorts.indexOf(option.airlineList[option.airlineList.length - 1].stopList[0].code) < 0)) {
        option.filtered = true
      }
      if (deptAirPorts.length > 0 && deptAirPorts.indexOf(option.airlineList[option.airlineList.length - 1].flightList[0].deptAirport)) {
        option.filtered = true
      }
      if (arrAirPorts.length > 0 && arrAirPorts.indexOf(option.airlineList[option.airlineList.length - 1].flightList[option.airlineList[option.airlineList.length - 1].flightList.length - 1].arrAirport)) {
        option.filtered = true
      }
    },
    calcFilterCondition () {
      const alliances = []
      const airlines = []
      const stopFlag = []
      const stopAirPort = []
      const deptAirPort = []
      const arrAirPort = []
      this.showIntlAirFlights = this.resultSegments[this.intlAirCurrentTab.id - 1]
      if (this.showIntlAirFlights) {
        this.showIntlAirFlights.forEach((option) => {
          option.airlineList[option.airlineList.length - 1].flightList.forEach((flight) => {
            if (flight.alliance !== '0') {
              alliances.push({
                id: flight.alliance,
                label: IntlairService.getIntlAirAlliance(flight.alliance),
                selected: false
              })
            }
            airlines.push({
              id: flight.airwayCode,
              label: this.$i18nMsg(flight.airwayNameCN, flight.airwayNameEN),
              selected: false
            })
          })
          const searchParam = this.intlAirSearchParam
          if (searchParam.intlAirSearchDto.owNoStopFlag !== '1') {
            stopFlag.push({
              id: '0',
              label: this.$t('air.label.nostop'),
              selected: false
            })
            stopFlag.push({
              id: '1',
              label: this.$t('air.label.stop'),
              selected: false
            })
            if (option.airlineList[option.airlineList.length - 1].stopList.length > 0) {
              stopAirPort.push({
                id: option.airlineList[option.airlineList.length - 1].stopList[0].code,
                label: this.$i18nMsg(option.airlineList[option.airlineList.length - 1].stopList[0].nameCN, option.airlineList[option.airlineList.length - 1].stopList[0].nameEN),
                selected: false
              })
            }
          }
          deptAirPort.push({
            id: option.airlineList[option.airlineList.length - 1].flightList[0].deptAirport,
            label: this.$i18nMsg(option.airlineList[option.airlineList.length - 1].flightList[0].deptAirportCN, option.airlineList[option.airlineList.length - 1].flightList[0].deptAirportEN),
            selected: false
          })
          arrAirPort.push({
            id: option.airlineList[option.airlineList.length - 1].flightList[option.airlineList[option.airlineList.length - 1].flightList.length - 1].arrAirport,
            label: this.$i18nMsg(option.airlineList[option.airlineList.length - 1].flightList[option.airlineList[option.airlineList.length - 1].flightList.length - 1].arrAirportCN, option.airlineList[option.airlineList.length - 1].flightList[option.airlineList[option.airlineList.length - 1].flightList.length - 1].arrAirportEN),
            selected: false
          })
        })

        this.filterConditions = []
        if (alliances.length > 0) {
          this.filterConditions.push({
            id: 'alliance',
            label: this.$t('air.label.alliance'),
            items: uniqBy(alliances, 'id')
          })
        }
        if (airlines.length > 0) {
          this.filterConditions.push({
            id: 'airline',
            label: this.$t('air.label.airline'),
            items: uniqBy(airlines, 'id')
          })
        }
        if (stopFlag.length > 0) {
          this.filterConditions.push({
            id: 'stopFlag',
            label: this.$t('air.label.stopFlag'),
            items: uniqBy(stopFlag, 'id')
          })
        }
        if (stopAirPort.length > 0) {
          this.filterConditions.push({
            id: 'stopAirPort',
            label: this.$t('air.label.stopAirPort'),
            items: uniqBy(stopAirPort, 'id')
          })
        }
        if (deptAirPort.length > 0) {
          this.filterConditions.push({
            id: 'deptAirPort',
            label: this.$t('air.label.deptAirPort'),
            items: uniqBy(deptAirPort, 'id')
          })
        }
        if (arrAirPort.length > 0) {
          this.filterConditions.push({
            id: 'arrAirPort',
            label: this.$t('air.label.arrAirPort'),
            items: uniqBy(arrAirPort, 'id')
          })
        }
        this.doSortFlights()
      }
    },
    doSearchIntlAir () {
      const searchParam = this.intlAirSearchParam
      searchParam.policyApplied = true
      this.config.searching = true
      IntlairService.searchIntlAir(searchParam).then(data => {
        this.config.searching = false
        this.resultData = data.resultData
        this.$store.dispatch('IntlAir/cacheResultData', this.resultData)
        // IndexedDB.openDB('intlAirResult', 1, null, {
        //   name: 'cacheResult',
        //   key: 'cacheResult'
        // }, function (db) {
        //   IndexedDB.addData(db, 'cacheResult', data.resultData)
        // })
        this.processResultData()
      })
    },
    processResultData () {
      if (this.resultData) {
        let lowestFlights = []
        let corporateFlights = []
        let intlAirFlights = []
        this.resultData['lowestFlights'].forEach(optionFormat => {
          optionFormat.optionList.forEach(option => {
            lowestFlights.push(option)
          })
        })
        this.resultData['corporateFlights'].forEach(optionFormat => {
          optionFormat.optionList.forEach(option => {
            corporateFlights.push(option)
          })
        })
        this.resultData['intlAirFlights'].forEach(optionFormat => {
          optionFormat.optionList.forEach(option => {
            intlAirFlights.push(option)
          })
        })
        this.resultSegments[0] = lowestFlights
        this.resultSegments[1] = corporateFlights
        this.resultSegments[2] = intlAirFlights
      }
      this.calcFilterCondition()
    },
    processReturnResultData (optionList) {
      if (optionList) {
        let lowestFlights = []
        let corporateFlights = []
        let intlAirFlights = []
        optionList.forEach((option) => {
          if (parseFloat(this.resultData.lowestValue) === (parseFloat(option.containTaxPrice))) {
            lowestFlights.push(option)
          }
          if (option.negotiatedFlag === 'true') {
            corporateFlights.push(option)
          }
          intlAirFlights.push(option)
        })
        this.resultSegments[0] = lowestFlights
        this.resultSegments[1] = corporateFlights
        this.resultSegments[2] = intlAirFlights
      }
      this.calcFilterCondition()
    },
    doSearchIntlAirCabin (option) {
      this.$coreShowLoading()
      this.$store.dispatch('IntlAir/allCabin')
      this.$store.dispatch('IntlAir/selectedOption', option)
      this.$store.dispatch('IntlAir/lastCurrentTab', this.intlAirCurrentTab)
      this.$goto('/intlair/searchIntlAirCabin')
    },
    backIntlairSearch () {
      if (this.intlAirSearchParam.intlAirSearchDto.interFlightType === 'OW') {
        this.$store.dispatch('IntlAir/storeIntlAirSearchParam')
        this.$store.dispatch('IntlAir/cacheResultData')
      } else {
        this.$store.dispatch('IntlAir/selectedSegment')
        this.$store.dispatch('IntlAir/lastCurrentTab')
      }
    }
  },
  mounted () {
    if (this[searchParamsKey]) {
      this.intlAirSearchParam = cloneDeep(this[searchParamsKey])
      if (this[lastCurrentTabKey]) {
        this.intlAirCurrentTab = cloneDeep(this[lastCurrentTabKey])
      } else if (this[firstCurrentTabKey]) {
        this.intlAirCurrentTab = cloneDeep(this[firstCurrentTabKey])
      }
      if (this.intlAirSearchParam.intlAirSearchDto.interFlightType === 'OW') {
        if (this[cacheResultDataKey]) {
          this.resultData = cloneDeep(this[cacheResultDataKey])
          this.processResultData()
        } else {
          this.doSearchIntlAir()
        }
        // let result = null
        // IndexedDB.openDB('intlAirResult', 1, null, {
        //   name: 'cacheResult',
        //   key: 'cacheResult'
        // }, (db) => {
        //   IndexedDB.getData(db, 'cacheResult', (resultData) => {
        //     result = cloneDeep(resultData)
        //     if (result) {
        //       this.resultData = cloneDeep(result)
        //       this.processResultData()
        //       this.calcFilterCondition()
        //     } else {
        //       this.doSearchIntlAir()
        //     }
        //   })
        // })
      } else {
        if (this[selectedSegmentKey]) {
          this.selectedSegment = cloneDeep(this[selectedSegmentKey])
          if (this[cacheResultDataKey]) {
            this.resultData = cloneDeep(this[cacheResultDataKey])
            this.processReturnResultData(this.selectedSegment)
          }
          // let result = null
          // IndexedDB.openDB('intlAirResult', 1, null, {
          //   name: 'cacheResult',
          //   key: 'cacheResult'
          // }, (db) => {
          //   IndexedDB.getData(db, 'cacheResult', (resultData) => {
          //     result = cloneDeep(resultData)
          //     if (result) {
          //       this.resultData = cloneDeep(result)
          //       this.processReturnResultData(this.selectedSegment)
          //     }
          //   })
          // })
        } else {
          this.$back('/search/intlair', { force: true })
        }
      }
    } else {
      this.$back('/search/intlair', { force: true })
    }
  }
}
</script>

<style scoped>

</style>
