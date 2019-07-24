<template>
  <div class="intelAirList">
    <f7-list class="planInfo">
      <order-detail-intl-segment :intl-air="intlAir" :cabins="cabins"/>
      <f7-list-item>
        <f7-row style="width: 100%;" v-if="intlAir && intlAir.psgrList">
          <f7-col width="50">{{$t('air.label.travelerAndReferNo')}}</f7-col>
          <f7-col width="50" :key="psgr.id" v-for="psgr in intlAir.psgrList"
                  class="text-align-right">
            {{$i18nMsg(psgr.passengerNameCN,psgr.passengerNameEN)}}{{psgr.pnrNo?'/'+psgr.pnrNo:''}}
          </f7-col>
        </f7-row>
      </f7-list-item>
      <f7-list-item v-if="intlAir && intlAir.airlineList" :header="$t('air.label.deadline')"
                    :title="intlAir.psgrList[0].deadline">
        <span style="position: absolute; right: 20px; top: 22px;">
          <f7-link :text="$t('air.label.restriction')" @click="$coreShowPopup(showOptionRuleInfo(intlAir))"/>&nbsp;
          <f7-link :text="$t('air.label.visaInfo')" v-if="hasVisaInfo(intlAir)"
                   @click="$coreShowPopup(showPackageVisaInfo(intlAir))"/>
        </span>
      </f7-list-item>
      <f7-list-item v-if="intlAir && intlAir.totalFare">
        <div slot="header">{{$t('air.label.totalPrice')}}</div>
        <div slot="title">
          <strong class="data-table-title">{{'¥' + (intlAir.totalFare + intlAir.totalTax)}}</strong>{{'(' + $t('air.label.ticketPrice') + '¥' + intlAir.totalFare + '+' + $t('air.label.tax') + '¥' + intlAir.totalTax + ')'}}
        </div>
      </f7-list-item>
      <f7-list-item :key="reasonCode.id" v-for="reasonCode in intlAir.reasonCodeList"
                    :header="$i18nMsg(reasonCode.nameCN,reasonCode.nameEN)"
                    :title="$i18nMsg(reasonCode.infoCN,reasonCode.infoEN)">
      </f7-list-item>
      <f7-list-item @click="toLLF()" v-if="intlAir.llfPackage" link="#" :after="$t('common.label.showMore')"
                    header="LLF"
                    :title="'¥'+(intlAir.llfPackage.totalFare+intlAir.llfPackage.totalTax)+'('+$t('air.label.taxFee')+'¥'+intlAir.llfPackage.totalTax+')'">
      </f7-list-item>
    </f7-list>
    <f7-popup :opened="showLlfFlag" @popup:closed="showLlfFlag = false" v-if="intlAir.llfPackage">
      <f7-page>
        <f7-navbar title="LLF">
          <f7-nav-right>
            <f7-link popup-close>{{$t('common.label.close')}}</f7-link>
          </f7-nav-right>
        </f7-navbar>
        <order-detail-intl-segment :intl-air="intlAir.llfPackage" :cabins="cabins"/>
      </f7-page>
    </f7-popup>
  </div>
</template>
<script>
import OrderService from '../../services/order/OrderService'
import { mapGetters } from 'vuex'
import OrderDetailIntlSegment from './order-detail-intl-segment'

export default {
  name: 'order-detail-intlp2p',
  components: { OrderDetailIntlSegment },
  props: {
    intlAir: {
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
    let showLlfFlag = false
    return { showLlfFlag }
  },
  computed: {
    ...mapGetters(['currentLoginUser'])
  },
  methods: {
    showOptionRuleInfo (option) {
      return OrderService.showOptionRuleInfo(option, this)
    },
    hasVisaInfo (option) {
      return OrderService.hasVisaInfo(option)
    },
    toLLF () {
      this.showLlfFlag = true
    },
    showPackageVisaInfo (option) {
      return OrderService.showPackageVisaInfo(option, this)
    }
  },
  mounted () {
  }
}
</script>

<style scoped>

</style>
