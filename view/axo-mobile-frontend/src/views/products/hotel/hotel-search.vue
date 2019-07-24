<template>
  <f7-page class="hotel">
    <f7-navbar :back-link="$t('common.label.back')"
               back-link-url="/book-travellers/hotel"
               back-link-force>
      <f7-nav-title>
        <span v-if="hotelTypes.length>1">
          {{$t('common.label.hotel')}}
        </span>
        <span v-if="hotelTypes.length===1">
          {{hotelTypes[0].label}}
        </span>
      </f7-nav-title>
      <common-home/>
      <f7-subnavbar v-if="hotelTypes.length>1">
        <f7-segmented raised>
          <f7-button :key="hotelType.id" @click="searchParam.hotelType=hotelType.id"
                     :active="searchParam.hotelType===hotelType.id" v-for="hotelType in hotelTypes">
            {{hotelType.label}}
          </f7-button>
        </f7-segmented>
      </f7-subnavbar>
    </f7-navbar>
    <f7-list form>
      <li v-if="searchParam.hotelType==='D'">
        <common-autocomplete
          key="domCity"
          v-validate="'required'"
          :data-vv-name="$t('hotel.label.city')"
          :label="$t('hotel.label.city')"
          :inline-label="true"
          :page-title="$t('hotel.label.city')"
          :placeholder="$t('hotel.label.city')"
          v-model="searchParam.domCity"
          @change="cityChange($event)"
          :text-property="$i18nMsg('nameCN', 'nameEN')"
          value-property="code"
          :close-on-select="true"
          input-class-name="text-align-right"
          class="item-link"
          :autocompleteConfig="getHotelAutoConfig('D')"
          :select-page-data-items="domHotelHotCities"/>
      </li>
      <li v-if="searchParam.hotelType==='I'">
        <common-autocomplete
          key="intlCity"
          v-validate="'required'"
          :data-vv-name="$t('hotel.label.city')"
          :label="$t('hotel.label.city')"
          :inline-label="true"
          :page-title="$t('hotel.label.city')"
          :placeholder="$t('hotel.label.city')"
          v-model="searchParam.intlCity"
          @change="cityChange($event)"
          :text-property="$i18nMsg('nameCN', 'nameEN')"
          value-property="code"
          :close-on-select="true"
          input-class-name="text-align-right"
          class="item-link"
          :autocompleteConfig="getHotelAutoConfig('I')"
          :select-page-data-items="intlHotelHotCities"/>
      </li>
      <li>
        <common-datepicker
          :label="$t('hotel.label.checkInDate')"
          :inline-label="true"
          :min-date="hotelCheckinStartDate"
          v-validate="'required'"
          :data-vv-name="$t('hotel.label.checkInDate')"
          :placeholder="$t('hotel.label.checkInDate')"
          name="checkInDate"
          input-class-name="text-align-right"
          class="item-link"
          @change="changeAutoHotelNameConfig"
          v-model="searchParam.hotelQueryParam.checkinDate"/>
      </li>
      <li>
        <common-datepicker
          :label="$t('hotel.label.checkOutDate')"
          :inline-label="true"
          :min-date="searchParam.hotelQueryParam.checkinDate"
          v-validate="'required'"
          :data-vv-name="$t('hotel.label.checkOutDate')"
          :placeholder="$t('hotel.label.checkOutDate')"
          name="checkOutDate"
          input-class-name="text-align-right"
          :default-min-date-start="1"
          class="item-link"
          @change="changeAutoHotelNameConfig"
          v-model="searchParam.hotelQueryParam.checkoutDate"/>
      </li>
      <f7-list-item :title="$t('hotel.label.roomCount')">
        <f7-stepper :value="searchParam.hotelQueryParam.roomCount"
                    :min="1" :max="config.roomCountMax"
                    @input="searchParam.hotelQueryParam.roomCount=parseInt($event.target.value)"/>
      </f7-list-item>
      <f7-list-item :title="$t('hotel.label.guestCount')">
        <f7-stepper :value="searchParam.hotelQueryParam.guestCount"
                    :min="1" :max="3"
                    @input="searchParam.hotelQueryParam.guestCount=parseInt($event.target.value)"/>
      </f7-list-item>
      <li v-if="config.city">
        <hotel-name-filter v-model="searchParam"/>
      </li>
      <li v-if="errors">
        <common-form-errors :value="errors.all()"/>
      </li>
      <f7-block>
        <f7-row>
          <f7-col>
            <f7-button fill large :disabled="!formValidator.isFormValid" @click="doSearchHotel" type="submit">
              {{$t(config.searchOnlyFlag?'common.label.searchOnly':'common.label.search')}}
            </f7-button>
          </f7-col>
        </f7-row>
      </f7-block>
    </f7-list>
  </f7-page>
</template>

<script>
import moment from 'moment'
import cloneDeep from 'lodash/cloneDeep'
import merge from 'lodash/merge'
import find from 'lodash/find'
import { ProductType } from '../../../consts/OrderConsts'
import HotelService from '../../../services/hotel/HotelService'
import MasterDataService from '../../../services/master/MasterDataService'
import PolicyControlService from '../../../services/policy/PolicyControlService'
import HotelAutoService from '../../../services/hotel/HotelAutoService'
import HotelNameFilter from './hotel-name-filter'
import ProductService from '../../../services/products/ProductService'

