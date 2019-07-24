<template>
  <f7-page>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"/>
      <f7-nav-title>{{$t('air.label.intl')}}</f7-nav-title>
      <f7-nav-right>
        <f7-link @click="doBookIntlAir">{{$t('common.label.book')}}</f7-link>
      </f7-nav-right>
    </f7-navbar>
    <f7-block-title>{{$t('air.label.chosenRoute')}}</f7-block-title>
    <f7-list class="no-flex airDetail" v-if="bookParam">
      <template>
        <f7-list-item :key="index" v-for="(airline, index) in bookParam.intlAirBookingDto.bookOption.airlineList" class="airInfo">
          <f7-row>
            <f7-col class="airType">
              <f7-chip v-if="intlAirSearchParam.intlAirSearchDto.interFlightType==='RT' && index===0" :text="$t('air.label.departure')" color="blue"/>
              <f7-chip v-if="intlAirSearchParam.intlAirSearchDto.interFlightType==='RT' && index===1" :text="$t('air.label.return')" color="green"/>
              <f7-chip v-if="intlAirSearchParam.intlAirSearchDto.interFlightType==='OW'" :text="$t('air.label.ow')" color="blue"/>
            </f7-col>
          </f7-row>
          <f7-row class="padding-bottom">
            <f7-col width="40" class="text-align-right">
              <f7-row>
                <f7-col>
                  {{airline.flightList[0].deptDate|date('MM-DD')}}
                </f7-col>
              </f7-row>
              <f7-row class="data-table-title">
                <f7-col>
                  {{airline.flightList[0].deptTime}}
                </f7-col>
              </f7-row>
              <f7-row>
                <f7-col>
                  {{$i18nMsg(airline.flightList[0].deptAirportCN, airline.flightList[0].deptAirportEN)}}
                  {{airline.flightList[0].deptTerm}}
                </f7-col>
              </f7-row>
            </f7-col>
            <f7-col width="20" class="text-align-center flightDuration">
              {{airline.durationHour}}h{{airline.durationMin}}m
            </f7-col>
            <f7-col width="40" class="text-align-left">
              <f7-row>
                <f7-col>
                  {{airline.flightList[airline.flightList.length-1].arrDate|date('MM-DD')}}
                </f7-col>
              </f7-row>
              <f7-row class="data-table-title">
                <f7-col>
                  {{airline.flightList[airline.flightList.length-1].arrTime}}
                </f7-col>
              </f7-row>
              <f7-row>
                <f7-col>
                  {{$i18nMsg(airline.flightList[airline.flightList.length-1].arrAirportCN, airline.flightList[airline.flightList.length-1].arrAirportEN)}}
                  {{airline.flightList[airline.flightList.length-1].arrTerm}}
                </f7-col>
              </f7-row>
            </f7-col>
          </f7-row>
          <f7-row class="padding-bottom">
            <f7-col class="text-align-center text-color-gray item-subtitle">
              {{$i18nMsg(airline.airwayNameCN, airline.airwayNameEN)}}(
              <span :key="index" v-for="(flight, index) in airline.flightList">
                                <span v-if="index!==0">|</span>{{flight.airwayCode}}{{flight.flightNo}}
                            </span>
              )
            </f7-col>
          </f7-row>
        </f7-list-item>
      </template>
      <f7-list-item>
        <f7-row no-gap>
          <f7-col width="80" style="white-space: nowrap;">
            <strong class="data-table-title">¥{{bookParam.intlAirBookingDto.bookOption.containTaxPrice}}</strong>
            ({{bookParam.intlAirBookingDto.bookOption.cabins}}{{bookParam.intlAirBookingDto.bookOption.physicalCabins|cabinTypeI}})
          </f7-col>
          <f7-col width="20" class="text-align-right" style="margin-top: 5px;">
            <f7-link @click="getInterRuleData(bookParam.intlAirBookingDto.bookOption)">
              {{$t('air.label.ruleAndLimit')}}
            </f7-link>
          </f7-col>
        </f7-row>
      </f7-list-item>
      <li class="airReason">
        <policy-reason close-on-select auto-selected v-model="bookParam.intlAirBookingDto.bookOption.resultHandlers"
                       @change="selectedReasonCodes(bookParam.intlAirBookingDto.bookOption)"/>
      </li>
    </f7-list>
    <f7-block-title>{{$t('common.label.traveller')}}</f7-block-title>
    <intlair-traveller-view v-model="travellers" v-if="bookParam" :intl-air-booking-dto="bookParam.intlAirBookingDto"
                            :intl-air-search-param="intlAirSearchParam"/>
    <!--<f7-toolbar class="fixed-bottom" position="bottom">
      <f7-button large raised fill @click="doBookIntlAir">预订</f7-button>
    </f7-toolbar>-->
  </f7-page>
