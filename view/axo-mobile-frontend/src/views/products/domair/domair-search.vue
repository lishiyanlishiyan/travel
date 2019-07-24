<template>
  <f7-page page-content with-subnavbar class="domair">
    <f7-navbar :back-link="$t('common.label.back')"
               back-link-url="/book-travellers/domair">
      <f7-nav-title>{{$t('air.label.dom')}}</f7-nav-title>
      <common-home/>
      <f7-subnavbar>
        <f7-segmented raised>
          <f7-button @click="searchDomFlightParam.airType='OW'" :active="searchDomFlightParam.airType==='OW'">
            {{$t('air.label.ow')}}
          </f7-button>
          <f7-button @click="searchDomFlightParam.airType='RT'" :active="searchDomFlightParam.airType==='RT'">
            {{$t('air.label.rt')}}
          </f7-button>
          <!--<f7-button @click="searchDomFlightParam.airType='MD'" :active="searchDomFlightParam.airType==='MD'">-->
          <!--{{$t('air.label.md')}}-->
          <!--</f7-button>-->
        </f7-segmented>
      </f7-subnavbar>
    </f7-navbar>
    <f7-list form>
      <template v-if="searchDomFlightParam.airType==='OW' || searchDomFlightParam.airType==='RT'">
        <li class="item-content no-padding-left">
          <f7-row class="item-inner no-padding-bottom no-padding-top" no-gap>
            <f7-col width="45">
              <common-autocomplete
                v-validate="'required'"
                :page-title="$t('air.label.from')"
                :data-vv-name="$t('air.label.from')"
                :placeholder="$t('air.label.from')"
                v-model="searchDomFlightParam.searchDomFlight.departPort"
                @change="searchDomFlightParam.searchDomFlight.departAirportCode=($event||{}).code"
                :text-property="$i18nMsg('nameCN', 'nameEN')"
                value-property="code"
                :close-on-select="true"
                :autocompleteConfig="domAirAutoConfig"
                :select-page-data-items="selectCityItems"/>
            </f7-col>
            <f7-col width="10" class="text-align-center" style="overflow: hidden;">
              <f7-list-item inline>
                <a href="#" @click="switchDomAirPorts(searchDomFlightParam)">
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
                v-model="searchDomFlightParam.searchDomFlight.arrivalPort"
                @change="searchDomFlightParam.searchDomFlight.arrivalAirportCode=($event||{}).code"
                :text-property="$i18nMsg('nameCN', 'nameEN')"
                value-property="code"
                :close-on-select="true"
                :autocompleteConfig="domAirAutoConfig"
                :select-page-data-items="selectCityItems"/>
            </f7-col>
          </f7-row>
        </li>
        <li>
          <common-datepicker
            :label="$t('air.label.deptDate')"
            :inline-label="true"
            :min-date="currentDate"
            :default-min-date-start="3"
            v-validate="'required'"
            :data-vv-name="$t('air.label.deptDate')"
            :placeholder="$t('air.label.deptDate')"
            name="deptDate"
            input-class-name="text-align-right"
            class="item-link"
            :value="searchDomFlightParam.searchDomFlight.deptDate"
            v-model="searchDomFlightParam.searchDomFlight.deptDate"></common-datepicker>
        </li>
        <f7-list-item :title="$t('air.label.deptTime')" smart-select
                      :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
          <select
            name="selectTime" v-model="searchDomFlightParam.searchDomFlight.deptTime">
            <option :key="index" v-for="(selectTime, index) in selectTimes" :value="selectTime.timeValue">
              {{selectTime.timeText}}
            </option>
          </select>
        </f7-list-item>
        <template v-if="searchDomFlightParam.airType==='RT'">
          <li>
            <common-datepicker
              key="returnDate"
              :min-date="returnMinDate"
              :label="$t('air.label.returnDate')"
              :inline-label="true"
              v-validate="'required'"
              :data-vv-name="$t('air.label.returnDate')"
              :placeholder="$t('air.label.returnDate')"
              name="returnDate"
              input-class-name="text-align-right"
              class="item-link"
              :value="searchDomFlightParam.searchDomFlight.returnDate"
              v-model="searchDomFlightParam.searchDomFlight.returnDate"/>
          </li>
          <f7-list-item :title="$t('air.label.returnTime')" smart-select
                        :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
            <select
              name="selectTime" v-model="searchDomFlightParam.searchDomFlight.returnTime">
              <option :key="index" v-for="(selectTime, index) in selectTimes" :value="selectTime.timeValue">
                {{selectTime.timeText}}
              </option>
            </select>
          </f7-list-item>
        </template>
      </template>
      <template v-if="searchDomFlightParam.airType==='MD'">
        <template v-for="(searchParam, index) in searchParams">
          <f7-list-item :key="'segment' + searchParam.uuid">
            <f7-chip :text="$t('common.label.segmentIdx', [index+1])" color="blue"/>
            <f7-col>
              <f7-link v-if="searchParams.length>1"
                       @click="removeSearchSegment(searchParam)">
                <f7-icon f7="close_round" size="20" color="red"/>
              </f7-link>&nbsp;
              <f7-link v-if="index===searchParams.length-1 && searchParams.length<3"
                       @click="addSearchSegment()">
                <f7-icon f7="add_round" size="20" color="green"/>
              </f7-link>
            </f7-col>
          </f7-list-item>
          <li class="item-content no-padding-left" :key="'city' + searchParam.uuid">
            <f7-row class="item-inner no-padding-bottom no-padding-top" no-gap>
              <f7-col width="45">
                <common-autocomplete
                  v-validate="'required'"
                  :page-title="$t('air.label.from') + searchParam.uuid"
                  :data-vv-name="$t('common.label.segmentIdx', [index+1]) + $t('air.label.from')"
                  :placeholder="$t('air.label.from')"
                  v-model="searchParam.searchDomFlight.departPort"
                  @change="searchParam.searchDomFlight.departAirportCode=($event||{}).code"
                  :text-property="$i18nMsg('nameCN', 'nameEN')"
                  value-property="code"
                  :close-on-select="true"
                  key="from"
                  :autocompleteConfig="domAirAutoConfig"
                  :select-page-data-items="selectCityItems"/>
              </f7-col>
              <f7-col width="10" class="text-align-center">
                <f7-list-item inline>
                  <a href="#" @click="switchDomAirPorts(searchParam)">
                    <f7-icon material="swap_horizontal_circle"></f7-icon>
                  </a>
                </f7-list-item>
              </f7-col>
              <f7-col width="45">
                <common-autocomplete
                  input-class-name="text-align-right"
                  v-validate="'required'"
                  :page-title="$t('air.label.to')"
                  :data-vv-name="$t('common.label.segmentIdx', [index+1]) + $t('air.label.to')"
                  :placeholder="$t('air.label.to')"
                  v-model="searchParam.searchDomFlight.arrivalPort"
                  @change="searchParam.searchDomFlight.arrivalAirportCode=($event||{}).code"
                  :text-property="$i18nMsg('nameCN', 'nameEN')"
                  value-property="code"
                  :close-on-select="true"
                  key="to"
                  :autocompleteConfig="domAirAutoConfig"
                  :select-page-data-items="selectCityItems"/>
              </f7-col>
            </f7-row>
          </li>
          <li :key="'deptDate' + searchParam.uuid">
            <common-datepicker
              :label="$t('air.label.deptDate')"
              :inline-label="true"
              :min-date="currentDate"
              :default-min-date-start="3"
              v-validate="'required'"
              :data-vv-name="$t('common.label.segmentIdx', [index+1]) + $t('air.label.deptDate')"
              :placeholder="$t('air.label.deptDate')"
              input-class-name="text-align-right"
              class="item-link"
              v-model="searchParam.searchDomFlight.deptDate"></common-datepicker>
          </li>
          <f7-list-item :key="'deptTime' + searchParam.uuid" :title="$t('air.label.deptTime')" smart-select
                        :smart-select-params="{openIn: 'sheet'}">
            <select
              name="selectTime" v-model="searchParam.searchDomFlight.deptTime">
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
          name="selectTime" v-model="searchDomFlightParam.cabinType">
          <option :key="index" v-for="(airCabinType, index) in domAirCabinTypes" :value="airCabinType.id">
            {{airCabinType.label}}
          </option>
        </select>
      </f7-list-item>
      <f7-list-item checkbox :title="$t('air.label.stopOver')"
                    @change="searchDomFlightParam.transitFlag=$event.target.checked?'1':''"
                    :checked="domAirDefaultSetup.domairNeedTransit===2"></f7-list-item>
      <li v-if="errors">
        <common-form-errors :value="errors.all()"/>
      </li>
      <f7-block>
        <f7-row>
          <f7-col>
            <f7-button fill large :disabled="!formValidator.isFormValid" @click="doSearchDomAir" type="submit">
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
import uniqueId from 'lodash/uniqueId'
import DomairService from '../../../services/domair/DomairService'
import PolicyControlService from '../../../services/policy/PolicyControlService'
import moment from 'moment'
import { ProductType } from '../../../consts/OrderConsts'
import ProductService from '../../../services/products/ProductService'

