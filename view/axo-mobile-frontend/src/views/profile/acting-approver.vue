<template>
  <f7-page>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"/>
      <f7-nav-title :title="$t('profile.label.alternativeApprover')"/>
      <f7-nav-right>
        <f7-link popup-open="#add-approver">{{$t('common.label.add')}}</f7-link>
      </f7-nav-right>
    </f7-navbar>
    <f7-list media-list>
      <f7-list-item
        swipeout
        v-for="(val, index) of nameList"
        :key="val.id"
        :title="$i18nMsg(val.agentUserNameCN, val.agentUserNameEN)"
        :text="val.effectDate+$t('common.label.until')+val.expiryDate"
      >
        <f7-swipeout-actions right>
          <f7-swipeout-button @click="deleteSecretary(index)" delete>{{$t('common.label.delete')}}</f7-swipeout-button>
        </f7-swipeout-actions>
      </f7-list-item>
    </f7-list>
    <f7-popup class="demo-popup" id="add-approver">
      <f7-page>
        <f7-navbar>
            <f7-nav-left popup-close="#add-approver">
              <f7-link popup-close="#add-approver">{{$t('common.label.close')}}</f7-link>
            </f7-nav-left>
            <f7-nav-title :title="$t('profile.label.alternativeApprover')"/>
            <f7-nav-right>
              <f7-link popup-close="#add-approver" @click="add">{{$t('common.label.confirm')}}</f7-link>
            </f7-nav-right>
        </f7-navbar>
        <f7-list inline-labels no-hairlines-md>
          <li>
            <common-autocomplete
              class="item-link"
              input-class-name="text-align-right"
              :label="$t('profile.label.name')"
              :inline-label="true"
              :page-title="$t('profile.label.alternativeApprover')"
              :text-property="$i18nMsg('userNameCN', 'userNameEN')"
              :text-property-fun="textPropertyFun"
              v-model="addApprover.agentUserId"
              value-property="uuid"
              :close-on-select="true"
              :request-source-on-open="true"
              input-events="search"
              auto-focus
              :autocompleteConfig="selectApproverConfig"/>
          </li>
          <li>
            <common-datepicker
              :label="$t('profile.label.effectDate')"
              :min-date="currentDate"
              name="effective"
              v-model="addApprover.effectDate"
            >
            </common-datepicker>
          </li>
          <li>
            <common-datepicker
              :label="$t('profile.label.expiryDate')"
              :min-date="currentDate"
              name="cut-off"
              v-model="addApprover.expiryDate"
            >
            </common-datepicker>
          </li>
        </f7-list>
      </f7-page>
    </f7-popup>
  </f7-page>
</template>

<script>
import cloneDeep from 'lodash/cloneDeep'
import ProfileService from '../../services/profile/ProfileService'
import { mapGetters } from 'vuex'
import moment from 'moment'

export default {
  name: 'acting-approver',
  data () {
    const selectApproverConfig = {
      keyWordKey: 'filterStr',
      searchMethod: this.searchApprover
    }
    return {
      selectApproverConfig,
      selectedApprover: null,
      currentDate: moment().startOf('d').toDate(),
      nameList: [],
      approverData: {
        userId: '',
        companyCode: '',
        approvers: []
      },
      addApprover: this.newApprovers()
    }
  },
  computed: {
    ...mapGetters(['loginResult', 'currentLoginUser'])
  },
  mounted () {
    let { userId, companyCode } = this.currentLoginUser
    this.approverData.userId = userId
    this.approverData.companyCode = companyCode
    this.loadApprovers()
  },
  methods: {
    newApprovers () {
      let { userId, companyCode } = this.$store.getters.currentLoginUser
      return {
        userId,
        companyCode,
        agentUserId: '',
        effectDate: '',
        expiryDate: ''
      }
    },
    searchApprover (param, config) {
      let argument = {
        '@class': 'com.citsamex.app.spi.data.caller.request.profile.axo.BackupsQueryParam',
        'keyWords': '',
        'pageSetting': {
          '@class': 'com.citsamex.app.spi.data.caller.common.PageSetting',
          'pageNumber': 1,
          'pageSize': 10
        },
        queryDto: {
          'travellerType': 2,
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
    clearAutoApprover () {
      this.$nextTick(() => {
        this.selectedApprover = null
      })
    },
    loadApprovers () {
      let { userId, companyCode } = this.currentLoginUser
      ProfileService.getAgentApprovers({ userId, companyCode }).then(res => {
        console.info(res)
        this.approverData.approvers = res.resultData.approvers || []
        this.nameList = cloneDeep(this.approverData.approvers)
        for (let i of this.nameList) {
          i.expiryDate = this.$date(i.expiryDate, 'YYYY-MM-DD')
          i.effectDate = this.$date(i.effectDate, 'YYYY-MM-DD')
        }
        console.info(this.nameList)
      })
    },
    add () {
      if (this.addApprover.effectDate > this.addApprover.expiryDate) {
        this.$coreError(this.$t('profile.label.endDate'))
        return
      }
      for (let i of this.nameList) {
        if (i.expiryDate >= this.addApprover.effectDate && i.expiryDate <= this.addApprover.expiryDate) {
          this.$coreError(this.$t('profile.label.twoApprovers'))
          return
        }
        if (i.effectDate >= this.addApprover.effectDate && i.effectDate <= this.addApprover.expiryDate) {
          this.$coreError(this.$t('profile.label.twoApprovers'))
          return
        }
      }
      this.save(true)
    },
    deleteSecretary (idx) {
      this.nameList.splice(idx, 1)
      this.save()
    },
    save (add) {
      this.addApprover.agentUserId = this.addApprover.agentUserId.userId
      this.approverData.approvers = cloneDeep(this.nameList)
      add && this.approverData.approvers.push(cloneDeep(this.addApprover))
      console.info(this.approverData.approvers.length)
      ProfileService.updateAgentApprovers(this.approverData).then(res => {
        console.log(res)
        console.log(this.addApprover)
        console.log(this.approverData)
        this.addApprover = this.newApprovers()
        this.loadApprovers()
      })
    }
  }
}
</script>
