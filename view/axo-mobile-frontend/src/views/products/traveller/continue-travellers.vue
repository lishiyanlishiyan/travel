<template>
  <div>
    <f7-list media-list>
      <f7-list-item
        :key="traveller.uuid"
        link
        v-for="traveller in basicSearchParam.bookTravellers"
        :subtitle="traveller.userEmail">
        <div class="item-title" slot="title">
          {{$i18nMsg(traveller.userNameCN, traveller.userNameEN)}}
          <f7-chip v-if="traveller.tempUserId" :text="$t('profile.label.guest')" color="blue"/>
        </div>
      </f7-list-item>
    </f7-list>
    <f7-list v-if="basicSearchParam.policyUser">
      <f7-list-item :title="$t('common.label.policyTraveller')">
        {{$i18nMsg(basicSearchParam.policyUser.userNameCN, basicSearchParam.policyUser.userNameEN)}}
      </f7-list-item>
    </f7-list>
  </div>
</template>

<script>
export default {
  name: 'continue-travellers',
  props: {
    value: {
      type: Object
    }
  },
  data () {
    const { value } = this.$props
    return {
      basicSearchParam: value || {}
    }
  },
  watch: {
    value: function (value) {
      this.basicSearchParam = value || {}
    }
  }
}
</script>

<style scoped>

</style>
