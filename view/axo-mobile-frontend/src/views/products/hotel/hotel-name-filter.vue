<template>
  <div>
    <ul>
      <li v-show="false">
        <common-autocomplete
          :label="$t('hotel.label.hotelName')"
          :inline-label="true"
          :page-title="$t('hotel.label.hotelName')"
          :placeholder="$t('hotel.label.hotelName')"
          v-model="config.autoHotelName"
          @change="selectHotel"
          :text-property="$i18nMsg('nameCN', 'nameEN')"
          value-property="id"
          :close-on-select="true"
          :request-source-on-open="true"
          input-events="search"
          input-class-name="text-align-right"
          class="item-link"
          :opened="config.showAutoHotelName"
          @closed="config.showAutoHotelName=false"
          :autocompleteConfig="config.autoHotelNameConfig"/>
      </li>
    </ul>
    <f7-list-input :value="searchParam.hotelQueryParam.hotelName"
                   @input="searchParam.hotelQueryParam.hotelName=$event.target.value"
                   inline-label
                   class="item-link searchable"
                   input-style="text-align: right;"
                   :placeholder="$t('hotel.label.hotelName')">
      <div slot="label">
        {{$t('hotel.label.hotelName')}}
        <f7-link
          v-if="searchParam.hotelQueryParam.cityCode && searchParam.hotelQueryParam.checkinDate && searchParam.hotelQueryParam.checkoutDate"
          @click="config.showAutoHotelName=true">
          <f7-icon f7="search" size="20"/>
        </f7-link>
      </div>
    </f7-list-input>
  </div>
</template>

<script>
import HotelAutoService from '../../../services/hotel/HotelAutoService'

export default {
  name: 'hotel-name-filter',
  props: {
    value: {
      type: Object
    }
  },
  data () {
    const { value } = this.$props
    const searchParam = value
    return {
      searchParam,
      config: {
        autoHotelName: null,
        autoHotelNameConfig: {
          searchMethod: HotelAutoService.getAutoHotelList(searchParam)
        },
        showAutoHotelName: false
      }
    }
  },
  watch: {
    value: {
      handler (value) {
        this.config.autoHotelNameConfig = {
          searchMethod: HotelAutoService.getAutoHotelList(value)
        }
      },
      deep: true
    }
  },
  methods: {
    selectHotel () {
      if (this.config.autoHotelName) {
        this.searchParam.hotelQueryParam.hotelName = this.config.autoHotelName[this.$i18nMsg('nameCN', 'nameEN')]
        this.$nextTick(() => {
          this.config.autoHotelName = null
        })
      }
    }
  }
}
</script>

<style scoped>

</style>