</template>

<script>
import { mapGetters } from 'vuex'
import IntlairService from '../../../services/intlair/IntlairService'
import ProductService from '../../../services/products/ProductService'
import IntlairTravellerView from '../traveller/intlair-traveller-view'
import cloneDeep from 'lodash/cloneDeep'
import PolicyReason from '../../../components/policy/policy-reason'
import { ProductType } from '../../../consts/OrderConsts'

const searchParamsKey = 'IntlAir/intlAirSearchParam'
const bookParamKey = 'IntlAir/intlAirBookingParam'
const bookTravellersKey = 'Order/bookTravellers'

export default {
  name: 'intlair-book',
  components: { PolicyReason, IntlairTravellerView },
  data () {
    return {
      intlAirSearchParam: null,
      bookParam: null,
      travellers: []
    }
  },
  watch: {},
  computed: {
    ...mapGetters([bookParamKey, 'currentLoginUser', bookTravellersKey, searchParamsKey])
  },
  methods: {
    selectedReasonCodes (option) {
      const reasonCodes = []
      option.resultHandlers.forEach(resultHandler => {
        if (resultHandler.selectedReason) {
          reasonCodes.push(resultHandler.selectedReason)
        }
      })
      this.$set(option, 'reasonCode', reasonCodes.join(','))
    },
    doBookIntlAir () {
      const bookParam = this.bookParam
      bookParam.policyApplied = true
      bookParam.policyUserId = this.$store.getters['Order/basicSearchParam'].policyUserId
      let isSelectedReason = true
      if (bookParam.intlAirBookingDto.bookOption.resultHandlers) {
        bookParam.intlAirBookingDto.bookOption.resultHandlers.forEach(resultHandler => {
          if (!resultHandler.selectedReason) {
            isSelectedReason = false
          }
        })
      }
      if (!isSelectedReason) {
        this.$coreAlert(this.$i18nBundle('common.msg.policyReasonCode'))
        return
      }
      let isUserNo = false
      let isUserCertsNo = false
      this.travellers.forEach(traveller => {
        if (traveller.selected) {
          isUserNo = true
          if (!traveller.selectedCert) {
            isUserCertsNo = true
          } else {
            if ('3,5,18,19'.indexOf(traveller.selectedCert.certType) !== -1) {
              traveller.userName = traveller.userNameCN
            } else {
              traveller.userName = traveller.surname + '/' + traveller.givenName
            }
          }
        }
      })
      if (!isUserNo) {
        this.$coreAlert(this.$i18nBundle('common.msg.selectTraveller'))
        return
      }
      if (isUserCertsNo) {
        this.$coreAlert(this.$i18nBundle('common.msg.certnoNotEmpty'))
        return
      }
      if (bookParam.intlAirBookingDto.bookOption && bookParam.intlAirBookingDto.llfOption) {
        const bookOption = bookParam.intlAirBookingDto.bookOption
        const llfOption = bookParam.intlAirBookingDto.llfOption
        if ((parseInt(llfOption.price) + parseInt(llfOption.tax)) >= (parseInt(bookOption.oldPrice) + parseInt(bookOption.tax))) {
          bookParam.intlAirBookingDto.llfOption = null
        }
      }
      bookParam.intlAirBookingDto.travelers = cloneDeep(this.travellers)
      bookParam.intlAirBookingDto.travelers.forEach(traveller => {
        let memberships = []
        traveller.flightSegmentList.forEach(flight => {
          if (flight.selectedMembership) {
            flight.selectedMembership.memberCardNo = flight.airwayCode + flight.flightNo + '-' + flight.selectedMembership.memberCardNo
            memberships.push(flight.selectedMembership)
          }
        })
        traveller.memberships = memberships
        traveller.flightSegmentList = []
      })
      console.info(bookParam)
      IntlairService.bookInterFlight(bookParam).then((data) => {
        console.info(data)
        if (data && data.success) {
          if (data.resultData.taNo) {
            this.$coreAlert(this.$i18nBundle('common.msg.bookSuccess')).then(() => {
              this.$gotoAxoTaPage(data.resultData.taNo, ProductType.IntlAir, true)
            })
          } else if (data.resultData.doubleBookTravelers) {
            let doubleBookMessage = ''
            data.resultData.doubleBookTravelers.forEach(traveller => {
              doubleBookMessage += this.$i18nMsg(traveller.userNameCN, traveller.userNameEN) + this.$i18nBundle('air.msg.selectDoubleBookingDescribe') + traveller.doubleBookingTaNo + '<br/>'
            })
            this.$coreAlert(doubleBookMessage) // 这里需要添加后续处理逻辑
          }
        } else {
          if (data && data.resultData && data.resultData.responseHead.resultMessage) {
            if (data.resultData.responseHead.resultMessage === 'INTLAIR_FLIGHT_BOOK_ERROR') {
              this.$coreAlert(this.$i18nBundle(data.resultData.responseHead.resultMessage))
            } else {
              this.$coreAlert(this.$i18nBundle('air.msg.seatreservationfailed '))
            }
          } else {
            this.$coreAlert(this.$i18nBundle('air.msg.seatreservationfailed '))
          }
        }
      })
    },

    getInterRuleData (option) {
      let cabinLimit = ''
      const searchParam = this.intlAirSearchParam
      searchParam.selectOption = option
      IntlairService.getInterRuleData(searchParam).then((data) => {
        console.info(data)
        if (data && data.success) {
          if (this.intlAirSearchParam.intlAirSearchDto.interFlightType === 'RT') {
            cabinLimit += this.$i18nBundle('air.label.departure') + ': ' + '<br>'
          }
          if (data.resultData.deptRule) {
            data.resultData.deptRule.forEach((deptRule, num) => {
              if (data.resultData.deptRule.length > 1) {
                cabinLimit += this.$i18nBundle('air.label.segment') + (num + 1) + '：'
              }
              if (deptRule.errors) {
                cabinLimit += this.$i18nBundle('air.msg.unknowCabinLimit') + '<br>'
              } else {
                cabinLimit += this.$i18nMsg(deptRule.ruleValueCN, deptRule.ruleValueEN) + '<br>'
              }
            })
          } else {
            cabinLimit += this.$i18nBundle('air.msg.unknowCabinLimit') + '<br>'
          }
          if (data.resultData.arrRule.length > 0) {
            cabinLimit += '<br>' + this.$i18nBundle('air.label.segment') + ': ' + '<br>'
            data.resultData.arrRule.forEach((arrRule, num) => {
              if (data.resultData.arrRule.length > 1) {
                cabinLimit += this.$i18nBundle('air.label.segment') + (num + 1) + '：'
              }
              if (arrRule.errors) {
                cabinLimit += this.$i18nBundle('air.msg.unknowCabinLimit') + '<br>'
              } else {
                cabinLimit += this.$i18nMsg(arrRule.ruleValueCN, arrRule.ruleValueEN) + '<br>'
              }
            })
          } else {
            cabinLimit += this.$i18nBundle('air.msg.unknowCabinLimit') + '<br>'
          }
        }
        this.$coreShowSheet(cabinLimit)
      })
      if (cabinLimit !== '') {
        this.$coreShowSheet(cabinLimit)
      }
    },

    getDomAirMealLabel: function (code) {
      return this.domAirMealCache[code] || this.$i18nBundle('air.label.snacks')
    }
  },
  mounted () {
    if (this[searchParamsKey]) {
      this.intlAirSearchParam = cloneDeep(this[searchParamsKey])
      const bookParam = this[bookParamKey]
      console.info(bookParam)
      if (bookParam) {
        this.bookParam = bookParam
        ProductService.loadTravellersDetails(this[bookTravellersKey]).then(travellers => {
          console.info(travellers)
          this.travellers = travellers
          this.travellers.forEach(traveller => {
            // 根据国籍过滤掉不需要的证件，以及过期的证件
            traveller.userCerts = traveller.userCerts.filter(cert => {
              if (!cert.expiryDate || Date.parse(cert.expiryDate) >= new Date()) { // 保留没有过期时间的证件和未过期的其他证件
                if ('3,5'.indexOf(cert.certType) === -1) { // 删除身份证，军官证
                  return cert
                }
              }
            })
            // 保留航空公司常客卡
            traveller.memberships = traveller.memberships.filter(item => {
              if (item.cardType === '1') {
                return item
              }
            })
            let flightSegmentList = []
            this.bookParam.intlAirBookingDto.bookOption.airlineList.forEach((airline, index) => {
              airline.flightList.forEach((flight, index1) => {
                flight.airlineIndex = index + 1
                flight.flightIndex = index1 + 1
                flight.flightSize = airline.flightList.length
                flightSegmentList.push(flight)
              })
              traveller.flightSegmentList = flightSegmentList
            })
          })
        })
      } else {
        // 跳转回去
        this.$back('/search/searchIntlAirCabin', { force: true })
      }
    } else {
      this.$back('/search/intlair', { force: true })
    }
  }
}
</script>

<style scoped>

</style>
