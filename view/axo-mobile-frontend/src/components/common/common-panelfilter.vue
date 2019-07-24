<template>
  <f7-panel :side="side" @panelClose="panelClose" :opened="open" ref="el">
    <f7-view cover :router="false">
      <f7-page>
        <f7-navbar>
          <f7-nav-left>
            <f7-link ref="closeButton" panel-close>{{closeLinkText}}</f7-link>
          </f7-nav-left>
          <f7-nav-title :title="title"/>
          <f7-nav-right>
            <f7-link @click="clearSelectItems()">{{clearLinkText}}</f7-link>
          </f7-nav-right>
        </f7-navbar>
        <div ref="itemsEl" class="select-item-values" :key="index"
             v-for="(conditionItem, index) in calcConditionItems">
          <div class="block-title margin-vertical">{{conditionItem.label}}</div>
          <div class="list">
            <ul class="padding">
              <li class="margin-bottom" v-for="(groupItems,idx) in conditionItem.calcItems" :key="idx">
                <div class="row left">
                  <label :class="colClass" v-for="item in groupItems" :key="item.id">
                    <f7-button @click="selectItem(item, conditionItem)" class="select-data-item" :fill="item.selected">
                      {{item.label}}
                    </f7-button>
                  </label>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </f7-page>
    </f7-view>
  </f7-panel>
</template>

<script>
import { groupByCount } from './utils/common-utils'
import clone from 'lodash/clone'

export default {
  model: {
    event: 'change'
  },
  name: 'common-panelfilter',
  props: {
    title: {
      type: String,
      default: 'Filter'
    },
    closeLinkText: {
      type: String,
      default: 'Close'
    },
    clearLinkText: {
      type: String,
      default: 'Clear'
    },
    closeOnSelect: {
      type: Boolean,
      default: false
    },
    value: {
      type: Array
    },
    colCount: {
      type: Number,
      default: 2
    },
    side: {
      type: String,
      default: 'right'
    },
    open: {
      type: Boolean,
      default: false
    }
  },
  data () {
    const { value, colCount } = this.$props
    const colClass = 'col-' + parseInt(100 / colCount)
    return {
      colClass,
      conditionItems: value,
      calcConditionItems: []
    }
  },
  watch: {
    '$props.value': function (v) {
      console.info('change........', v)
      this.conditionItems = v
      this.reCalcConditionItems()
    },
    '$props.open': function (v) {
      console.info('open.........', v)
      // this.opened = v
    }
  },
  mounted () {

  },
  computed: {},
  methods: {
    panelClose: function () {
      this.$emit('onClose', false)
    },
    reCalcConditionItems () {
      let calcGroupItems = []
      console.info(this.conditionItems)
      if (this.conditionItems) {
        calcGroupItems = this.conditionItems.map(conditionItem => {
          const calcConditionItem = clone(conditionItem)
          console.info(calcConditionItem)
          calcConditionItem.calcItems = groupByCount(conditionItem.items, this.colCount)
          return calcConditionItem
        })
      }
      console.info('calc........', this.conditionItems, calcGroupItems)
      this.calcConditionItems = calcGroupItems
      this.$emit('change', this.conditionItems)
    },
    clearSelectItems () {
      if (this.conditionItems) {
        this.conditionItems.forEach(conditionItem => {
          if (conditionItem.items) {
            conditionItem.items.forEach(item => {
              item.selected = false
            })
          }
        })
        this.$emit('change', this.conditionItems)
      }
    },
    selectItem (item, conditionItem) {
      if (conditionItem.singleSelect && conditionItem.items) {
        conditionItem.items.forEach(_item => {
          _item.selected = false
        })
      }
      item.selected = !item.selected
      console.info('selected......', this.conditionItems)
      this.$emit('change', this.conditionItems)
      const { closeOnSelect, side } = this.$props
      if (closeOnSelect) {
        this.$f7.panel.close(side)
      }
    }
  }
}
</script>

<style scoped>

</style>
