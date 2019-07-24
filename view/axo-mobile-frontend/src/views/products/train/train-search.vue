<template>
  <f7-page page-content with-subnavbar class="train">
    <f7-navbar :back-link="$t('common.label.back')"
               back-link-url="/book-travellers/train">
      <f7-nav-title>{{$t('train.label.train')}}</f7-nav-title>
      <common-home/>
      <f7-subnavbar>
        <f7-segmented raised>
          <f7-button @click="searchTrainScheduleParam.trainRouteType='O'" :active="searchTrainScheduleParam.trainRouteType==='O'">
            {{$t('air.label.ow')}}
          </f7-button>
          <f7-button @click="searchTrainScheduleParam.trainRouteType='R'" :active="searchTrainScheduleParam.trainRouteType==='R'">
            {{$t('air.label.rt')}}
          </f7-button>
        </f7-segmented>
      </f7-subnavbar>
    </f7-navbar>
    <f7-list form>
      <template v-if="searchTrainScheduleParam.trainRouteType==='O' || searchTrainScheduleParam.trainRouteType==='R'">
        <li class="item-content no-padding-left">
          <f7-row class="item-inner no-padding-bottom no-padding-top" no-gap>
            <f7-col width="45">
              <common-autocomplete
                      v-validate="'required'"
                      :page-title="$t('air.label.from')"
                      :data-vv-name="$t('air.label.from')"
                      :placeholder="$t('air.label.from')"
                      v-model="searchTrainScheduleParam.searchParam.departStation"
                      @change="searchTrainScheduleParam.searchParam.departStationCode=($event||{}).code"
                      :text-property="$i18nMsg('nameCN', 'nameEN')"
                      value-property="code"
                      :close-on-select="true"
                      :autocompleteConfig="trainAutoConfig"
                      :select-page-data-items="selectStationItems"/>
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
                      v-model="searchTrainScheduleParam.searchParam.arrivalStation"
                      @change="searchTrainScheduleParam.searchParam.arrivalStationCode=($event||{}).code"
                      :text-property="$i18nMsg('nameCN', 'nameEN')"
                      value-property="code"
                      :close-on-select="true"
                      :autocompleteConfig="trainAutoConfig"
                      :select-page-data-items="selectStationItems"/>
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
                  :value="searchTrainScheduleParam.searchParam.departDate"
                  v-model="searchTrainScheduleParam.searchParam.departDate"></common-datepicker>
        </li>
        <template v-if="searchTrainScheduleParam.trainRouteType==='R'">
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
                    :value="searchTrainScheduleParam.searchParam.returnDate"
                    v-model="searchTrainScheduleParam.searchParam.returnDate"/>
          </li>
        </template>
      </template>
      <li v-if="errors">
        <common-form-errors :value="errors.all()"/>
      </li>
      <f7-block>
        <f7-row>
          <f7-col>
            <f7-button fill large :disabled="!formValidator.isFormValid" @click="doSearchTrain" type="submit">
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
import TrainService from '../../../services/train/TrainService'
import PolicyControlService from '../../../services/policy/PolicyControlService'
import moment from 'moment'
import { ProductType } from '../../../consts/OrderConsts'
import ProductService from '../../../services/products/ProductService'

export default {
  name: 'train',
  data () {
    let searchTrainScheduleParam = TrainService.getSearchParam()
    const trainDefaultSetup = this.$defaultSetup(ProductType.Train)
    console.info('train default setup', trainDefaultSetup)
    const departStation = trainDefaultSetup.departStation
    const arrivalStation = trainDefaultSetup.arrivalStation
    const trainAutoConfig = {
      keyWordKey: 'queryStr',
      searchMethod: TrainService.getTrainStation
    }
    merge(searchTrainScheduleParam, {
      searchParam: {
        departStation,
        arrivalStation,
        departStationCode: departStation ? departStation.code : '',
        arrivalStationCode: arrivalStation ? arrivalStation.code : ''
      }
    })
    ProductService.processExternalOrderAutoInput(this, searchTrainScheduleParam, ProductType.Train)
    return {
      formValidator: {},
      trainDefaultSetup,
      searchTrainScheduleParam,
      searchParams: [],
      currentDate: moment().startOf('d').toDate(),
      returnMinDate: moment().startOf('d').toDate(),
      selectStationItems: [],
      trainAutoConfig
    }
  },
  watch: {
    'searchTrainScheduleParam.searchParam.departDate': function watchValue (deptDate) {
      if (deptDate) {
        this.returnMinDate = moment(deptDate).startOf('d').toDate()
        if (this.searchTrainScheduleParam.searchParam.returnDate) {
          if (moment(deptDate) > moment(this.searchTrainScheduleParam.searchParam.returnDate)) {
            this.searchTrainScheduleParam.searchParam.returnDate = moment(deptDate).startOf('d').add(1, 'd').toArray()
          }
        }
      }
    }
  },
  methods: {
    doSearchTrain ($event) {
      const searchParam = cloneDeep(this.searchTrainScheduleParam)
      PolicyControlService.doPolicyControl(TrainService.checkTrainSearchPolicys, searchParam, () => {
        this.$store.dispatch('Train/storeSearchTrainScheduleParams', this.calcSearchParams())
        this.$store.dispatch('Train/storeSelectedTrainSegments')
        // this.$store.dispatch('Train/firstResultData')
        // this.$store.dispatch('Train/lastResultData')
        if (this.searchTrainScheduleParam.trainRouteType === 'R') {
          this.$goto('/train/doSsearchTrainSchedule')
        } else {
          this.$goto('/train/doSsearchLastTrainSchedule')
        }
      })
      $event.preventDefault()
    },
    calcSearchParams () {
      const searchTrainScheduleParam = cloneDeep(this.searchTrainScheduleParam)
      const searchParams = [searchTrainScheduleParam]
      if (this.searchTrainScheduleParam.trainRouteType === 'R') {
        const rtParam = cloneDeep(searchTrainScheduleParam)
        this.switchtRtParam(rtParam)
        searchParams[1] = rtParam
      }
      return searchParams
    },
    switchtRtParam (rtParam) {
      const searchParam = rtParam.searchParam
      const tmpStation = searchParam.departStation
      searchParam.departStation = searchParam.arrivalStation
      searchParam.arrivalStation = tmpStation
      searchParam.departStationCode = searchParam.departStation.code
      searchParam.arrivalStationCode = searchParam.arrivalStation.code
      searchParam.departDate = rtParam.returnDate
    }
  },
  mounted () {
    this.$initFormValidate()
    const self = this
    this.$store.dispatch('Train/storeTrainHotStations').then(() => {
      self.selectStationItems = [{
        label: this.$i18nBundle('common.label.hot'),
        items: this.$store.getters['Train/dataConfig'].trainHotStations || []
      }]
    })
  }
}
</script>

<style scoped>
</style>
