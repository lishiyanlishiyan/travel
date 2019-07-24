<template>
  <f7-page page-content :with-subnavbar="intlOption.optionList && intlOption.optionList.length>1" class="intelAirList">
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"/>
      <f7-nav-title>{{editFlag==='1'?$t('air.label.selectOption'):$t('air.label.viewOption')}}</f7-nav-title>
      <f7-subnavbar v-if="intlOption.optionList && intlOption.optionList.length>1">
        <f7-segmented raised>
          <f7-button :key="option.id"
                     v-html="$t('air.label.option')+(index+1)"
                     :active="option.id===selectedOption.id"
                     @click="selectedOption=option"
                     v-for="(option,index) in intlOption.optionList">
          </f7-button>
        </f7-segmented>
      </f7-subnavbar>
    </f7-navbar>
    <div :key="airline.id" v-for="(airline,idx) in selectedOption.airlineList">
      <f7-block-title>
        <f7-row no-gap class="flightInfo">
          <f7-col width="40">
            <f7-chip color="blue" :text="$t('air.label.segment')+(idx+1)"/>
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
                    {{showIntlDuration(segment.flightDuration)}}
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
                <div class="item-subtitle">
                    {{segment.airline?$i18nMsg(segment.airline.nameCN, segment.airline.nameEN):''}}&nbsp;
                    {{segment.actionCode}}{{segment.flightNo}}{{segment.planeType?'('+segment.planeType.typeAlias+')':''}}
                    <f7-chip v-if="segment.shareAirwayCode" color="blue" :text="$t('air.label.share')"/>
                </div>
              </f7-col>
            </f7-row>
          </div>
        </f7-list-item>
      </f7-list>
    </div>
    <f7-list class="planInfo">
      <f7-list-item v-if="selectedOption && selectedOption.psgrList">
        <f7-row>
          <f7-col width="40">{{$t('air.label.travelerAndReferNo')}}</f7-col>
          <f7-col width="60">
            <f7-col width="100" :key="psgr.id" v-for="psgr in selectedOption.psgrList"
                    class="text-align-right">
              {{$i18nMsg(psgr.passengerNameCN,psgr.passengerNameEN)}}{{psgr.pnrNo?'/'+psgr.pnrNo:''}}
            </f7-col>
          </f7-col>
        </f7-row>
      </f7-list-item>
      <f7-list-item v-if="selectedOption && selectedOption.airlineList">
         <div slot="after">
              <f7-link :text="$t('air.label.restriction')" @click="$coreShowPopup(showOptionRuleInfo(selectedOption))"/>&nbsp;
              <f7-link v-if="hasVisaInfo(selectedOption)" :text="$t('air.label.visaInfo')" @click="$coreShowPopup(showPackageVisaInfo(selectedOption))"/>
            </div>
        <div slot="header">{{$t('air.label.deadline')}}</div>
        <div slot="title">{{selectedOption.psgrList[0].deadline}}</div>
      </f7-list-item>
      <f7-list-item v-if="selectedOption && selectedOption.totalFare">
        <div slot="header">{{$t('air.label.totalPrice')}}</div>
        <div slot="title"> <strong class="data-table-title">
          {{'¥'+(selectedOption.totalFare+selectedOption.totalTax)}}
        </strong>
          {{'('+$t('air.label.ticketPrice')+'¥'+selectedOption.totalFare+'+'+$t('air.label.tax')+'¥'+selectedOption.totalTax+')'}}
        </div>
      </f7-list-item>
    </f7-list>
    <div>
      <f7-block>
        <f7-row>
          <f7-col v-if="editFlag==='1'">
            <f7-button fill large @click="selectOption()">{{$t('air.label.selectOption')}}</f7-button>
          </f7-col>
        </f7-row>
      </f7-block>
    </div>
  </f7-page>
</template>
<script>
import OrderService from '../../services/order/OrderService'
import { mapGetters } from 'vuex'
import moment from 'moment'
import PolicyService from '../../services/policy/PolicyControlService'

export default {
  name: 'order-detail-intloption-select',
  data () {
    const currentTaNo = this.$f7route.params.taNo
    const packageId = this.$f7route.params.packageId
    const editFlag = this.$f7route.params.edit
    let intlOption = {}
    let selectedOption = {}
    return { currentTaNo, packageId, editFlag, intlOption, selectedOption }
  },
  computed: {
    ...mapGetters(['currentLoginUser'])
  },
  methods: {
    initIntlOptions () {
      let self = this
      let { companyCode, userId } = self.currentLoginUser
      let param = {
        companyCode,
        taNo: self.currentTaNo,
        packageId: self.packageId,
        userId
      }
      OrderService.getIntlOptions(param).then(function (data) {
        if (data && data.success) {
          self.intlOption = data.resultData
          if (self.intlOption) {
            self.selectedOption = self.intlOption.optionList[0]
          }
        } else if (data) {
          self.$coreError(data.message).then(function () {
            self.$back()
          })
        }
      })
    },
    getPhsicalCabinName (physicalCabin) {
      return OrderService.getPhsicalCabinName(physicalCabin, this)
    },
    selectOption () {
      let self = this
      let { companyCode, userId } = self.currentLoginUser
      self.$coreConfirm(self.$t('air.msg.submitOptionConfirm')).then(function () {
        let param = {
          packageType: self.intlOption.packageType,
          taNo: self.currentTaNo,
          policyApplied: false,
          policyUserId: self.intlOption.policyUserId,
          optionId: self.selectedOption.id,
          companyCode: companyCode,
          userId: userId
        }
        OrderService.submitSelectOption(param).then(function (data) {
          if (data && data.success) {
            let resultData = data.resultData
            if (resultData.policyControlResult) {
              PolicyService.handlePolicyControl(resultData.policyControlResult, function (resultData, reasonCodes) {
                param.policyApplied = true
                if (reasonCodes) {
                  param.reasonCode = reasonCodes.join(',')
                }
                OrderService.submitSelectOption(param).then(function (data) {
                  if (data && data.success) {
                    self.$store.dispatch('Order/storeOrderDetail')
                    self.$back({ force: true })
                  } else if (data) {
                    self.$coreError(data.message)
                  }
                })
              })
            } else {
              self.$store.dispatch('Order/storeOrderDetail')
              self.$back({ force: true })
            }
          } else if (data) {
            self.$coreError(data.message)
          }
        })
      })
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
    showOptionRuleInfo (option) {
      return OrderService.showOptionRuleInfo(option, this)
    },
    hasVisaInfo (option) {
      return OrderService.hasVisaInfo(option)
    }
  },
  mounted () {
    let self = this
    self.initIntlOptions()
  }
}
</script>

<style scoped>

</style>
