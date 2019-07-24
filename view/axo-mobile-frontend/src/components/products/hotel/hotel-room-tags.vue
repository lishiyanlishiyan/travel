<template>
  <f7-row>
    <f7-col>
      <f7-chip v-if="hotelRoom.corporate" :text="$t('common.label.corp')" color="blue"/>
      <f7-chip color="blue" v-if="isInstantConfirm(hotelRoom.interfaceId)" :text="$t('hotel.label.confirmImmediately')"></f7-chip>
      <f7-chip color="purple" v-if="hotelRoom.needGuarantee"
               :text="hotelRoom.interfaceId===8?$t('hotel.label.conditionalGuarantee'):$t('hotel.label.guarantee')"/>
      <f7-chip color="purple" v-if="hotelRoom.prepay" :text="$t('hotel.label.prepay')"></f7-chip>
      <f7-chip color="purple" v-if="!hotelRoom.prepay" :text="$t('hotel.label.selfpay')"></f7-chip>
      <f7-chip color="red" v-if="!hotelRoom.changeable" :text="$t('hotel.label.nonCancelable')"></f7-chip>
    </f7-col>
  </f7-row>
</template>

<script>
export default {
  name: 'hotel-room-tags',
  props: {
    value: { type: Object }
  },
  data () {
    const { value } = this.$props
    return {
      hotelRoom: value
    }
  },
  methods: {
    isInstantConfirm (interfaceId) {
      return interfaceId === 3 || interfaceId === 8 || interfaceId === 20 // GDS, Elong, 华住酒店时立即确认的
    }
  }
}
</script>

<style scoped>

</style>
