<template>
  <f7-page>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"/>
      <f7-nav-title :title="$t('profile.label.basicPersonalInfo')"/>
      <f7-nav-right>
        <f7-link @click="save">{{$t('common.label.save')}}</f7-link>
      </f7-nav-right>
    </f7-navbar>
    <f7-list inline-labels no-hairlines-md>
      <f7-list-item :title="$t('profile.label.nationality')" v-if="dataList.userId" smart-select :smart-select-params="{closeOnSelect: true}">
        <label>
          <select name="nationality"
                  :value="dataList.defaultNation"
                  @change="dataList.defaultNation = $event.target.value">
            <option v-for="(val, index) of CountryList"
                    :key="index"
                    :value="val.code">
              {{$i18nMsg(val.nameCN,val.nameEN)}}
            </option>
          </select>
        </label>
      </f7-list-item>
      <f7-list-input
        v-show="dataList.defaultNation === 'CN' || dataList.defaultNation === 'HK' || dataList.defaultNation === 'MO' || dataList.defaultNation === 'TW'"
        :label="$t('profile.label.surNameCN')"
        type="text"
        readonly
        :value="dataList.surNameCN"
      >
      </f7-list-input>
      <f7-list-input
        v-show="dataList.defaultNation === 'CN' || dataList.defaultNation === 'HK' || dataList.defaultNation === 'MO' || dataList.defaultNation === 'TW'"
        :label="$t('profile.label.givenNameCN')"
        type="text"
        readonly
        :value="dataList.givenNameCN"
      >
      </f7-list-input>
      <f7-list-input
        :label="$t('profile.label.surNameEN')"
        type="text"
        readonly
        :value="dataList.surNameEN"
      >
      </f7-list-input>
      <f7-list-input
        :label="$t('profile.label.givenNameEN')"
        type="text"
        readonly
        :value="dataList.givenNameEN"
      >
      </f7-list-input>
      <f7-list-item :title="$t('profile.label.gender')" v-if="dataList.userId" smart-select :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
        <label>
          <select name="sex"
            :value="dataList.gender"
            @change="dataList.gender = $event.target.value"
          >
            <option value="MR">{{$t('common.label.male')}}</option>
            <option value="MS">{{$t('common.label.female')}}</option>
          </select>
        </label>
      </f7-list-item>
      <li>
        <common-datepicker
          :label="$t('profile.label.birthday')"
          name="birthday"
          v-model="dataList.birthday"
        >
        </common-datepicker>
      </li>
      <f7-list-input
        :label="$t('profile.label.telephone')"
        type="tel"
        clear-button
        :value="dataList.phone"
        @input="dataList.phone = $event.target.value"
      >
      </f7-list-input>
      <f7-list-input
        :label="$t('profile.label.mobile')"
        type="tel"
        clear-button
        :value="dataList.mobile"
        @input="dataList.mobile = $event.target.value"
      >
      </f7-list-input>
      <f7-list-input
        :label="$t('profile.label.email')"
        type="email"
        readonly
        :value="dataList.email"
      >
      </f7-list-input>
      <f7-list-item :title="$t('profile.label.emailLanguage')" v-if="dataList.userId" smart-select :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
        <label>
          <select name="emailLanguage"
            :value="dataList.emailLanguage"
            @change="dataList.emailLanguage = $event.target.value"
          >
            <option value="zh_CN">{{$t('common.label.Chinese')}}</option>
            <option value="en_US">{{$t('common.label.English')}}</option>
          </select>
        </label>
      </f7-list-item>
    </f7-list>
  </f7-page>
</template>

<script>
import ProfileService from '../../services/profile/ProfileService'
import MasterDataService from '../../services/master/MasterDataService'
import { mapGetters } from 'vuex'
import cloneDeep from 'lodash/cloneDeep'

export default {
  name: 'personal-data',
  data () {
    return {
      dataList: {},
      data: {
        userBasic: []
      },
      CountryList: []
    }
  },
  computed: {
    ...mapGetters(['loginResult', 'currentLoginUser'])
  },
  mounted () {
    let { userId } = this.currentLoginUser
    this.data.userId = userId
    this.loadBasicInfo()
    MasterDataService.searchAllCountryList().then(res => {
      console.info(res)
      this.CountryList = res
    })
  },
  methods: {
    loadBasicInfo () {
      let { userId, userBasic } = this.currentLoginUser
      ProfileService.getUserBasicInfo({ userId, userBasic }).then(res => {
        console.info(res)
        this.dataList = cloneDeep(res.resultData.userBasic)
        let nameCN = this.dataList.nameCN.split()
        let nameEN = this.dataList.nameEN.split(' ')
        let surNameCN = nameCN[0].substring(0, 1)
        let givenNameCN = nameCN[0].substring(1, nameCN[0].length)
        let surNameEN = nameEN[0]
        let givenNameEN = nameEN[1]
        this.dataList.surNameCN = surNameCN
        this.dataList.givenNameCN = givenNameCN
        this.dataList.surNameEN = surNameEN
        this.dataList.givenNameEN = givenNameEN
        console.info(this.dataList)
      })
    },
    save () {
      let phone = /^(0\d{2,3}-)?([2-9]\d{6,7})+(([-#])\d{1,6})?$/
      let mobile = /^1[345789]\d{9}$/
      if (this.dataList.phone !== '') {
        if (!phone.test(this.dataList.phone) && !mobile.test(this.dataList.phone)) {
          this.$coreError(this.$t('profile.label.correctPhone'))
          return
        }
      }
      if (!mobile.test(this.dataList.mobile)) {
        this.$coreError(this.$t('profile.label.correctMoblie'))
        return
      }
      this.data.userBasic = cloneDeep(this.dataList)
      ProfileService.updateProfileBasic(this.data).then(res => {
        console.log(res)
        if (res.success) {
          let changeList = this.dataList
          console.log(changeList)
          this.dataList = changeList
          console.log(this.dataList)
          this.loadBasicInfo()
        }
      })
    }
  }
}
</script>
