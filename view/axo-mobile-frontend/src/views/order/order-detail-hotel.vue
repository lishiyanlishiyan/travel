<template>
  <f7-page page-content>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"/>
      <f7-nav-title>{{$t('common.label.order')}}: {{order.taNo}}-{{$t('common.label.hotel')}}</f7-nav-title>
    </f7-navbar>
    <f7-list v-for="(hotel, i) in order.hotelList" :key="hotel.id" media-list class="normalWeight">
      <f7-list-item>
        <div class="text-color-primary"
             v-if="((hotel.gdsConfirmNo && hotel.qiantaoPrepayFlag !== 2) || (hotel.qiantaoOrderNo && hotel.qiantaoPrepayFlag === 2)) && (hotel.interfaceId !==8 ||(hotel.interfaceId===8 && hotel.interfaceProcessStatus!=='3'))">
            <span class="text-color-orange"
                  v-if="order.statusCode==='1' && hotel.autoCancelFlag===0 && hotel.cancelConfirmNo">
                {{$t('hotel.msg.autoCanceled')}}
              <span>
                {{$t('hotel.label.hotelCancelConfirmNo')}}
                <span>{{hotel.cancelConfirmNo}}</span>
              </span>
            </span>
          <div v-if="order.statusCode==='1' && hotel.autoCancelFlag===0">
              <span class="text-color-orange">
                {{$t('hotel.msg.autoCancelDisabled',[$date(hotel.lastCancelTime, 'YYYY-MM-DD HH:mm')])}}
              </span>
          </div>
          <div v-if="order.statusCode==='1' && hotel.orderStatus!==0 && hotel.autoCancelFlag!==0">
            <span v-if="hotel.prepayFlag===1 && checkNonCancelableOrder(hotel)">{{$t('hotel.msg.nonCancelable')}}</span>
            <span v-if="(hotel.prepayFlag!==1 || !checkNonCancelableOrder(hotel)) && hotel.qiantaoPrepayFlag !== 2">
                <span v-if="$isLocale('zh_CN')" class="text-color-orange">
                  {{$t('hotel.msg.hotelPrintStart')}}
                  <b>{{hotel.autoCancelTime|date('YYYY-MM-DD HH:mm')}}</b>
                  {{$t('hotel.msg.hotelPrintMid')}}
                  <span v-if="apprConfig.apprETA==='N'">{{$t('hotel.msg.hotelPrintNonApprove')}}</span>
                  <span v-if="apprConfig.apprETA!=='N'">{{$t('hotel.msg.hotelPrintApprove')}}</span>
                </span>
                <span v-if="$isLocale('en_US')" class="text-color-orange">
                  {{$t('hotel.msg.hotelPrintStart')}}
                  <span v-if="apprConfig.apprETA==='N'">{{$t('hotel.msg.hotelPrintNonApprove')}}</span>
                  <span v-if="apprConfig.apprETA!=='N'">{{$t('hotel.msg.hotelPrintApprove')}}</span>
                  <b>{{hotel.autoCancelTime|date('YYYY-MM-DD HH:mm')}}</b>
                  {{$t('hotel.msg.hotelPrintEnd')}}
                </span>
              </span>
          </div>
          <strong v-if="hotel.orderStatus===0">
            {{$t('hotel.msg.hotelCanceled')}}
          </strong>
        </div>
        <div :class="hotel.orderStatus===0?'text-color-orange':'text-color-blue'"
             v-if="(!hotel.gdsConfirmNo && hotel.qiantaoPrepayFlag !== 2) || (!hotel.qiantaoOrderNo && hotel.qiantaoPrepayFlag === 2) || !(hotel.interfaceId !==8 ||(hotel.interfaceId===8 && hotel.interfaceProcessStatus!=='3'))">
          <strong v-if="hotel.orderStatus===0">
            {{$t('hotel.msg.requestCanceled')}}
          </strong>
          <div v-if="hotel.orderStatus!=0 && (hotel.needAmexService==1 || hotel.qiantaoPrepayFlag == '2')">
            <span v-if="isPrepayBookAfterApproved(hotel)">
              {{$t('hotel.msg.prepayBookingRemind')}}
            </span>
            <span v-if="!isPrepayBookAfterApproved(hotel)">
              {{$t('hotel.msg.confirmBookingRemind')}}
            </span>
          </div>
          <div v-if="hotel.orderStatus!=0 && hotel.needAmexService!=1">
            {{$t('hotel.msg.selfBookedRecord')}}
          </div>
        </div>
      </f7-list-item>
      <f7-list-item class="no-flex">
        <f7-row>
          <f7-col class="text-align-center">
            {{$i18nMsg(hotel.hotelNationNameCN, hotel.hotelNationNameEN)}}
            -
            {{$i18nMsg(hotel.hotelCityNameCN,hotel.hotelCityNameEN)}}
          </f7-col>
        </f7-row>
        <f7-row no-gap>
          <f7-col class="data-table-title col-40 text-align-right" style="font-size: 22px;">
            {{hotel.checkinDate|date('MM-DD')}}
          </f7-col>
          <f7-col class="col-20 text-align-center">
            <f7-chip color="gray" :text="(hotel.totalStay)+($t('hotel.label.stayPer'))"/>
          </f7-col>
          <f7-col class="data-table-title col-40" style="font-size: 22px;">
            {{hotel.checkoutDate|date('MM-DD')}}
          </f7-col>
        </f7-row>
      </f7-list-item>
      <!--<f7-list-item class="normalWeight">
        <div slot="title">
          {{$t('hotel.label.city')}}
        </div>
        <div slot="after">

        </div>
      </f7-list-item>-->
      <li>
        <hotel-base-info :hotel="hotel"/>
      </li>
      <f7-list-item>
        <template v-if="!hotel.hotelRoomInfo && hotel.roomType">
          <!--房型名称-->
          <div slot="title">
            {{hotel.roomType}}
          </div>
          <!--GDS房型中文转译开启后，原英文房型-->
          <div slot="footer" v-if="hotel.originalRoomName">
            {{hotel.originalRoomName}}
          </div>
        </template>
        <template v-if="hotel.hotelRoomInfo">
          <div slot="title">
            {{$i18nMsg(hotel.hotelRoomInfo.roomNameCn,hotel.hotelRoomInfo.roomNameEn)}}
          </div>
          <div slot="subtitle" v-if="hotel.hotelRoomInfo">
            <span v-if="hotel.hotelRoomInfo.bedType">
              {{$i18nMsg(hotel.hotelRoomInfo.bedNameCn, hotel.hotelRoomInfo.bedNameEn)}}
            </span>
            <span v-if="hotel.hotelRoomInfo.bedType">
              {{$i18nMsg(hotel.hotelRoomInfo.breakfastNameCn, hotel.hotelRoomInfo.breakfastNameEn)}}
            </span>
            <span v-if="hotel.hotelRoomInfo.bedType">
              {{$i18nMsg(hotel.hotelRoomInfo.netNameCn, hotel.hotelRoomInfo.netNameEn)}}
            </span>
          </div>
        </template>
        <!--房间的特性tag，比如立即确认-->
        <div slot="footer">
          <hotel-room-tags :value="calcHotelRoom(hotel)"/>
        </div>
        <!--几间房，住几晚-->
        <div slot="after">
          {{hotel.roomCount}} {{$t('hotel.label.roomPer')}}
          {{hotel.totalStay}} {{$t('hotel.label.stayPer')}}
        </div>
      </f7-list-item>
      <f7-list-item :title="$t('hotel.label.averagePerNight')" class="normalWeight">
        <div slot="after" class="text-color-orange">
          <strong>
            {{hotel.averagePrice|currency(hotel.currencyCode)}}
          </strong>
        </div>
      </f7-list-item>
      <f7-list-item :title="$t('air.label.totalPrice')" class="normalWeight">
        <div slot="after" class="text-color-orange">
          <strong>
            {{hotel.totalPrice|currency(hotel.currencyCode)}}
          </strong>
        </div>
      </f7-list-item>
      <f7-list-item :title="$t('hotel.label.serviceChargeIncluded')" class="normalWeight" v-if="hotel.taxPrice">
        <div slot="after" class="text-color-orange">
          <strong>
            {{hotel.taxPrice|currency(hotel.currencyCode)}}
          </strong>
        </div>
      </f7-list-item>
      <f7-list-item :title="$t('common.label.traveller')">
        <div slot="after">
              <span :key="traveller.id" v-for="(traveller, idx) in hotel.passengers">
                {{$i18nMsg(traveller.nameCN,traveller.nameEN)}}
                <f7-chip v-if="traveller.psgrType==='T'" :text="$t('profile.label.guest')" color="blue"/>
                <span v-if="idx!==hotel.passengers.length-1">,</span>
              </span>
        </div>
      </f7-list-item>
      <f7-list-item :title="$t('hotel.label.gdsConfirmNo')" v-if="hotel.gdsConfirmNo || hotel.qiantaoOrderNo">
        <div slot="after">
          <span v-if="hotel.qiantaoPrepayFlag !== 2 && hotel.gdsConfirmNo">
            {{hotel.gdsConfirmNo}}
          </span>
          <span v-if="hotel.qiantaoPrepayFlag === 2 && hotel.qiantaoOrderNo">
            {{hotel.qiantaoOrderNo}}
          </span>
        </div>
      </f7-list-item>
      <f7-list-item :title="$t('hotel.label.hotelCancelConfirmNo')" v-if="hotel.cancelConfirmNo">
        <div slot="after">
          {{hotel.cancelConfirmNo}}
        </div>
      </f7-list-item>
      <f7-list-item :title="$t('hotel.label.hotelConfirmNo')" v-if="hotel.hotelConfirmNo">
        <div slot="after">
          {{hotel.hotelConfirmNo}}
        </div>
      </f7-list-item>
      <f7-list-item :title="$t('hotel.label.specialNeeds')" v-if="hotel.specialRequest">
        <div slot="after">
          {{$i18nMsg(hotel.specialRequestCN, hotel.specialRequest)}}
        </div>
      </f7-list-item>
      <f7-list-item :title="$t('common.label.membership')" v-if="hotel.membershipCardNo">
        <div slot="after">
          {{hotel.membershipCardNo}}
        </div>
      </f7-list-item>
      <template v-if="hotel.hotelId && hotel.hotelId!==-1">
        <f7-list-item :title="$t('hotel.label.cancellationRules')">
          <div slot="footer">
            <div v-if="hotel.hotelId!==-1 && (hotel.gdsConfirmNo || hotel.qiantaoPrepayFlag === 2)">
              <span
                v-if="hotel.prepayFlag===1 && checkNonCancelableOrder(hotel)">{{$t('hotel.msg.nonCancelable')}}</span>
              <span v-if="(hotel.prepayFlag!==1 || !checkNonCancelableOrder(hotel)) && hotel.cancelPolicyDesc">
                {{$i18nMsg(hotel.cancelPolicyDesc, hotel.cancelPolicyDescEN)}}
              </span>
              <span v-if="(hotel.prepayFlag!==1 || !checkNonCancelableOrder(hotel)) && !hotel.cancelPolicyDesc">
                {{$t('hotel.msg.elongCancel1')}}
                {{hotel.lastCancelTime|date('YYYY-MM-DD HH:mm:ss')}}
                {{$t('hotel.msg.elongCancel1')}}
              </span>
            </div>
            <div v-if="hotel.hotelId!==-1 && !hotel.gdsConfirmNo && hotel.qiantaoPrepayFlag !== 2">
              <span>{{$t('hotel.msg.nonGDSCancellationRules')}}</span>
            </div>
          </div>
        </f7-list-item>
        <f7-list-item v-if="hotel.qiantaoPrepayFlag !== 2 && hotel.prepayFlag !== 1"
                      :title="$t('hotel.label.guaranteeRules')">
          <div slot="footer">
            {{$i18nMsg(hotel.guaranteeRule, hotel.guaranteeRuleEN)}}
          </div>
        </f7-list-item>
      </template>
      <template v-if="!hotel.hotelId || hotel.hotelId===-1">
        <f7-list-item :title="$t('common.label.remark')" v-if="hotel.requestRemark">
          <div slot="after">
            {{hotel.requestRemark}}
          </div>
        </f7-list-item>
        <f7-list-item :title="$t('hotel.label.needCitsgbtService')">
          <div slot="after">
            <span v-if="hotel.needAmexService===1">{{$t('common.label.yes')}}</span>
            <span v-if="hotel.needAmexService!==1">{{$t('common.label.no')}}</span>
          </div>
        </f7-list-item>
      </template>
      <f7-list-item :title="$t('common.label.payType')">
        <div slot="after">
          <span v-if="isPrepay(hotel)">{{$t('hotel.label.prepay')}}</span>
          <span v-if="!isPrepay(hotel)">{{$t('hotel.label.selfpay')}}</span>
        </div>
      </f7-list-item>
      <template v-if="hotel.reasonCodeList">
        <f7-list-item :key="reasonCode.id" v-for="reasonCode in hotel.reasonCodeList">
          <div slot="title">
            {{$i18nMsg(reasonCode.nameCN,reasonCode.nameEN)}}
          </div>
          <div slot="subtitle">
            {{$i18nMsg(reasonCode.infoCN,reasonCode.infoEN)}}
          </div>
        </f7-list-item>
      </template>
      <order-product-button :current-btns="productBtn[i]" :product-order="hotel" :ref-name="'hotelAction'+i"/>
    </f7-list>
  </f7-page>
