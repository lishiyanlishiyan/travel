<template>
  <f7-page class="others">
    <f7-navbar :back-link="$t('common.label.back')"
               back-link-url="/book-travellers/others"
               >
      <f7-nav-title>{{$t('common.label.other')}}</f7-nav-title>
      <f7-nav-right>
        <f7-link popup-open="#add-card">{{$t('common.label.add')}}</f7-link>
      </f7-nav-right>
    </f7-navbar>
    <f7-list>
      <f7-list-item
        v-for="(val,index) of otherList"
        :key="val.id"
        swipeout
      >
        <f7-list-item
          readonly
          :title="$t('order.label.productType')"
          :after="$i18nMsg(productTypes[val.typeId-1].nameCN, productTypes[val.typeId-1].nameEN)"
        >
        </f7-list-item>
        <f7-list-item
          readonly
          v-if="otherSearchParam.newOthers.typeId != 5"
          :title="$t('common.label.area')"
          :after="$i18nMsg(val.City.ctryNameCN, val.City.ctryNameEN)"
        >
        </f7-list-item>
        <f7-list-item
          readonly
          v-if="otherList[index].typeId != 5"
          :title="$t('common.label.city')"
          :after="$i18nMsg(val.City.nameCN, val.City.nameEN)"
        >
        </f7-list-item>
        <f7-list-item
          readonly
          v-show="otherList[index].typeId === 2"
          :title="$t('air.label.to')"
          :after="$i18nMsg(val.toCity.nameCN, val.toCity.nameEN)"
        >
        </f7-list-item>
        <f7-list-item
          readonly
          :title="$t('common.label.startDate')"
          :after="val.orderDate"
        >
        </f7-list-item>
        <f7-list-item
          readonly
          :title="$t('common.label.endDate')"
          :after="val.endDate"
        >
        </f7-list-item>
        <f7-list-item
          readonly
          :title="$t('common.label.notionalCost')"
          :after="val.price"
        >
        </f7-list-item>
        <f7-list-item
          readonly
          :title="$t('common.label.remark')"
          :after="val.remark"
        >
        </f7-list-item>
        <f7-swipeout-actions right>
          <f7-swipeout-button delete @click="delOthers(index)">{{$t('common.label.delete')}}</f7-swipeout-button>
        </f7-swipeout-actions>
      </f7-list-item>
    </f7-list>
    <f7-list>
      <f7-button fill large @click="saveOther" v-if="otherList.length >= 1">
        {{$t('common.label.confirm')}}
      </f7-button>
    </f7-list>
    <f7-popup class="demo-popup" id="add-card">
      <f7-page>
        <f7-navbar>
          <f7-nav-left>
            <f7-link popup-close="#add-card">{{$t('common.label.close')}}</f7-link>
          </f7-nav-left>
          <f7-nav-title :title="$t('common.label.other')"/>
          <f7-nav-right>
            <f7-link popup-close="#add-card" @click="addOthers">{{$t('common.label.confirm')}}</f7-link>
          </f7-nav-right>
        </f7-navbar>
        <f7-list inline-labels no-hairlines-md>
          <f7-list-item :title="$t('common.label.type')" smart-select :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
            <label>
              <select name="Types"
                      v-model="otherSearchParam.newOthers.typeId"
                      @change="typeIdChange"
              >
                <option v-for="(productType, index) in productTypes "
                        :key="index"
                        :value="productType.typeId"
                        :selected="true"
                >{{$i18nMsg(productType.nameCN, productType.nameEN)}}
                </option>
              </select>
            </label>
          </f7-list-item>
          <f7-list-item :title="$t('common.label.area')" v-if="otherSearchParam.newOthers.typeId != 5" smart-select :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
            <label>
              <select name="Nation"
                v-model="otherSearchParam.newOthers.deptNation"
                @change="calcHotCities(otherSearchParam.newOthers.deptNation)"
              >
                <option value="CN">{{$t('common.label.inland')}}</option>
                <option value="ES">{{$t('common.label.internation')}}</option>
              </select>
            </label>
          </f7-list-item>
          <li v-if="otherSearchParam.newOthers.typeId != 5">
            <common-autocomplete
              key="City"
              class="item-link"
              v-validate="'required'"
              input-class-name="text-align-right"
              :label="$t('common.label.city')"
              :page-title="$t('common.label.city')"
              :data-vv-name="$t('common.label.city')"
              :inline-label="true"
              :text-property="$i18nMsg('nameCN', 'nameEN')"
              v-model="otherSearchParam.newOthers.City"
              @change="cityChange($event)"
              value-property="code"
              :close-on-select="true"
              :request-source-on-open="true"
              :autocompleteConfig="getAutoConfig(otherSearchParam.newOthers.deptNation)"
              :select-page-data-items="othersHotCities"/>
          </li>
          <li v-if="otherSearchParam.newOthers.typeId === 2">
            <common-autocomplete
              key="City"
              class="item-link"
              v-validate="'required'"
              input-class-name="text-align-right"
              :label="$t('air.label.to')"
              :page-title="$t('air.label.to')"
              :data-vv-name="$t('common.label.city')"
              :inline-label="true"
              :text-property="$i18nMsg('nameCN', 'nameEN')"
              v-model="otherSearchParam.newOthers.toCity"
              @change="cityChange($event)"
              value-property="code"
              :close-on-select="true"
              :request-source-on-open="true"
              :autocompleteConfig="getAutoConfig(otherSearchParam.newOthers.deptNation)"
              :select-page-data-items="othersHotCities"/>
          </li>
          <li>
            <common-datepicker
              v-model="otherSearchParam.newOthers.orderDate"
              :label="$t('common.label.startDate')"
              :min-date="currentDate"
              name="orderDate"
            >
            </common-datepicker>
          </li>
          <li>
            <common-datepicker
              v-model="otherSearchParam.newOthers.endDate"
              :label="$t('common.label.endDate')"
              :min-date="currentDate"
              name="endDate"
            >
            </common-datepicker>
          </li>
          <f7-list-input
            :label="$t('common.label.notionalCost')"
            type="text"
            :value="otherSearchParam.newOthers.price"
            @input="otherSearchParam.newOthers.price=$event.target.value"
          >
          </f7-list-input>
          <f7-list-input
            :label="$t('common.label.remark')"
            type="text"
            :value="otherSearchParam.newOthers.remark"
            @input="otherSearchParam.newOthers.remark=$event.target.value"
          >
          </f7-list-input>
          <f7-list-item
            checkbox
            :title="$t('hotel.label.needCitsgbtService')"
            name="demo-checkbox"
            :disabled="otherSearchParam.newOthers.typeId === 1 || otherSearchParam.newOthers.typeId === 2 || otherSearchParam.newOthers.typeId === 3 || otherSearchParam.newOthers.typeId === 5 || otherSearchParam.newOthers.typeId === 6 || otherSearchParam.newOthers.typeId === 7"
            :checked="otherSearchParam.newOthers.needAmexService === 1"
            @change="needAmexService"
          >
          </f7-list-item>
          <f7-block-title>{{$t('common.label.traveller')}}</f7-block-title>
          <others-traveller-view v-model="travellers"/>
        </f7-list>
      </f7-page>
    </f7-popup>
  </f7-page>
