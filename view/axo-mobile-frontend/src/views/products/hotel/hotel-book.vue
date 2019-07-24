<template>
  <f7-page class="hotel">
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')" :back-link-url="hotelBookBackUrl"/>
      <f7-nav-title>
        {{$t('common.label.checkOrder')}}
      </f7-nav-title>
      <f7-nav-right>
        <f7-link v-if="!hotelRoom.bookButtonEnable && (hotelRoom.interfaceId!==1 && hotelRoom.interfaceId!==9)"
                 @click="doBookHotel">{{$t('common.label.book')}}
        </f7-link>
        <f7-link v-if="!hotelRoom.bookButtonEnable && (hotelRoom.interfaceId===1 || hotelRoom.interfaceId===9)"
                 @click="doBookHotel">{{$t('common.label.request')}}
        </f7-link>
      </f7-nav-right>
      <f7-subnavbar>
        <f7-link/>
        <f7-link>
          <span v-if="city">
            {{$i18nMsg(city.nameCN, city.nameEN)}}
          </span>
          <span v-if="searchParam.hotelQueryParam">
            {{searchParam.hotelQueryParam.checkinDate|date('MM/DD')}}-{{searchParam.hotelQueryParam.checkoutDate|date('MM/DD')}}
            <f7-chip v-if="searchParam.today" :text="$t('hotel.label.today')" color="red"/>
          </span>
        </f7-link>
        <f7-link/>
      </f7-subnavbar>
    </f7-navbar>
    <hotel-city-policy v-model="cityPolicyStr"/>
    <f7-block-title>{{$t('hotel.label.checkInfo')}}</f7-block-title>
    <f7-list :media-list="!hotelRequest" inline-labels form name="hotelRequestForm">
      <template v-if="!hotelRequest">
        <li>
          <hotel-base-info :hotel="hotel" :phone="false"/>
        </li>
        <f7-list-item>
          <!--房型名称-->
          <div slot="title">
            {{hotelRoom.roomName}}
          </div>
          <!--Elong房型描述-->
          <div slot="subtitle" v-if="hotelRoom.roomNameDesc">
            {{hotelRoom.roomNameDesc}}
          </div>
          <!--NonGDS协议房型-->
          <div slot="subtitle" v-if="!hotelRoom.roomNameDesc && hotelRoom.bedType">
            {{hotelRoom.bedType}}/{{hotelRoom.breakfast}}/{{hotelRoom.network}}
          </div>
          <!--GDS房型中文转译开启后，原英文房型-->
          <div slot="footer" v-if="hotelRoom.originalRoomName">
            {{hotelRoom.originalRoomName}}
          </div>
          <!--GDS房型中文转译关闭-->
          <div slot="footer" v-if="!hotelRoom.originalRoomName && hotelRoom.interfaceId===3">
            {{hotelRoom.roomName}}
          </div>
          <!--房间的特性tag，比如立即确认-->
          <div slot="footer">
            <hotel-room-tags :value="hotelRoom"/>
          </div>
          <!--几间房，住几晚-->
          <div slot="after">
            {{searchParam.hotelQueryParam.roomCount}} {{$t('hotel.label.roomPer')}}
            {{totalStay}} {{$t('hotel.label.stayPer')}}
          </div>
        </f7-list-item>
      </template>
      <template v-if="hotelRequest">
        <li v-show="false">
          <common-autocomplete
            :label="$t('hotel.label.hotelName')"
            inline-label
            :page-title="$t('hotel.label.hotelName')"
            :placeholder="$t('hotel.label.hotelName')"
            v-model="config.autoHotelName"
            @change="hotelRoomBookingParam.roomBookingBasic.hotelName=(config.autoHotelName||{})[$i18nMsg('nameCN', 'nameEN')]"
            :text-property="$i18nMsg('nameCN', 'nameEN')"
            value-property="id"
            :close-on-select="true"
            :request-source-on-open="true"
            input-events="search"
            input-class-name="text-align-right"
            class="item-link"
            :opened="config.showAutoHotelName"
            @closed="config.showAutoHotelName=false"
            :autocompleteConfig="config.autoHotelNameConfig"/>
        </li>
        <f7-list-input class="normalWeight"
                       :data-vv-name="$t('hotel.label.hotelName')"
                       v-validate="'required'"
                       clear-button
                       :value="hotelRoomBookingParam.roomBookingBasic.hotelName"
                       @input="hotelRoomBookingParam.roomBookingBasic.hotelName=$event.target.value">
          <div slot="label" :class="errors.first($t('hotel.label.hotelName'))?'text-color-red':'text-color-blue'">
            {{$t('hotel.label.hotelName')}}
            <f7-link
              v-if="!hotelRoomEdit"
              @click="config.showAutoHotelName=true">
              <f7-icon f7="search" size="20"/>
            </f7-link>
          </div>
        </f7-list-input>
        <f7-list-input class="normalWeight"
                       clear-button
                       :value="hotelRoomBookingParam.roomBookingBasic.remark"
                       @input="hotelRoomBookingParam.roomBookingBasic.remark=$event.target.value">
          <div slot="label">
            {{$t('common.label.remark')}}
          </div>
        </f7-list-input>
        <f7-list-item v-if="!hotelRecord" :title="$t('hotel.label.roomCount')" inline-label>
          {{searchParam.hotelQueryParam.roomCount}} {{$t('hotel.label.roomPer')}}
          {{totalStay}} {{$t('hotel.label.stayPer')}}
        </f7-list-item>
        <template v-if="hotelRecord">
          <f7-list-item :title="$t('hotel.label.roomCount')" inline-label>
            <f7-stepper :value="hotelRoomBookingParam.roomBookingBasic.roomCountShow"
                        :min="1" :max="config.roomCountMax"
                        @input="hotelRoomBookingParam.roomBookingBasic.roomCountShow=parseInt($event.target.value)"/>
          </f7-list-item>
          <f7-list-item :title="$t('hotel.label.guestCount')" inline-label>
            <f7-stepper :value="hotelRoomBookingParam.roomBookingBasic.guestCount"
                        :min="1" :max="3"
                        @input="hotelRoomBookingParam.roomBookingBasic.guestCount=parseInt($event.target.value)"/>
          </f7-list-item>
        </template>
      </template>
      <template v-if="!hotelDefaultSetup.onHoldBookEnable && !hotelRecord">
        <li v-if="config.forceGuaranteeEnable">
          <f7-list-item :title="$t('hotel.label.forceGuarantee')"
                        smart-select class="normalWeight"
                        :class="{'text-color-red': errors.first($t('hotel.label.forceGuarantee')), 'text-color-blue': !config.forceGuaranteeEnable1 && !errors.first($t('hotel.label.forceGuarantee'))}"
                        :smart-select-params="{openIn: 'sheet', leftRadio: true, overflowEnable:true, closeOnSelect: true}">
            <select v-model="hotelRoom.useGuarantee"
                    :data-vv-name="$t('hotel.label.forceGuarantee')"
                    v-validate="{required: config.forceGuaranteeEnable && !config.forceGuaranteeEnable1}"
                    @change="onChangeUseGuarantee"
                    name="hotelRoomUseGuarantee">
              <option :key="index" v-for="(guaranteeOption, index) in guaranteeOptions"
                      :selected="guaranteeOption.id===hotelRoom.useGuarantee" :value="guaranteeOption.id">
                {{guaranteeOption.label}}
              </option>
            </select>
            <f7-icon v-if="errors.first($t('hotel.label.forceGuarantee'))" f7="alert_fill" slot="after" class="text-color-orange"></f7-icon>
          </f7-list-item>
        </li>
        <f7-list-item
          v-if="hotelRoom.hotelId && !isNonGDS(hotelRoom.interfaceId)"
          :title="$t('hotel.label.cancellationRules')" class="normalWeight">
          <!--预付酒店-->
          <div slot="footer" v-if="isPrepayRoom(hotelRoom)">
            {{hotelRoom.cancelRule}}
          </div>
          <!--非预付酒店-->
          <div slot="subtitle" v-if="!isPrepayRoom(hotelRoom)">
            <f7-chip v-if="hotelRoom.needGuarantee || hotelRoom.useGuarantee"
                     :text="$t('hotel.msg.cancelPolicyPreGuarantee')" color="blue"></f7-chip>
            <f7-chip v-if="!hotelRoom.needGuarantee && !hotelRoom.useGuarantee && hotelRoom.prepayType !== 2"
                     :text="$t('hotel.msg.cancelPolicyPreNotGuarantee')"
                     color="blue"></f7-chip>
          </div>
          <div slot="footer" v-if="!isPrepayRoom(hotelRoom)">
            <!--艺龙不需要担保-->
            <span v-if="hotelRoom.interfaceId===8 && !hotelRoom.needGuarantee1">
              {{$t('hotel.msg.elongForceGuarantee')}}
            </span>
            <!--(强制担保||客户选择使用担保) && 未超过取消时限-->
            <span v-if="(hotelRoom.needGuarantee || hotelRoom.useGuarantee) && !config.afterCancelDeadLine">
              {{$t('hotel.msg.cancelPolicyGuarantee', [$date(hotelRoom.cancelDeadLine, 'YYYY-MM-DD HH:mm')])}}
              {{$date(hotelRoom.cancelDeadLine, 'YYYY-MM-DD HH:mm')}}
            </span>
            <!--(强制担保||客户选择使用担保) && 超过取消时限-->
            <span class="text-color-orange"
                  v-if="(hotelRoom.needGuarantee || hotelRoom.prepayType === 2 || hotelRoom.useGuarantee) && config.afterCancelDeadLine">
              {{$t('hotel.msg.afterCancelDeadLine', [$date(hotelRoom.cancelDeadLine, 'YYYY-MM-DD HH:mm')])}}
            </span>
            <!--不强制担保 && 客户未选择使用担保 && 非elong不需要担保-->
            <span
              v-if="!hotelRoom.needGuarantee && !hotelRoom.useGuarantee && !(hotelRoom.interfaceId===8 && !hotelRoom.needGuarantee1)">
              {{$t('hotel.msg.cancelPolicyNotGuarantee', [hotelRoom.guaranteeDate, hotelRoom.guaranteeTime])}}
            </span>
          </div>
        </f7-list-item>
      </template>
      <f7-list-item :title="$t('hotel.label.guaranteeRules')"
                    v-if="hotelRoom.hotelId" class="normalWeight">
        <div slot="text">
          <span v-if="hotelRoom.prepay">{{$t('hotel.msg.prepay')}}</span>
          <span v-if="!hotelRoom.needGuarantee && !hotelRoom.prepay">{{$t('hotel.msg.nonGuarantee')}}</span>
          <span v-if="hotelRoom.needGuarantee && hotelRoom.prepayType !== 2">{{$t('hotel.msg.guarantee')}}</span>
        </div>
      </f7-list-item>
      <f7-list-item :title="$t('hotel.label.addition')" class="normalWeight"
                    v-if="hotelRoom.addRemarks && hotelRoom.addRemarks.length>0">
        <div slot="text">
          {{hotelRoom.addRemarks.join(',')}}
        </div>
      </f7-list-item>
      <f7-list-item :title="$t('hotel.label.cityBudget')" class="normalWeight"
                    v-if="cityBudget && (hotelRoom.corperate?cityBudget.corpBudget>=0:cityBudget.budget>=0)">
        <div slot="after" class="text-color-red">
          {{cityBudget.currency}} {{formatBudget(hotelRoom.corperate?cityBudget.corpBudget:cityBudget.budget)}}
        </div>
      </f7-list-item>
      <template v-if="!hotelRequest">
        <f7-list-item :title="$t('hotel.label.averagePerNight')" class="normalWeight">
          <div slot="after" class="text-color-orange">
            <strong>
              {{hotelRoom.averagePrice|currency(hotelRoom.currencyCode)}}
            </strong>
          </div>
        </f7-list-item>
      </template>
      <template v-if="hotelRequest">
        <li v-if="config.currencySelectEnable">
          <common-autocomplete
            v-show="false"
            class="item-link normalWeight"
            input-class-name="text-align-right"
            :inline-label="true"
            :page-title="$t('hotel.label.currencyCode')"
            :placeholder="$t('hotel.label.currencyCode')"
            text-property="currencyType"
            v-model="exchangeRate"
            @change="hotelRoom.currencyCode=exchangeRate.currencyType"
            value-property="currencyType"
            :close-on-select="true"
            :request-source-on-open="true"
            :opened="config.toSelectCurrencyCode"
            @closed="config.toSelectCurrencyCode=false"
            :autocomplete-data-items="exchangeRates">
            <div slot="label">
              {{$t('hotel.label.currencyCode')}}
            </div>
          </common-autocomplete>
        </li>
        <f7-list-input class="normalWeight"
                       :data-vv-name="$t('hotel.label.averagePerNight')"
                       v-validate="'required|decimal:2'"
                       clear-button
                       :value="hotelRoom.averagePrice"
                       @input="hotelRoom.averagePrice=$event.target.value">
          <div slot="label"
               :class="errors.first($t('hotel.label.averagePerNight'))?'text-color-red':'text-color-blue'">
            {{$t('hotel.label.averagePerNight')}}
          </div>
        </f7-list-input>
      </template>
      <f7-list-item :title="`${$t('air.label.totalPrice')}(${$t('hotel.label.serviceChargeIncluded')})`"
                    class="normalWeight">
        <div slot="after" class="text-color-orange">
          <f7-link v-if="config.currencySelectEnable" @click="config.toSelectCurrencyCode=true">
            <f7-icon f7="world" size="20"></f7-icon>
          </f7-link>
          <strong>
            {{hotelRoom.totalPrice|currency(hotelRoom.currencyCode)}}
          </strong>
        </div>
      </f7-list-item>
      <li v-if="errorMessage||errors.all()">
        <common-form-errors :value="errorMessage||errors.all()"/>
      </li>
    </f7-list>
    <f7-block-title>{{$t('common.label.traveller')}}
    </f7-block-title>
    <hotel-traveller-view v-model="config.travellerRoomList"
                          :credit-card-cert="hotelRoom.interfaceId === 8"
                          :credit-card="!hotelRecord && isShowCreditCard(hotelRoom)"
                          :membership="!hotelRecord && isShowMembership(hotelRoom)"
                          :special-need="!hotelRecord"
                          :guest-count="hotelRoomBookingParam.roomBookingBasic.guestCount"
                          :special-needs="prepareRoomSpecialNeedOptions()"/>
    <!--<f7-toolbar class="fixed-bottom" position="bottom">
      <f7-button v-if="!hotelRoom.bookButtonEnable && (hotelRoom.interfaceId===1 || hotelRoom.interfaceId===9)" large
                 raised fill @click="doBookHotel">{{$t('common.label.request')}}
      </f7-button>
      <f7-button v-if="!hotelRoom.bookButtonEnable && (hotelRoom.interfaceId!==1 && hotelRoom.interfaceId!==9)" large
                 raised fill @click="doBookHotel">{{$t('common.label.book')}}
      </f7-button>
    </f7-toolbar>-->
  </f7-page>
