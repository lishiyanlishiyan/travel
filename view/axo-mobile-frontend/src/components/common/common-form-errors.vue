<template>
  <f7-list-item v-if="error" :class="className">
    <f7-icon slot="media" :class="className" f7="alert_fill"/>
    {{error}}
  </f7-list-item>
</template>

<script>

import { isArray, isString } from 'lodash/lang'

export default {
  name: 'common-form-errors',
  props: {
    value: {
      type: [String, Array]
    },
    className: {
      type: String,
      default: 'text-color-orange'
    }
  },
  watch: {
    value: function (value) {
      this.error = this.calcError(value)
    }
  },
  data () {
    const { value } = this.$props
    return {
      error: this.calcError(value)
    }
  },
  methods: {
    calcError (errors) {
      if (isArray(errors)) {
        return errors[0]
      } else if (isString(errors)) {
        return errors
      }
    }
  }
}
</script>

<style scoped>

</style>
