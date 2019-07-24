<template>
  <div>
    <f7-list :key="index" v-for="(travellerRoom, index) in travellerRoomList">
      <template v-if="!travellerRoom.remainFlag">
        <f7-list-item>
          <div slot="title">
            <strong>{{`${$t('hotel.label.room')} ${index+1}`}}</strong>
            <span>({{guestCount}} {{$t('hotel.label.guestPer')}})</span>
          </div>
          <div slot="after">
            <f7-link @click="editTravellerRoom"
                     v-if="travellers.length>1">
              {{$t('hotel.label.assign')}}
            </f7-link>
          </div>
        </f7-list-item>
        <f7-list-item :key="tIndex" v-for="(traveller, tIndex) in travellerRoom.travellerList">
          <div class="item-title" slot="title">
            {{$i18nMsg(traveller.userNameCN, traveller.userNameEN)}}
            ({{traveller.userEmail}})
            <f7-chip v-if="traveller.tempUserId" :text="$t('profile.label.guest')" color="blue"/>
          </div>
        </f7-list-item>
        <f7-list-item v-if="specialNeedEnable" smart-select
                      :smart-select-params="{openIn: 'popup', closeOnSelect: true, renderItemRemark: specialNeedRemark}">
          <div slot="title">
            {{$t('hotel.label.specialNeeds')}}
          </div>
          <label>
            <select
              v-model="travellerRoom.specialNeeds"
              @change="travellerRoom.specialNeed = travellerRoom.specialNeeds.join(',')"
              multiple
              name="specialNeed">
              <option :key="index" v-for="(specialNeed, index) in specialNeeds" :value="specialNeed.id">
                {{$t(specialNeed.label)}}
              </option>
            </select>
          </label>
        </f7-list-item>
        <f7-list-item :title="$t('common.label.cert')" smart-select
                      v-if="certEnable"
                      :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
          <label>
            <select :value="travellerRoom.selectedCert&&travellerRoom.selectedCert.id"
                    @change="changeAttrs(travellerRoom, 'cert', $event)"
                    name="selectedCert">
              <option :key="index" v-for="(userCert, index) in travellerRoom.userCerts" :value="userCert.id">
                {{$i18nMsg(userCert.certNameCN,userCert.certNameEN)}}-{{$i18nMsg(userCert.userNameCN,
                userCert.userNameEN)}}-{{userCert.certNo}}
              </option>
            </select>
          </label>
        </f7-list-item>
        <li v-if="creditCardEnable">
          <traveller-credit-card v-model="travellerRoom.selectedCreditCard" :cert="creditCardCert"
                                 :travellers="travellerRoom.travellerList"/>
        </li>
        <f7-list-item v-if="membershipEnable" smart-select
                      :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
          <div slot="title">
            {{$t('common.label.membership')}}
          </div>
          <label>
            <select
              @change="changeAttrs(travellerRoom, 'membership', $event)"
              name="selectedMembership">
              <option value="">-----</option>
              <option :key="index" v-for="(membership, index) in travellerRoom.memberships" :value="membership.id">
                {{$i18nMsg(membership.vendorNameCN, membership.vendorNameEN)}}-{{membership.memberCardNo}}
              </option>
            </select>
          </label>
        </f7-list-item>
      </template>
    </f7-list>
    <f7-popup class="traveller-room-popup" :opened="popupOpened" @popup:closed="popupOpened = false">
      <f7-page>
        <f7-navbar>
          <f7-nav-left>
            <f7-link @click="closeTravellerRoom">{{$t('common.label.close')}}</f7-link>
          </f7-nav-left>
          <f7-nav-title>{{$t('hotel.label.rooms')}}</f7-nav-title>
          <f7-nav-right>
            <f7-link @click="saveTravellerRoom">{{$t('common.label.confirm')}}</f7-link>
          </f7-nav-right>
        </f7-navbar>
        <f7-list sortable sortable-enabled @sortable:sort="onMoveTraveller">
          <template v-for="travellerRoom in sortTravellerRoomList">
            <f7-list-item :data-id-key="travellerRoom.uuid" :key="travellerRoom.uuid"
                          bg-color="gray"
                          class="text-color-white"
                          :sortable="false"
                          group-title>
              <div>
                <strong v-if="travellerRoom.remainFlag">
                  {{travellerRoom.roomTitle}}
                  <f7-badge color="blue">{{travellerRoom.travellerList.length}}</f7-badge>
                </strong>
                <span v-if="!travellerRoom.remainFlag">
                  {{travellerRoom.roomTitle}}
                  <f7-badge
                    :color="(travellerRoom.travellerList.length>guestCount || !travellerRoom.travellerList.length)?'red':'blue'">
                  {{travellerRoom.travellerList.length}}
                  </f7-badge>
                </span>
              </div>
            </f7-list-item>
            <f7-list-item :data-id-key="traveller.uuid" :key="`${travellerRoom.uuid}/${traveller.uuid}`"
                          v-for="(traveller) in travellerRoom.travellerList">
              <div class="item-title" slot="title">
                {{$i18nMsg(traveller.userNameCN, traveller.userNameEN)}}
                ({{traveller.userEmail}})
                <f7-chip v-if="traveller.tempUserId" :text="$t('profile.label.guest')" color="blue"/>
              </div>
            </f7-list-item>
          </template>
        </f7-list>
        <f7-list>
          <li v-if="errorMessage">
            <common-form-errors :value="errorMessage"/>
          </li>
        </f7-list>
      </f7-page>
    </f7-popup>
  </div>
</template>