</template>

<script>
import cloneDeep from 'lodash/cloneDeep'
import OtherService from '../../../services/others/OtherService'
import ProductService from '../../../services/products/ProductService'
import MasterDataService from '../../../services/master/MasterDataService'
import OthersTravellerView from '../traveller/others-traveller-view'
import { mapGetters } from 'vuex'
import moment from 'moment'
import orderBy from 'lodash/orderBy'

const bookTravellersKey = 'Order/bookTravellers'

export default {
  components: { OthersTravellerView },
  data () {
    let otherSearchParam = OtherService.getOtherSearchParam()
    return {
      otherSearchParam,
      othersHotCities: [],
      otherList: [],
      productTypes: [],
      travellers: [],
      currentDate: moment().startOf('d').toDate(),
      saveOthers: {
        companyCode: otherSearchParam.companyCode,
        userId: otherSearchParam.userId,
        taNo: otherSearchParam.taNo,
        externalOrderNo: otherSearchParam.externalOrderNo,
        travelSkyOrderNo: otherSearchParam.travelSkyOrderNo,
        policyUserId: otherSearchParam.policyUserId,
        policyApplied: otherSearchParam.policyApplied,
        productItems: [],
        otherPsgrs: []
      }
    }
  },
  computed: {
    ...mapGetters([bookTravellersKey, 'loginResult', 'currentLoginUser'])
  },
  mounted () {
    OtherService.getOtherDefaultSetup(this.otherSearchParam).then(res => {
      console.log(res)
      this.productTypes = res.resultData.defaultSetup.productTypes
    })
    ProductService.loadTravellersDetails(this[bookTravellersKey]).then(travellers => {
      console.info(travellers)
      this.travellers = travellers
      this.travellers.forEach(traveller => {
        // 根据国籍过滤掉不需要的证件，以及过期的证件
        let filteredCerts = traveller.userCerts.filter(cert => {
          if (!cert.expiryDate || Date.parse(cert.expiryDate) >= new Date()) { // 保留没有过期时间的证件（身份证等）和未过期的其他证件
            if ((traveller.nationality === 'CN' && '2,3,5'.indexOf(cert.certType) !== -1) || // 大陆旅客留下身份证，军官证，护照
                ((traveller.nationality === 'HK' || traveller.nationality === 'MO') && cert.certType === '18') || // 香港澳门旅客留下港澳居民来往内地通行证
                (traveller.nationality === 'TW' && cert.certType === '19') || // 台湾旅客留下台湾居民来往大陆通行证
                ('CN,HK,MO,TW'.indexOf(traveller.nationality) === -1)) { // 其他国际保留未过期全部证件
              return cert
            }
          }
        })
        filteredCerts = orderBy(filteredCerts, ['certType'], ['desc'])
        traveller.userCerts = filteredCerts
        if (traveller.travelPreference && traveller.travelPreference.seatPreferenceCode !== '' &&
            traveller.travelPreference.mealPreferenceCode !== '') {
          this.travelPreference = true
        }
      })
    })
    this.calcHotCities(this.otherSearchParam.newOthers.deptNation)
  },
  methods: {
    needAmexService () {
      if (this.otherSearchParam.newOthers.needAmexService === 0) {
        this.otherSearchParam.newOthers.needAmexService = 1
      } else {
        this.otherSearchParam.newOthers.needAmexService = 0
      }
    },
    typeIdChange () {
      if (this.otherSearchParam.newOthers.typeId === 1 || this.otherSearchParam.newOthers.typeId === 2) {
        this.otherSearchParam.newOthers.needAmexService = 0
      } else {
        this.otherSearchParam.newOthers.needAmexService = 1
      }
    },
    calcHotCities (othersType) {
      console.log(othersType)
      let Type
      if (othersType === 'ES') {
        Type = 'I'
      } else if (othersType === 'CN') {
        Type = 'D'
      }
      console.log(Type)
      this.$store.dispatch('Others/storeOthersHotCities', Type).then(() => {
        const othersCitiesKey = 'othersHotCities'
        this[othersCitiesKey] = [{
          label: this.$i18nBundle('common.label.hot'),
          items: this.$store.getters[`Others/othersHotCities`] || []
        }]
      })
    },
    getAutoConfig (type) {
      if (type === 'ES') {
        type = 'I'
      } else if (type === 'CN') {
        type = 'D'
      }
      return {
        keyWordKey: 'keyWords',
        searchMethod: MasterDataService.getAutoSearchCityList(type)
      }
    },
    cityChange ($event) {
      let city = $event
      if (!$event) {
        city = city || {}
      }
      this.otherSearchParam.newOthers.cityCode = city.code
      this.otherSearchParam.newOthers.countryCode = city.ctryCode
      // this.changeAutoHotelNameConfig()
    },
    addOthers () {
      if (this.otherSearchParam.newOthers.typeId === 2) {
        this.otherSearchParam.newOthers.deptCity = this.otherSearchParam.newOthers.City.code + '/' + this.otherSearchParam.newOthers.toCity.code
      } else {
        this.otherSearchParam.newOthers.deptCity = this.otherSearchParam.newOthers.City.code
      }
      if (this.otherSearchParam.newOthers.typeId !== 5) {
        if (this.otherSearchParam.newOthers.City === '' || this.otherSearchParam.newOthers.typeId === '') {
          this.$coreError(this.$t('common.label.requiredInformation'))
          return
        }
      }
      if (this.otherSearchParam.newOthers.typeId === 2) {
        if (this.otherSearchParam.newOthers.City === '' || this.otherSearchParam.newOthers.typeId === '' || this.otherSearchParam.newOthers.toCity === '') {
          this.$coreError(this.$t('common.label.requiredInformation'))
          return
        }
      }
      this.otherList.push(cloneDeep(this.otherSearchParam.newOthers))
      for (let i of this.otherList) {
        i.id = Math.random()
      }
      this.otherSearchParam.newOthers.City = this.otherSearchParam.newOthers.toCity = this.otherSearchParam.newOthers.orderDate = this.otherSearchParam.newOthers.endDate = this.otherSearchParam.newOthers.price = this.otherSearchParam.newOthers.remark = ''
      console.log(this.otherList)
    },
    delOthers (idx) {
      this.otherList.splice(idx, 1)
    },
    travellerToPsgrs () {
      // private Long id;
      // private String companyCode;
      // private String taNo;
      // private Long otherOrderId;
      // private String passengerType;
      // private String passengerId;
      // private String passengerNameCN;
      // private String passengerNameEN;
      // private String passengerEmail;
      // private String blueskyId;
      // // 是否在小订单中被选中 1,未被选中; 0,被选中
      // private Integer selected  = 0;
      return cloneDeep(this.travellers).map(traveller => {
        const { companyCode, passengerType, selected, userNameCN, userNameEN, userEmail } = traveller
        return {
          companyCode,
          passengerType,
          selected: selected ? 1 : 0,
          passengerId: traveller.passengerType === 'U' ? traveller.userId : traveller.tempUserId,
          passengerNameCN: userNameCN,
          passengerNameEN: userNameEN,
          passengerEmail: userEmail,
          taNo: this.otherSearchParam.taNo
        }
      })
    },
    saveOther () {
      this.saveOthers.productItems = cloneDeep(this.otherList)
      for (let i of this.travellerToPsgrs()) {
        if (i.selected) {
          this.saveOthers.otherPsgrs.push(cloneDeep(i))
        }
      }
      // this.saveOthers.otherPsgrs = this.travellerToPsgrs()
      console.log(this.saveOthers)
      OtherService.saveOtherOrder(this.saveOthers).then(res => {
        console.log(res)
        if (res.success) {
          this.$goto(`/order/detail/${res.resultData.taNo}`)
        }
      })
    }
  }
}
</script>
