<template>
  <div>
    <f7-list v-if="$hasRole('ARRANGER')">
      <f7-list-item link @click="toSelectTraveller=true">
        <f7-link slot="title">
          <f7-icon f7="add_round" size="22"/>&nbsp;
          {{$t('common.label.selectTraveller')}}
        </f7-link>
      </f7-list-item>
      <li v-show="false">
        <common-autocomplete
          class="item-link"
          input-class-name="text-align-right"
          :label="$t('common.label.traveller')"
          :inline-label="true"
          :page-title="$t('common.label.selectTraveller')"
          :placeholder="$t('common.label.selectTraveller')"
          :text-property="$i18nMsg('userNameCN', 'userNameEN')"
          :text-property-fun="textPropertyFun"
          v-model="selectedTraveller"
          @change="selectTraveller($event)"
          value-property="uuid"
          :close-on-select="true"
          :request-source-on-open="true"
          input-events="search"
          :opened="toSelectTraveller"
          @closed="toSelectTraveller=false"
          auto-focus
          :autocompleteConfig="selectTravellerConfig"/>
      </li>
    </f7-list>
    <f7-list media-list>
      <f7-list-item
        :swipeout="$hasRole('ARRANGER')"
        :key="traveller.uuid"
        link
        v-for="traveller in travellerTab.selectedTravellers"
        :subtitle="traveller.userEmail">
        <div class="item-title" slot="title">
          {{$i18nMsg(traveller.userNameCN, traveller.userNameEN)}}
          <f7-chip v-if="traveller.tempUserId" :text="$t('profile.label.guest')" color="blue"/>
        </div>
        <f7-swipeout-actions right>
          <f7-swipeout-button @click="removeTraveller(traveller)" delete>
            {{$t('common.label.delete')}}
          </f7-swipeout-button>
        </f7-swipeout-actions>
      </f7-list-item>
    </f7-list>
  </div>
</template>

<script>
import ProductService from '../../../services/products/ProductService'
import { mapGetters } from 'vuex'

export default {
  name: 'traveller-select',
  model: {
    event: 'change'
  },
  props: {
    value: {
      type: Object
    }
  },
  data () {
    const selectTravellerConfig = {
      keyWordKey: 'keyWords',
      searchMethod: ProductService.searchAutocompleteTravellers
    }
    const { value } = this.$props
    return {
      travellerTab: value,
      selectTravellerConfig,
      selectedTraveller: null,
      toSelectTraveller: false
    }
  },
  computed: {
    ...mapGetters(['currentLoginUser'])
  },
  watch: {
    value: {
      handler (v) {
        this.travellerTab = v
      },
      deep: true
    }
  },
  mounted () {
    const currentLoginTraveller = ProductService.getTravellerByUserBasic(this.currentLoginUser)
    if (this.$hasRole('TRAVELLER')) {
      ProductService.validateTraveller(currentLoginTraveller.userId, 1, { loading: false }).then(() => {
      }, () => {
        this.validateTravellerError().then(() => {
          this.removeTraveller(currentLoginTraveller)
        })
      })
    } else {
      this.removeTraveller(currentLoginTraveller)
    }
  },
  methods: {
    textPropertyFun (item) {
      if (item.data) {
        const data = item.data
        return `
          <div class="item-title-row">
            <div class="item-title">
            ${this.$i18nMsg(data.userNameCN, data.userNameEN)}
            ${data.tempUserId ? `
            <div class="chip color-blue">
              <div class="chip-label">${this.$i18nBundle('profile.label.guest')}</div>
            </div>
            ` : ''}
            </div>
          </div>
          <div class="item-subtitle">${data.userEmail}</div>
        `
      } else {
        return `<div class="item-title">${item.text}</div>`
      }
    },
    clearAutoTraveller () {
      this.$nextTick(() => {
        this.selectedTraveller = null
      })
    },
    checkTravellerIndex (travellers, traveller) {
      return this.$checkItemIndex(travellers, traveller, ['userId', 'tempUserId'])
    },
    selectTraveller (traveller) {
      if (traveller) {
        const found = this.checkTravellerIndex(this.travellerTab.selectedTravellers, traveller)
        if (found === -1) {
          ProductService.validateTraveller(traveller.passengerType === 'T' ? traveller.tempUserId : traveller.userId, 1).then(result => {
            if (result.msgCN) {
              this.$coreError(this.$i18nMsg(result.msgCN, result.msgEN))
            } else {
              this.travellerTab.selectedTravellers.push(traveller)
              this.selectCreator(traveller.passengerType === 'T' ? traveller.contactUser : traveller)
            }
          })
        } else {
          this.$coreAlert(this.$i18nBundle('common.msg.travellerAdded'))
        }
        this.clearAutoTraveller()
      }
    },
    selectCreator (creator) {
      if (creator) {
        let idx = this.checkTravellerIndex(this.travellerTab.selectedCreators, creator)
        if (idx === -1) {
          this.travellerTab.selectedCreators.push(creator)
          this.travellerTab.policyUserId = creator.userId
        }
      }
    },
    removeTraveller (traveller) {
      const index = this.checkTravellerIndex(this.travellerTab.selectedTravellers, traveller)
      this.travellerTab.selectedTravellers.splice(index, 1)
      const needRemoveCreator = traveller.userId !== this.currentLoginUser.userId
      if (needRemoveCreator) {
        this.removeCreator(traveller.passengerType === 'T' ? traveller.contactUser : traveller, true)
      }
      this.onChange()
    },
    removeCreator (creator) {
      const index = this.checkTravellerIndex(this.travellerTab.selectedCreators, creator)
      this.travellerTab.selectedCreators.splice(index, 1)
      const policyUser = ProductService.reCalcPolicyUser(this.travellerTab.selectedCreators, this.travellerTab.policyUserId)
      this.travellerTab.policyUserId = policyUser.userId ? policyUser.userId : null
    },
    onChange () {
      this.$emit('change', this.travellerTab)
    }
  }
}
</script>

<style scoped>

</style>
