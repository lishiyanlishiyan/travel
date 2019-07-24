<template>
  <div>
    <f7-list :key="index" v-for="(traveller, index) in travellers">
      <f7-list-item
        checkbox
        :checked="traveller.selected"
        @change="selectTraveller(traveller, $event)">
        <div class="item-title" slot="title">
          {{$i18nMsg(traveller.userNameCN, traveller.userNameEN)}}
          ({{traveller.userEmail}})
          <f7-chip v-if="traveller.tempUserId" :text="$t('profile.label.guest')" color="blue"/>
        </div>
      </f7-list-item>
      <f7-list-item :title="$t('common.label.cert')" smart-select :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
        <label>
          <select :value="traveller.selectedCert&&traveller.selectedCert.id"
                  @change="changeAttrs(traveller, 'cert', $event)"
                  name="selectedCert">
            <option :key="index" v-for="(userCert, index) in traveller.userCerts" :value="userCert.id">
              {{$i18nMsg(userCert.certNameCN,userCert.certNameEN)}}-{{$i18nMsg(userCert.userNameCN, userCert.userNameEN)}}-{{userCert.certNo}}
            </option>
          </select>
        </label>
      </f7-list-item>
      <f7-list-item :key="index" v-for="(flight, index) in traveller.flightSegmentList" smart-select :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
        <div slot="title">
          <f7-row>
            <f7-col>
              {{$t('common.label.membership')}}
              <f7-chip v-if="intlAirSearchParam.intlAirSearchDto.interFlightType==='RT' && flight.airlineIndex===1 && flight.flightSize===2" :text="$t('air.label.departure') + flight.flightIndex" color="blue"/>
              <f7-chip v-if="intlAirSearchParam.intlAirSearchDto.interFlightType==='RT' && flight.airlineIndex===2 && flight.flightSize===2" :text="$t('air.label.return') + flight.flightIndex" color="green"/>
              <f7-chip v-if="intlAirSearchParam.intlAirSearchDto.interFlightType==='RT' && flight.airlineIndex===1 && flight.flightSize===1" :text="$t('air.label.departure')" color="blue"/>
              <f7-chip v-if="intlAirSearchParam.intlAirSearchDto.interFlightType==='RT' && flight.airlineIndex===2 && flight.flightSize===1" :text="$t('air.label.return')" color="green"/>
              <f7-chip v-if="intlAirSearchParam.intlAirSearchDto.interFlightType==='OW' && flight.flightSize===2" :text="$t('air.label.ow') + flight.flightIndex" color="blue"/>
              <f7-chip v-if="intlAirSearchParam.intlAirSearchDto.interFlightType==='OW' && flight.flightSize===1" :text="$t('air.label.ow')" color="blue"/>
            </f7-col>
          </f7-row>
        </div>
        <label>
          <select @change="changeSegmentAttrs(traveller, flight, 'membership', $event)"
                  name="selectedMembership">
            <option value="">-----</option>
            <option :key="index" v-for="(membership, index) in traveller.memberships" :value="membership.id">
              {{$i18nMsg(membership.vendorNameCN, membership.vendorNameEN)}}-{{membership.memberCardNo}}
            </option>
          </select>
        </label>
      </f7-list-item>
    </f7-list>
  </div>
</template>

<script>

import find from 'lodash/find'
import cloneDeep from 'lodash/cloneDeep'
import ProductService from '../../../services/products/ProductService'
import ProfileConsts from '../../../consts/ProfileConsts'

const TRAVELLER_ATTR_MAP = cloneDeep(ProfileConsts.TRAVELLER_ATTR_MAP)

export default {
  name: 'intlair-traveller-view',
  model: {
    event: 'change'
  },
  props: {
    value: {
      type: Array
    },
    cert: {
      type: Boolean,
      default: true
    },
    creditCard: {
      type: Boolean,
      default: false
    },
    membership: {
      type: Boolean,
      default: true
    },
    intlAirBookingDto: {
      type: Object
    },
    intlAirSearchParam: {
      type: Object
    }
  },
  watch: {
    value: {
      handler (v) {
        this.travellers = this.initSelectTravellers(v)
      },
      deep: true
    }
  },
  data () {
    const { cert, creditCard, membership } = this.$props
    return {
      travellers: [],
      certEnable: cert,
      creditCardEnable: creditCard,
      membershipEnable: membership
    }
  },
  mounted () {
    const { value } = this.$props
    this.travellers = this.initSelectTravellers(value)
  },
  methods: {
    changeAttrs: ProductService.changeTravellerAttrs,
    initSelectTravellers: ProductService.initSelectTravellers,
    selectTraveller: ProductService.selectBookTraveller,
    changeSegmentAttrs (traveller, flight, type, $event) {
      console.info(traveller, type, $event.target.value)
      const v = TRAVELLER_ATTR_MAP[type]
      if (type === 'membership') {
        flight[v.selectKey] = cloneDeep(find(traveller[v.itemsKey], { id: parseInt($event.target.value) }))
      }
      this.$forceUpdate()
    }
  }
}
</script>

<style scoped>

</style>
