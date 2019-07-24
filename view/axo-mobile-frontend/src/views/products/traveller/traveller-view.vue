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
      <f7-list-item :title="$t('common.label.cert')"
                    v-if="certEnable"
                    smart-select
                    :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
        <label>
          <select :value="traveller.selectedCert&&traveller.selectedCert.id"
                  @change="changeAttrs(traveller, 'cert', $event)"
                  name="selectedCert">
            <option :key="index" v-for="(userCert, index) in traveller.userCerts" :value="userCert.id">
              {{$i18nMsg(userCert.certNameCN,userCert.certNameEN)}}-{{$i18nMsg(userCert.userNameCN,
              userCert.userNameEN)}}-{{userCert.certNo}}
            </option>
          </select>
        </label>
      </f7-list-item>
      <f7-list-item v-if="membershipEnable" smart-select :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
        <div slot="title">
          <f7-row>
            <f7-col>
              {{$t('common.label.membership')}}
            </f7-col>
          </f7-row>
        </div>
        <label>
          <select
            @change="changeAttrs(traveller, 'membership', $event)"
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

import ProductService from '../../../services/products/ProductService'

export default {
  name: 'traveller-view',
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

<style scoped>

</style>
