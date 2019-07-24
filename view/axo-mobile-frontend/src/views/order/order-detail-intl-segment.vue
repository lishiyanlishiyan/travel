<template>
  <div class="intelAirList">
    <div :key="airline.id" v-for="(airline,idx) in intlAir.airlineList">
      <f7-block-title>
        <f7-row no-gap class="flightInfo">
          <f7-col width="40">
            <f7-chip color="green" v-if="idx===0" :text="$t('air.label.departure')"/>
            <f7-chip color="blue" v-if="idx!==0" :text="$t('air.label.return')"/>
            {{airline.intlAirSegments[0].deptDate|date('YYYY-MM-DD')}}
          </f7-col>
          <f7-col width="60">
            {{airline.deptCityEntity?$i18nMsg(airline.deptCityEntity.nameCN,airline.deptCityEntity.nameEN):airline.deptCity}}→{{airline.arrivalCityEntity?$i18nMsg(airline.arrivalCityEntity.nameCN,airline.arrivalCityEntity.nameEN):airline.arrivalCity}}
          </f7-col>
        </f7-row>
      </f7-block-title>
      <f7-list class="no-flex">
        <f7-list-item>
          <div :key="segment.id" v-for="segment in airline.intlAirSegments">
            <f7-row no-gap class="transitInfo item-subtitle" v-if="segment.stopOver">
              <f7-col class="text-align-center text-color-purple" width="60">{{segment.deptPort?$i18nMsg(segment.deptPort.nameCN,segment.deptPort.nameEN):segment.deptPortCode}}</f7-col>
              <f7-col class="text-color-gray" width="20">{{$t('air.label.transitStay')}}</f7-col>
              <f7-col class="text-color-gray" width="20">{{showIntlDuration(segment.stopOver,'H')}}h{{showIntlDuration(segment.stopOver,'M')}}m</f7-col>
            </f7-row>
            <f7-row no-gap>
              <f7-col width="70">
                <f7-row>
                  <f7-col width="25" class="data-table-title">
                    {{segment.deptDate|date('HH:mm')}}
                    <span v-if="!isSameDay(airline.intlAirSegments[0].deptDate,segment.deptDate)">{{segment.deptDate|date('MM-DD')}}</span>
                  </f7-col>
                  <f7-col class="flightDuration" width="25">
                    {{showFlightDuration(segment.flightDuration)}}
                  </f7-col>
                  <f7-col width="50" class="data-table-title">
                    {{segment.arrivalDate|date('HH:mm')}}
                    <span v-if="!isSameDay(airline.intlAirSegments[0].deptDate,segment.arrivalDate)">{{segment.arrivalDate|date('MM-DD')}}</span>
                  </f7-col>
                </f7-row>
                <f7-row class="text-color-gray item-subtitle">
                  <f7-col width="50">
                    {{segment.deptPort?$i18nMsg(segment.deptPort.nameCN,segment.deptPort.nameEN):segment.deptPortCode}}{{segment.deptTerminal}}
                  </f7-col>
                  <f7-col width="50">
                    {{segment.arrivalPort?$i18nMsg(segment.arrivalPort.nameCN,segment.arrivalPort.nameEN):segment.arrivalPortCode}}{{segment.arrivalTerminal}}
                  </f7-col>
                </f7-row>
              </f7-col>
              <f7-col width="30" class="flightNo">
                <f7-row class="item-subtitle">
                  <f7-col>
                    {{segment.airline?$i18nMsg(segment.airline.nameCN, segment.airline.nameEN):''}}
                  </f7-col>
                </f7-row>
                <f7-row>
                  <f7-col>
                    {{segment.actionCode}}{{segment.flightNo}}
                    {{segment.planeType ?'':segment.planeStyle?'('+segment.planeStyle+')':''}}
                    <f7-link v-if="segment.planeType" :text="'('+segment.planeType.typeAlias+')'" @click="$coreShowSheet($i18nMsg(segment.planeType.nameCN+':<br/>'+segment.planeType.capabilityCN,segment.planeType.nameEN+':<br/>'+segment.planeType.capabilityEN))"></f7-link>
                    <f7-chip v-if="segment.shareAirwayCode" color="blue" :text="$t('air.label.share')"/>
                  </f7-col>
                </f7-row>
              </f7-col>
            </f7-row>
          </div>
        </f7-list-item>
      </f7-list>
    </div>
    <div>
    </div>
  </div>
</template>
<script>
import OrderService from '../../services/order/OrderService'
import { mapGetters } from 'vuex'
import moment from 'moment'

export default {
  name: 'order-detail-intl-segment',
  props: {
    intlAir: {
      type: Object
    },
    cabins: {
      type: Array
    }
  },
  data () {
    return {}
  },
  computed: {
    ...mapGetters(['currentLoginUser'])
  },
  methods: {
    getPhsicalCabinName (physicalCabin) {
      return OrderService.getPhsicalCabinName(physicalCabin, this)
    },
    showIntlDuration (dateStr, type) {
      return OrderService.showIntlDuration(dateStr, type)
    },
    isSameDay (deptDate, currentDate) {
      let deptDateStr = moment(deptDate).format('MM-DD')
      let currentDateStr = moment(currentDate).format('MM-DD')
      return deptDateStr === currentDateStr
    },
    showPackageVisaInfo (option) {
      return OrderService.showPackageVisaInfo(option, this)
    },
    showFlightDuration (duration) {
      return OrderService.showFlightDuration(duration)
    }
  },
  mounted () {
  }
}
</script>

<style scoped>

</style>
