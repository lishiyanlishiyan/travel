<template>
  <f7-page>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"/>
      <f7-nav-title :title="$t('profile.label.creditCard')"/>
      <f7-nav-right>
        <f7-link v-show="cardEditable" @click="addi" popup-open="#add-card">{{$t('common.label.add')}}</f7-link>
      </f7-nav-right>
    </f7-navbar>
    <f7-list media-list>
      <f7-list-item
        v-for="(val,index) of list"
        :key="val.id"
        swipeout
        @swipeout:deleted="deleteCard(index)"
        :title="$i18nMsg(val.bankNameCN, val.bankNameEN)"
        :after="$cardConfusion(val.cardNo)"
      >
        <span slot="text">
          {{val.expriyDate? $t('profile.label.expriyDate') + ': ' + $date(val.expriyDate,'YYYY-MM'):''}}
          <f7-icon v-if="val.expriyDate && checkIdAvailability(val.expriyDate)===0" f7="alert_fill" class="text-color-yellow" size="16"></f7-icon>
          <f7-icon v-if="val.expriyDate && checkIdAvailability(val.expriyDate)===-1" f7="alert_fill" class="text-color-orange" size="16"></f7-icon>
        </span>
        <f7-swipeout-actions right>
          <f7-swipeout-button @click="detail(index)">{{$t('profile.label.detail')}}</f7-swipeout-button>
          <f7-swipeout-button v-show="cardEditable" delete :confirm-text="val.cpDefault?$t('profile.label.deleteDefault'):''">{{$t('common.label.delete')}}</f7-swipeout-button>
        </f7-swipeout-actions>
      </f7-list-item>
    </f7-list>
    <f7-popup class="demo-popup" :opened="showEditPopup" @popup:closed="showEditPopup=false" id="add-card" @popup:open="openPopup">
      <f7-page>
        <f7-navbar>
          <f7-nav-left>
            <f7-link popup-close="#add-card">{{$t('common.label.close')}}</f7-link>
          </f7-nav-left>
          <f7-nav-title :title="$t('profile.label.creditCard')"/>
          <f7-nav-right v-if="addition">
            <f7-link v-if="checked" popup-close="#add-card" @click="add">{{$t('common.label.confirm')}}</f7-link>
          </f7-nav-right>
        </f7-navbar>
        <f7-list inline-labels no-hairlines-md>
          <f7-list-input
            :label="$t('profile.label.cardHolderName')"
            type="text"
            readonly
            :value="updateList.cardHolderName"
          >
          </f7-list-input>
          <f7-list-input
            v-show="addition"
            :label="$t('profile.label.cardHolderMobile')"
            type="text"
            clear-button
            :value="updateList.cardHolderMobile"
            @input="updateList.cardHolderMobile= $event.target.value"
          >
          </f7-list-input>
          <f7-list-input
            v-show="look"
            :label="$t('profile.label.cardHolderMobile')"
            type="text"
            readonly
            :value="updateList.cardHolderMobile"
          >
          </f7-list-input>
          <f7-list-item :title="$t('profile.label.certType')" v-if="updateList.certType" v-show="addition" smart-select :smart-select-params="{openIn: 'sheet'}">
            <label>
              <select name="TypeId"
                      v-model="updateList.certType"
                      @change="certTypes"
              >
                <option v-for="(val, index) in CertTypesList "
                        :key="index"
                        :value="val.code"
                        :selected="true"
                >{{$i18nMsg(val.nameCN, val.nameEN)}}
                </option>
              </select>
            </label>
          </f7-list-item>
          <f7-list-input
            v-show="look"
            :label="$t('profile.label.certType')"
            type="text"
            readonly
            :value="$i18nMsg(detailC.certNameCN, detailC.certNameEN)"
          >
          </f7-list-input>
          <f7-list-input
            v-show="addition"
            :label="$t('profile.label.certNum')"
            type="tel"
            clear-button
            :value="updateList.certNum"
            @input="updateList.certNum=$event.target.value"
          >
          </f7-list-input>
          <f7-list-input
            v-show="look"
            :label="$t('profile.label.certNum')"
            type="tel"
            readonly
            :value="detailC.certNum"
          >
          </f7-list-input>
          <f7-list-input
            v-show="addition"
            :label="$t('profile.label.cardNo')"
            type="text"
            clear-button
            maxlength="16"
            :value="updateList.cardNo"
            @input="getCardBin($event)"
          >
          </f7-list-input>
          <f7-list-input
            v-show="look"
            :label="$t('profile.label.cardNo')"
            type="text"
            readonly
            :value="detailC.cardNo"
          >
          </f7-list-input>
          <li v-show="addition">
            <common-datepicker
              v-model="updateList.expriyDate"
              :label="$t('profile.label.expriyDate')"
              :min-date="currentDate"
              name="valid"
            >
            </common-datepicker>
          </li>
          <f7-list-input
            v-show="look"
            :label="$t('profile.label.expriyDate')"
            type="text"
            readonly
            :value="detailC.expriyDate|date('YYYY-MM')"
          >
          </f7-list-input>
          <f7-list-item v-if="CreditCardBanksList.length>0" ref="updateList.bankCode" :title="$t('profile.label.bankName')" v-show="addition" smart-select :smart-select-params="{openIn: 'sheet'}">
            <label>
              <select name="bank"
                      :value="updateList.bankCode"
                      @change="updateList.bankCode=$event.target.value"
              >
                <option v-for="(val, index) in CreditCardBanksList "
                        :key="index"
                        :value="val.code"
                        :selected="val.nameCN"
                >{{$i18nMsg(val.nameCN, val.nameEN)}}
                </option>
              </select>
            </label>
          </f7-list-item>
          <f7-list-input
            v-show="look"
            :label="$t('profile.label.bankName')"
            type="text"
            readonly
            :value="$i18nMsg(detailC.bankNameCN, detailC.bankNameEN)"
          >
          </f7-list-input>
          <f7-list-item v-if="CreditCardInstitutesList.length>0" ref="updateList.instituteCode" :title="$t('profile.label.instituteName')" v-show="addition" smart-select :smart-select-params="{openIn: 'sheet'}">
            <label>
              <select name="issuer"
                      :value="updateList.instituteCode"
                      @change="updateList.instituteCode=$event.target.value"
              >
                <option v-for="(val, index) in CreditCardInstitutesList "
                        :key="index"
                        :value="val.code"
                        :selected="val.nameCN"
                >{{$i18nMsg(val.nameCN, val.nameEN)}}
                </option>
              </select>
            </label>
          </f7-list-item>
          <f7-list-input
            v-show="look"
            :label="$t('profile.label.instituteName')"
            type="text"
            readonly
            :value="$i18nMsg(detailC.instituteNameCN, detailC.instituteNameEN)"
          >
          </f7-list-input>
          <f7-list-item accordion-item :title="$t('profile.label.Clause')">
            <f7-accordion-content>
              <f7-block id="block">
              </f7-block>
            </f7-accordion-content>
          </f7-list-item>
          <f7-list-item
            v-show="addition"
            checkbox
            :checked="updateList.sofFlag === 1"
            :title="$t('profile.label.withoutCard')"
            name="demo-checkbox"
            @change="sof"
          >
          </f7-list-item>
          <f7-list-item
            v-show="look"
            id="withoutCard"
            checkbox
            disabled
            :checked="detailC.sofFlag === 1"
            :title="$t('profile.label.withoutCard')"
            name="demo-checkbox"
          >
          </f7-list-item>
          <f7-list-item
            v-show="addition"
            checkbox
            :title="$i18nMsg(companyConfigExpand.cardAgreeMsgCn, companyConfigExpand.cardAgreeMsgEn)"
            name="demo-checkbox"
            :checked="checked"
            @change="check"
          >
          </f7-list-item>
          <f7-list-item
            v-show="look"
            checkbox
            disabled
            :title="$i18nMsg(companyConfigExpand.cardAgreeMsgCn, companyConfigExpand.cardAgreeMsgEn)"
            name="demo-checkbox"
            checked
          >
          </f7-list-item>
          <f7-list-item
            v-show="addition"
            checkbox
            :title="$t('profile.label.defaultCard')"
            name="demo-checkbox"
            :checked="updateList.cpDefault === 1"
            @change="cpDefault"
          >
          </f7-list-item>
          <f7-list-item
            v-show="look"
            id="defaultCard"
            :disabled="detailC.sofFlag !== 1 || checkDefaultDisable()"
            checkbox
            @change="setDefault"
            :checked="detailC.cpDefault === 1"
            :title="$t('profile.label.defaultCard')"
            name="demo-checkbox"
          >
          </f7-list-item>
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
      CreditCardBanksList: [],
      CreditCardInstitutesList: [],
      CertTypesList: [],
      basicInfo: {},
      Certs: [],
      list: [],
      detailC: {},
      companyConfigExpand: {},
      cardPrefix: [],
      currentDate: moment().startOf('d').toDate(),
      addition: true,
      look: true,
      checked: false,
      cardEditable: null,
      forceCorporate: null,
      creditCardsParams: {
        userId: '',
        companyCode: '',
        creditCards: []
      },
      showEditPopup: false,
      updateList: this.newCard()
    }
  },
  computed: {
    ...mapGetters(['loginResult', 'currentLoginUser'])
  },
  created () {
  },
  mounted () {
    let { userId, companyCode, userBasic, barNo } = this.currentLoginUser
    this.creditCardsParams.userId = userId
    this.creditCardsParams.companyCode = companyCode
    ProfileService.getBarCreditCardConfig({ companyCode, barno: barNo }).then(res => {
      console.log(res)
      this.cardEditable = res.resultData.cardEditable
      this.forceCorporate = res.resultData.forceCorporate
      this.cardPrefix = res.resultData.cardPrefix
    })
    ProfileService.getSofTextConfig({ companyCode }).then(res => {
      console.log(res)
      let companyConfigExpand = res.resultData.companyConfigExpand
      var block = document.getElementById('block')
      block.innerHTML = this.$i18nMsg(res.resultData.companyConfigExpand.cardInstructionCn, res.resultData.companyConfigExpand.cardInstructionEn)
      this.companyConfigExpand = companyConfigExpand
    })
    ProfileService.getCreditCardBanks().then(res => {
      console.log(res)
      this.CreditCardBanksList = res.resultData.creditCardBanks
      let ob = { code: '', nameCN: '请选择发卡银行', nameEN: 'Please select the issuing bank' }
      this.CreditCardBanksList[this.CreditCardBanksList.length] = ob
    })
    // 机构
    ProfileService.getCreditCardInstitutes().then(res => {
      console.log(res)
      this.CreditCardInstitutesList = res.resultData.creditCardInstitutes
      let ob = { code: '', nameCN: '请选择发卡机构', nameEN: 'Please select the card issuer' }
      this.CreditCardInstitutesList[this.CreditCardInstitutesList.length] = ob
      this.loadCard()
    })
    // 类型
    ProfileService.getCertTypes().then(res => {
      console.log(res)
      this.CertTypesList = res.resultData.userCertTypes
    })
    ProfileService.getUserBasicInfo({ userId, userBasic }).then(res => {
      console.info(res)
      this.basicInfo = res.resultData.userBasic
      this.updateList.cardHolderName = res.resultData.userBasic.nameEN
      this.updateList.cardHolderMobile = res.resultData.userBasic.mobile
      this.Certs = res.resultData.userBasic.userCerts || []
      console.info(this.Certs)
      this.types()
    })
  },
  methods: {
    loadCard () {
      let { userId, companyCode } = this.currentLoginUser
      let insObj = {}
      for (let ins of this.CreditCardInstitutesList) {
        insObj[ins.realCode] = ins
      }
      ProfileService.getCreditCards({ companyCode, userId }).then(res => {
        for (let i of res.resultData.creditCards) {
          i.companyCode = companyCode
          let ins = insObj[i.instituteCode]
          i.instituteCode = ins ? ins.code : i.instituteCode
        }
        this.creditCardsParams.creditCards = res.resultData.creditCards
        this.list = cloneDeep(this.creditCardsParams.creditCards)
        console.log(this.list)
      })
      console.log(this.creditCardsParams)
    },
    openPopup () {
      this.updateList.bankCode = this.updateList.instituteCode = null
      this.$refreshValue('CreditCardBanksList')
      this.$refreshValue('CreditCardInstitutesList')
    },
    newCard () {
      let { userId, companyCode } = this.$store.getters.currentLoginUser
      return {
        userId,
        companyCode,
        bankCode: '',
        cardHolderMobile: '',
        cardHolderName: '',
        cardNo: '',
        cardNoShow: '',
        cardType: '',
        cardTypeName: '',
        certType: '',
        certNum: '',
        cpDefault: 0,
        expriyDate: '',
        instituteCode: '',
        isAdd: true,
        isConfirmed: 1,
        isSofEnable: true,
        selectedBankDisable: false,
        selectedInsituteDisable: false,
        sofFlag: 0
      }
    },
    detail (idx) {
      this.showEditPopup = true
      console.log(this.list[idx])
      this.detailC = this.list[idx]
      for (let i of this.CertTypesList) {
        if (i.code === this.detailC.certType) {
          this.detailC.certNameCN = i.nameCN
          this.detailC.certNameEN = i.nameEN
        }
      }
      this.look = true
      this.addition = false
    },
    checkDefaultDisable () {
      let barNum = this.detailC.cardNo.substring(0, 6)
      return this.forceCorporate && this.cardPrefix.indexOf(barNum) === -1
    },
    setDefault ($event) {
      console.log($event)
      let barNum = this.detailC.cardNo.substring(0, 6)
      let listNum = []
      let cloneNum
      for (let i of this.list) {
        cloneNum = i.cardNo
        cloneNum = cloneNum.substring(0, 6)
        listNum.push(cloneNum)
      }
      if (this.forceCorporate) {
        if (this.detailC.cpDefault === 0 && this.cardPrefix.indexOf(barNum) !== -1) {
          for (let i of this.list) {
            if (i.cpDefault === 1) {
              i.cpDefault = 0
            }
          }
        }
      } else if (this.cardPrefix.indexOf(barNum) !== -1) {
        if (this.detailC.cpDefault === 0) {
          for (let i of this.list) {
            if (i.cpDefault === 1) {
              i.cpDefault = 0
            }
          }
        }
      } else if (this.cardPrefix.indexOf(barNum) === -1) {
        for (let i in listNum) {
          if (this.cardPrefix.indexOf(listNum[i]) !== -1) {
            $event.target.checked = false
            this.$coreError(this.$t('profile.label.regulations'))
            return
          } else {
            if (this.detailC.cpDefault === 0) {
              for (let i of this.list) {
                if (i.cpDefault === 1) {
                  i.cpDefault = 0
                }
              }
            }
          }
        }
      }
      this.detailC.cpDefault = $event.target.checked ? 1 : 0
      this.creditCardsParams.creditCards = cloneDeep(this.list)
      ProfileService.updateCreditCards(this.creditCardsParams).then(res => {
        if (res.success) {
          this.loadCard()
        } else {
          this.detailC.cpDefault = 0
          this.$coreError(res.message)
        }
      })
    },
    addi () {
      this.look = false
      this.addition = true
    },
    check () {
      this.checked = !this.checked
    },
    sof () {
      if (this.updateList.sofFlag === 0) {
        this.updateList.sofFlag = 1
      } else {
        this.updateList.sofFlag = 0
      }
    },
    cpDefault () {
      if (this.updateList.cpDefault === 0) {
        this.updateList.cpDefault = 1
      } else {
        this.updateList.cpDefault = 0
      }
    },
    types () {
      this.updateList.certType = this.Certs[0].certType
      this.updateList.certNum = this.Certs[0].certNo
    },
    certTypes () {
      for (let i of this.Certs) {
        if (this.updateList.certType === i.certType) {
          this.updateList.certNum = i.certNo
        }
      }
    },
    getCardBin (event) {
      this.updateList.cardNo = event.target.value
      let keyWords = { 'keyWords': this.updateList.cardNo }
      let barNum = this.updateList.cardNo.substring(0, 6)
      if (this.updateList.cardNo.length >= 13) {
        ProfileService.getCardBinInfo(keyWords).then(res => {
          console.log(res)
          this.updateList.cardType = res.resultData.cardBinMsg.cardType
          this.updateList.cardTypeName = res.resultData.cardBinMsg.cardTypeName
          res.resultData.cardBinMsg.empty = ''
          if (res.resultData.cardBinMsg.vendor !== 'Others') {
            this.$setSmartSelectValue('updateList.bankCode', res.resultData.cardBinMsg.vendor)
          } else {
            this.$setSmartSelectValue('updateList.bankCode', res.resultData.cardBinMsg.empty)
          }
          if (res.resultData.cardBinMsg.institutionCode !== 'Others') {
            this.$setSmartSelectValue('updateList.instituteCode', res.resultData.cardBinMsg.institutionCode)
          } else {
            this.$setSmartSelectValue('updateList.instituteCode', res.resultData.cardBinMsg.empty)
          }
          if (this.cardPrefix.indexOf(barNum) !== -1) {
            if (this.updateList.sofFlag === 0) {
              this.updateList.sofFlag = 1
            }
          }
        })
      }
    },
    deleteCard (card) {
      this.list.splice(card, 1)
      this.save()
    },
    add () {
      let cardNum = /^\d{13,16}$/
      let barNum = this.updateList.cardNo.substring(0, 6)
      let listNum = []
      let cloneNum
      for (let i of this.list) {
        cloneNum = i.cardNo
        cloneNum = cloneNum.substring(0, 6)
        listNum.push(cloneNum)
      }
      if (!cardNum.test(this.updateList.cardNo)) {
        this.$coreError(this.$t('profile.label.CreditCardNumber'))
        return
      }
      if (this.updateList.sofFlag !== 1 && this.updateList.cpDefault === 1) {
        this.$coreError(this.$t('profile.label.defaultPayment'))
        return
      }
      if (this.updateList.cpDefault === 1) {
        if (this.forceCorporate) {
          if (this.cardPrefix.indexOf(barNum) === -1) {
            this.$coreError(this.$t('profile.label.regulations'))
            return
          } else if (this.cardPrefix.indexOf(barNum) !== -1) {
            for (let i of this.list) {
              if (i.cpDefault === 1) {
                i.cpDefault = 0
              }
            }
          }
        } else if (this.cardPrefix.indexOf(barNum) === -1) {
          for (let i in listNum) {
            if (this.cardPrefix.indexOf(listNum[i]) !== -1) {
              this.$coreError(this.$t('profile.label.regulations'))
              return
            }
          }
          for (let i of this.list) {
            if (i.cpDefault === 1) {
              i.cpDefault = 0
            }
          }
        } else {
          for (let i of this.list) {
            if (i.cpDefault === 1) {
              i.cpDefault = 0
            }
          }
        }
      } else if (this.cardPrefix.indexOf(barNum) !== -1) {
        for (let i in listNum) {
          if (this.cardPrefix.indexOf(listNum[i]) !== -1) {
            this.updateList.cpDefault = 0
            this.updateList.cardNoShow = this.$cardConfusion(this.updateList.cardNo)
            this.save(true)
            return
          } else {
            this.list[i].cpDefault = 0
            this.updateList.cpDefault = 1
          }
        }
      }
      this.updateList.cardNoShow = this.$cardConfusion(this.updateList.cardNo)
      this.save(true)
    },
    save (add) {
      this.creditCardsParams.creditCards = cloneDeep(this.list)
      add && this.creditCardsParams.creditCards.push(cloneDeep(this.updateList))
      console.log(this.creditCardsParams)
      ProfileService.updateCreditCards(this.creditCardsParams).then(res => {
        console.log(res)
        console.log(this.updateList)
        if (res.success) {
          this.updateList.cardNo = this.updateList.expriyDate = ''
          this.updateList.cpDefault = this.updateList.sofFlag = 0
          this.checked = false
          this.loadCard()
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
