<template>
  <f7-page>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"/>
      <f7-nav-title>{{$t('air.label.dom')}}</f7-nav-title>
      <f7-nav-right>
        <f7-link @click="doBookDomAir">{{$t('common.label.book')}}</f7-link>
      </f7-nav-right>
    </f7-navbar>
    <f7-block-title>{{$t('air.label.chosenRoute')}}</f7-block-title>
    <f7-list class="no-flex airDetail" v-if="bookParam">
      <template v-for="(domAirFlight, index) in bookParam.domAirBooking.selectedFlights">
        <f7-list-item :key="'segmentAirInfo' + index" class="airInfo">
          <f7-row>
            <f7-col class="airType">
              <f7-chip v-if="bookParam.domAirBooking.airType==='RT' && index===0" :text="$t('air.label.departure')" color="blue"/>
              <f7-chip v-if="bookParam.domAirBooking.airType==='RT' && index===1" :text="$t('air.label.return')" color="green"/>
              <f7-chip v-if="bookParam.domAirBooking.airType==='OW'" :text="$t('air.label.ow')" color="blue"/>
              <f7-chip v-if="bookParam.domAirBooking.airType==='MD'" :text="$t('air.label.ow', [index+1]) " color="blue"/>
            </f7-col>
          </f7-row>
          <f7-row class="padding-bottom">
            <f7-col width="40" class="text-align-right">
              <f7-row>
                <f7-col>
                  {{domAirFlight.deptDate|date('MM-DD')}}
                </f7-col>
              </f7-row>
              <f7-row class="data-table-title">
                <f7-col>
                  {{domAirFlight.deptDate|date('HH:mm')}}
                </f7-col>
              </f7-row>
              <f7-row>
                <f7-col>
                  {{$i18nMsg(domAirFlight.deptCityNameCN, domAirFlight.deptCityNameEN)}}
                  {{domAirFlight.departTerm}}
                </f7-col>
              </f7-row>
            </f7-col>
            <f7-col width="20" class="text-align-center flightDuration">
              {{domAirFlight.flightHours}}h{{domAirFlight.flightMinuts}}m
            </f7-col>
            <f7-col width="40" class="text-align-left">
              <f7-row>
                <f7-col>
                  {{domAirFlight.arrivalDate|date('MM-DD')}}
                </f7-col>
              </f7-row>
              <f7-row class="data-table-title">
                <f7-col>
                  {{domAirFlight.arrivalDate|date('HH:mm')}}
                </f7-col>
              </f7-row>
              <f7-row>
                <f7-col>
                  {{$i18nMsg(domAirFlight.arrivalCityNameCN, domAirFlight.arrivalCityNameEN)}}
                  {{domAirFlight.arrivalTerm}}
                </f7-col>
              </f7-row>
            </f7-col>
          </f7-row>
          <f7-row class="padding-bottom">
            <f7-col class="text-align-center text-color-gray item-subtitle">
              {{$i18nMsg(domAirFlight.carrierNameCN,
              domAirFlight.carrierNameEN)}}({{domAirFlight.flightCarrier}}{{domAirFlight.flightNo}})
              |
              {{$i18nMsg(domAirFlight.planeTypeDto.nameCN, domAirFlight.planeTypeDto.nameEN)||domAirFlight.airCraft}}
              {{domAirFlight.airMeal&&'|'}}
              {{domAirFlight.airMeal&&getDomAirMealLabel(domAirFlight.airMeal)}}
            </f7-col>
          </f7-row>
        </f7-list-item>
        <f7-list-item :key="'segment' + index">
          <f7-row no-gap>
            <f7-col width="80" style="white-space: nowrap;">
              <strong class="data-table-title">¥{{domAirFlight.selectedCabin.sellPrice}}</strong>
              +{{domAirFlight.taxAirport}}+{{domAirFlight.taxFuel}}
              ({{domAirFlight.selectedCabin.discountRate}}%
              /
              {{domAirFlight.selectedCabin.cabinCode}}{{domAirFlight.selectedCabin.cabinType|cabinType}})
            </f7-col>
            <f7-col width="20" class="text-align-right" style="margin-top: 5px;">
              <f7-link @click="doGetCabinLimit(domAirFlight, domAirFlight.selectedCabin)">
                {{$t('air.label.ruleAndLimit')}}
              </f7-link>
            </f7-col>
          </f7-row>
        </f7-list-item>
        <li :key="'reason' + index" class="airReason" style="margin: 0;">
          <policy-reason close-on-select
                         auto-selected
                         v-model="domAirFlight.resultHandlers"
                         @change="selectedReasonCodes(domAirFlight)"/>
        </li>
      </template>
    </f7-list>
    <div class="block block-strong">
      <span class="text-color-orange">{{$t('air.msg.certwarning')}}</span><br/>
      <span class="text-color-orange" v-if="travelPreference" v-html="$t('air.msg.likeFoodinfo')"></span>
    </div>
    <f7-block-title>{{$t('common.label.traveller')}}</f7-block-title>
    <domair-traveller-view v-model="travellers" v-if="bookParam" :dom-air-booking="bookParam.domAirBooking"/>
    <!--<f7-toolbar class="fixed-bottom" position="bottom">
      <f7-button large raised fill @click="doBookDomAir">预订</f7-button>
    </f7-toolbar>-->
    <f7-list>
      <!--<input type="checkbox" id="checkRegulation"
        @change="checkRegulation=$event.target.checked?'1':'0'"
          :checked="checkRegulation==='1'">{{$t('air.msg.disclaimerConfirm')}}-->
      <f7-list-item
      checkbox id="checkRegulation"
        @change="checkRegulation=$event.target.checked?'1':'0'"
        :checked="checkRegulation==='1'">
        <div class="item-title" slot="title" style="overflow: visible; white-space: normal;">
          {{$t('air.msg.disclaimerConfirm')}}
        </div>
      </f7-list-item>
    </f7-list>
    <div class="block block-strong" v-if="transportPath.length > 0" style="margin: -16px 0 0;">
      {{$t('air.msg.conditionsTransport')}}
      <f7-link :key="index" v-for="(flight, index) in transportPath" style="line-height: 24px; display: inline;" :href="flight.path" target="_blank" :external="true">
        {{$i18nMsg(flight.carrierNameCN, flight.carrierNameEN)}}<span v-if="index!==transportPath.length">{{$t('common.label.caesura')}}</span>
      </f7-link>
      <f7-link style="line-height: 24px; display: inline;" :href="termsPath + ($store.getters.globalConfig.currentLocale === 'zh_CN' ? 'CN/Regulation_CN.htm' : 'EN/Regulation_EN.htm')" target="_blank" :external="true">
        {{$t('air.msg.dangerousGoodsDocumentatio')}}
      </f7-link>
      <f7-link style="line-height: 24px; display: inline;" :href="termsPath + ($store.getters.globalConfig.currentLocale === 'zh_CN' ? 'CN/' : 'EN/') + 'CAAC_General_Rules.htm'" target="_blank" :external="true">
        {{$t('air.msg.caacGenaralRegulations')}}
      </f7-link>
    </div>
  </f7-page>
