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
  name: 'others-traveller-view',
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
    domAirBooking: {
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
    selectTraveller: ProductService.selectBookTraveller
  }
}
</script>
