<template>
  <f7-page page-content class="intelAirList">
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"
                   @back-click="backIntlairSearch"></f7-nav-left>
      <f7-nav-title>
        <div v-if="intlAirSearchParam">
          {{$i18nMsg(intlAirSearchParam.intlAirSearchDto.departPort.nameCN, intlAirSearchParam.intlAirSearchDto.departPort.nameEN)}}
          <f7-icon v-if="selectedOption.airlineList.length > 1" material="swap_horizontal_circle"></f7-icon>
          <f7-icon v-if="selectedOption.airlineList.length === 1" f7="arrow_right"/>
          {{$i18nMsg(intlAirSearchParam.intlAirSearchDto.arrivalPort.nameCN, intlAirSearchParam.intlAirSearchDto.arrivalPort.nameEN)}}
        </div>
        <div v-if="!intlAirSearchParam">
          {{$t('air.label.intl')}}
        </div>
      </f7-nav-title>
    </f7-navbar>
    <div :key="index" v-for="(airline, index) in selectedOption.airlineList">
      <f7-block-title>
        <f7-row no-gap class="flightInfo">
          <f7-col width="40">
            <f7-chip color="blue" :text="$t('air.label.departure')" v-if="index===0 && intlAirSearchParam.intlAirSearchDto.interFlightType==='RT'"/>
            <f7-chip color="green" :text="$t('air.label.return')" v-if="index===1 && intlAirSearchParam.intlAirSearchDto.interFlightType==='RT'"/>
            {{airline.flightList[0].deptDate}}
          </f7-col>
          <f7-col width="40" v-if="index===0">
            {{$i18nMsg(intlAirSearchParam.intlAirSearchDto.departPort.nameCN, intlAirSearchParam.intlAirSearchDto.departPort.nameEN)}}
            →
            {{$i18nMsg(intlAirSearchParam.intlAirSearchDto.arrivalPort.nameCN, intlAirSearchParam.intlAirSearchDto.arrivalPort.nameEN)}}
          </f7-col>
          <f7-col width="40" v-if="index===1">
            {{$i18nMsg(intlAirSearchParam.intlAirSearchDto.arrivalPort.nameCN, intlAirSearchParam.intlAirSearchDto.arrivalPort.nameEN)}}
            →
            {{$i18nMsg(intlAirSearchParam.intlAirSearchDto.departPort.nameCN, intlAirSearchParam.intlAirSearchDto.departPort.nameEN)}}
          </f7-col>
          <f7-col width="20" class="text-align-right" style="text-transform:lowercase;">
            {{airline.durationHour}}h{{airline.durationMin}}m
          </f7-col>
        </f7-row>
      </f7-block-title>
      <f7-list class="no-flex">
        <template>
          <f7-list-item>
            <div :key="index" v-for="(flight, index) in airline.flightList">
              <f7-row no-gap class="transitInfo item-subtitle" v-if="index===1 && airline.stopList.length>0">
                <f7-col class="text-align-center text-color-purple" width="60">
                  {{$i18nMsg(airline.stopList[0].nameCN, airline.stopList[0].nameEN)}}({{$i18nMsg(airline.stopList[0].cityNameCN, airline.stopList[0].cityNameEN)}})
                </f7-col>
                <f7-col class="text-color-gray" width="20">{{$t('air.label.transitStay')}}</f7-col>
                <f7-col class="text-color-gray" width="20">
                  {{airline.stopList[0].durationHour}}h{{airline.stopList[0].durationMin}}m
                </f7-col>
              </f7-row>
              <f7-row no-gap>
                <f7-col width="70">
                  <f7-row>
                    <f7-col width="25" class="data-table-title">
                      {{flight.deptTime}}
                      <span>{{flight.deptDate|date('MM-DD')}}</span>
                    </f7-col>
                    <f7-col class="flightDuration" width="25">
                      {{flight.durationHour}}h{{flight.durationMin}}m
                    </f7-col>
                    <f7-col width="50" class="data-table-title">
                      {{flight.arrTime}}
                      <span>{{flight.arrDate|date('MM-DD')}}</span>
                    </f7-col>
                  </f7-row>
                  <f7-row class="text-color-gray item-subtitle">
                    <f7-col width="50">
                      {{$i18nMsg(flight.deptAirportCN, flight.deptAirportEN)}}{{flight.deptTerm}}
                    </f7-col>
                    <f7-col width="50">
                      {{$i18nMsg(flight.arrAirportCN, flight.arrAirportEN)}}{{flight.arrTerm}}
                    </f7-col>
                  </f7-row>
                </f7-col>
                <f7-col width="30" class="flightNo">
                  <f7-row class="item-subtitle">
                    <f7-col>
                      {{$i18nMsg(flight.airwayNameCN, flight.airwayNameEN)}}
                      {{flight.airwayCode}}{{flight.flightNo}}({{flight.equipment}})
                      <f7-chip v-if="flight.shareAirwayCode" color="blue" :text="$t('air.label.share')"/>
                    </f7-col>
                  </f7-row>
                </f7-col>
              </f7-row>
            </div>
          </f7-list-item>
        </template>
      </f7-list>
    </div>
    <f7-block-title class="cabinInfo">{{$t('air.label.cabinType')}}</f7-block-title>
    <f7-list class="no-flex">
      <li v-if="allCabin.length===0">
        <f7-row class="item-content no-gap">
          <f7-col width="60">
            <f7-row>
              <f7-col>
                {{selectedOption.physicalCabins|cabinTypeI}} {{selectedOption.cabins}}
                <div style="display: inline-block;">
                  <f7-chip v-if="selectedOption.negotiatedFlag==='true'" :text="$t('common.label.corp')" color="blue"
                           style="margin-top: -3px;"/>
                  <f7-chip v-if="selectedOption.residueFlag==='0'" :text="$t('air.label.seatShortage')" color="red"
                           style="margin-top: -3px;"/>
                </div>
              </f7-col>
            </f7-row>
          </f7-col>
          <f7-col width="25" class="text-color-orange text-align-right padding-right"
                  v-if="parseFloat(selectedOption.containTaxPrice)===parseFloat(selectedResultData.lowestValue)">
            ¥{{selectedOption.containTaxPrice}}
          </f7-col>
          <f7-col width="25" class="text-color-blue text-align-right padding-right"
                  v-if="parseFloat(selectedOption.containTaxPrice)!==parseFloat(selectedResultData.lowestValue)">
            ¥{{selectedOption.containTaxPrice}}
          </f7-col>
          <f7-col width="15">
            <f7-button @click="toBookIntlAir(selectedOption)" :text="$t('common.label.book')"></f7-button>
          </f7-col>
        </f7-row>
      </li>
      <li v-for="(option, index) in allCabin" :key="index">
        <f7-row class="item-content no-gap">
          <f7-col width="60">
            <f7-row>
              <f7-col>
                {{option.physicalCabins|cabinTypeI}} {{option.cabins}}
                <div style="display: inline-block;">
                  <f7-chip v-if="option.negotiatedFlag==='true'" :text="$t('common.label.corp')" color="blue"
                           style="margin-top: -3px;"/>
                  <f7-chip v-if="option.residueFlag==='0'" :text="$t('air.label.seatShortage')" color="red"
                           style="margin-top: -3px;"/>
                </div>
              </f7-col>
            </f7-row>
          </f7-col>
          <f7-col width="25" class="text-color-orange text-align-right padding-right"
                  v-if="parseFloat(option.containTaxPrice)===parseFloat(selectedResultData.lowestValue)">
            ¥{{option.containTaxPrice}}
          </f7-col>
          <f7-col width="25" class="text-color-blue text-align-right padding-right"
                  v-if="parseFloat(option.containTaxPrice)!==parseFloat(selectedResultData.lowestValue)">
            ¥{{option.containTaxPrice}}
          </f7-col>
          <f7-col width="15">
            <f7-button @click="toBookIntlAir(option)" :text="$t('common.label.book')"></f7-button>
          </f7-col>
        </f7-row>
      </li>
    </f7-list>
  </f7-page>
