<template>
  <f7-page page-content with-subnavbar class="intlair">
    <f7-navbar :back-link="$t('common.label.back')"
               back-link-url="/book-travellers/intlair"
               back-link-force>
      <f7-nav-title>{{$t('air.label.intl')}}</f7-nav-title>
      <common-home/>
      <f7-subnavbar>
        <f7-segmented raised>
          <f7-button @click="intlAirSearchParam.intlAirSearchDto.interFlightType='OW'" :active="intlAirSearchParam.intlAirSearchDto.interFlightType==='OW'">
            {{$t('air.label.ow')}}
          </f7-button>
          <f7-button @click="intlAirSearchParam.intlAirSearchDto.interFlightType='RT'" :active="intlAirSearchParam.intlAirSearchDto.interFlightType==='RT'">
            {{$t('air.label.rt')}}
          </f7-button>
        </f7-segmented>
      </f7-subnavbar>
    </f7-navbar>
    <f7-list form>
      <template>
        <li class="item-content no-padding-left">
          <f7-row class="item-inner no-padding-bottom no-padding-top" no-gap>
            <f7-col width="45">
              <common-autocomplete
                v-validate="'required'"
                :page-title="$t('air.label.from')"
                :data-vv-name="$t('air.label.from')"
                :placeholder="$t('air.label.from')"
                v-model="intlAirSearchParam.intlAirSearchDto.departPort"
                @change="intlAirSearchParam.intlAirSearchDto.owFromCity=($event||{}).code"
                :text-property="$i18nMsg('nameCN', 'nameEN')"
                value-property="code"
                :close-on-select="true"
                :autocompleteConfig="intlAirAutoConfig"
                :select-page-data-items="selectCityItems"/>
            </f7-col>
            <f7-col width="10" class="text-align-center" style="overflow: hidden;">
              <f7-list-item inline>
                <a href="#" @click="switchIntlAirPorts(intlAirSearchParam)">
                  <f7-icon material="swap_horizontal_circle"></f7-icon>
                </a>
              </f7-list-item>
            </f7-col>
            <f7-col width="45">
              <common-autocomplete
                input-class-name="text-align-right"
                v-validate="'required'"
                :page-title="$t('air.label.to')"
                :data-vv-name="$t('air.label.to')"
                :placeholder="$t('air.label.to')"
                v-model="intlAirSearchParam.intlAirSearchDto.arrivalPort"
                @change="intlAirSearchParam.intlAirSearchDto.owToCity=($event||{}).code"
                :text-property="$i18nMsg('nameCN', 'nameEN')"
                value-property="code"
                :close-on-select="true"
                :autocompleteConfig="intlAirAutoConfig"
                :select-page-data-items="selectCityItems"/>
            </f7-col>
          </f7-row>
        </li>
        <li>
          <common-datepicker
            :label="$t('air.label.deptDate')"
            :inline-label="true"
            :min-date="currentDate"
            v-validate="'required'"
            :default-min-date-start="3"
            :data-vv-name="$t('air.label.deptDate')"
            :placeholder="$t('air.label.deptDate')"
            name="deptDate"
            input-class-name="text-align-right"
            class="item-link"
            :value="intlAirSearchParam.intlAirSearchDto.owFromDate"
            v-model="intlAirSearchParam.intlAirSearchDto.owFromDate"></common-datepicker>
        </li>
        <f7-list-item :title="$t('air.label.deptTime')" smart-select
                      :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
          <select
            name="selectTime" v-model="intlAirSearchParam.intlAirSearchDto.owFromTime">
            <option :key="index" v-for="(selectTime, index) in selectTimes" :value="selectTime.timeValue">
              {{selectTime.timeText}}
            </option>
          </select>
        </f7-list-item>
        <template v-if="intlAirSearchParam.intlAirSearchDto.interFlightType==='RT'">
          <li>
            <common-datepicker
              key="returnDate"
              :min-date="intlAirSearchParam.intlAirSearchDto.owFromDate?intlAirSearchParam.intlAirSearchDto.owFromDate:currentDate"
              :label="$t('air.label.returnDate')"
              :inline-label="true"
              v-validate="'required'"
              :data-vv-name="$t('air.label.returnDate')"
              :placeholder="$t('air.label.returnDate')"
              name="returnDate"
              input-class-name="text-align-right"
              class="item-link"
              :value="intlAirSearchParam.intlAirSearchDto.rtReturnDate"
              v-model="intlAirSearchParam.intlAirSearchDto.rtReturnDate"/>
          </li>
          <f7-list-item :title="$t('air.label.returnTime')" smart-select
                        :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
            <select
              name="selectTime" v-model="intlAirSearchParam.intlAirSearchDto.rtToTime">
              <option :key="index" v-for="(selectTime, index) in selectTimes" :value="selectTime.timeValue">
                {{selectTime.timeText}}
              </option>
            </select>
          </f7-list-item>
        </template>
      </template>
      <f7-list-item :title="$t('air.label.cabinType')" smart-select
                    :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
        <select
          v-validate="'required'"
          :data-vv-as="$t('air.label.cabinType')"
          name="selectTime" v-model="intlAirSearchParam.intlAirSearchDto.owSeatType">
          <option :key="index" v-for="(airCabinType, index) in intlAirCabinTypes" :value="airCabinType.id">
            {{airCabinType.label}}
          </option>
        </select>
      </f7-list-item>
      <f7-list-item checkbox :title="$t('air.label.direct')"
                    v-if="intlAirDefaultSetup.directSetup"
                    @change="intlAirSearchParam.intlAirSearchDto.owNoStopFlag=$event.target.checked?'1':'0'"
                    :checked="intlAirDefaultSetup.directSetup===2"></f7-list-item>
      <li v-if="errors">
        <common-form-errors :value="errors.all()"/>
      </li>
      <f7-block>
        <f7-row>
          <f7-col>
            <f7-button fill large :disabled="!formValidator.isFormValid" @click="doSearchIntlAir" type="submit">
              {{$t('common.label.search')}}
            </f7-button>
          </f7-col>
        </f7-row>
      </f7-block>
    </f7-list>
  </f7-page>
