<template>
  <f7-page>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"/>
      <f7-nav-title :title="$t('profile.label.membership')"/>
      <f7-nav-right>
        <f7-link popup-open="#add-card">{{$t('common.label.add')}}</f7-link>
      </f7-nav-right>
    </f7-navbar>
    <f7-list media-list>
      <f7-list-item
        v-for="(val, index) of list"
        :key="val.id"
        swipeout
        :after="val.memberCardNo"
        :title="$i18nMsg(val.vendorNameCN, val.vendorNameEN)"
        :text="$i18nMsg(val.cardTypeNameCN, val.cardTypeNameEN)"
      >
        <f7-swipeout-actions right>
          <f7-swipeout-button @click="deleteRegular(index)" delete>{{$t('common.label.delete')}}</f7-swipeout-button>
        </f7-swipeout-actions>
      </f7-list-item>
    </f7-list>
    <f7-popup class="demo-popup" id="add-card">
      <f7-page>
        <f7-navbar>
          <f7-nav-left>
            <f7-link popup-close="#add-card">{{$t('common.label.close')}}</f7-link>
          </f7-nav-left>
          <f7-nav-title :title="$t('profile.label.addMemberCard')"/>
          <f7-nav-right>
            <f7-link popup-close="#add-card" @click="add">{{$t('common.label.confirm')}}</f7-link>
          </f7-nav-right>
        </f7-navbar>
        <f7-list inline-labels no-hairlines-md>
          <f7-list-item :title="$t('profile.label.memberCardType')" smart-select :smart-select-params="{openIn: 'sheet'}">
            <label for="cardType">
              <select name="genre"
                      @change="addList.cardType= $event.target.value"
                      :value="addList.cardType"
                      id="cardType"
              >
                <option value="1">{{$i18nBundle('profile.label.airline')}}</option>
                <option value="3">{{$i18nBundle('profile.label.hotel')}}</option>
              </select>
            </label>
        </f7-list-item>
        <li v-if="addList.cardType">
        <common-autocomplete
          class="item-link"
          input-class-name="text-align-right"
          :label="$t('profile.label.provider')"
          :inline-label="true"
          :text-property="$i18nMsg('nameCN', 'nameEN')"
          :text-property-fun="textPropertyFun"
          value-property="uuid"
          v-model="addList.vendorCode"
          :close-on-select="true"
          :request-source-on-open="true"
          input-events="search"
          auto-focus
          :autocompleteConfig="selectProviderConfig"/>
        </li>
        <f7-list-input
            :label="$t('profile.label.memberCardNum')"
            type="text"
            clear-button
            @input="addList.memberCardNo= $event.target.value"
            :value="addList.memberCardNo"
          >
          </f7-list-input>
        </f7-list>
      </f7-page>
    </f7-popup>
  </f7-page>
</template>

<script>
import cloneDeep from 'lodash/cloneDeep'
import ProfileService from '../../services/profile/ProfileService'
import { mapGetters } from 'vuex'
export default {
  data () {
    const selectProviderConfig = {
      keyWordKey: 'keyWords',
      searchMethod: this.searchAutocompleteProviders
    }
    return {
      selectProviderConfig,
      selectedProvider: null,
      list: [],
      idInfo: {
        memberships: [],
        userId: ''
      },
      addList: this.newRegular()
    }
  },
  computed: {
    ...mapGetters(['loginResult', 'currentLoginUser'])
  },
  mounted () {
    let { userId, memberships } = this.currentLoginUser
    this.idInfo.userId = userId // 赋值
    this.idInfo.memberships = memberships // 赋值
    this.loadRegular()
  },
  methods: {
    newRegular () {
      let { userId } = this.$store.getters.currentLoginUser
      return {
        userId,
        cardTypeNameCN: '',
        cardType: '',
        memberCardNo: '',
        vendorCode: '',
        vendorNameCN: ''
      }
    },
    loadRegular () {
      let { userId, memberships } = this.currentLoginUser
      ProfileService.getMemberships({ userId, memberships }).then(res => {
        this.idInfo.memberships = res.resultData.memberships || []
        console.log(res)
        this.list = cloneDeep(this.idInfo.memberships)
        console.log(this.list)
      })
    },
    deleteRegular (rgc) {
      this.list.splice(rgc, 1)
      this.save()
    },
    add () {
      for (let i of this.list) {
        if (i.vendorCode === this.addList.vendorCode.code && i.memberCardNo === this.addList.memberCardNo) {
          this.$coreError(this.$t('profile.label.membershipExists'))
          return
        }
      }
      this.save(true)
    },
    save (add) {
      this.addList.vendorCode = this.addList.vendorCode.code
      this.idInfo.memberships = cloneDeep(this.list)
      add && this.idInfo.memberships.push(cloneDeep(this.addList))
      console.log(this.idInfo.memberships.length)
      console.log(this.idInfo)
      ProfileService.updateMemberships(this.idInfo).then(res => {
        console.log(res)
        console.log(this.addList)
        if (res.success) {
          let cardType = this.addList.cardType
          this.addList = this.newRegular()
          this.addList.cardType = cardType
          this.loadRegular()
        } else {
          this.$coreError(res.message)
        }
      })
    },
    textPropertyFun (item) {
      if (item.data) {
        const data = item.data
        return `
          <div class="item-title-row">
            <div class="item-title">
            ${this.$i18nMsg(data.nameCN, data.nameEN)}
            </div>
          </div>
        `
      } else {
        return `<div class="item-title">${item.text}</div>`
      }
    },
    clearAutoProvider () {
      this.$nextTick(() => {
        this.selectedProvider = null
      })
    },
    searchAutocompleteProviders (param, config) {
      let cardType = document.getElementById('cardType').value
      let type
      if (cardType === '1') {
        type = '1'
        this.addList.cardType = '1'
      } else {
        type = '3'
        this.addList.cardType = '3'
      }
      let providers = {
        '@class': 'com.citsamex.app.spi.data.caller.request.master.profile.GetMembershipVendorParam',
        'keyWords': '',
        'pageSetting': {
          '@class': 'com.citsamex.app.spi.data.caller.common.PageSetting',
          'pageNumber': 1,
          'pageSize': 15
        },
        'type': type
      }
      return new Promise((resolve, reject) => {
        ProfileService.getMembershipVendors(Object.assign(providers, param), config).then((result) => {
          console.log(result)
          if (result.success && result.resultData && result.resultData.membershipVendors) {
            result.resultData.membershipVendors.forEach(membershipVendor => {
              membershipVendor.uuid = membershipVendor.code
            })
            resolve(result.resultData.membershipVendors)
          } else {
            reject(result)
          }
        }, reject)
      }).catch(function (error) {
        console.log(error)
      })
    }
  }
}
</script>
<style lang="scss" scoped>

</style>
