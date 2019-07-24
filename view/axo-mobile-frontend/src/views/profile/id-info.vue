<template>
  <f7-page>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"/>
      <f7-nav-title :title="$t('profile.label.certInfo')"/>
      <f7-nav-right>
        <f7-link v-show="ID.length < 4" popup-open="#add-id">{{$t('common.label.add')}}</f7-link>
      </f7-nav-right>
    </f7-navbar>
    <f7-list media-list>
      <f7-list-item
        v-for="(val, index) of ID"
        swipeout
        :key="val.id"
        :after="val.certNo"
        :title="$i18nMsg(val.certNameCN,val.certNameEN)"
      >
        <span slot="text">
          {{val.expiryDate? $t('profile.label.expriyDate') + ': ' + $date(val.expiryDate,'YYYY-MM-DD'):''}}
          <f7-icon v-if="val.expiryDate && checkIdAvailability(val.expiryDate)===0" f7="alert_fill" class="text-color-yellow" size="16"></f7-icon>
          <f7-icon v-if="val.expiryDate && checkIdAvailability(val.expiryDate)===-1" f7="alert_fill" class="text-color-orange" size="16"></f7-icon>
        </span>
        <f7-swipeout-actions right v-show="ID.length !== 1">
          <f7-swipeout-button @click="deleteCert(index)" delete>{{$t('common.label.delete')}}</f7-swipeout-button>
        </f7-swipeout-actions>
      </f7-list-item>
    </f7-list>
    <f7-popup class="demo-popup" id="add-id" @popup:open="openPopup">
      <f7-page>
        <f7-navbar>
          <f7-nav-left>
            <f7-link popup-close="#add-id">{{$t('common.label.close')}}</f7-link>
          </f7-nav-left>
          <f7-nav-title :title="$t('profile.label.addID')"/>
          <f7-nav-right>
            <f7-link popup-close="#add-id" @click="add">{{$t('common.label.confirm')}}</f7-link>
          </f7-nav-right>
        </f7-navbar>
        <f7-list inline-labels no-hairlines-md>
          <f7-list-item v-if="idTypeList.length>0" :title="$t('profile.label.idType')" smart-select :smart-select-params="{openIn: 'sheet'}">
            <label>
              <select name="idType"
                      :value="addInfo.certType"
                      @change="addInfo.certType = $event.target.value"
              >
                <option v-for="(val, index) of idTypeList"
                        :key="index"
                        :value="val.code"
                >
                  {{$i18nMsg(val.nameCN,val.nameEN)}}
                </option>
              </select>
            </label>
          </f7-list-item>
          <f7-list-input
            :label="$t('profile.label.idNumber')"
            type="text"
            clear-button
            :value="addInfo.certNo"
            @input="addInfo.certNo = $event.target.value"
          >
          </f7-list-input>
          <li v-show="addInfo.certType && addInfo.certType !== '5'">
            <common-datepicker
              :label="$t('profile.label.expriyDate')"
              name="expire"
              :min-date="currentDate"
              v-model="addInfo.expiryDate"
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
  data () {
    return {
      ID: [],
      idTypeList: [],
      idExist: [],
      currentDate: moment().startOf('d').toDate(),
      idInfo: {
        userId: '',
        companyCode: '',
        userCerts: []
      },
      addInfo: this.newCert()
    }
  },
  computed: {
    ...mapGetters(['loginResult', 'currentLoginUser'])
  },
  mounted () {
    let { userId, companyCode } = this.currentLoginUser
    this.idInfo.userId = userId
    this.idInfo.companyCode = companyCode
    this.loadCerts()
    this.loadCertTypes()
  },
  methods: {
    openPopup () {
      console.info('open..........', this.addInfo)
      this.addInfo.certType = null
      this.$refreshValue('idTypeList')
    },
    newCert () {
      let { userId, companyCode } = this.$store.getters.currentLoginUser
      return {
        userId,
        companyCode,
        certNo: '',
        certType: '',
        expiryDate: ''
      }
    },
    loadCertTypes () {
      ProfileService.getCertTypes().then(res => {
        console.info(res)
        this.idTypeList = (this.idTypeList).concat(res.resultData.userCertTypes)
        console.info(this.idTypeList)
      })
    },
    loadCerts () {
      let { userId, companyCode } = this.currentLoginUser
      ProfileService.getUserCerts({ userId, companyCode }).then(res => {
        console.info(res)
        this.idInfo.userCerts = res.resultData.userCerts || []
        this.ID = cloneDeep(this.idInfo.userCerts)
        console.info(this.ID)
        console.info(this.currentLoginUser)
      })
    },
    deleteCert (idx) {
      this.ID.splice(idx, 1)
      this.save()
    },
    add () {
      let Otype = this.addInfo.certType
      let Onum = this.addInfo.certNo
      let reg = /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/
      let idNum = /^[0-9a-zA-Z]+$/
      if (Otype === '5') {
        if (!reg.test(Onum)) {
          this.$coreError(this.$t('profile.label.identity'))
          return
        }
      }
      if (Otype === '') {
        this.$coreError(this.$t('profile.label.idSelect'))
        return
      }
      if (!idNum.test(Onum)) {
        this.$coreError(this.$t('profile.label.correctId'))
        return
      }
      for (let i of this.ID) {
        if (i.certType === '5' && Otype === '5') {
          this.$coreError(this.$t('profile.label.oneId'))
          return
        }
        if (i.certNo === Onum) {
          this.$coreError(this.$t('profile.label.numRepeated'))
          return
        }
      }
      this.save(true)
    },
    save (add) {
      this.idInfo.userCerts = cloneDeep(this.ID)
      add && this.idInfo.userCerts.push(cloneDeep(this.addInfo))
      console.info(this.idInfo.userCerts.length)
      ProfileService.updateUserCerts(this.idInfo).then(res => {
        console.log(res)
        console.log(this.addInfo)
        if (res.success) {
          let certType = this.addInfo.certType
          this.addInfo = this.newCert()
          this.addInfo.certType = certType
          this.loadCerts()
        } else {
          this.$coreError(res.message)
        }
      })
    },
    checkIdAvailability (expiryDate) {
      const todayMoment = moment()
      const expiryMoment = moment(expiryDate)
      let difference = expiryMoment.diff(todayMoment, 'months')
      if (difference <= 0) {
        return -1
      } else if (difference > 0 && difference < 6) {
        return 0
      } else {
        return 1
      }
    }
  }
}
</script>
