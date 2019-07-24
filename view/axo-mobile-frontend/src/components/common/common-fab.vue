<template>
  <f7-fab :position="position" slot="fixed">
    <f7-icon f7="menu"></f7-icon>
    <f7-icon f7="close"></f7-icon>
    <f7-fab-buttons position="top">
      <f7-fab-button :key="index" v-for="(button, index) in configButtons"
                     fab-close
                     :label="button.label" @click="button.click">
        <f7-icon v-if="button.f7||button.material"
                 :f7="button.f7" :material="button.material"/>
      </f7-fab-button>
      <f7-fab-button v-if="home" fab-close :label="$t('common.label.index')" @click="backHomePage">
        <f7-icon f7="home_fill"/>
      </f7-fab-button>
    </f7-fab-buttons>
  </f7-fab>
</template>

<script>
export default {
  name: 'common-home',
  props: {
    home: {
      type: Boolean,
      default: true
    },
    buttons: {
      type: Array
    },
    position: {
      type: String,
      default: 'right-bottom'
    }
  },
  data () {
    const { buttons } = this.$props
    return {
      configButtons: buttons || []
    }
  },
  watch: {
    buttons: function (buttons) {
      this.configButtons = buttons || []
    }
  },
  methods: {
    backHomePage ($event) {
      this.$backHomePage()
      $event.preventDefault()
    }
  }
}
</script>

<style scoped>

</style>
