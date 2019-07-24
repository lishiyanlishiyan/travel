<template>
  <f7-list media-list class="policyReason" style="margin: 0;">
    <f7-list-item :key="resultHandler.id" v-for="(resultHandler) in resultHandlers"
                  class="normalWeight"
                  smart-select
                  :smart-select-params="{openIn: 'popup', closeOnSelect: closeOnSelect, leftRadio: true, overflowEnable: true, renderItemRemark: reasonCodeRemark(resultHandler)}">
      <div slot="title" :class="resultHandler.selectedReasonCode?'text-color-blue':'text-color-red'">
        {{$i18nMsg(resultHandler.infoCN, resultHandler.infoEN)}}
      </div>
      <select :key="resultHandler.id"
              @change="selectReasonCode(resultHandler)"
              v-model="resultHandler.selectedReasonCode"
              name="reasonCode">
        <option :key="index" v-for="(reasonCode, index) in resultHandler.reasonCodes" :value="reasonCode.code">
          {{$i18nMsg(reasonCode.infoCN, reasonCode.infoEN)}}
        </option>
      </select>
      <div slot="after">
        <f7-icon v-if="!resultHandler.selectedReasonDto" f7="alert_fill" class="iconAlert text-color-orange"></f7-icon>
        <div v-if="resultHandler.selectedReasonDto">
           {{$i18nMsg(resultHandler.selectedReasonDto.infoCN, resultHandler.selectedReasonDto.infoEN)}}
        </div>
      </div>
    </f7-list-item>
  </f7-list>
</template>

<script>
import find from 'lodash/find'

export default {
  name: 'policy-reason',
  model: {
    event: 'change'
  },
  props: {
    value: {
      type: Array
    },
    autoSelected: { // 单个自动选中
      type: Boolean,
      default: false
    },
    closeOnSelect: {
      type: Boolean,
      default: false
    }
  },
  data () {
    const { value } = this.$props
    return {
      resultHandlers: this.initResultHandlers(value || [])
    }
  },
  watch: {
    value (v) {
      this.resultHandlers = this.initResultHandlers(v || [])
    }
  },
  mounted () {
  },
  methods: {
    findSelectedReason (resultHandler) {
      let selectedReason = null
      if (resultHandler.selectedReasonCode) {
        selectedReason = find(resultHandler.reasonCodes, { code: resultHandler.selectedReasonCode })
      }
      return selectedReason
    },
    selectReasonCode (resultHandler) {
      let selectedReason = this.findSelectedReason(resultHandler)
      resultHandler.selectedReasonDto = selectedReason
      resultHandler.selectedReason = this.calcReason(resultHandler, selectedReason)
      this.$forceUpdate()
      this.$emit('change', this.resultHandlers)
    },
    initResultHandlers (handlers) {
      console.info('init............')
      const { autoSelected } = this.$props
      let hasSelected = false
      handlers.forEach(resultHandler => {
        const reasonCode = resultHandler.reasonCodes[0]
        let selected = autoSelected && resultHandler.reasonCodes.length === 1
        resultHandler.selectedReasonCode = selected ? reasonCode.code : null
        resultHandler.selectedReason = selected ? this.calcReason(resultHandler, reasonCode) : null
        if (selected) {
          hasSelected = true
        }
      })
      if (hasSelected) {
        this.$emit('change', handlers)
      }
      return handlers
    },
    calcReason (resultHandler, reasonCode) {
      return resultHandler && reasonCode ? `${resultHandler.id}#${reasonCode.id}` : null
    },
    reasonCodeRemark (resultHandler) {
      return () => {
        if (resultHandler.remarkCN || resultHandler.remarkEN) {
          return `<ul class="no-padding-left">
              <li class="list-group-title">${this.$i18nBundle('common.label.remark')}</li>
              <li>
                  <div class="item-content padding-right">${this.$i18nMsg(resultHandler.remarkCN, resultHandler.remarkEN)}</div>
              </li>
          </ul>`
        }
        return ''
      }
    }
  }
}
</script>

<style scoped>

</style>
