<template>
  <div>
    <div class="timeline-item-time">
      <products-icons :train="true"/>
      {{productOrder.departureDate|date('MM-DD HH:mm')}} -
      {{productOrder.arrivalDate|date('MM-DD HH:mm')}}
    </div>
    <div class="timeline-item-title">
      <f7-row>
        <f7-col width="45" class="text-align-right">
          {{$i18nMsg(productOrder.deptStation.cityNameCN, productOrder.deptStation.cityNameEN)}}
        </f7-col>
        <f7-col width="10" class="text-align-center">-</f7-col>
        <f7-col width="45" class="text-align-left">
          {{$i18nMsg(productOrder.arrivalStation.cityNameCN, productOrder.arrivalStation.cityNameEN)}}
        </f7-col>
      </f7-row>
    </div>
    <div class="timeline-item-subtitle">
      <f7-row>
        <f7-col width="50" class="text-align-right">
          {{$i18nMsg(productOrder.deptStation.nameCN, productOrder.deptStation.nameEN)}}
        </f7-col>
        <f7-col width="50" class="text-align-left">
          {{$i18nMsg(productOrder.arrivalStation.nameCN, productOrder.arrivalStation.nameEN)}}
        </f7-col>
      </f7-row>
    </div>
    <div class="timeline-item-text">
      <f7-row>
        <f7-col class="text-align-center">
          {{productOrder.trainNo}}
        </f7-col>
      </f7-row>
    </div>
  </div>
</template>

<script>
import ProductsIcons from '../../../components/products/products-icons'

export default {
  name: 'timeline-train',
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
