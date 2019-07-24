<template>
  <f7-list>
    <li>
      <common-autocomplete
        :label="$t('hotel.label.address')"
        :inline-label="true"
        :page-title="$t('hotel.label.address')"
        :placeholder="$t('hotel.label.address')"
        v-model="config.autoHotelAddress"
        @change="onSelectLocationInputTip($event)"
        :text-property="$i18nMsg('nameCn', 'nameEn')"
        value-property="id"
        :close-on-select="true"
        :request-source-on-open="true"
        input-events="search"
        input-class-name="text-align-right"
        class="item-link"
        clear-button
        :autocompleteConfig="config.autoHotelAddressConfig"/>
    </li>
    <!--<f7-list-item :title="$t('hotel.label.distance')" smart-select-->
                  <!--inline-label-->
                  <!--:smart-select-params="{openIn: 'sheet', closeOnSelect: true}">-->
      <!--<select name="selectTime" v-model="searchParam.hotelQueryParam.distance">-->
        <!--<option :key="index" v-for="(item, index) in distanceList" :value="item.id">-->
          <!--{{$t(item.label)}}-->
        <!--</option>-->
      <!--</select>-->
    <!--</f7-list-item>-->
  </f7-list>
</template>

<script>
import HotelAutoService from '../../../services/hotel/HotelAutoService'
import HotelConsts from '../../../consts/HotelConsts'
import HotelService from '../../../services/hotel/HotelService'
import AmapMapService from '../../../services/map/AmapMapService'

export default {
  name: 'hotel-address-filter',
  props: {
    value: {
      type: Object
    },
    city: {
      type: Object
    },
    companyAddressEnable: {
      type: Boolean
    }
  },
  data () {
    const { value, city, companyAddressEnable } = this.$props
    const searchParam = value
    const distanceList = HotelConsts.HOTEL_DISTANCE_LIST
    searchParam.hotelQueryParam.distance = distanceList[0].id
    return {
      searchParam,
      distanceList,
      config: {
        autoHotelAddress: null,
        autoHotelAddressConfig: this.calcAutoHotelAddressConfig(searchParam, city, companyAddressEnable)
      }
    }
  },
  watch: {
    city: {
      handler (city) {
        console.info('city change..........', city)
        this.config.autoHotelAddressConfig = this.calcAutoHotelAddressConfig(this.searchParam, city, this.companyAddressEnable)
      },
      deep: true
    },
    searchParam: {
      handler (searchParam) {
        console.info('searchParam change..........', searchParam)
        this.config.autoHotelAddressConfig = this.calcAutoHotelAddressConfig(searchParam, this.city, this.companyAddressEnable)
      },
      deep: true
    }
  },
  methods: {
    calcAutoHotelAddressConfig (searchParam, city, companyAddressEnable) {
      return {
        searchMethod: HotelAutoService.getAutoHotelAddressList(searchParam, city, companyAddressEnable)
      }
    },
    onSelectLocationInputTip (locationInputTip) {
      if (locationInputTip) {
        console.info(locationInputTip, this.city.nameCN, locationInputTip.addressCn)
        if (!locationInputTip.location) {
          this.searchParam.hotelQueryParam.location = null
          if (this.city.ctryCode === 'CN') {
            this.$coreShowLoading()
            AmapMapService.initAmapService().then(() => {
              AmapMapService.getPointByAddress(this.city.nameCN, locationInputTip.addressCn).then(point => {
                this.$coreHideLoading()
                console.info('point...........', point)
                if (point) {
                  this.searchParam.hotelQueryParam.location = point
                } else {
                  this.$coreAlert(this.$t('hotel.error.addressQueryError'))
                }
              })
            })
          } else {
            HotelService.getCityLocation({
              countryCode: this.city.ctryCode,
              cityName: locationInputTip.nameCn
            }).then(data => {
              if (data && data.resultData && data.resultData.location) {
                console.info('point...........', data.resultData.location)
                this.searchParam.hotelQueryParam.location = data.resultData.location
              } else {
                this.$coreAlert(this.$t('hotel.error.addressQueryError'))
              }
            })
          }
        } else {
          this.searchParam.hotelQueryParam.location = locationInputTip.location
        }
      } else {
        this.searchParam.hotelQueryParam.location = null
      }
    }
  }
}
</script>

<style scoped>

</style>
