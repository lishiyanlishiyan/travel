<template>
  <div>
    <f7-list-item link @click="showCreditCardPopup=true">
      <div slot="title">
        {{$t('common.label.creditCard')}}
      </div>
      <div slot="after">
        {{value?this.$cardConfusion(value.cardNo):''}}
      </div>
    </f7-list-item>
    <f7-popup class="traveller-room-popup" :opened="showCreditCardPopup" @popup:closed="showCreditCardPopup = false">
      <f7-page>
        <f7-navbar>
          <f7-nav-left>
            <f7-link popup-close>
              {{$t('common.label.close')}}
            </f7-link>
          </f7-nav-left>
          <f7-nav-title>{{$t('hotel.label.selectCreditCard')}}</f7-nav-title>
          <f7-nav-right>
            <f7-link @click="okCreditCard">{{$t('common.label.confirm')}}</f7-link>
          </f7-nav-right>
        </f7-navbar>
        <f7-list inline-labels form name="creditCardForm">
          <f7-list-item link v-if="selectCreditCardDataItems.length" @click="toSelectCreditCard=true">
            <f7-link slot="title">
              <f7-icon f7="card"/>&nbsp;
              {{$t('hotel.label.selectCreditCardInProfile')}}
            </f7-link>
          </f7-list-item>
          <li v-show="false">
            <common-autocomplete
              class="item-link"
              input-class-name="text-align-right"
              :inline-label="true"
              :page-title="$t('common.label.creditCard')"
              :placeholder="$t('common.label.creditCard')"
              :text-property-fun="textPropertyFun"
              text-property="cardNo"
              v-model="creditCard"
              @change="selectCreditCard"
              :opened="toSelectCreditCard"
              @closed="toSelectCreditCard=false"
              value-property="cardNo"
              :close-on-select="true"
              :request-source-on-open="true"
              :autocompleteConfig="selectCreditCardConfig"
              :autocomplete-data-items="selectCreditCardDataItems">
              <div slot="label">
                <strong>{{$t('hotel.label.selectCreditCard')}}</strong>
              </div>
            </common-autocomplete>
          </li>
          <li v-show="false">
            <input type="hidden"
                   :data-vv-name="$t('profile.label.instituteName')"
                   v-validate="'required'"
                   v-model="creditCard.instituteCode">
          </li>
          <f7-list-item :title="$t('profile.label.instituteName')"
                        v-if="creditCardInstitutes.length && creditCard"
                        key="instituteCode"
                        smart-select
                        :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
            <label>
              <select name="instituteCode"
                      :value="creditCard.instituteCode"
                      @change="creditCard.instituteCode=$event.target.value">
                <option v-for="(val,index) in creditCardInstitutes"
                        :key="index"
                        :value="val.realCode">
                  {{val.nameCN}}
                </option>
              </select>
            </label>
          </f7-list-item>
          <f7-list-input
            v-show="false"
            :label="$t('profile.label.cardNo')"
            :data-vv-name="$t('profile.label.cardNo')"
            v-validate="'required|numeric'"
            type="hidden"
            clear-button
            :value="creditCard.cardNo"
            @input="creditCard.cardNo= $event.target.value"
          />
          <f7-list-input
            :label="$t('profile.label.cardNo')"
            type="text"
            clear-button
            :value="creditCard.cardNoLabel"
            @input="inputCreditCardNo($event)"
          />
          <f7-list-input
            :label="$t('profile.label.cardHolderName')"
            :data-vv-name="$t('profile.label.cardHolderName')"
            v-validate="'required'"
            type="text"
            clear-button
            :value=" creditCard.cardHolderName"
            @input=" creditCard.cardHolderName= $event.target.value"
          />
          <li>
            <common-datepicker
              :data-vv-name="$t('profile.label.expriyDate')"
              v-validate="'required'"
              v-model="creditCard.expriyDate"
              date-format="yyyy-mm"
              :label="$t('profile.label.expriyDate')"
              name="valid"/>
            <!--<common-picker-->
              <!--:data-vv-name="$t('profile.label.expriyDate')"-->
              <!--v-validate="'required'"-->
              <!--v-model="creditCard.expriyDate"-->
              <!--:label="$t('profile.label.expriyDate')"-->
              <!--date-picker-->
              <!--date-format="yyyy-MM"-->
              <!--name="valid"/>-->
          </li>
          <f7-list-input
            v-if="needCvv"
            label="CVV"
            clear-button
            data-vv-name="CVV"
            type="number"
            v-validate="'required|numeric|length:3'"
            :value="creditCard.veryfyCode"
            @input="creditCard.veryfyCode= $event.target.value"/>
          <f7-list-item smart-select
                        v-if="cert && config.userCerts"
                        :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
            <div slot="title">
              <strong>{{$t('hotel.label.selectCert')}}</strong>
            </div>
            <label>
              <select :value="creditCard.certType"
                      @change="changeAttrs(config, 'cert', $event)"
                      name="selectedCert">
                <option :key="index" v-for="(userCert, index) in config.userCerts" :value="userCert.id">
                  {{$i18nMsg(userCert.certNameCN,userCert.certNameEN)}}-{{$i18nMsg(userCert.userNameCN,
                  userCert.userNameEN)}}-{{userCert.certNo}}
                </option>
              </select>
            </label>
          </f7-list-item>
          <li v-show="false" v-if="cert">
            <input type="hidden"
                   :data-vv-name="$t('profile.label.certType')"
                   v-validate="'required'"
                   v-model="config.selectedCert.certType">
          </li>
          <f7-list-item v-if="cert && config.certTypesList.length"
                        :title="$t('profile.label.certType')" smart-select
                        :smart-select-params="{openIn: 'sheet'}">
            <label>
              <select
                @change="config.selectedCert.certType = $event.target.value"
                :value="config.selectedCert.certType">
                <option v-for="(val, index) in config.certTypesList"
                        :key="index"
                        :value="val.code">{{val.nameCN}}
                </option>
              </select>
            </label>
          </f7-list-item>
          <f7-list-input
            v-if="cert"
            :label="$t('profile.label.certNum')"
            :data-vv-name="$t('profile.label.certNum')"
            v-validate="'required'"
            clear-button
            :value="config.selectedCert.certNo"
            @input="config.selectedCert.certNo= $event.target.value"/>
          <li>
            <common-form-errors :value="errors.all()" v-if="errors"/>
          </li>
        </f7-list>
      </f7-page>
    </f7-popup>
  </div>
