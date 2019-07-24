<template>
  <f7-page page-content>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')" back-link-url="/order"/>
      <f7-nav-title>{{$t('common.label.order')}}: {{order.taNo}}-{{$t('air.label.dom')}}</f7-nav-title>
    </f7-navbar>
      <div :key="segment.id" v-for="(segment,index) in domAirSegments" class="orderDetail list">
        <f7-block strong style="padding-bottom: 5px;">
          <f7-row class="padding-bottom">
            <f7-col width="40" class="text-align-right">
              <f7-row class="item-subtitle">
                <f7-col>
                  {{segment.deptDate|date('MM-DD')}}
                </f7-col>
              </f7-row>
              <f7-row class="data-table-title">
                <f7-col>
                  {{segment.deptDate|date('HH:mm')}}
                </f7-col>
              </f7-row>
              <f7-row class="item-subtitle">
                <f7-col>
                  {{$i18nMsg(segment.deptPort.nameCN, segment.deptPort.nameEN)}}
                  {{segment.terminal}}
                </f7-col>
              </f7-row>
            </f7-col>
            <f7-col width="20" class="flightDuration">
              {{segment.duration|duration}}
            </f7-col>
            <f7-col width="40" class="text-align-left">
              <f7-row class="item-subtitle">
                <f7-col>
                  {{segment.arrivalDate|date('MM-DD')}}
                </f7-col>
              </f7-row>
              <f7-row class="data-table-title">
                <f7-col>
                  {{segment.arrivalDate|date('HH:mm')}}
                </f7-col>
              </f7-row>
              <f7-row class="item-subtitle">
                <f7-col>
                  {{$i18nMsg(segment.arrivalPort.nameCN, segment.arrivalPort.nameEN)}}
                  {{segment.arrivalTerminal}}
                </f7-col>
              </f7-row>
            </f7-col>
          </f7-row>
          <f7-row class="padding-bottom item-subtitle">
            <f7-col class="text-align-center text-color-gray">
              {{$i18nMsg(segment.airlineNameCN, segment.airlineNameEN)}}({{segment.flightNo}})
              &nbsp;&nbsp;|&nbsp;&nbsp;
              {{segment.planeType?'':segment.planeTypeCode}}
              <f7-link v-if="segment.planeType" :text="segment.planeType.typeAlias" @click="$coreShowSheet($i18nMsg(segment.planeType.nameCN+':<br/>'+segment.planeType.capabilityCN,segment.planeType.nameEN+':<br/>'+segment.planeType.capabilityEN))"></f7-link>
            </f7-col>
          </f7-row>
          <f7-row class="padding-vertical item-subtitle" style="padding-top: 8px; align-items: baseline;">
            <f7-col width="80">
              <strong class="data-table-title">¥{{segment.price}}</strong>
              +{{segment.airportTax}}
              +{{segment.fuelTax}}({{segment.discount}}%/{{segment.cabin}}{{segment.cabinType|cabinType}})
            </f7-col>
            <f7-col width="20" class="text-align-center" style="margin-top: 5px;">
              <f7-link @click="$coreShowPopup(segment.domSegmentLimit.limitRebook)">
                {{$t('air.label.ruleAndLimit')}}
              </f7-link>
            </f7-col>
          </f7-row>
          <f7-row class="padding-vertical item-subtitle">
            <f7-col width="40">{{$i18nBundle('air.label.travelerAndReferNoAndTicketingDeadline')}}</f7-col>
            <f7-col width="60">
              <f7-col width="100" :key="passenger.id" v-for="passenger in segment.psgrList"
                      class="text-align-right">
                {{$i18nMsg(passenger.passengerNameCN,passenger.passengerNameEN)}}/{{passenger.pnrNo}}<br>
                <span v-if="passenger.pnrDeadline">{{passenger.pnrDeadline}}</span>
                <span v-if="!passenger.pnrDeadline">{{$i18nBundle('air.label.NoTicketingDeadline')}}</span>
              </f7-col>
            </f7-col>
          </f7-row>

          <f7-row class="padding-vertical item-subtitle no-gap">
            <f7-col width="40">{{$i18nBundle('air.label.yFullPrice')}}¥{{segment.yPrice}}</f7-col>
            <f7-col width="30" class="text-align-center">
              {{$i18nBundle('air.label.LLF')}}¥{{segment.lowestPrice}}
            </f7-col>
            <f7-col width="30" class="text-align-right">
              {{$i18nBundle('air.label.LLF_Loss')}}¥{{segment.price-segment.lowestPrice}}
            </f7-col>
          </f7-row>
          <f7-row class="padding-vertical item-subtitle" :key="reasonCode.id" v-for="reasonCode in segment.reasonCodeList">
            <f7-col width="50">{{$i18nMsg(reasonCode.nameCN,reasonCode.nameEN)}}：</f7-col>
            <f7-col class="text-align-right" width="50">{{$i18nMsg(reasonCode.infoCN,reasonCode.infoEN)}}</f7-col>
          </f7-row>
        </f7-block>
        <order-product-button :ref-name="'domAirAction'+index" :product-order="segment" :current-btns="productBtn[index]"></order-product-button>
      </div>
      <f7-sheet :opened="showPsgrFlag" @sheet:closed="showPsgrFlag = false">
        <f7-page>
          <f7-navbar>
            <f7-nav-left>
              <f7-link sheet-close>{{$t('common.label.close')}}</f7-link>
            </f7-nav-left>
            <f7-nav-title>{{$t('air.label.travelerAndReferNo')}}</f7-nav-title>
            <f7-nav-right>
              <f7-link @click="submitDelDomAir">{{$t('common.label.confirm')}}</f7-link>
            </f7-nav-right>
          </f7-navbar>
          <f7-list>
            <f7-list-item checkbox v-for="psgr in currentSegment.psgrList" :value="psgr.passengerId" :key="psgr.id" @change="changePsgr">
              <div slot="title">{{$i18nMsg(psgr.passengerNameCN,psgr.passengerNameEN)}}/{{psgr.pnrNo}}</div>
            </f7-list-item>
          </f7-list>
        </f7-page>
      </f7-sheet>
  </f7-page>
