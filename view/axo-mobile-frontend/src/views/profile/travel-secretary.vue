<template>
  <f7-page>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"/>
      <f7-nav-title :title="$t('profile.label.travelArranger')"/>
    </f7-navbar>
    <f7-list>
      <f7-list-item>
        <f7-link slot="title" @click="toSelectArranger=true">
          <f7-icon f7="add_round" size="22"/>&nbsp;
          {{$t('profile.label.addTravelArranger')}}
        </f7-link>
      </f7-list-item>
    </f7-list>
    <f7-list media-list>
      <f7-list-item
          v-for="(val, index) of nameList"
          swipeout
          :key="val.id"
          :subtitle="val.arrangerUserEmail"
          :title="$i18nMsg(val.arrangerUserNameCN, val.arrangerUserNameEN)"
      >
        <f7-swipeout-actions right>
          <f7-swipeout-button @click="deleteSecretary(index)" delete>{{$t('common.label.delete')}}</f7-swipeout-button>
        </f7-swipeout-actions>
      </f7-list-item>
    </f7-list>
    <li v-show="false">
      <common-autocomplete
        class="item-link"
        input-class-name="text-align-right"
        :label="$t('profile.label.travelArranger')"
        :inline-label="true"
        :page-title="$t('profile.label.travelArranger')"
        :placeholder="$t('profile.label.addTravelArranger')"
        :text-property="$i18nMsg('userNameCN', 'userNameEN')"
        :text-property-fun="textPropertyFun"
        v-model="addName.arrangerUserId"
        value-property="uuid"
        @change="add($event)"
        :close-on-select="true"
        :request-source-on-open="true"
        :opened="toSelectArranger"
        @closed="toSelectArranger=false"
        input-events="search"
        auto-focus
        :autocompleteConfig="selectArrangerConfig"/>
    </li>
  </f7-page>
</template>

<script>
import cloneDeep from 'lodash/cloneDeep'
import ProfileService from '../../services/profile/ProfileService'
import { mapGetters } from 'vuex'

export default {
  name: 'travle-secretary',
  data () {
    const selectArrangerConfig = {
      keyWordKey: 'filterStr',
      searchMethod: this.searchArranger
    }
    return {
      nameList: [],
      selectedArranger: null,
      selectArrangerConfig,
      toSelectArranger: false,
      secretary: {
        userId: '',
        companyCode: '',
        arrangerList: []
      },
      addName: this.newSecretary()
    }
  },
  computed: {
    ...mapGetters(['loginResult', 'currentLoginUser'])
  },
  mounted () {
    let { userId, companyCode } = this.currentLoginUser
    this.secretary.userId = userId
    this.secretary.companyCode = companyCode
    this.loadSecretary()
  },
  methods: {
    newSecretary () {
      let { userId, companyCode } = this.$store.getters.currentLoginUser
      return {
        travelerUserId: userId,
        companyCode,
        arrangerUserId: '',
        arrangerUserName: '',
        arrangerUserNameCN: '',
        arrangerUserNameEN: '',
        email: ''
      }
    },
    searchArranger (param, config) {
      let argument = {
        '@class': 'com.citsamex.app.spi.data.caller.request.profile.axo.BackupsQueryParam',
        'keyWords': '',
        'pageSetting': {
          '@class': 'com.citsamex.app.spi.data.caller.common.PageSetting',
          'pageNumber': 1,
          'pageSize': 20
        },
        queryDto: {
          'travellerType': 3,
          'cpCode': this.currentLoginUser.companyCode
        }
      }
      return new Promise((resolve, reject) => {
        ProfileService.getAutoTravellerBackups(Object.assign(argument, param), config).then((result) => {
          console.info(result)
          if (result.success && result.resultData && result.resultData.travellers) {
            result.resultData.travellers.forEach(traveller => {
              traveller.uuid = traveller.userId
            })
            resolve(result.resultData.travellers)
          } else {
            reject(result)
          }
        }, reject)
      }).catch(function (error) {
        console.log(error)
      })
    },
    textPropertyFun (item) {
      if (item.data) {
        const data = item.data
        return `
          <div class="item-title-row">
            <div class="item-title">
            ${this.$i18nMsg(data.userNameCN, data.userNameEN)}
            </div>
          </div>
          <div class="item-subtitle">${data.userEmail}</div>
        `
      } else {
        return `<div class="item-title">${item.text}</div>`
      }
    },
    clearAutoArranger () {
      this.$nextTick(() => {
        this.selectedArranger = null
      })
    },
    loadSecretary () {
      let { userId, companyCode } = this.currentLoginUser
      ProfileService.getTravelArrangers({ userId, companyCode }).then(res => {
        console.info(res)
        this.secretary.arrangerList = res.resultData.arrangers || []
        this.nameList = cloneDeep(this.secretary.arrangerList)
      })
    },
    add (arranger) {
      let arr = []
      let found
      for (let i = 0; i < this.nameList.length; i++) {
        arr.push(this.nameList[i].arrangerUserId)
      }
      found = arr.indexOf(arranger.userId)
      if (arranger) {
        if (found === -1) {
          this.addName.arrangerUserName = this.addName.arrangerUserId.userNameCN
          this.addName.arrangerUserNameCN = this.addName.arrangerUserId.userNameCN
          this.addName.arrangerUserNameEN = this.addName.arrangerUserId.userNameEN
          this.addName.email = this.addName.arrangerUserId.userEmail
          this.addName.arrangerUserId = this.addName.arrangerUserId.userId
          this.secretary.arrangerList = cloneDeep(this.nameList)
          this.secretary.arrangerList.push(cloneDeep(this.addName))
          ProfileService.updateArrangerList(this.secretary).then(res => {
            console.log(res)
            console.log(this.addName)
            console.log(this.secretary)
            console.log(arranger)
            this.addName = this.newSecretary()
            this.loadSecretary()
          })
        } else {
          this.$coreAlert(this.$i18nBundle('common.msg.arrangerAdded'))
        }
      }
    },
    deleteSecretary (idx) {
      this.nameList.splice(idx, 1)
      this.secretary.arrangerList = cloneDeep(this.nameList)
      console.info(this.secretary.arrangerList.length)
      ProfileService.updateArrangerList(this.secretary).then(() => {
        this.addName = this.newSecretary()
        this.loadSecretary()
      })
    }
  }
}
</script>