</template>

<script>

import cloneDeep from 'lodash/cloneDeep'
import ProductService from '../../../services/products/ProductService'
import ProfileService from '../../../services/profile/ProfileService'
import HotelService from '../../../services/hotel/HotelService'

export default {
  name: 'traveller-credit-card',
  model: {
    event: 'change'
  },
  props: {
    value: { type: Object },
    travellers: { type: Array },
    cert: {
      type: Boolean,
      default: false
    }
  },
  data () {
    const { value } = this.$props
    const initCreditCard = {
      cardNo: '',
      cardHolderName: '',
      validYear: '',
      validMonth: '',
      expriyDate: '',
      instituteCode: '',
      veryfyCode: ''
    }
    return {
      config: {
        selectedCert: {
          certType: '',
          certNo: ''
        },
        userCerts: [],
        certTypesList: []
      },
      creditCard: value ? cloneDeep(value) : initCreditCard,
      showCreditCardPopup: false,
      selectCreditCardConfig: {
        keyWordKey: 'keyWords'
      },
      selectCreditCardDataItems: [],
      creditCardInstitutes: [],
      needCvv: false,
      toSelectCreditCard: false
    }
  },
  mounted () {
    // 机构
    ProfileService.getCreditCardInstitutes(undefined, { loading: false }).then(data => {
      this.creditCardInstitutes = data.resultData.creditCardInstitutes
    })
    const { cert } = this.$props
    if (cert) {
      ProfileService.getCertTypes().then(data => {
        this.config.certTypesList = data.resultData.userCertTypes
      })
    }
    this.$initFormValidate('creditCardForm')
    this.calcTravellerAttrs(this.travellers)
  },
  watch: {
    value: function (value) {
      if (value !== this.creditCard) {
        this.creditCard = value ? cloneDeep(value) : null
      }
    },
    travellers: function (travellers) {
      this.calcTravellerAttrs(this.travellers)
    }
  },
  methods: {
    changeAttrs () {
      ProductService.changeTravellerAttrs.apply(this, arguments)
      this.$refreshValue('config.certTypesList', [])
    },
    selectCreditCard () {
      if (this.creditCard) {
        this.creditCard.cardNoLabel = this.$cardConfusion(this.creditCard.cardNo)
      }
      this.$refreshValue('creditCardInstitutes', [])
    },
    inputCreditCardNo ($event) {
      if (this.creditCard) {
        let value = $event.target.value
        if (value && value.indexOf('*') > -1) {
          value = ''
        }
        this.creditCard.cardNoLabel = value
        this.creditCard.cardNo = value
      }
    },
    calcTravellerAttrs (travellers) {
      const userId = ProductService.getUserIdsStr(travellers || [])
      if (userId) {
        ProfileService.getCreditCards({
          userId,
          companyCode: travellers[0].companyCode
        }, { loading: false }).then(data => {
          this.selectCreditCardDataItems = data.resultData.creditCards || []
        })
      }
      const { cert } = this.$props
      if (cert) {
        let userCerts = []
        travellers.forEach(traveler => {
          userCerts = userCerts.concat(traveler.userCerts || [])
        })
        this.config.userCerts = userCerts
        this.config.selectedCert = {}
      }
    },
    textPropertyFun (item) {
      if (item.data) {
        const data = item.data
        return `
          <div class="item-title-row">
            <div class="item-title">
            ${this.$cardConfusion(data.cardNo)}
            </div>
            <div class="item-after">
            ${data.cardHolderName}
            </div>
          </div>
          <div class="item-subtitle">
          ${this.$i18nMsg(data.bankNameCN, data.bankNameEN)}
            <div class="chip color-blue">
              <div class="chip-label">${this.$i18nMsg(data.instituteNameCN, data.instituteNameEN)}</div>
            </div>
          </div>
        `
      } else {
        return `<div class="item-title">${item.text}</div>`
      }
    },
    okCreditCard () {
      this.$formValidate().then(() => {
        const { cert } = this.$props
        if (cert) {
          HotelService.elongValidateCreditCard({
            creditCardDto: {
              cardNo: this.creditCard.cardNo
            }
          }).then(data => {
            if (data && data.resultData) {
              this.needCvv = data.resultData.needVerifyCode
              if (!data.resultData.valid) {
                this.$coreError(this.$t('hotel.msg.creditCardInvalid'))
              } else if (data.resultData.needVerifyCode && !this.creditCard.veryfyCode) {
                this.$coreAlert(this.$t('hotel.msg.cvvNeeded'))
              } else {
                this.doOkCreditCard()
              }
            }
          })
        } else {
          this.doOkCreditCard()
        }
      })
    },
    doOkCreditCard () {
      this.$emit('change', this.creditCard)
      this.showCreditCardPopup = false
    }
  }
}
</script>

<style scoped>

</style>
