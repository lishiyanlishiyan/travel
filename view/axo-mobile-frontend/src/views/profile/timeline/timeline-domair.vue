<template>
  <div>
    <div class="timeline-item-time">
      <products-icons :dom-air="true"/>
      {{productOrder.deptDate|date('MM-DD HH:mm')}} -
      {{productOrder.arrivalDate|date('MM-DD HH:mm')}}
    </div>
    <div class="timeline-item-title">
      <f7-row>
        <f7-col width="45" class="text-align-right">
          {{$i18nMsg(productOrder.deptPort.cityNameCN, productOrder.deptPort.cityNameEN)}}
        </f7-col>
        <f7-col width="10" class="text-align-center">-</f7-col>
        <f7-col width="45" class="text-align-left">
          {{$i18nMsg(productOrder.arrivalPort.cityNameCN, productOrder.arrivalPort.cityNameEN)}}
        </f7-col>
      </f7-row>
    </div>
    <div class="timeline-item-subtitle">
      <f7-row>
        <f7-col width="50" class="text-align-right">
          {{$i18nMsg(productOrder.deptPort.nameCN, productOrder.deptPort.nameEN)}}
          {{productOrder.terminal}}
        </f7-col>
        <f7-col width="50" class="text-align-left">
          {{$i18nMsg(productOrder.arrivalPort.nameCN, productOrder.arrivalPort.nameEN)}}
          {{productOrder.arrivalTerminal}}
        </f7-col>
      </f7-row>
    </div>
    <div class="timeline-item-text">
      <f7-row>
        <f7-col class="text-align-center">
          {{productOrder.planeType && $i18nMsg(productOrder.planeType.nameCN, productOrder.planeType.nameEN)}}
        </f7-col>
      </f7-row>
    </div>
  </div>
</template>

<script>
import ProductsIcons from '../../../components/products/products-icons'

export default {
  name: 'timeline-domair',
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
