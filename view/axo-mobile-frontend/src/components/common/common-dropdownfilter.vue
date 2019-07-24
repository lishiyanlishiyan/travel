<template>
  <div class="common-dropdown">
    <a @click="toggleDropdownMenu($event)" class="dropdown-trigger" ref="dropdownTrigger"
       :class="{'has-selected-items': config.hasSelected, present: config.isOpened}">
      <slot name="trigger"/>
    </a>
    <div ref="dropdownMenu" class="autocomplete-dropdown display-none tab-dropdown">
      <div class="autocomplete-dropdown-inner">
        <template v-if="simpleMode">
          <div ref="itemsEl" class="select-item-values" :key="index"
               v-for="(conditionItem, index) in calcConditionItems">
            <div class="block-title" v-if="conditionItem.label">
              {{conditionItem.label}}
              <f7-icon class="text-color-orange" v-if="conditionItem.hasSelected"/>
            </div>
            <div class="list" style="padding: 0 10px;" :class="{'margin-vertical': !conditionItem.label}">
              <ul class="">
                <li class="margin-bottom" v-for="(groupItems,idx) in conditionItem.calcItems" :key="idx">
                  <div class="row">
                    <label :class="colClass" v-for="item in groupItems" :key="item[valueProperty]">
                      <f7-button v-if="item.isUnlimited" raised outline
                                 @click="clearCurrentSelectItems(conditionItem)"
                                 class="select-data-item button-filter"
                                 :fill="!conditionItem.hasSelected">
                        {{$t('common.label.unlimited')}}
                      </f7-button>
                      <f7-button v-if="!item.isUnlimited" raised outline
                                 @click="selectItem(item, conditionItem, $event)"
                                 class="select-data-item button-filter"
                                 :fill="item.selected">
                        {{item[textProperty]}}
                      </f7-button>
                    </label>
                    <template v-if="groupItems.length%colCount!=0">
                      <div v-for="n in colCount-groupItems.length%colCount" :key="n" :class="colClass"></div>
                    </template>
                  </div>
                </li>
              </ul>
            </div>
          </div>
        </template>
        <template v-if="!simpleMode">
          <f7-row ref="itemsEl" class="no-gap">
            <f7-col width="25" class="menu" style="background: #eee;">
              <f7-row :key="index" v-for="(conditionItem, index) in calcConditionItems">
                <f7-col>
                  <f7-link :class="{'active': conditionItem._active}"
                           @click="changeConditionActive(calcConditionItems, conditionItem)">
                    {{conditionItem.label}}
                    <f7-icon class="text-color-orange" v-if="conditionItem.hasSelected"/>
                  </f7-link>
                </f7-col>
              </f7-row>
            </f7-col>
            <f7-col width="75" v-show="conditionItem._active" :key="index"
                    v-for="(conditionItem, index) in calcConditionItems" class="menu">
              <f7-row v-if="conditionItem.hasSubItems">
                <f7-col width="35" style="background: #f6f6f6;">
                  <f7-row :key="index" v-for="(item, index) in conditionItem.items">
                    <f7-col>
                      <f7-link :class="{'active': item._active}"
                               @click="changeConditionActive(conditionItem.items, item)">
                        {{item.label}}
                        <f7-icon class="text-color-orange" v-if="item.hasSelected"/>
                      </f7-link>
                    </f7-col>
                  </f7-row>
                </f7-col>
                <f7-col width="65" v-show="item._active" :key="index"
                        v-for="(item, index) in conditionItem.items">
                  <f7-row v-if="addUnlimitedOption">
                    <f7-col>
                      <label>
                        <f7-radio
                          class="select-data-item"
                          :checked="!item.hasSelected"
                          @change="clearCurrentSelectItems(item)"
                        />
                        {{$t('common.label.unlimited')}}
                      </label>
                    </f7-col>
                  </f7-row>
                  <f7-row v-for="(subItem, idx) in item.items" :key="idx">
                    <f7-col>
                      <label>
                        <f7-checkbox v-if="!conditionItem.singleSelect" class="select-data-item"
                                     :checked="subItem.selected"
                                     @change="selectItem(subItem, conditionItem, $event)"></f7-checkbox>
                        <f7-radio
                          v-if="conditionItem.singleSelect"
                          class="select-data-item"
                          :checked="subItem.selected"
                          @change="selectItem(subItem, conditionItem, $event)"
                        />
                        {{subItem[textProperty]}}
                      </label>
                    </f7-col>
                  </f7-row>
                </f7-col>
              </f7-row>
              <f7-row v-if="addUnlimitedOption && !conditionItem.hasSubItems">
                <f7-col>
                  <label>
                    <f7-radio
                      class="select-data-item"
                      :checked="!conditionItem.hasSelected"
                      @change="clearCurrentSelectItems(conditionItem)"
                    />
                    {{$t('common.label.unlimited')}}
                  </label>
                </f7-col>
              </f7-row>
              <!--eslint-disable-next-line-->
              <f7-row v-if="!conditionItem.hasSubItems" v-for="(item,idx) in conditionItem.items" :key="idx">
                <f7-col>
                  <label>
                    <f7-checkbox v-if="!conditionItem.singleSelect" class="select-data-item"
                                 :checked="item.selected"
                                 @change="selectItem(item, conditionItem, $event)"></f7-checkbox>
                    <f7-radio
                      v-if="conditionItem.singleSelect"
                      class="select-data-item"
                      :checked="item.selected"
                      @change="selectItem(item, conditionItem, $event)"
                    />
                    {{item[textProperty]}}
                  </label>
                </f7-col>
              </f7-row>
            </f7-col>
          </f7-row>
        </template>
      </div>
      <div style="padding: 10px 10px 5px;border-top: 1px solid #ddd;" v-if="!closeOnClick">
        <f7-row>
          <f7-col>
            <f7-button @click="clearSelectItems" fill color="gray">{{clearLinkText}}</f7-button>
          </f7-col>
          <f7-col>
            <f7-button @click="confirmClick($event)" fill>{{closeLinkText}}</f7-button>
          </f7-col>
        </f7-row>
      </div>
    </div>
    <div ref="dropdownBackdrop" class="popover-backdrop dropdown-backdrop"/>
  </div>
