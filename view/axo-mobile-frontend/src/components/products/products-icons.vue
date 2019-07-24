<template>
  <f7-icon>
    <f7-icon size="25" v-if="domAir" material="flight" :color="color"/>
    <f7-icon size="25" v-if="intlAir" material="public"  :color="color"/>
    <f7-icon size="25" v-if="hotel" material="hotel" :color="color"/>
    <f7-icon size="25" v-if="train" material="train" :color="color"/>
    <f7-icon size="25" v-if="other" f7="document_fill" :color="color"/>
    <f7-icon size="25" v-if="carRental" material="local_taxi" :color="color"/>
    <f7-icon size="25" v-if="offline" material="headset" :color="color"/>
  </f7-icon>
</template>

<script>
export default {
  name: 'products-icons',
  props: {
    color: {
      type: String,
      default: 'blue'
    },
    domAir: {
      type: Boolean
    },
    intlAir: {
      type: Boolean
    },
    hotel: {
      type: Boolean
    },
    train: {
      type: Boolean
    },
    other: {
      type: Boolean
    },
    carRental: {
      type: Boolean
    },
    offline: {
      type: Boolean
    }
  }
}
</script>

<style scoped>

</style>