</template>

<script>
import merge from 'lodash/merge'
import cloneDeep from 'lodash/cloneDeep'
import IntlairService from '../../../services/intlair/IntlairService'
import PolicyControlService from '../../../services/policy/PolicyControlService'
import moment from 'moment'
import { ProductType } from '../../../consts/OrderConsts'
import ProductService from '../../../services/products/ProductService'

export default {
  name: 'intlair-search',
  data () {
    let intlAirSearchParam = IntlairService.getSearchParam()
    const intlAirDefaultSetup = this.$defaultSetup(ProductType.IntlAir)
    console.info('intlAir default setup', intlAirDefaultSetup)
    let intlAirCabinTypes = IntlairService.getIntlAirCabinTypes(intlAirDefaultSetup.selectableCabinStr)
    if (this.$store.getters['Order/basicSearchParam'].policyUser.custNo && this.$store.getters['Order/basicSearchParam'].policyUser.custNo.length >= 6) {
      const barno = this.$store.getters['Order/basicSearchParam'].policyUser.custNo.substring(0, 6)
      let flag = true
      if (intlAirDefaultSetup.relationGroupList) {
        intlAirDefaultSetup.relationGroupList.forEach(relationGroup => {
          if (flag && relationGroup.relateList) {
            relationGroup.relateList.forEach(relate => {
              if (flag && relate.relateNo === barno) {
                if (relationGroup.cabinTypeGroup && relationGroup.cabinTypeGroup.cabinTypeI) {
                  flag = false
                  intlAirCabinTypes = IntlairService.getIntlAirCabinTypes(relationGroup.cabinTypeGroup.cabinTypeI)
                }
              }
            })
          }
        })
      }
    }
    const selectTimes = intlAirDefaultSetup.timeOptionList
    const intlAirAutoConfig = {
      keyWordKey: 'keyWords',
      searchMethod: IntlairService.getIntlAirCitys
    }
    merge(intlAirSearchParam, {
      intlAirSearchDto: {
        owSeatType: intlAirCabinTypes[0].id,
        departPort: null,
        arrivalPort: null,
        owNoStopFlag: intlAirDefaultSetup.directSetup === '2' ? '1' : '0'
      }
    })
    ProductService.processExternalOrderAutoInput(this, intlAirSearchParam, ProductType.IntlAir)
    return {
      formValidator: {},
      intlAirDefaultSetup,
      intlAirSearchParam,
      selectTimes,
      intlAirCabinTypes,
      currentDate: moment().startOf('d').add(3, 'd').toDate(),
      selectCityItems: [],
      intlAirAutoConfig
    }
  },
  watch: {
    'intlAirSearchParam.intlAirSearchDto.owFromDate': function watchValue (deptDate) {
      if (deptDate) {
        if (this.intlAirSearchParam.intlAirSearchDto.rtReturnDate) {
          if (moment(deptDate) > moment(this.intlAirSearchParam.intlAirSearchDto.rtReturnDate)) {
            this.intlAirSearchParam.intlAirSearchDto.rtReturnDate = moment(deptDate).startOf('d').add(1, 'd').toArray()
          }
        }
      }
    }
  },
  methods: {
    switchIntlAirPorts (searchParam) {
      const intlAirSearchDto = searchParam.intlAirSearchDto
      const tmpPort = intlAirSearchDto.departPort
      intlAirSearchDto.departPort = intlAirSearchDto.arrivalPort
      intlAirSearchDto.arrivalPort = tmpPort
      intlAirSearchDto.owFromCity = intlAirSearchDto.departPort.code
      intlAirSearchDto.owToCity = intlAirSearchDto.arrivalPort.code
    },
    doSearchIntlAir ($event) {
      $event.preventDefault()
      if (this.intlAirSearchParam.intlAirSearchDto.departPort.ctryCode === 'CN' && this.intlAirSearchParam.intlAirSearchDto.arrivalPort.ctryCode === 'CN') {
        this.$coreAlert(this.$i18nBundle('air.msg.bookinter'))
        return
      }
      if (this.intlAirSearchParam.intlAirSearchDto.owSeatType === 'A') {
        this.intlAirSearchParam.intlAirSearchDto.owSeatType = ''
      } else if (this.intlAirSearchParam.intlAirSearchDto.owSeatType === 'Y') {
        this.intlAirSearchParam.intlAirSearchDto.owSeatType = 'Economy'
      } else if (this.intlAirSearchParam.intlAirSearchDto.owSeatType === 'C') {
        this.intlAirSearchParam.intlAirSearchDto.owSeatType = 'Business'
      } else if (this.intlAirSearchParam.intlAirSearchDto.owSeatType === 'F') {
        this.intlAirSearchParam.intlAirSearchDto.owSeatType = 'First'
      }
      if (this.intlAirSearchParam.intlAirSearchDto.interFlightType === 'RT') {
        this.intlAirSearchParam.intlAirSearchDto.rtFromCity = this.intlAirSearchParam.intlAirSearchDto.owFromCity
        this.intlAirSearchParam.intlAirSearchDto.rtToCity = this.intlAirSearchParam.intlAirSearchDto.owToCity
        this.intlAirSearchParam.intlAirSearchDto.rtFromDate = this.intlAirSearchParam.intlAirSearchDto.owFromDate
        this.intlAirSearchParam.intlAirSearchDto.rtFromTime = this.intlAirSearchParam.intlAirSearchDto.owFromTime
        this.intlAirSearchParam.intlAirSearchDto.rtNoStopFlag = this.intlAirSearchParam.intlAirSearchDto.owNoStopFlag
        this.intlAirSearchParam.intlAirSearchDto.rtSeatType = this.intlAirSearchParam.intlAirSearchDto.owSeatType
      }
      const searchParam = cloneDeep(this.intlAirSearchParam)
      PolicyControlService.doPolicyControl(IntlairService.checkIntlAirSearchPolicys, searchParam, () => {
        this.$store.dispatch('IntlAir/storeIntlAirSearchParam', searchParam)
        this.$store.dispatch('IntlAir/cacheResultData')
        // IndexedDB.openDB('intlAirResult', 1, null, {
        //   name: 'cacheResult',
        //   key: 'cacheResult'
        // }, function (db) {
        //   IndexedDB.clearStore(db, 'cacheResult')
        // })
        this.$store.dispatch('IntlAir/firstCurrentTab')
        this.$store.dispatch('IntlAir/lastCurrentTab')
        if (this.intlAirSearchParam.intlAirSearchDto.interFlightType === 'RT') {
          this.$goto('/intlair/searchIntlAir')
        } else {
          this.$goto('/intlair/searchLastIntlAir')
        }
      })
    }
  },
  mounted () {
    this.$initFormValidate()
    const self = this
    this.$store.dispatch('IntlAir/storeIntlAirHotCitys').then(() => {
      self.selectCityItems = [{
        label: this.$i18nBundle('common.label.hot'),
        items: this.$store.getters['IntlAir/dataConfig'].intlAirHotCitys || []
      }]
    })
  }
}
</script>

<style scoped>
</style>
