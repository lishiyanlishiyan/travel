<template>
  <f7-block strong class="intlOrder">
    <div :key="airline.id" v-for="airline in intlOption.airlineList">
      <f7-row no-gap class="padding-vertical">
        <f7-col width="75" class="text-align-center">
          <f7-row>
            <f7-col width="30" class="data-table-title text-clip" style="line-height: 38px;">{{airline.deptDate|date('MM-DD')}}</f7-col>
            <f7-col width="70" class="data-table-subtitle text-clip" style="line-height: 38px;">
              {{airline.deptCityEntity?$i18nMsg(airline.deptCityEntity.nameCN,airline.deptCityEntity.nameEN):airline.deptCity}}
              <f7-icon f7="arrow_right"/>
              {{airline.arrivalCityEntity?$i18nMsg(airline.arrivalCityEntity.nameCN,airline.arrivalCityEntity.nameEN):airline.arrivalCity}}
            </f7-col>
          </f7-row>
        </f7-col>
        <f7-col width="25" class="text-align-center">
          <f7-row class="item-subtitle">
            <f7-col>
              {{(airline.preferTime && airline.preferTime!=='-1')?airline.preferTime:'00:00-24:00'}}
            </f7-col>
          </f7-row>
          <f7-row class="item-subtitle">
            <f7-col>
              {{getCabinName(airline.cabin)}}
            </f7-col>
          </f7-row>
        </f7-col>
      </f7-row>
    </div>
    <f7-row class="padding-vertical item-subtitle">
      <f7-col width="40">{{$t('air.label.travelerAndReferNo')}}</f7-col>
      <f7-col width="60">
        <f7-col width="100" :key="psgr.id" v-for="psgr in intlOption.psgrList"
                class="text-align-right">
          {{$i18nMsg(psgr.passengerNameCN,psgr.passengerNameEN)}}{{psgr.pnrNo?'/'+psgr.pnrNo:''}}
        </f7-col>
      </f7-col>
    </f7-row>
    <f7-row class="padding-vertical item-subtitle text-align-center" v-if="intlOption.remark">
      <f7-col>
        <f7-link :text="$t('common.label.remark')" @click="$coreShowPopup(intlOption.remark)"/>
      </f7-col>
    </f7-row>
    <div v-if="intlOption.reasonCodeList">
      <f7-row class="padding-vertical item-subtitle" :key="reasonCode.id" v-for="reasonCode in intlOption.reasonCodeList">
        <f7-col width="50">{{$i18nMsg(reasonCode.nameCN,reasonCode.nameEN)}}：</f7-col>
        <f7-col class="text-align-right" width="50">{{$i18nMsg(reasonCode.infoCN,reasonCode.infoEN)}}</f7-col>
      </f7-row>
    </div>
    <f7-row class="item-subtitle" v-if="showOptionView(intlOption)">
      <f7-col class="text-color-gray" width="100" v-if="!intlOption.optionList || intlOption.optionList.length===0 ||intlOption.status===1">
        {{$t('air.label.optionPrepare')}}
      </f7-col>
    </f7-row>
    <f7-row v-if="showRequestView(intlOption) && intlOption.totalFare" class="padding-vertical item-subtitle">
      <f7-col width="50">
        {{$t('air.label.personFare')}}
      </f7-col>
      <f7-col width="50" class="text-align-right">
        {{intlOption.totalFare}}
      </f7-col>
    </f7-row>

  </f7-block>
</template>
<script>
import { ProductType } from '../../consts/OrderConsts'
import OrderService from '../../services/order/OrderService'

export default {
  name: 'order-detail-intloption',
  props: {
    intlOption: {
      type: Object
    },
    cabins: {
      type: Array
    },
    order: {
      type: Object
    }
  },
  data () {
    const intlDefaultSetup = this.$defaultSetup(ProductType.IntlAir)
    const intlQCMode = intlDefaultSetup.qcMode
    const cabinList = intlDefaultSetup.cabinList
    return { intlQCMode, showOptions: false, editFlag: false, cabinList }
  },
  methods: {
    getCabinName (cabin) {
      let self = this
      let result = self.$t('air.label.cabinA')
      if (self.cabins) {
        for (let currentCabin of self.cabins) {
          if (currentCabin.value === cabin) {
            result = self.$t(cabin.title, cabin.value ? cabin.value : result)
            break
          }
        }
      }
      return result
    },
    showOptionView (intlAir) {
      return OrderService.showOptionView(intlAir, this.order, this.intlQCMode)
    },
    showRequestView (intlAir) {
      let self = this
      let qcMode = self.intlQCMode
      if (qcMode) {
        return intlAir.emailFlag === 'Y' && self.order.statusCode === '1' &&
            self.order.historyFlag !== 1 && !self.order.tcUserId
      }
      return intlAir.emailFlag === 'N' && self.order.statusCode === '1' && self.order.historyFlag !== 1 &&
          self.order.orderButton.currentCreatorFlag && !self.order.tcUserId
    }
  },
  mounted () {

  }
}
</script>

<style scoped>

</style>