<script>
import uniqueId from 'lodash/uniqueId'
import find from 'lodash/find'
import cloneDeep from 'lodash/cloneDeep'
import forEach from 'lodash/forEach'
import ProductService from '../../../services/products/ProductService'
import TravellerCreditCard from './traveller-credit-card'
import ProfileConsts from '../../../consts/ProfileConsts'

export default {
  name: 'hotel-traveller-view',
  components: { TravellerCreditCard },
  model: {
    event: 'change'
  },
  props: {
    value: {
      type: Array
    },
    cert: {
      type: Boolean,
      default: false
    },
    creditCard: {
      type: Boolean,
      default: true
    },
    creditCardCert: {
      type: Boolean,
      default: false
    },
    membership: {
      type: Boolean,
      default: true
    },
    specialNeed: {
      type: Boolean,
      default: true
    },
    guestCount: {
      type: Number,
      default: 1
    },
    specialNeeds: {
      type: Array
    }
  },
  watch: {
    value: {
      handler (v) {
        if (this.travellerRoomList !== v) {
          this.travellerRoomList = this.initTravellerRooms(v)
        }
      },
      deep: true
    },
    cert: function (v) {
      this.certEnable = !!v
    },
    creditCard: function (v) {
      this.creditCardEnable = !!v
    },
    membership: function (v) {
      this.membershipEnable = !!v
    },
    specialNeed: function (v) {
      this.specialNeedEnable = !!v
    }
  },
  data () {
    const { cert, creditCard, membership, specialNeed } = this.$props
    return {
      errorMessage: null,
      travellers: [],
      travellerRoomList: [],
      sortTravellerRoomList: [],
      certEnable: cert,
      creditCardEnable: creditCard,
      membershipEnable: membership,
      specialNeedEnable: specialNeed,
      popupOpened: false
    }
  },
  mounted () {
    const { value } = this.$props
    this.travellerRoomList = this.initTravellerRooms(value)
  },
  methods: {
    changeAttrs: ProductService.changeTravellerAttrs,
    editTravellerRoom () {
      this.popupOpened = true
    },
    specialNeedRemark () {
      return `<ul class="no-padding-left">
                <li>
                  <div class="padding text-color-orange">
                    <i class="f7-icons" style="font-size: 18px">info_round_fill</i>
                    ${this.$i18nBundle('hotel.msg.specialNeedsLabel')}
                  </div>
                </li>
              </ul>`
    },
    saveTravellerRoom () {
      if (this.checkTravellerRooms(true)) {
        this.$emit('change', this.sortTravellerRoomList)
        this.popupOpened = false
      }
    },
    closeTravellerRoom () {
      this.initSortTravellerRooms(this.travellerRoomList)
      this.popupOpened = false
    },
    onMoveTraveller (e) {
      if (e.detail.to === 0) { // 限制移动到第一位
        this.initSortTravellerRooms(this.sortTravellerRoomList)
      } else {
        this.onSortRoomTravellers()
      }
      this.checkTravellerRooms()
    },
    onSortRoomTravellers () {
      const sortTravellerRooms = cloneDeep(this.sortTravellerRoomList)
      let currentRoom = sortTravellerRooms[0]
      this.$$('[data-id-key]').each((i, target) => {
        let uuid = this.$$(target).attr('data-id-key')
        if (uuid.length < 32) { // room
          currentRoom = find(sortTravellerRooms, { uuid: uuid })
          currentRoom.travellerList = []
        } else {
          let traveller = find(this.travellers, { uuid: uuid })
          currentRoom.travellerList.push(traveller)
        }
      })
      this.sortTravellerRoomList = sortTravellerRooms
    },
    initTravellerRooms (travellerRoomList) {
      travellerRoomList.forEach(travellerRoom => {
        forEach(ProfileConsts.TRAVELLER_ATTR_MAP, item => {
          let key = item.itemsKey
          travellerRoom[key] = []
          travellerRoom.travellerList.forEach(traveller => {
            if (traveller[key]) {
              travellerRoom[key] = travellerRoom[key].concat(traveller[key])
            }
          })
        })
      })
      this.initSortTravellerRooms(travellerRoomList)
      this.initTravellers(travellerRoomList)
      return travellerRoomList
    },
    initSortTravellerRooms (travellerRooms) {
      const rooms = cloneDeep(travellerRooms)
      rooms.forEach((travellerRoom, index) => {
        travellerRoom.uuid = uniqueId()
        travellerRoom.roomTitle = travellerRoom.remainFlag ? this.$t('hotel.label.unassigned')
          : `${this.$t('hotel.label.room')} ${index + 1} (${this.guestCount} ${this.$t('hotel.label.guestPer')})`
      })
      this.sortTravellerRoomList = rooms
    },
    initTravellers (travellerRoomList) {
      let travellers = []
      travellerRoomList.forEach(travellerRoom => {
        travellers = travellers.concat(travellerRoom.travellerList)
      })
      this.travellers = travellers
    },
    checkTravellerRooms (showMsg) {
      const { guestCount } = this.$props
      this.errorMessage = null
      for (let i = 0; i < this.sortTravellerRoomList.length; i++) {
        let travellerRoom = this.sortTravellerRoomList[i]
        if (!travellerRoom.remainFlag) {
          if (travellerRoom.travellerList.length === 0) {
            this.errorMessage = this.$t('hotel.label.selectedRoomTraveller')
            break
          }
          if (travellerRoom.travellerList.length > guestCount) {
            this.errorMessage = this.$t('hotel.error.countError')
            break
          }
        }
      }
      if (showMsg && this.errorMessage) {
        this.$coreError(this.errorMessage)
      }
      return !this.errorMessage
    }
  }
}
</script>

<style scoped>

</style>