</template>

<script>
import { Positioning } from 'positioning'
import clone from 'lodash/clone'
import cloneDeep from 'lodash/cloneDeep'
import numeral from 'numeral'
import { calcConditionItemSelected, clearConditionItemsSelected, groupByCount } from './utils/common-utils'

export default {
  name: 'common-dropdown',
  model: {
    event: 'change'
  },
  props: {
    showBackdrop: {
      type: Boolean,
      default: true
    },
    open: {
      type: Boolean,
      default: false
    },
    closeLinkText: {
      type: String,
      default: 'Confirm'
    },
    clearLinkText: {
      type: String,
      default: 'Clear'
    },
    closeOnClick: {
      type: Boolean,
      default: false
    },
    placement: {
      type: String,
      default: 'bottom-auto'
    },
    value: {
      type: Array
    },
    colCount: {
      type: Number,
      default: 3
    },
    valueProperty: {
      type: String,
      default: 'id'
    },
    textProperty: {
      type: String,
      default: 'label'
    },
    backdropContainerEl: {
      type: String,
      default: '.view-main .page-current'
    },
    triggerEl: {
      type: String
    },
    animate: {
      type: Boolean,
      default: true
    },
    simpleMode: {
      type: Boolean,
      default: false
    },
    addUnlimitedOption: {
      type: Boolean,
      default: true
    }
  },
  data () {
    const { value, colCount } = this.$props
    const colClass = 'col-' + parseInt(100 / colCount)
    return {
      colClass,
      conditionItems: cloneDeep(value),
      calcConditionItems: [],
      initPosition: null,
      endPosition: null,
      config: { hiding: false, opening: false, hasSelected: false, initHasSelected: false, isOpened: false }
    }
  },
  watch: {
    '$props.open': function watchValue (v) {
      if (v === true) {
        this.showDropdownMenu()
      } else {
        this.hideDropdownMenu()
      }
    },
    '$props.value': function (v) {
      this.conditionItems = cloneDeep(v)
      this.reCalcConditionItems(true)
    }
  },
  mounted () {
    const { open } = this.$props
    if (open) {
      this.showDropdownMenu()
    }
    this.reCalcConditionItems(true)
  },
  methods: {
    attachEvents () {
      const { closeOnClick } = this.$props
      this.$$('body').on('click', '.popover-backdrop', this.hideDropdownMenu)
      this.$$('body').on('click', '.dropdown-trigger', this.checkBeforeHide)
      if (closeOnClick) {
        this.$$(this.$refs.dropdownMenu).on('click', this.hideDropdownMenu)
      }
    },
    detachEvents () {
      this.$$('body').off('click', '.popover-backdrop', this.hideDropdownMenu)
      this.$$('body').off('click', '.dropdown-trigger', this.checkBeforeHide)
      this.$$(this.$refs.dropdownMenu).off('click', this.hideDropdownMenu)
    },
    openDropdownMenu () {
      const { value } = this.$props
      this.conditionItems = cloneDeep(value)
      this.reCalcConditionItems()
      this.$nextTick(() => {
        this.showDropdownMenu()
      })
    },
    calcDropdownPosition () {
      const { triggerEl, placement } = this.$props
      const positioning = new Positioning()
      let trigger = this.$refs.dropdownTrigger
      if (triggerEl && this.$$(triggerEl).length > 0) {
        trigger = this.$$(triggerEl)[0]
      }
      const position = positioning.positionElements(trigger, this.$refs.dropdownMenu, placement, true)
      return position
    },
    showDropdownMenu () {
      const { showBackdrop, backdropContainerEl, placement, animate } = this.$props
      const $dropdown = this.$$(this.$refs.dropdownMenu)
      const $dropdownBackdrop = this.$$(this.$refs.dropdownBackdrop)
      if (this.config.opening || !$dropdown.is('.display-none') || this.$store.getters.dropDownOpening) {
        return
      }
      this.config.opening = true
      this.config.isOpened = true
      this.$store.dispatch('storeDropDownOpening', true)
      const isTop = placement.split('-')[0] === 'top'
      const position = this.calcDropdownPosition()
      let top = position.top
      const height = $dropdown.appendTo(backdropContainerEl).css('z-index', 499).removeClass('display-none').height()
      $dropdown.addClass('display-none')
      if (isTop) {
        top = top - height
      }
      this.initPosition = {
        top: numeral(isTop ? (top + height) : (top - height)).format('0.00')
      }
      this.endPosition = {
        top: top
      }
      $dropdown.css({
        top: this.initPosition.top + 'px',
        left: position.left + 'px'
      }).removeClass('display-none')
      if (showBackdrop) {
        $dropdownBackdrop.appendTo(backdropContainerEl).css('z-index', 495).addClass('backdrop-in')
      }
      if (animate) {
        const self = this
        $dropdown.animate({
          top: this.endPosition.top + 'px'
        }, {
          complete () {
            self.onOpenDropdownMenu()
          }
        })
      } else {
        $dropdown.css({
          top: this.endPosition.top + 'px'
        })
        this.onOpenDropdownMenu()
      }
    },
    toggleDropdownMenu ($event) {
      const $dropdown = this.$$(this.$refs.dropdownMenu)
      if ($dropdown.is('.display-none')) {
        this.openDropdownMenu($event)
      } else {
        this.hideDropdownMenu($event)
      }
    },
    checkBeforeHide ($event) {
      if (!this.$refs.dropdownTrigger.contains($event.target)) {
        this.hideDropdownMenu($event)
      }
    },
    hideDropdownMenu ($event, change) {
      const self = this
      const { animate } = this.$props
      const $dropdown = this.$$(this.$refs.dropdownMenu)
      if (this.config.hiding || $dropdown.is('.display-none')) {
        return
      }
      this.config.hiding = true
      this.config.isOpened = false
      this.config.hasSelected = !change ? this.config.initHasSelected : this.config.hasSelected
      if (animate) {
        $dropdown.animate({
          top: this.initPosition ? this.initPosition.top : null
        }, {
          complete () {
            self.onCloseDropdownMenu()
          }
        })
      } else {
        self.onCloseDropdownMenu()
      }
    },
    onCloseDropdownMenu () {
      this.config.hiding = false
      const $dropdownBackdrop = this.$$(this.$refs.dropdownBackdrop)
      const $dropdown = this.$$(this.$refs.dropdownMenu)
      $dropdown.addClass('display-none')
      $dropdownBackdrop.removeClass('backdrop-in')
      this.detachEvents()
      this.$emit('onClose')
    },
    onOpenDropdownMenu () {
      this.$store.dispatch('storeDropDownOpening', false)
      this.config.opening = false
      this.attachEvents()
      this.$emit('onOpen')
    },
    reCalcConditionItems (init) {
      const { simpleMode, addUnlimitedOption } = this.$props
      let calcGroupItems = []
      if (this.conditionItems) {
        calcGroupItems = this.conditionItems.map((conditionItem, index) => {
          const calcConditionItem = clone(conditionItem)
          if (simpleMode) {
            let items = addUnlimitedOption ? [{
              isUnlimited: true
            }] : []
            items = items.concat(conditionItem.items)
            calcConditionItem.calcItems = groupByCount(items, this.colCount)
          }
          calcConditionItem._active = index === 0
          calcConditionItem.hasSubItems = !!(calcConditionItem.items[0] && calcConditionItem.items[0].items)
          if (calcConditionItem.hasSubItems) {
            calcConditionItem.items.forEach((item, idx) => {
              item._active = idx === 0
            })
          }
          return calcConditionItem
        })
      }
      calcGroupItems = simpleMode ? calcGroupItems.filter(groupItem => !groupItem.hasSubItems) : calcGroupItems
      this.calcConditionItems = calcGroupItems
      this.reCalcConditionItemsSelected()
      this.config.initHasSelected = init ? this.config.hasSelected : this.config.initHasSelected
    },
    clearSelectItems () {
      clearConditionItemsSelected(this.calcConditionItems)
      this.reCalcConditionItemsSelected()
      this.onChange()
    },
    clearCurrentSelectItems (conditionItem) {
      conditionItem.items.forEach(item => {
        if (!item.isUnlimited && item.hasOwnProperty('selected')) {
          item.selected = false
        }
      })
      this.reCalcConditionItemsSelected()
      this.onChange()
    },
    reCalcConditionItemsSelected () {
      let hasSelected = false
      this.calcConditionItems.forEach(_item => {
        calcConditionItemSelected(_item)
        if (_item.hasSelected) {
          hasSelected = true
        }
      })
      this.config.hasSelected = hasSelected
    },
    confirmClick ($event) {
      this.onChange(true)
      this.hideDropdownMenu($event, true)
    },
    selectItem (item, conditionItem, $event) {
      if (conditionItem.singleSelect && conditionItem.items) {
        conditionItem.items.forEach(_item => {
          if (_item !== item) {
            _item.selected = false
          }
        })
      }
      item.selected = !item.selected
      this.reCalcConditionItemsSelected()
      const { closeOnClick } = this.$props
      this.onChange()
      if (closeOnClick) {
        this.hideDropdownMenu($event)
      }
    },
    changeConditionActive (conditionItems, conditionItem) {
      conditionItems.forEach(item => {
        item._active = false
      })
      conditionItem._active = true
      this.$forceUpdate()
      this.$nextTick(() => {
        const position = this.calcDropdownPosition()
        const $dropdown = this.$$(this.$refs.dropdownMenu)
        $dropdown.css({
          top: position.top + 'px'
        })
      })
    },
    onChange (force) {
      const { closeOnClick } = this.$props
      if (closeOnClick || force) {
        this.$emit('change', this.conditionItems)
      }
    }
  },
  beforeDestroy () {
    this.detachEvents()
  }
}
</script>

<style scoped>

</style>