</template>

<script>
import { mapGetters } from 'vuex'
import moment from 'moment'
import cloneDeep from 'lodash/cloneDeep'
import HotelCityPolicy from '../../../components/products/hotel/hotel-city-policy'
import HotelService from '../../../services/hotel/HotelService'
import HotelRoomTags from '../../../components/products/hotel/hotel-room-tags'
import ProductService from '../../../services/products/ProductService'
import PolicyControlService from '../../../services/policy/PolicyControlService'
import HotelTravellerView from '../traveller/hotel-traveller-view'
import { ProductType } from '../../../consts/OrderConsts'
import HotelAutoService from '../../../services/hotel/HotelAutoService'
import HotelConsts from '../../../consts/HotelConsts'
import HotelBaseInfo from './hotel-base-info'

const hotelRoomDetailParamKey = 'Hotel/hotelRoomDetailParam'
const bookTravellersKey = 'Order/bookTravellers'

export default {
  name: 'hotel-book',
  components: { HotelBaseInfo, HotelTravellerView, HotelRoomTags, HotelCityPolicy },
  data () {
    const hotelDefaultSetup = this.$defaultSetup(ProductType.Hotel)
    const hotelRoomDetailParam = cloneDeep(this.$store.getters[hotelRoomDetailParamKey])
    const hotelQueryParam = hotelRoomDetailParam.searchParam.hotelQueryParam
    // hotelRoomDetailParam.hotelRoom.useGuarantee = false
    hotelRoomDetailParam.hotelRoom.cancelRule1 = hotelRoomDetailParam.hotelRoom.cancelRule
    hotelRoomDetailParam.hotelRoom.needGuarantee1 = !!hotelRoomDetailParam.hotelRoom.needGuarantee
    const hotelRoomBookingParam = HotelService.getHotelRoomBookingParam(hotelRoomDetailParam.searchParam)
    hotelRoomBookingParam.roomBookingBasic.bookRoom = hotelRoomDetailParam.hotelRoom
    hotelRoomBookingParam.roomBookingBasic.lowestRoom = hotelRoomDetailParam.lowestRoom
    const hotelRequest = !!hotelRoomDetailParam.requestHotelType
    const hotelRecord = hotelRoomDetailParam.requestHotelType === 3
    const hotelRoomEdit = hotelRoomDetailParam.requestHotelType === 2
    const hotelBookBackUrl = hotelRequest && !hotelRoomEdit ? '/hotel/doSearchHotel' : '/hotel/hotelDetail'
    const roomCountMax = (this.$store.getters[bookTravellersKey] || []).length || 1
    return Object.assign({
      totalStay: moment(hotelQueryParam.checkoutDate).diff(moment(hotelQueryParam.checkinDate), 'days'),
      cityBudget: null,
      travellers: [],
      config: {
        today: hotelRoomDetailParam.searchParam.today,
        travellerRoomList: [],
        arrivalEarlyTimeStr: '14:00:00',
        arrivalLateTimeStr: '23:59:00',
        forceGuaranteeEnable: false,
        forceGuaranteeEnable1: false,
        afterCancelDeadLine: moment().isAfter(hotelRoomDetailParam.hotelRoom.cancelDeadLine),
        currencySelectEnable: hotelRoomDetailParam.hotelRequest && hotelRoomDetailParam.city.ctryCode !== 'CN',
        toSelectCurrencyCode: false,
        autoHotelName: null,
        autoHotelNameConfig: {
          searchMethod: HotelAutoService.getAutoHotelList(hotelRoomDetailParam.searchParam)
        },
        showAutoHotelName: false,
        roomCountMax
      },
      errorMessage: null,
      guaranteeOptions: [],
      hotelDefaultSetup,
      exchangeRates: [],
      exchangeRate: {
        currencyType: hotelRoomDetailParam.hotelRoom.currencyCode
      },
      hotelRoomBookingParam,
      hotelRequest,
      hotelRecord,
      hotelRoomEdit,
      hotelBookBackUrl
    }, hotelRoomDetailParam)
  },
  computed: {
    ...mapGetters(['currentLoginUser', bookTravellersKey])
  },
  watch: {
    'hotelRoom.averagePrice': function (p) {
      this.calcRequestPrice(p)
    },
    'hotelRoomBookingParam.roomBookingBasic.guestCount': function () {
      this.calcHotelRecord()
    },
    'hotelRoomBookingParam.roomBookingBasic.roomCountShow': function () {
      this.calcHotelRecord()
    }
  },
  mounted () {
    this.loadBookTravellers()
    this.onChangeUseGuarantee()
    this.calcForceGuaranteeOptions()
    this.initHotelRequest()
  },
  methods: {
    calcHotelRecord () {
      this.config.travellerRoomList = this.calcTravellerRoom(this.hotelRoomBookingParam.roomBookingBasic, this.travellers)
      this.calcRequestPrice(this.hotelRoom.averagePrice)
    },
    calcRequestPrice (p) {
      this.hotelRoom.totalPrice = this.totalStay * p
      if (this.hotelRecord) {
        this.hotelRoom.totalPrice = this.hotelRoom.totalPrice * this.hotelRoomBookingParam.roomBookingBasic.roomCountShow
      }
      this.hotelRoom.maxPrice = p
      this.hotelRoomBookingParam.roomBookingBasic.averagePrice = p
    },
    initHotelRequest () {
      this.$initFormValidate('hotelRequestForm')
      if (this.hotelRequest) {
        if (this.config.currencySelectEnable) {
          HotelService.getExchangeRates().then(data => {
            this.exchangeRates = data.resultData.exchangeRates || []
          })
        }
        this.calcRequestPrice(0)
      }
    },
    formatBudget: HotelService.formatBudget,
    isNonGDS: HotelService.isNonGDS,
    isNeedCreditCard (hotelRoom) {
      return Boolean(!this.hotelDefaultSetup.onHoldBookEnable && !this.isPrepayRoom(hotelRoom) && (hotelRoom.needGuarantee || hotelRoom.useGuarantee))
    },
    isShowCreditCard (hotelRoom) {
      return this.isNeedCreditCard(hotelRoom)
    },
    isShowMembership (hotelRoom) {
      return hotelRoom.interfaceId !== 19 && hotelRoom.interfaceId !== 20 && hotelRoom.prepayType !== 2
    },
    isPrepayRoom (hotelRoom) {
      return (hotelRoom.interfaceId === 8 || hotelRoom.interfaceId === 22) && hotelRoom.prepay
    },
    isNormalRoom (hotelRoom) {
      return hotelRoom.hotelId && !this.isNonGDS(hotelRoom.interfaceId)
    },
    calcForceGuaranteeEnable (hotelRoom, hotelDefaultSetup) {
      return this.isNormalRoom(hotelRoom) && !hotelDefaultSetup.onHoldBookEnable && !hotelRoom.prepay &&
        ((hotelRoom.interfaceId !== 8 && hotelRoom.interfaceId !== 20 && !hotelRoom.needGuarantee) ||
          (hotelRoom.interfaceId === 8 && hotelRoom.needGuarantee1 && (!hotelRoom.needGuarantee ||
            hotelRoom.useGuarantee)))
    },
    calcForceGuaranteeEnable1 (hotelRoom, hotelDefaultSetup) {
      return !hotelDefaultSetup.onHoldBookEnable && hotelRoom.prepayType !== 2 && !hotelRoom.needGuarantee && this.isNonGDS(hotelRoom.interfaceId)
    },
    calcForceGuaranteeOptions () {
      const hotelRoom = this.hotelRoom
      const hotelDefaultSetup = this.hotelDefaultSetup
      let forceGuaranteeEnable1 = this.calcForceGuaranteeEnable1(hotelRoom, hotelDefaultSetup)
      let forceGuaranteeEnable = forceGuaranteeEnable1 || this.calcForceGuaranteeEnable(hotelRoom, hotelDefaultSetup)
      if (forceGuaranteeEnable) {
        this.guaranteeOptions = [{
          id: false,
          label: this.$t(forceGuaranteeEnable1 ? 'hotel.msg.notUseGuarantee1' : 'hotel.msg.notUseGuarantee', [hotelRoom.guaranteeDate, hotelRoom.guaranteeTime])
        }, {
          id: true,
          label: this.$t('hotel.msg.useGuarantee')
        }]
      }
      this.config.forceGuaranteeEnable1 = forceGuaranteeEnable1
      this.config.forceGuaranteeEnable = forceGuaranteeEnable
    },
    onChangeUseGuarantee () {
      const hotelRoom = this.hotelRoom
      if (hotelRoom.interfaceId === 8 && hotelRoom.needGuarantee1) {
        if (!hotelRoom.useGuarantee) {
          this.config.arrivalLateTimeStr = hotelRoom.guaranteeTime + ':00'
        } else {
          this.config.arrivalLateTimeStr = '23:59:00'
        }
        this.onHotelArrivalTimeChange()
      }
    },
    onHotelArrivalTimeChange () {
      HotelService.arrivalParser(this.config)
      if (this.config.arrivalEarlyTimeStr && this.config.arrivalLateTimeStr) {
        const earlyDate = moment(moment(this.hotelRoomBookingParam.roomBookingBasic.checkInDate).format('YYYY-MM-DD') +
          ' ' + this.config.arrivalEarlyTimeStr).toDate()
        const lateDate = moment(moment(this.hotelRoomBookingParam.roomBookingBasic.checkInDate).format('YYYY-MM-DD') +
          ' ' + this.config.arrivalLateTimeStr).toDate()
        this.hotelRoomBookingParam.roomBookingBasic.arrivalEarlyTime = earlyDate
        this.hotelRoomBookingParam.roomBookingBasic.arrivalLateTime = lateDate
        this.doElongGuaranteeValidate()
      }
    },
    doElongGuaranteeValidate () {
      let guaranteeParam = {
        hotelCode: this.hotelRoom.hotelCode,
        roomTypeId: this.hotelRoom.roomTypeId,
        ratePlanId: this.hotelRoom.roomId,
        arriveEarlyTime: this.hotelRoomBookingParam.roomBookingBasic.arrivalEarlyTime,
        arriveLaterTime: this.hotelRoomBookingParam.roomBookingBasic.arrivalLateTime,
        checkInDate: this.hotelRoomBookingParam.roomBookingBasic.checkInDate,
        checkOutDate: this.hotelRoomBookingParam.roomBookingBasic.checkOutDate,
        roomNum: 1,
        totalPrice: this.hotelRoom.totalPrice
      }
      HotelService.validateGuaranteeInfo(guaranteeParam).then(data => {
        if (data && data.success) {
          this.hotelRoom.needGuarantee = data.resultData.needGuarantee
          this.hotelRoom.cancelRule = this.hotelRoom.needGuarantee ? this.hotelRoom.cancelRule1 : ''
          this.hotelRoom.cancelDeadLine = data.resultData.cancelDeadLine
          // this.config.travellerRoomList.filter(travellerRoom => !travellerRoom.remainFlag).forEach(travellerRoom => {
          //   travellerRoom.needGuarantee = this.hotelRoom.needGuarantee
          // })
          this.config.afterCancelDeadLine = moment().isAfter(this.hotelRoom.cancelDeadLine)
          this.calcForceGuaranteeOptions()
          this.$forceUpdate()
          this.$formValidate()
        }
      })
    },
    newTravellerRoom (traveller) {
      return {
        specialNeed: '',
        specialNeeds: [],
        travellerList: traveller ? [traveller] : [],
        mainTraveller: null,
        selectedCreditCard: null,
        selectedMembership: null
        // needGuarantee: this.hotelRoom.needGuarantee
      }
    },
    calcTravellerRoom (roomBasic, travellers) {
      const travellerRooms = []
      let travellerAssign = 0
      let travellerLength = travellers.length
      for (let i = 0; i < roomBasic.roomCountShow; i++) {
        let travellerRoom = travellerRooms[i] = this.newTravellerRoom(travellers[travellerAssign])
        travellerAssign++
        for (let j = 1; j < roomBasic.guestCount; j++) {
          if (travellerLength - travellerAssign === roomBasic.roomCountShow - i - 1) {
            break
          }
          travellerRoom.travellerList.push(travellers[travellerAssign])
          travellerAssign++
        }
      }
      let remainTravellerRoom = this.newTravellerRoom()
      remainTravellerRoom.remainFlag = true
      for (let i = travellerAssign; i < travellers.length; i++) {
        remainTravellerRoom.travellerList.push(travellers[i])
      }
      travellerRooms.push(remainTravellerRoom)
      return travellerRooms
    },
    removeSelectItems (traveller) {
      this.$removeAttrs(traveller, ['userCreditCards', 'memberships', 'userCerts', 'specialNeeds'])
    },
    doBookHotel () {
      let travellerRoomList = this.config.travellerRoomList.filter(travellerRoom => !travellerRoom.remainFlag)
      if (this.validateBookParam(travellerRoomList)) {
        travellerRoomList = cloneDeep(travellerRoomList)
        travellerRoomList.forEach(travellerRoom => {
          let roomTravellers = travellerRoom.travellerList
          travellerRoom.travellerList = []
          roomTravellers.forEach((traveller, i) => {
            if (i === 0) {
              traveller.selectedMembership = travellerRoom.selectedMembership
              if ((this.hotelRoom.needGuarantee || this.hotelRoom.useGuarantee) && travellerRoom.selectedCreditCard) {
                traveller.selectedCreditCard = travellerRoom.selectedCreditCard
                let expriyDate = moment(traveller.selectedCreditCard.expriyDate)
                traveller.selectedCreditCard.expriyDate = expriyDate.toDate()
                traveller.selectedCreditCard.validYear = expriyDate.format('YYYY')
                traveller.selectedCreditCard.validMonth = expriyDate.format('MM')
                if (this.hotelRoom.interfaceId === 8) {
                  traveller.selectedCert = travellerRoom.selectedCert
                }
              } else {
                traveller.selectedCreditCard = null
                traveller.selectedCert = null
              }
              this.$removeAttrs(travellerRoom, ['selectedMembership', 'selectedCreditCard', 'selectedCert'])
              travellerRoom.mainTraveller = traveller
            } else {
              travellerRoom.travellerList.push(traveller)
            }
            this.removeSelectItems(traveller)
          })
          this.removeSelectItems(travellerRoom)
        })
        let selectedTravellers = cloneDeep(this.travellers)
        selectedTravellers.forEach(traveller => {
          this.removeSelectItems(traveller)
        })
        Object.assign(this.hotelRoomBookingParam.roomBookingBasic, {
          selectedTravellers,
          travellerRoomList,
          needAmexService: !this.hotelRecord
        })
        if (!this.hotelRecord) {
          this.confirmGuaranteeAndBook(travellerRoomList)
        } else {
          this.doInternalBookHotel()
        }
      }
    },
    confirmGuaranteeAndBook (travellerRoomList) {
      const hotelRoom = this.hotelRoom
      const isToday = this.searchParam.today
      const needGuarantee = hotelRoom.needGuarantee
      let hasNotGuarantee = false
      travellerRoomList.forEach(travellerRoom => {
        if (travellerRoom.mainTraveller && !travellerRoom.mainTraveller.selectedCreditCard) {
          hasNotGuarantee = true
        }
      })
      let cpFlag = HotelService.isBehindTheTimes(this.hotelRoomBookingParam.roomBookingBasic.checkInDate, hotelRoom.cancelPolicyOption, hotelRoom.cancelPolicyNumeric)
      const cancelablePrepay = this.$t('hotel.msg.elongPrepayCancelable')
      const nonCancelablePrepay = this.$t('hotel.msg.elongPrepayNonCancelable')
      if (needGuarantee && hotelRoom.prepayType !== 2) {
        this.guaranteeBookMessage(isToday, cpFlag)
      } else if (hotelRoom.prepay) {
        this.$coreConfirm(hotelRoom.changeable ? cancelablePrepay : nonCancelablePrepay).then(this.doInternalBookHotel)
      } else {
        if (hasNotGuarantee) {
          this.$coreConfirm(this.$t('hotel.msg.todayAndNoGuarantee')).then(this.doInternalBookHotel)
        } else {
          this.guaranteeBookMessage(isToday, cpFlag)
        }
      }
      return false
    },
    guaranteeBookMessage (isToday, cpFlag) {
      const VAR_ALERT_TODAY_GUARANTEE = this.$t('hotel.msg.todayAndGuarantee')
      const VAR_ALERT_NO_TODAY_GUARANTEE = this.$t('hotel.msg.notTodayAndGuarantee')
      if (isToday || cpFlag) {
        this.$coreConfirm(VAR_ALERT_TODAY_GUARANTEE).then(this.doInternalBookHotel)
      } else {
        this.$coreConfirm(VAR_ALERT_NO_TODAY_GUARANTEE).then(this.doInternalBookHotel)
      }
    },
    doInternalBookHotel () {
      const bookMethod = this.hotelRequest ? 'bookHotelRequest' : 'bookGDSRoom'
      HotelService[bookMethod](this.hotelRoomBookingParam).then(data => {
        if (data && data.success) {
          if (data.resultData.taNo) {
            this.$coreAlert(this.$i18nBundle('common.msg.bookSuccess')).then(() => {
              this.$gotoAxoTaPage(data.resultData.taNo, ProductType.Hotel, true)
            })
          } else if (data.resultData.policyControlResult) {
            // 策略控制执行
            PolicyControlService.handlePolicyControl(data.resultData.policyControlResult, (resultData, reasonCodes) => {
              this.hotelRoomBookingParam.policyApplied = true
              this.hotelRoomBookingParam.roomBookingBasic.reasonCode = reasonCodes.join(',')
              this.doInternalBookHotel()
            })
          }
        } else if (data && data.resultData && data.resultData.responseHead.resultCode === 'DOUBLE_BOOK_ERROR') { // 重复预订
          let msg = data.message
          this.$coreConfirm(msg, { title: this.$i18nBundle('hotel.error.repeatOrder') }).then(() => {
            this.hotelRoomBookingParam.confirmDoubleBooking = true
            this.doInternalBookHotel()
          })
        } else if (data && data.resultData && data.resultData.responseHead.resultCode === 'CREDITCARD_TYPE_INVALID' && this.hotelRoomBookingParam.hasUnionpayCreditCard) {
          this.$coreAlert(this.$i18nBundle('hotel.msg.unionpayCardTypeError'))
        } else if (data && !data.success) {
          this.$coreAlert(data.message)
        }
      })
    },
    validateBookParam (travellerRoomList) {
      const hotelRoom = this.hotelRoom
      this.errorMessage = null
      if (this.isNeedCreditCard(hotelRoom)) {
        for (let i = 0; i < travellerRoomList.length; i++) {
          const travellerRoom = travellerRoomList[i]
          if (!travellerRoom.selectedCreditCard) {
            this.showErrorMsg(this.$t('hotel.msg.creditCardNeeded'))
            return false
          }
        }
      }
      if (!this.checkAbacusCreditCardType(travellerRoomList)) {
        this.showErrorMsg(this.$t('hotel.msg.unionpayError'))
        return false
      }
      let errorMsg = this.errors.all()[0]
      if (errorMsg) {
        this.showErrorMsg(errorMsg)
        return false
      }
      return true
    },
    showErrorMsg (msg) {
      this.errorMessage = msg
      this.$coreError(msg).then(this.$noop, this.$noop)
    },
    checkAbacusAndUnionpay (travellerRoomList) {
      let result = []
      if (this.hotelRoom.interfaceId === 3) {
        travellerRoomList.forEach(travellerRoom => {
          if (travellerRoom.selectedCreditCard && travellerRoom.selectedCreditCard.cardNo &&
            travellerRoom.selectedCreditCard.instituteCode === 'UP') {
            result.push(travellerRoom.selectedCreditCard.cardNo)
          }
        })
      }
      return result
    },
    checkAbacusCreditCardType (travellerRoomList) {
      const result = this.checkAbacusAndUnionpay(travellerRoomList)
      this.hotelRoomBookingParam.hasUnionpayCreditCard = result.length > 0
      if (result.length > 0) {
        for (let i = 0; i < result.length; i++) {
          if (!/^62/.test(result[i])) {
            return false
          }
        }
      }
      return true
    },
    loadBookTravellers () {
      ProductService.loadTravellersDetails(this[bookTravellersKey]).then(travellers => {
        this.travellers = travellers
        this.travellers.forEach(traveller => {
          // 根据国籍过滤掉不需要的证件，以及过期的证件
          traveller.userCerts = traveller.userCerts.filter(cert => {
            if (!cert.expiryDate || Date.parse(cert.expiryDate) >= new Date()) { // 保留没有过期时间的证件（身份证等）和未过期的其他证件
              return cert
            }
          })
        })
        this.config.travellerRoomList = this.calcTravellerRoom(this.hotelRoomBookingParam.roomBookingBasic, this.travellers)
      })
    },
    showHotelDescription () {
      const desc = this.$i18nMsg(this.hotel.descriptionCN, this.hotel.descriptionEN)
      const title = this.$i18nMsg(this.hotel.nameCN, this.hotel.nameEN)
      this.$coreShowPopup(desc, title)
    },
    prepareRoomSpecialNeedOptions () {
      let results = []
      if (this.isRoomNameContiansBedKeyword() && !this.isRoomNameContiansKingAndTwinKeyword()) { // 如果房间名称中包含双和大关键字或者只包含关键字“床”，specialNeed中去掉大床房和双床房的选项
        HotelConsts.HOTEL_SPECIAL_NEEDS.forEach(item => {
          if (item.id.indexOf('Twin Bed') === -1 && item.id.indexOf('King Bed') === -1) {
            results.push(item)
          }
        })
      } else { // 否则显示所有特殊需求选项
        results = cloneDeep(HotelConsts.HOTEL_SPECIAL_NEEDS)
      }
      return results
    },
    isRoomNameContiansBedKeyword () {
      let str = this.hotelRoom.bedType ? this.hotelRoom.bedType.toLowerCase() : (this.hotelRoom.roomName || '').toLowerCase()
      return str.indexOf('床') !== -1 || str.indexOf('bed') !== -1
    },
    isRoomNameContiansKingAndTwinKeyword () {
      let str = this.hotelRoom.bedType ? this.hotelRoom.bedType.toLowerCase() : (this.hotelRoom.roomName || '').toLowerCase()
      return (str.indexOf('双') !== -1 && str.indexOf('大') !== -1) ||
        ((str.indexOf('twin') !== -1 || str.indexOf('double') !== -1) &&
          (str.indexOf('king') !== -1 || str.indexOf('queen') !== -1))
    },
    isQiantaoPrepay: HotelService.isQiantaoPrepay
  }
}
</script>

<style scoped>

</style>