</template>
<script>

import OrderProductButton from './order-product-button'
import { mapGetters } from 'vuex'
import HotelBaseInfo from '../products/hotel/hotel-base-info'
import HotelRoomTags from '../../components/products/hotel/hotel-room-tags'
import { ProductType } from '../../consts/OrderConsts'
import HotelService from '../../services/hotel/HotelService'

export default {
  name: 'order-detail-hotel',
  components: { HotelRoomTags, HotelBaseInfo, OrderProductButton },
  computed: {
    ...mapGetters(['Order/orderDetail', 'currentLoginUser'])
  },
  data () {
    let order = {}
    let productBtn = []
    const hotelDefaultSetup = this.$defaultSetup(ProductType.Hotel)
    const companyDefaultSetup = this.$defaultSetup(ProductType.Company)
    console.info(companyDefaultSetup.approvalConfigDto)
    return {
      order,
      productBtn,
      hotelDefaultSetup,
      apprConfig: companyDefaultSetup.approvalConfigDto
    }
  },
  methods: {
    isPrepayBookAfterApproved (hotel) {
      return hotel.prepayFlag === 1 && (hotel.interfaceId === 8 || hotel.qiantaoPrepayFlag === 2) && this.hotelDefaultSetup.prepayBookAfterApproved
    },
    calcProductBtn (hotel) {
      const order = this.order
      const hotelBtns = []
      if (order.historyFlag !== 1 && order.orderButton.currentCreatorFlag && !order.tcUserId) {
        if (this.calParsedCancelButton(hotel, order)) {
          hotelBtns.push({
            title: this.$t('common.label.delete'),
            click: this.cancelHotelOrder,
            color: 'red'
          })
        }
      }
      return hotelBtns
    },
    calParsedCancelButton (hotel, order) {
      const currentOrderFlag = order.orderButton.currentCreatorFlag
      const gdsCancelableFlag = order.statusCode === '1' || (hotel.hasCancelButton === 'Y' && parseInt(order.statusCode) <= 4)
      const interfaceProcessStatus = parseInt(hotel.interfaceProcessStatus)
      if (hotel.qiantaoPrepayFlag === 2) {
        return interfaceProcessStatus !== 3
      }
      if (hotel.interfaceId === 3) {
        return hotel.orderStatus !== 0 && gdsCancelableFlag && currentOrderFlag
      } else if (hotel.interfaceId === 8) {
        return hotel.orderStatus !== 0 && (interfaceProcessStatus === 0 || hotel.interfaceProcessStatus === 1) && gdsCancelableFlag && currentOrderFlag
      } else {
        return hotel.orderStatus !== 0 && currentOrderFlag && gdsCancelableFlag
      }
    },
    checkNonCancelableOrder (hotel) {
      return hotel && !hotel.changeable
    },
    cancelHotelOrder (hotel) {
      this.$coreConfirm(this.$t('hotel.msg.cancelConfirm')).then(() => {
        HotelService.cancelHotelOrder({
          taNo: hotel.taNo,
          hotelOrderId: hotel.id,
          syncFlag: true,
          cancelBy: this.currentLoginUser.userId
        }).then(data => {
          if (data && data.success) {
            this.$back({ force: true })
          } else {
            this.$coreError(data.message)
          }
        })
      })
    },
    isPrepay (hotel) {
      return (!this.hotelDefaultSetup.qiantaoHotelPrepayEnable && hotel.prepayFlag === 1) ||
        (this.hotelDefaultSetup.qiantaoHotelPrepayEnable && hotel.qiantaoPrepayFlag === 2)
    },
    calcMembershipCardNo (hotel) {
      if (hotel.passengers) {
        for (let i = 0; i < hotel.passengers.length; i++) {
          if (hotel.passengers[i].cardNo) {
            hotel.membershipCardNo = hotel.passengers[i].cardNo
            break
          }
        }
      }
    },
    calcHotelRoom (hotel) {
      const { interfaceId, prepayFlag, corporateFlag, changeable } = hotel
      return {
        interfaceId,
        changeable,
        corporate: corporateFlag === '1',
        prepay: prepayFlag === 1
      }
    }
  },
  mounted () {
    let self = this
    self.order = self['Order/orderDetail']
    if (self.order && self.order.hotelList) {
      self.order.hotelList.forEach(hotel => {
        this.calcMembershipCardNo(hotel)
        this.productBtn.push(this.calcProductBtn(hotel))
      })
    } else {
      self.$back()
    }
  }
}
</script>

<style scoped>

</style>
