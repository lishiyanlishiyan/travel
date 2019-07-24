<template>
  <f7-page>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')" back-link-url="/hotel/doSearchHotel"/>
      <f7-nav-title>
        <span v-if="city">
          {{$i18nMsg(city.nameCN, city.nameEN)}}
        </span>
        <span v-if="searchParam.hotelQueryParam">
          {{searchParam.hotelQueryParam.checkinDate|date('MM/DD')}}-{{searchParam.hotelQueryParam.checkoutDate|date('MM/DD')}}
          <f7-chip v-if="searchParam.today" :text="$t('hotel.label.today')" color="red"/>
        </span>
      </f7-nav-title>
      <common-home/>
    </f7-navbar>
    <f7-list media-list>
      <li>
        <hotel-base-info :hotel="hotel"/>
      </li>
    </f7-list>
    <hotel-city-policy v-model="cityPolicyStr"/>
    <f7-list media-list>
      <f7-list-item link :key="index"
                    :title="hotelRoom.roomName"
                    @click="toBookHotel(hotelRoom)"
                    v-for="(hotelRoom, index) in hotelRooms">
        <div slot="subtitle" v-if="hotelRoom.roomNameDesc">
          {{hotelRoom.roomNameDesc}}
        </div>
        <div slot="subtitle" v-if="!hotelRoom.roomNameDesc && hotelRoom.bedType">
          {{hotelRoom.bedType}}/{{hotelRoom.breakfast}}/{{hotelRoom.network}}
        </div>
        <div slot="text" v-if="hotelRoom.originalRoomName">
          {{hotelRoom.originalRoomName}}
        </div>
        <div slot="text"
             v-if="!hotelRoom.originalRoomName && hotelRoom.roomName && hotelRoom.roomName.length>20">
          {{hotelRoom.roomName}}
        </div>
        <div slot="after" class="text-color-orange">
          <strong>
            {{hotelRoom.averagePrice|currency(hotelRoom.currencyCode)}}
          </strong>
        </div>
        <div slot="footer">
          <hotel-room-tags :value="hotelRoom"/>
        </div>
      </f7-list-item>
    </f7-list>
  </f7-page>
</template>

<script>

import cloneDeep from 'lodash/cloneDeep'
import HotelCityPolicy from '../../../components/products/hotel/hotel-city-policy'
import HotelRoomTags from '../../../components/products/hotel/hotel-room-tags'
import HotelService from '../../../services/hotel/HotelService'
import { ProductType } from '../../../consts/OrderConsts'
import HotelBaseInfo from './hotel-base-info'

const hotelDetailParamKey = 'Hotel/hotelDetailParam'

export default {
  name: 'hotel-detail',
  components: { HotelBaseInfo, HotelRoomTags, HotelCityPolicy },
  data () {
    const hotelDetailParam = cloneDeep(this.$store.getters[hotelDetailParamKey])
    const hotelDefaultSetup = this.$defaultSetup(ProductType.Hotel)
    const companyDefaultSetup = this.$defaultSetup(ProductType.Company)
    this.$processBookEnable(hotelDefaultSetup)
    return Object.assign({
      hotelDefaultSetup,
      companyDefaultSetup,
      axoDomain: process.env.VUE_APP_AXO_DOMAIN
    }, hotelDetailParam)
  },
  mounted () {
  },
  methods: {
    showHotelDescription () {
      const desc = this.$i18nMsg(this.hotel.descriptionCN, this.hotel.descriptionEN)
      const title = this.$i18nMsg(this.hotel.nameCN, this.hotel.nameEN)
      this.$coreShowPopup(desc, title)
    },
    validateBeforeBook (hotelRoom) {
      if (!this.hotelDefaultSetup.bookEnable || (this.companyDefaultSetup.bookEnableNeedExternalOrder && !this.searchParam.externalOrderNo)) {
        this.showErrorMsg(this.$t('common.msg.bookDisabled'))
        return false
      }
      // 千淘预付当天11点以后禁止预订
      if (hotelRoom.prepayType === 2 && HotelService.checkDayAndHour(this.searchParam.checkinDate)) {
        this.showErrorMsg(this.$t('hotel.msg.bookDisabledQiantao'))
        return false
      }
      // 华住酒店最大选择3家
      if (hotelRoom.interfaceId === 20 && this.searchParam.roomCount > 3) {
        this.showErrorMsg(this.$t('hotel.msg.moreRoomHuazhu'))
        return
      }
      return true
    },
    showErrorMsg (msg) {
      this.$coreError(msg).then(this.$noop, this.$noop)
    },
    toBookHotel (hotelRoom) {
      if (this.validateBeforeBook(hotelRoom)) {
        if (hotelRoom.interfaceId === 3 && !hotelRoom.hasShowDetail) {
          HotelService.showHotelRoomDetail({
            policyUserId: this.searchParam.policyUserId,
            companyCode: this.searchParam.companyCode,
            roomQueryBasic: this.searchRoomPricesParam.roomQueryBasic,
            roomId: hotelRoom.roomId,
            roomInterfaceId: hotelRoom.interfaceId,
            corporate: hotelRoom.corporate,
            amexCorporate: hotelRoom.amexCorporate
          }).then(data => {
            if (data && data.success) {
              const roomDetail = data.resultData.hotelRoom
              hotelRoom.roomName = roomDetail.roomName
              hotelRoom.originalRoomName = roomDetail.originalRoomName
              hotelRoom.hotelRates = roomDetail.hotelRates
              hotelRoom.currencyCode = roomDetail.currencyCode
              if (!hotelRoom.cancelRule) {
                hotelRoom.cancelRule = roomDetail.cancelRule
              } else {
                hotelRoom.abacusDetailCancelRule = roomDetail.cancelRule
              }
              hotelRoom.hasShowDetail = true
              this.internalToBookHotel(hotelRoom)
            } else {
              this.$coreError(data.message)
            }
          })
        } else {
          this.internalToBookHotel(hotelRoom)
        }
      }
    },
    internalToBookHotel (hotelRoom) {
      const hotelRoomDetailParam = Object.assign({}, cloneDeep(this.$store.getters[hotelDetailParamKey]), { hotelRoom })
      delete hotelRoomDetailParam.hotelRooms
      this.$store.dispatch('Hotel/storeHotelRoomDetailParam', hotelRoomDetailParam)
      this.$goto('/hotel/toBookHotel')
    },
    isQiantaoPrepay: HotelService.isQiantaoPrepay
  }
}
</script>

<style scoped>

</style>