</template>

<script>
import { mapGetters } from 'vuex'
import cloneDeep from 'lodash/cloneDeep'
import IntlairService from '../../../services/intlair/IntlairService'
import merge from 'lodash/merge'
import PolicyControlService from '../../../services/policy/PolicyControlService'
import { ProductType } from '../../../consts/OrderConsts'

const searchParamsKey = 'IntlAir/intlAirSearchParam'
const cacheResultDataKey = 'IntlAir/cacheResultData'
const selectedOptionKey = 'IntlAir/selectedOption'
const allCabinKey = 'IntlAir/allCabin'
export default {
  name: 'intlair-search-cabin',
  data () {
    const intlAirDefaultSetup = this.$defaultSetup(ProductType.IntlAir)
    const companyDefaultSetup = this.$defaultSetup(ProductType.Company)
    this.$processBookEnable(intlAirDefaultSetup)
    const checkMode = intlAirDefaultSetup.checkMode ? intlAirDefaultSetup.checkMode : '10'
    return {
      selectedResultData: {},
      selectedOption: {},
      resultData: {},
      intlAirSearchParam: null,
      allCabin: [],
      travellers: [],
      intlAirDefaultSetup,
      companyDefaultSetup,
      checkMode
    }
  },
  computed: {
    ...mapGetters([searchParamsKey, cacheResultDataKey, selectedOptionKey, allCabinKey])
  },
  methods: {
    doSearchIntlAirAllCabin () {
      const searchParam = this.intlAirSearchParam
      searchParam.selectOption = this.selectedOption
      IntlairService.doSearchIntlAirAllCabin(searchParam).then(data => {
        this.resultData = data.resultData
        if (this.resultData && this.resultData['flightCabinMapList']) {
          this.allCabin = this.resultData['flightCabinMapList']
          this.$store.dispatch('IntlAir/allCabin', this.allCabin)
        }
      })
    },
    toBookIntlAir (option) {
      const bookParam = this.processBookParam(option)
      if (this.validateBeforeBook(bookParam)) {
        PolicyControlService.doPolicyControl(IntlairService.checkIntlAirBookPolicys, bookParam, (resultData, reasonCodes, handlers) => {
          console.info(resultData, reasonCodes, handlers)
          bookParam.intlAirBookingDto.bookOption.resultHandlers = handlers
          if (this.checkMode === '00') {
            this.toBookIntlAirNext(bookParam)
          } else {
            if (this.checkMode === '10' || this.checkMode === '11') {
              this.checkIntlAV(bookParam, option)
            } else if (this.checkMode === '01') {
              this.checkOptionPricing(bookParam, option)
            } else {
              this.$coreAlert(this.$i18nBundle('air.msg.noseat'))
            }
          }
        }, { showReasonCode: false })
      }
    },
    validateBeforeBook (bookParam) {
      if (!this.intlAirDefaultSetup.bookEnable || (this.companyDefaultSetup.bookEnableNeedExternalOrder && !bookParam.intlAirBookingDto.externalOrderNo)) {
        this.$coreError(this.$t('common.msg.bookDisabled'))
        return false
      }
      return true
    },
    checkIntlAV (bookParam, option) {
      const searchParam = this.intlAirSearchParam
      searchParam.selectOption = option
      IntlairService.checkIntlAV(searchParam).then(data => {
        if (data && data.success && data.resultData.responseHead.resultMessage) {
          const avResult = data.resultData.responseHead.resultMessage
          console.info(avResult)
          const resultJson = JSON.parse(avResult)
          const avHasSeat = '123456789A'
          let checkIntlavFlag = '0'
          for (let key in resultJson) {
            if (avHasSeat.indexOf(resultJson[key]) < 0) {
              checkIntlavFlag = '1'
            }
          }
          if (checkIntlavFlag === '0') {
            if (this.checkMode === '11') {
              this.checkOptionPricing(bookParam, option)
            } else {
              this.toBookIntlAirNext(bookParam)
            }
          } else {
            this.$coreAlert(this.$i18nBundle('air.msg.noseat'))
          }
        } else {
          this.$coreAlert(this.$i18nBundle('air.msg.noseat'))
        }
      })
    },
    checkOptionPricing (bookParam, option) {
      const searchParam = this.intlAirSearchParam
      searchParam.selectOption = option
      IntlairService.checkOptionPricing(searchParam).then(data => {
        if (data && data.success) {
          if (data.resultData.price && data.resultData.tax) {
            bookParam.intlAirBookingDto.bookOption.price = data.resultData.price
            bookParam.intlAirBookingDto.bookOption.tax = data.resultData.tax
            bookParam.intlAirBookingDto.bookOption.containTaxPrice = parseFloat(data.resultData.price) + parseFloat(data.resultData.tax)
          }
          this.toBookIntlAirNext(bookParam)
        } else {
          this.$coreAlert(this.$i18nBundle('air.msg.noseat'))
        }
      })
    },
    toBookIntlAirNext (bookParam) {
      this.$store.dispatch('IntlAir/storeIntlAirBookingParam', bookParam)
      this.$goto('/intlair/bookIntlAir')
    },
    processBookParam (option) {
      let bookParam = IntlairService.getBookParam()
      merge(bookParam.intlAirBookingDto, {
        bookOption: option,
        llfOption: this.selectedResultData.llfOptionVO
      })
      bookParam.intlAirBookingDto.bookOption.price = bookParam.intlAirBookingDto.bookOption.amount
      bookParam.intlAirBookingDto.bookOption.oldPrice = bookParam.intlAirBookingDto.bookOption.amount
      bookParam.intlAirBookingDto.bookOption.interFlightType = this.intlAirSearchParam.intlAirSearchDto.interFlightType
      bookParam.intlAirBookingDto.bookOption['@class'] = 'com.citsamex.app.spi.data.dto.air.international.IbeBookFlightOptionDto'
      return bookParam
    },
    backIntlairSearch () {
      this.$store.dispatch('IntlAir/selectedOption')
      this.$store.dispatch('IntlAir/allCabin')
    }
  },
  mounted () {
    if (this[searchParamsKey]) {
      this.intlAirSearchParam = cloneDeep(this[searchParamsKey])
      if (this[cacheResultDataKey]) {
        this.selectedResultData = cloneDeep(this[cacheResultDataKey])
      }
      // let result = null
      // IndexedDB.openDB('intlAirResult', 1, null, {
      //   name: 'cacheResult',
      //   key: 'cacheResult'
      // }, (db) => {
      //   IndexedDB.getData(db, 'cacheResult', (resultData) => {
      //     result = cloneDeep(resultData)
      //     console.log(result)
      //     if (result) {
      //       this.selectedResultData = cloneDeep(result)
      //     }
      //   })
      // })
      if (this[selectedOptionKey]) {
        this.selectedOption = cloneDeep(this[selectedOptionKey])
        if (this[allCabinKey]) {
          this.allCabin = cloneDeep(this[allCabinKey])
        } else {
          this.doSearchIntlAirAllCabin()
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
