<template>
  <div>
    <div class="timeline-item-time">
      <products-icons :hotel="true"/>
      {{productOrder.checkinDate|date('MM-DD')}}
    </div>
    <div class="timeline-item-title">
      <f7-row>
        <f7-col width="30" class="text-align-left">
          {{$i18nMsg(productOrder.hotelCityNameCN, productOrder.hotelCityNameEN)}}
        </f7-col>
        <f7-col width="70" class="text-align-left">
          {{productOrder.hotelInfo && $i18nMsg(productOrder.hotelInfo.nameCn, productOrder.hotelInfo.nameEn)}}
        </f7-col>
      </f7-row>
    </div>
    <div class="timeline-item-subtitle">
      <f7-row>
        <f7-col>
          {{productOrder.hotelInfo && $i18nMsg(productOrder.hotelInfo.addressCn, productOrder.hotelInfo.addressEn)}}
        </f7-col>
      </f7-row>
    </div>
    <div class="timeline-item-text">
      <f7-row>
        <f7-col class="text-align-center">

        </f7-col>
      </f7-row>
    </div>
  </div>
</template>

<script>
import ProductsIcons from '../../../components/products/products-icons'

export default {
  name: 'timeline-hotel',
  components: { ProductsIcons },
  props: {
    productOrder: {
      type: Object
    }
  }
}
</script>

<style scoped>

</style>