</template>
<script>

import OrderProductButton from './order-product-button'
import { mapGetters } from 'vuex'
import OrderService from '../../services/order/OrderService'

export default {
  name: 'order-detail-domair',
  components: { OrderProductButton },
  computed: {
    ...mapGetters(['Order/orderDetail', 'currentLoginUser'])
  },
  data () {
    let order = {}
    let productBtn = []
    let domAirSegments = []
    let currentSegment = {}
    let showPsgrFlag = false
    return { order, productBtn, domAirSegments, currentSegment, showPsgrFlag }
  },
  methods: {
    initDomBtn (order) {
      let self = this
      let domAirBtns = []
      if (order.orderButton.currentCreatorFlag && order.statusCode < 2 && order.historyFlag !== 1 && !order.tcUserId) {
        domAirBtns.push({
          title: self.$t('common.label.delete'),
          click: self.toDeleteDomAir,
          color: 'red'
        })
      }
      return domAirBtns
    },
    changePsgr (event) {
      let self = this
      let psgrList = self.currentSegment.psgrList
      let currentPsgrId = event.target.value
      for (let psgr of psgrList) {
        if (psgr.passengerId === currentPsgrId) {
          psgr.selected = !psgr.selected
        }
      }
    },
    initDomAirSegment (domAirList) {
      return OrderService.initDomAirSegment(domAirList)
    },
    toDeleteDomAir (segment) {
      let self = this
      self.currentSegment = Object.assign({}, segment)
      if (segment.psgrList.length > 1) {
        self.showPsgrFlag = true
      } else {
        for (let psgr of self.currentSegment.psgrList) {
          psgr.selected = true
        }
        self.$coreConfirm(self.$t('order.msg.cancel')).then(self.submitDelDomAir)
      }
    },
    submitDelDomAir () {
      let self = this
      self.showPsgrFlag = false
      self.currentSegment.psgrList = self.currentSegment.psgrList.filter(item => item.selected)
      let { userId, companyCode } = self.currentLoginUser
      let param = {
        cancelBy: userId,
        cancelPnr: true,
        domAirSegments: [self.currentSegment],
        taNo: self.order.taNo,
        companyCode: companyCode
      }
      OrderService.cancelDomAirOrder(param).then(function (data) {
        if (data && data.success) {
          self.$back({ force: true })
        } else if (data) {
          self.$coreError(data.message)
        }
      })
    }
  },
  mounted () {
    let self = this
    self.order = self['Order/orderDetail']
    if (self.order && self.order.newDomAirList) {
      self.domAirSegments = self.initDomAirSegment(self.order.newDomAirList)
      if (self.domAirSegments) {
        let index = 0
        for (let segment of self.domAirSegments) {
          self.productBtn[index] = self.initDomBtn(self.order, segment)
          index++
        }
      }
      console.log(self.domAirSegments)
    } else {
      self.$back()
    }
  }
}
</script>

<style scoped>

</style>