export default {
  name: 'domair',
  data () {
    let searchDomFlightParam = DomairService.getSearchParam()
    const domAirDefaultSetup = this.$defaultSetup(ProductType.DomAir)
    console.info('domAir default setup', domAirDefaultSetup)
    const departPort = domAirDefaultSetup.defaultDeptAirport
    const arrivalPort = domAirDefaultSetup.defaultArrivalAirport
    let domAirCabinTypes = DomairService.getDomAirCabinTypes(domAirDefaultSetup.selectableCabinStr)
    if (this.$store.getters['Order/basicSearchParam'].policyUser.custNo && this.$store.getters['Order/basicSearchParam'].policyUser.custNo.length >= 6) {
      const barno = this.$store.getters['Order/basicSearchParam'].policyUser.custNo.substring(0, 6)
      let flag = true
      if (domAirDefaultSetup.relationGroupList) {
        domAirDefaultSetup.relationGroupList.forEach(relationGroup => {
          if (flag && relationGroup.relateList) {
            relationGroup.relateList.forEach(relate => {
              if (flag && relate.relateNo === barno) {
                if (relationGroup.cabinTypeGroup && relationGroup.cabinTypeGroup.cabinTypeD) {
                  flag = false
                  domAirCabinTypes = DomairService.getDomAirCabinTypes(relationGroup.cabinTypeGroup.cabinTypeD)
                }
              }
            })
          }
        })
      }
    }
    const selectTimes = domAirDefaultSetup.timeOptionList
    const domAirAutoConfig = {
      keyWordKey: 'keyWords',
      searchMethod: DomairService.getDomAirCities
    }
    merge(searchDomFlightParam, {
      airType: 'OW',
      transitFlag: domAirDefaultSetup.domairNeedTransit === 2 ? '1' : '',
      cabinType: domAirCabinTypes[0].id,
      searchDomFlight: {
        departPort,
        arrivalPort,
        departAirportCode: departPort ? departPort.code : '',
        arrivalAirportCode: arrivalPort ? arrivalPort.code : ''
      }
    })
    ProductService.processExternalOrderAutoInput(this, searchDomFlightParam, ProductType.DomAir)
    return {
      formValidator: {},
      domAirDefaultSetup,
      searchDomFlightParam,
      searchParams: [],
      selectTimes,
      domAirCabinTypes,
      currentDate: moment().startOf('d').toDate(),
      returnMinDate: moment().startOf('d').toDate(),
      selectCityItems: [],
      domAirAutoConfig
    }
  },
  watch: {
    'searchDomFlightParam.airType': function watchValue (airType) {
      if (this.searchParams.length === 0 && airType === 'MD') {
        const searchParam1 = this.newSearchParam()
        // const searchParam2 = this.newSearchParam()
        // const searchParams = [searchParam1, searchParam2]
        // this.searchParams = searchParams
        this.searchParams = [searchParam1]
        this.$nextTick(() => {
          this.$forceUpdate()
        })
      }
    },
    'searchDomFlightParam.searchDomFlight.deptDate': function watchValue (deptDate) {
      if (deptDate) {
        this.returnMinDate = moment(deptDate).startOf('d').toDate()
        if (this.searchDomFlightParam.searchDomFlight.returnDate) {
          if (moment(deptDate) > moment(this.searchDomFlightParam.searchDomFlight.returnDate)) {
            this.searchDomFlightParam.searchDomFlight.returnDate = moment(deptDate).startOf('d').add(1, 'd').toArray()
          }
        }
      }
    }
  },
  methods: {
    doSearchDomAir ($event) {
      const searchParam = cloneDeep(this.getSearchParam())
      PolicyControlService.doPolicyControl(DomairService.checkDomAirSearchPolicys, searchParam, () => {
        this.$store.dispatch('DomAir/storeSearchDomFlightParams', this.getSearchParams())
        this.$store.dispatch('DomAir/storeSelectedDomAirSegments')
        this.$store.dispatch('DomAir/firstResultData')
        this.$store.dispatch('DomAir/lastResultData')
        if (this.searchDomFlightParam.airType === 'RT') {
          this.$goto('/domair/doSearchDomAir')
        } else {
          this.$goto('/domair/doSearchLastDomAir')
        }
      })
      $event.preventDefault()
    },
    getSearchParam () {
      if (this.searchDomFlightParam.airType === 'MD') {
        return this.searchParams[0]
      } else {
        return this.searchDomFlightParam
      }
    },
    getSearchParams () {
      if (this.searchDomFlightParam.airType === 'MD') {
        return this.searchParams
      } else {
        return this.calcSearchParams()
      }
    },
    calcSearchParams () {
      const searchParam = this.newSearchParam()
      const searchParams = [searchParam]
      if (this.searchDomFlightParam.airType === 'RT') {
        const rtParam = cloneDeep(searchParam)
        this.switchDomAirPorts(rtParam, true)
        searchParams[1] = rtParam
      }
      return searchParams
    },
    newSearchParam () {
      const searchParam = cloneDeep(this.searchDomFlightParam)
      searchParam.policyUserId = this.$store.getters['Order/basicSearchParam'].policyUserId
      searchParam.uuid = uniqueId()
      return searchParam
    },
    addSearchSegment () {
      const searchParam = this.newSearchParam()
      this.searchParams.push(searchParam)
    },
    removeSearchSegment (searchParam) {
      if (this.searchParams.length > 1) {
        this.searchParams.splice(this.searchParams.indexOf(searchParam), 1)
      }
    },
    switchDomAirPorts (searchParam, rt) {
      const searchDomFlight = searchParam.searchDomFlight
      const tmpPort = searchDomFlight.departPort
      searchDomFlight.departPort = searchDomFlight.arrivalPort
      searchDomFlight.arrivalPort = tmpPort
      searchDomFlight.departAirportCode = searchDomFlight.departPort.code
      searchDomFlight.arrivalAirportCode = searchDomFlight.arrivalPort.code
      if (rt) {
        searchDomFlight.rtFlag = true
        searchDomFlight.deptDate = searchDomFlight.returnDate
        searchDomFlight.deptTime = searchDomFlight.returnTime
      }
    }
  },
  mounted () {
    this.$initFormValidate()
    const self = this
    this.$store.dispatch('DomAir/storeDomAirHotAirports').then(() => {
      self.selectCityItems = [{
        label: this.$i18nBundle('common.label.hot'),
        items: this.$store.getters['DomAir/dataConfig'].domAirHotAirports || []
      }]
    })
  }
}
</script>

<style scoped>
</style>