const searchParamsKey = 'Hotel/hotelSearchParam'
const bookTravellersKey = 'Order/bookTravellers'

export default {
  name: 'hotel',
  components: { HotelNameFilter },
  data () {
    const todayDate = moment().startOf('d')
    const hotelDefaultSetup = this.$defaultSetup(ProductType.Hotel)
    let hotelTypes = HotelService.getHotelTypes(hotelDefaultSetup.searchRegion)
    const searchParam = HotelService.getHotelSearchParam()
    const savedSearchParam = cloneDeep(this.$store.getters[searchParamsKey]) || {}
    merge(searchParam, this.$copyAttrsByKeys({}, savedSearchParam, ['domCity', 'intlCity']))
    merge(searchParam.hotelQueryParam,
      this.$copyAttrsByKeys({}, savedSearchParam.hotelQueryParam || {}, ['checkinDate', 'checkoutDate']))
    if (!searchParam.hotelType || !find(hotelTypes, { id: searchParam.hotelType })) {
      searchParam.hotelType = hotelTypes[0].id
    }
    const hotelCheckinStartDate = hotelDefaultSetup.oldDateRequestEnable ? null : todayDate.toDate()
    this.checkHotelDate(searchParam.hotelQueryParam, hotelDefaultSetup, todayDate)
    const roomCountMax = (this.$store.getters[bookTravellersKey] || []).length || 1
    ProductService.processExternalOrderAutoInput(this, searchParam, ProductType.Hotel)
    return {
      formValidator: {},
      hotelDefaultSetup,
      hotelTypes,
      searchParam,
      hotelCheckinStartDate,
      domHotelHotCities: [],
      intlHotelHotCities: [],
      config: {
        city: null,
        autoHotelName: null,
        autoHotelNameConfig: this.getAutoHotelNameConfig(searchParam),
        showAutoHotelName: false,
        roomCountMax,
        searchOnlyFlag: this.$isSearchOnly()
      }
    }
  },
  watch: {
    'searchParam.hotelType': function (hotelType) {
      this.cityChange(hotelType === 'D' ? this.searchParam.domCity : this.searchParam.intlCity)
      this.$formValidate()
    }
  },
  mounted () {
    this.hotelTypes.forEach(hotelType => {
      this.calcHotCities(hotelType.id)
    })
    this.$initFormValidate()
    this.cityChange()
  },
  methods: {
    checkHotelDate (hotelQueryParam, hotelDefaultSetup, todayDate) {
      if (!hotelDefaultSetup.oldDateRequestEnable) {
        if (hotelQueryParam.checkinDate && moment(hotelQueryParam.checkinDate).isBefore(todayDate)) {
          hotelQueryParam.checkinDate = null
        }
        if (hotelQueryParam.checkoutDate && moment(hotelQueryParam.checkoutDate).isBefore(todayDate)) {
          hotelQueryParam.checkoutDate = null
        }
      }
    },
    getHotelAutoConfig (hotelType) {
      return {
        keyWordKey: 'keyWords',
        searchMethod: MasterDataService.getAutoSearchCityList(hotelType)
      }
    },
    getAutoHotelNameConfig (searchParam) {
      if (searchParam.hotelQueryParam.cityCode && searchParam.hotelQueryParam.checkinDate && searchParam.hotelQueryParam.checkoutDate) {
        return {
          searchMethod: HotelAutoService.getAutoHotelList(searchParam)
        }
      }
    },
    calcHotCities (hotelType) {
      this.$store.dispatch('Hotel/storeHotelHotCities', hotelType).then(() => {
        const hotelCitiesKey = hotelType === 'D' ? 'domHotelHotCities' : 'intlHotelHotCities'
        this[hotelCitiesKey] = [{
          label: this.$i18nBundle('common.label.hot'),
          items: this.$store.getters[`Hotel/${hotelCitiesKey}`] || []
        }]
      })
    },
    cityChange ($event) {
      let city = $event
      if (!$event) {
        city = this.searchParam.hotelType === 'D' ? this.searchParam.domCity : this.searchParam.intlCity
        city = city || {}
      }
      this.searchParam.hotelQueryParam.cityCode = city.code
      this.searchParam.hotelQueryParam.countryCode = city.ctryCode
      this.config.city = city
      this.changeAutoHotelNameConfig()
    },
    changeAutoHotelNameConfig () {
      this.config.autoHotelNameConfig = this.getAutoHotelNameConfig(this.searchParam)
    },
    doSearchHotel ($event) {
      const searchParam = cloneDeep(this.searchParam)
      searchParam.today = moment().isSame(this.searchParam.hotelQueryParam.checkinDate, 'day')
      if (this.config.searchOnlyFlag) {
        this.internalSearchHotel(searchParam)
      } else {
        PolicyControlService.doPolicyControl(HotelService.checkHotelSearchPolicies, searchParam, () => {
          this.internalSearchHotel(searchParam)
        })
      }
      $event.preventDefault()
    },
    internalSearchHotel (searchParam) {
      this.$store.dispatch('Hotel/storeHotelSearchParam', searchParam)
      this.$store.dispatch('Hotel/storeHotelSearchStatus')
      this.$goto('/hotel/doSearchHotel')
    }
  }
}
</script>

<style scoped>

</style>
