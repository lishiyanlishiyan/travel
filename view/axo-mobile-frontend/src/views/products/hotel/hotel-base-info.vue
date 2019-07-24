<template>
  <div>
    <!--预定前酒店信息-->
    <f7-list-item v-if="!hotel.taNo" link @click="showHotelDescription">
      <div slot="title">
        <f7-link @click="showHotelDescription">
          {{$i18nMsg(hotel.nameCN, hotel.nameEN)}}
          <span v-if="hotel.hotelId===-1">
                <span v-if="hotel.hotelName">{{hotel.hotelName}}</span>
                <span v-if="!hotel.hotelName">{{$i18nMsg(hotel.hotelNameCN,hotel.hotelNameEN)}}</span>
            </span>
          <span v-if="hotel.hotelId!==-1">{{$i18nMsg(hotel.hotelNameCN,hotel.hotelNameEN)}}</span>
        </f7-link>
      </div>
      <div slot="subtitle">
        <f7-icon :key="n" size="small" color="orange" v-for="n in hotel.stars||0"
                 f7="star_fill"/>
        <f7-chip v-if="hotel.corporate" :text="$t('common.label.corp')" color="blue"/>
        <f7-chip v-if="isQiantaoPrepay(hotel, hotelDefaultSetup)" :text="$t('hotel.label.prepay')" color="purple"/>
      </div>
      <img slot="media" v-if="hotel.picture" :src="`${axoDomain}/${hotel.picture||''}`" width="85" height="85"/>
      <div slot="text">
        {{$i18nMsg(hotel.addressCN, hotel.addressEN)}}
      </div>
      <div slot="footer" v-if="phone || fax">
        <f7-row class="margin-top">
          <f7-col v-if="phone">
            <f7-link :href="`tel:${hotel.phone}`" icon-size="18" icon-f7="phone_fill" :external="true">
              {{hotel.phone}}
            </f7-link>
          </f7-col>
          <f7-col v-if="fax">
            <f7-link :href="`fax:${hotel.fax}`" icon-size="18" icon-material="perm_phone_msg" :external="true">
              {{hotel.phone}}
            </f7-link>
          </f7-col>
        </f7-row>
      </div>
    </f7-list-item>
    <!--订单酒店信息hotel为HotelOrderDto-->
    <f7-list-item v-if="hotel.taNo" link @click="showHotelDescription">
      <div slot="title">
        <f7-link @click="showHotelDescription">
          <span v-if="hotel.hotelId===-1">
                <span v-if="hotel.hotelName">{{hotel.hotelName}}</span>
                <span v-if="!hotel.hotelName">{{$i18nMsg(hotel.hotelNameCN,hotel.hotelNameEN)}}</span>
            </span>
          <span v-if="hotel.hotelId!==-1">{{$i18nMsg(hotel.hotelNameCN,hotel.hotelNameEN)}}</span>
        </f7-link>
      </div>
      <template v-if="hotel.hotelInfo">
        <div slot="subtitle">
          <f7-icon :key="n" size="small" color="orange" v-for="n in hotel.hotelInfo.stars||0"
                   f7="star_fill"/>
          <f7-chip v-if="hotel.corporateFlag==='1'" :text="$t('common.label.corp')" color="blue"/>
          <f7-chip v-if="isQiantaoPrepay(hotel.hotelInfo, hotelDefaultSetup)" :text="$t('hotel.label.prepay')"
                   color="purple"/>
        </div>
        <div slot="text">
          {{$i18nMsg(hotel.hotelAddressCN, hotel.hotelAddressEN)}}
        </div>
        <div slot="footer" v-if="phone || fax">
          <f7-row class="margin-top">
            <f7-col v-if="phone">
              <f7-link :href="`tel:${hotel.hotelInfo.phone}`" icon-size="18" icon-f7="phone_fill" :external="true">
                {{hotel.hotelInfo.phone}}
              </f7-link>
            </f7-col>
            <f7-col v-if="fax">
              <f7-link :href="`fax:${hotel.hotelInfo.fax}`" icon-size="18" icon-material="perm_phone_msg"
                       :external="true">
                {{hotel.hotelInfo.phone}}
              </f7-link>
            </f7-col>
          </f7-row>
        </div>
      </template>
    </f7-list-item>
  </div>
</template>

<script>
import { ProductType } from '../../../consts/OrderConsts'
import HotelService from '../../../services/hotel/HotelService'

export default {
  name: 'hotel-base-info',
  props: {
    hotel: {
      type: Object
    },
    phone: {
      type: Boolean,
      default: true
    },
    fax: {
      type: Boolean
    }
  },
  data () {
    const hotelDefaultSetup = this.$defaultSetup(ProductType.Hotel)
    return {
      axoDomain: process.env.VUE_APP_AXO_DOMAIN,
      hotelDefaultSetup
    }
  },
  methods: {
    processHotelInfo (hotel) {
      if (hotel && hotel.hotelInfo) {
        hotel.hotelInfo.corporate = hotel.corporateFlag === '1'
      }
    },
    showHotelDescription ($event) {
      if (!this.hotel.taNo || this.hotel.hotelInfo) {
        const hotel = this.hotel.taNo ? this.hotel.hotelInfo : this.hotel
        const desc = this.$i18nMsg(hotel.descriptionCN || hotel.descCn, hotel.descriptionEN || hotel.descEn)
        const title = this.$i18nMsg(hotel.nameCN || hotel.nameCn, hotel.nameEN || hotel.nameEn)
        this.$coreShowPopup(desc, title)
      }
      $event.stopPropagation()
    },
    isQiantaoPrepay: HotelService.isQiantaoPrepay
  }
}
</script>

<style scoped>

</style>
