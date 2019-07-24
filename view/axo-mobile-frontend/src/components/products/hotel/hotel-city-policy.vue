<template>
  <f7-list accordion-list v-if="value">
    <f7-list-item accordion-item class="text-color-red">
      <div slot="title">
        <strong>{{$t('hotel.label.cityPolicyTitle')}}</strong>
      </div>
      <f7-accordion-content>
        <f7-block class="text-color-purple margin-bottom" v-html="value"/>
      </f7-accordion-content>
    </f7-list-item>
  </f7-list>
</template>

<script>
export default {
  name: 'hotel-city-policy',
  props: {
    value: {
      type: String
    }
  }
}
</script>

<style scoped>

</style>
