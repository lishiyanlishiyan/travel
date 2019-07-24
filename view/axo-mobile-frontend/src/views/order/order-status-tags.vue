<template>
  <span>
    <f7-chip v-if="order.statusCode==='1'" :text="$i18nMsg(order.statusCN, order.statusEN)" color="blue"/>
    <f7-chip v-if="order.statusCode==='2' || order.statusCode==='3'"
             :text="$i18nMsg(order.statusCN, order.statusEN)" color="yellow"/>
    <f7-chip v-if="order.statusCode==='4'" :text="$i18nMsg(order.statusCN, order.statusEN)" color="green"/>
    <f7-chip v-if="order.statusCode==='5'" :text="$i18nMsg(order.statusCN, order.statusEN)" color="red"/>
    <f7-chip v-if="order.statusCode==='6'" :text="$i18nMsg(order.statusCN, order.statusEN)" color="gray"/>
  </span>
</template>

<script>
export default {
  name: 'order-status-tags',
  props: {
    order: {
      type: Object
    }
  }
}
</script>

<style scoped>

</style>
