<template>
  <f7-list v-if="order">
    <f7-list-item accordion-item>
      <div slot="title">
        <strong>
          {{order.taNo}}
          <span v-if="order.externalLinkNo">
          ({{order.externalLinkNo}})
        </span>
          <products-icons :offline="!!order.tcUserId"/>
          <order-status-tags :order="order"/>
        </strong>
      </div>
      <f7-accordion-content>
        <f7-list>
          <f7-list-item :title="$t('order.label.createBy')">
            <div slot="after">
              {{$i18nMsg(order.creatorNameCN,order.creatorNameEN)}}
            </div>
          </f7-list-item>
          <f7-list-item :title="$t('order.label.createDate')">
            <div slot="after">
              {{order.createDate|date('YYYY-MM-DD HH:mm')}}
            </div>
          </f7-list-item>
          <f7-list-item :title="$t('common.label.traveller')">
            <div slot="after">
              <span :key="traveller.id" v-for="(traveller, idx) in order.travellers">
                {{$i18nMsg(traveller.travelerNameCN,traveller.travelerNameEN)}}
                <f7-chip v-if="traveller.travelerType==='T'" :text="$t('profile.label.guest')" color="blue"/>
                <span v-if="idx!==order.travellers.length-1">,</span>
              </span>
            </div>
          </f7-list-item>
          <template v-if="order.dynamicDataList">
            <f7-list-item :key="dynamicData.id" v-for="dynamicData in order.dynamicDataList"
                          v-show="dynamicData.dataValue">
              <div slot="title">
                {{$i18nMsg(dynamicData.labelCN,dynamicData.labelEN)}}
              </div>
              <div slot="after">
                {{$i18nMsg(dynamicData.dataValueCN,dynamicData.dataValueEN)}}
              </div>
            </f7-list-item>
          </template>
          <template v-if="order.orderApproval && order.orderApproval.reasonCodeList">
            <f7-list-item :key="reasonCode.id" v-for="reasonCode in order.orderApproval.reasonCodesList">
              <div slot="title">
                {{$i18nMsg(reasonCode.nameCN,reasonCode.nameEN)}}
              </div>
              <div slot="after">
                {{$i18nMsg(reasonCode.infoCN,reasonCode.infoEN)}}
              </div>
            </f7-list-item>
          </template>
          <f7-list-item :title="$t('air.label.dom')" v-if="order.existDomAir">
            <div slot="after" class="text-color-blue">
              <b v-if="!order.taApproveOrderInfo">
                {{order.price.domAirPrice|currency('￥')}}
              </b>
              <b v-if="order.taApproveOrderInfo">
                {{order.taApproveOrderInfo.domAirPrice|currency('￥')}}
              </b>
              <f7-icon f7="check_round_fill" size="small"></f7-icon>
            </div>
          </f7-list-item>
          <f7-list-item :title="$t('air.label.intl')" v-if="order.existIntlAir || order.existIntlFlight">
            <div slot="after" class="text-color-blue">
              <b v-if="!order.taApproveOrderInfo">
                {{order.price.intPrice|currency('￥')}}
              </b>
              <b v-if="order.taApproveOrderInfo">
                {{order.taApproveOrderInfo.intlAirPrice|currency('￥')}}
              </b>
              <f7-icon f7="check_round_fill" size="small"></f7-icon>
            </div>
          </f7-list-item>
          <f7-list-item :title="$t('common.label.hotel')" v-if="order.existHotel">
            <div slot="after" class="text-color-blue">
              <b v-if="!order.taApproveOrderInfo">
                {{order.price.hotelPrice|currency('￥')}}
              </b>
              <b v-if="order.taApproveOrderInfo">
                {{order.taApproveOrderInfo.hotelPrice|currency('￥')}}
              </b>
              <f7-icon f7="check_round_fill" size="small"></f7-icon>
            </div>
          </f7-list-item>
          <f7-list-item :title="$t('common.label.train')" v-if="order.existTrain">
            <div slot="after" class="text-color-blue">
              <b v-if="!order.taApproveOrderInfo">
                {{order.price.trainPrice|currency('￥')}}
              </b>
              <b v-if="order.taApproveOrderInfo">
                {{order.taApproveOrderInfo.trainPrice|currency('￥')}}
              </b>
              <f7-icon f7="check_round_fill" size="small"></f7-icon>
            </div>
          </f7-list-item>
          <f7-list-item :title="$t('common.label.other')" v-if="order.existOther">
            <div slot="after" class="text-color-blue">
              <b v-if="!order.taApproveOrderInfo">
                {{order.price.otherPrice|currency('￥')}}
              </b>
              <b v-if="order.taApproveOrderInfo">
                {{order.taApproveOrderInfo.otherPrice|currency('￥')}}
              </b>
              <f7-icon f7="check_round_fill" size="small"></f7-icon>
            </div>
          </f7-list-item>
          <f7-list-item :title="$t('common.label.carRental')" v-if="order.existCarRental">
            <div slot="after" class="text-color-blue">
              <b v-if="!order.taApproveOrderInfo">
                {{order.price.carPrice|currency('￥')}}
              </b>
              <b v-if="order.taApproveOrderInfo">
                {{order.taApproveOrderInfo.carRentalPrice|currency('￥')}}
              </b>
              <f7-icon f7="check_round_fill" size="small"></f7-icon>
            </div>
          </f7-list-item>
          <f7-list-item :title="$t('order.label.totalPrice')">
            <div slot="after" class="text-color-orange">
              <b v-if="!order.taApproveOrderInfo">
                <span v-if="order.price.totalPrice">
                  {{order.price.totalPrice|currency('￥')}}
                </span>
                <span v-if="!order.price.totalPrice">
                  {{0|currency('￥')}}
                </span>
              </b>
              <b v-if="order.taApproveOrderInfo">
                 <span v-if="order.taApproveOrderInfo.totalPrice">
                {{order.taApproveOrderInfo.totalPrice|currency('￥')}}
                 </span>
                <span v-if="!order.taApproveOrderInfo.totalPrice">
                {{0|currency('￥')}}
                 </span>
              </b>
              <f7-icon f7="check_round_fill" size="small"></f7-icon>
            </div>
          </f7-list-item>
        </f7-list>
      </f7-accordion-content>
    </f7-list-item>
  </f7-list>
</template>

<script>
import OrderStatusTags from './order-status-tags'
import ProductsIcons from '../../components/products/products-icons'

export default {
  name: 'order-head',
  components: { ProductsIcons, OrderStatusTags },
  props: {
    order: {
      type: Object
    }
  }
}
</script>

<style scoped>

</style>