</template>

<script>
import { mapGetters } from 'vuex'
import DomairService from '../../../services/domair/DomairService'
import ProductService from '../../../services/products/ProductService'
import DomairTravellerView from '../traveller/domair-traveller-view'
import cloneDeep from 'lodash/cloneDeep'
import PolicyReason from '../../../components/policy/policy-reason'
import { ProductType } from '../../../consts/OrderConsts'
import orderBy from 'lodash/orderBy'

const bookParamKey = 'DomAir/bookDomFlightParam'
const bookTravellersKey = 'Order/bookTravellers'

export default {
  name: 'domair-book',
  components: { PolicyReason, DomairTravellerView },
  data () {
    return {
      bookParam: null,
      travellers: [],
      travelPreference: false,
      termsPath: process.env.VUE_APP_TERMS_PATH,
      transportPath: [],
      checkRegulation: '0',
      domAirMealCache: {
        'B': this.$i18nBundle('air.label.breakFast'),
        'L': this.$i18nBundle('air.label.lunch'),
        'D': this.$i18nBundle('air.label.dinner')
      }
    }
  },
  watch: {},
  computed: {
    ...mapGetters([bookParamKey, 'currentLoginUser', 'DomAir/cacheResultData', bookTravellersKey])
  },
  mounted () {
    const bookParam = this['DomAir/cacheResultData']
    // this.$store.dispatch('DomAir/storeSelectedDomAirSegments')
    console.info(bookParam)
    if (bookParam) {
      this.bookParam = bookParam
      ProductService.loadTravellersDetails(this[bookTravellersKey]).then(travellers => {
        console.info(travellers)
        this.travellers = travellers
        this.travellers.forEach(traveller => {
          // 根据国籍过滤掉不需要的证件，以及过期的证件
          let filteredCerts = traveller.userCerts.filter(cert => {
            if (!cert.expiryDate || Date.parse(cert.expiryDate) >= new Date()) { // 保留没有过期时间的证件（身份证等）和未过期的其他证件
              if ((traveller.nationality === 'CN' && '2,3,5'.indexOf(cert.certType) !== -1) || // 大陆旅客留下身份证，军官证，护照
                  ((traveller.nationality === 'HK' || traveller.nationality === 'MO') && cert.certType === '18') || // 香港澳门旅客留下港澳居民来往内地通行证
                  (traveller.nationality === 'TW' && cert.certType === '19') || // 台湾旅客留下台湾居民来往大陆通行证
                  ('CN,HK,MO,TW'.indexOf(traveller.nationality) === -1)) { // 其他国际保留未过期全部证件
                return cert
              }
            }
          })
          filteredCerts = orderBy(filteredCerts, ['certType'], ['desc'])
          traveller.userCerts = filteredCerts
          // 保留航空公司常客卡
          traveller.memberships = traveller.memberships.filter(item => {
            if (item.cardType === '1') {
              return item
            }
          })
          if (traveller.travelPreference && traveller.travelPreference.seatPreferenceCode !== '' &&
              traveller.travelPreference.mealPreferenceCode !== '') {
            this.travelPreference = true
          }
        })
      })
      let flights = ''
      this.bookParam.domAirBooking.selectedFlights.forEach(flight => {
        if (flights.indexOf(flight.flightCarrier) < 0 && flight.airline && flight.airline.regulationFileFlag && flight.airline.regulationFileFlag === '1') {
          flights += flight.flightCarrier + ','
          flight.path = this.termsPath + (this.$store.getters.globalConfig.currentLocale === 'zh_CN' ? 'CN/' : 'EN/') + flight.flightCarrier + '.htm'
          this.transportPath.push(flight)
        }
      })
    } else {
      // 跳转回去
      this.$back('/domair/doSearchLastDomAir', { force: true })
    }
  },
  methods: {
    selectedReasonCodes (flight) {
      const reasonCodes = []
      flight.resultHandlers.forEach(resultHandler => {
        if (resultHandler.selectedReason) {
          reasonCodes.push(resultHandler.selectedReason)
        }
      })
      this.$set(flight, 'reasonCodes', reasonCodes.join(','))
    },
    doBookDomAir () {
      if (this.checkRegulation === '0') {
        this.$coreAlert(this.$i18nBundle('air.msg.tremsNoConfirm'))
        return
      }
      const bookParam = this.bookParam
      bookParam.policyApplied = true
      bookParam.policyUserId = this.$store.getters['Order/basicSearchParam'].policyUserId
      let isSelectedReason = true
      bookParam.domAirBooking.selectedFlights.forEach(flight => {
        if (flight.resultHandlers) {
          flight.resultHandlers.forEach(resultHandler => {
            if (!resultHandler.selectedReason) {
              isSelectedReason = false
            }
          })
        }
      })
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
      bookParam.domAirBooking.travelers = this.travellers
      console.info(bookParam)
      DomairService.bookDomFlight(bookParam).then((data) => {
        console.info(data)
        if (data && data.success) {
          if (data.resultData.taNo) {
            this.$coreAlert(this.$i18nBundle('common.msg.bookSuccess')).then(() => {
              this.$gotoAxoTaPage(data.resultData.taNo, ProductType.DomAir, true)
            })
          } else if (data.resultData.doubleBookTravelers) {
            let doubleBookMessage = ''
            data.resultData.doubleBookTravelers.forEach(traveller => {
              doubleBookMessage += this.$i18nMsg(traveller.userNameCN, traveller.userNameEN) + this.$i18nBundle('air.msg.selectDoubleBookingDescribe') + traveller.doubleBookingTaNo + '<br/>'
            })
            this.$coreAlert(doubleBookMessage) // 这里需要添加后续处理逻辑
          } else if (data.resultData.pnrList) {
            let pnrErrorMsg = ''
            data.resultData.pnrList.forEach(pnrDto => {
              if (pnrDto.pnrErrorMsg) {
                pnrErrorMsg += pnrDto.pnrErrorMsg + '<br/>'
              }
            })
            this.$coreAlert(pnrErrorMsg)
          }
        } else {
          this.$coreAlert(data.resultData.responseHead.resultMessage)
        }
      })
    },

    doGetCabinLimit (flight, flightCabin) {
      let cabinLimit = null
      let getDomairCabinLimitParam = {
        policyUserId: this.$store.getters['Order/basicSearchParam'].policyUserId,
        companyCode: this.currentLoginUser.companyCode,
        lang: this.$store.getters.globalConfig.currentLocale,
        domAirFlightDto: {}
      }
      getDomairCabinLimitParam.domAirFlightDto = cloneDeep(flight)
      getDomairCabinLimitParam.domAirFlightDto.selectCabin = cloneDeep(flightCabin)
      DomairService.getCabinLimit(getDomairCabinLimitParam).then((data) => {
        console.info(data)
        if (data && data.success) {
          if (data.resultData.airCabinLimitDto) {
            if (data.resultData.airCabinLimitDto.formattedLimitationDesc) {
              cabinLimit = data.resultData.airCabinLimitDto.formattedLimitationDesc
              if (cabinLimit === null) {
                cabinLimit = this.$i18nBundle('air.msg.unknowCabinLimit')
              }
            } else {
              cabinLimit = this.$i18nBundle('air.msg.unknowCabinLimit')
            }
          } else {
            cabinLimit = this.$i18nBundle('air.msg.unknowCabinLimit')
          }
        }
        this.$coreShowSheet(cabinLimit)
      })
      if (cabinLimit !== null) {
        this.$coreShowSheet(cabinLimit)
      }
    },

    getDomAirMealLabel: function (code) {
      return this.domAirMealCache[code] || this.$i18nBundle('air.label.snacks')
    }
  }
}
</script>

<style scoped>

</style>
