<template>
  <div class="common-dropdown">
    <a @click="toggleDropdownMenu()" class="dropdown-trigger" ref="dropdownTrigger">
      <slot name="trigger"/>
    </a>
    <div ref="dropdownMenu" class="autocomplete-dropdown display-none">
      <div class="autocomplete-dropdown-inner">
        <slot/>
      </div>
    </div>
    <div ref="dropdownBackdrop" class="popover-backdrop dropdown-backdrop"/>
  </div>
</template>

<script>
import { Positioning } from 'positioning'
import numeral from 'numeral'

export default {
  name: 'common-dropdown',
  props: {
    showBackdrop: {
      type: Boolean,
      default: true
    },
    open: {
      type: Boolean,
      default: false
    },
    closeOnClick: {
      type: Boolean,
      default: true
    },
    placement: {
      type: String,
      default: 'bottom-auto'
    },
    animate: {
      type: Boolean,
      default: true
    },
    backdropContainerEl: {
      type: String,
      default: '.view-main .page-current'
    }
  },
  data () {
    return {
      initPosition: null,
      endPosition: null,
      config: { hiding: false, opening: false }
    }
  },
  watch: {
    '$props.open': function watchValue (v) {
      if (v === true) {
        this.showDropdownMenu()
      } else {
        this.hideDropdownMenu()
      }
    }
  },
  mounted () {
    const { open } = this.$props
    if (open) {
      this.showDropdownMenu()
    }
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
    showDropdownMenu () {
      const { showBackdrop, placement, animate, backdropContainerEl } = this.$props
      const $link = this.$$(this.$refs.dropdownTrigger)
      const $dropdown = this.$$(this.$refs.dropdownMenu)
      const $dropdownBackdrop = this.$$(this.$refs.dropdownBackdrop)
      if (this.config.opening || !$dropdown.is('.display-none') || this.$store.getters.dropDownOpening) {
        return
      }
      this.config.opening = true
      this.$store.dispatch('storeDropDownOpening', true)
      const positioning = new Positioning()
      const isTop = placement.split('-')[0] === 'top'
      const position = positioning.positionElements(this.$refs.dropdownTrigger, this.$refs.dropdownMenu, placement, true)
      const container = backdropContainerEl
      let top = position.top
      const height = $dropdown.appendTo(container).css('z-index', 499).removeClass('display-none').height() // 隐藏元素不能计算高度，需要先显示出来
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
      $link.addClass('present')
      if (showBackdrop) {
        $dropdownBackdrop.appendTo(container).css('z-index', 495).addClass('backdrop-in')
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
    toggleDropdownMenu () {
      const $dropdown = this.$$(this.$refs.dropdownMenu)
      if ($dropdown.is('.display-none')) {
        this.showDropdownMenu()
      } else {
        this.hideDropdownMenu()
      }
    },
    checkBeforeHide ($event) {
      if (!this.$refs.dropdownTrigger.contains($event.target)) {
        this.hideDropdownMenu($event)
      }
    },
    hideDropdownMenu () {
      const { animate } = this.$props
      const self = this
      const $link = this.$$(this.$refs.dropdownTrigger)
      const $dropdown = this.$$(this.$refs.dropdownMenu)
      $link.removeClass('present')
      if (self.config.hiding || $dropdown.is('.display-none')) {
        return
      }
      this.config.hiding = true
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
    onOpenDropdownMenu () {
      this.$store.dispatch('storeDropDownOpening', false)
      this.config.opening = false
      this.attachEvents()
      this.$emit('onOpen')
    },
    onCloseDropdownMenu () {
      this.config.hiding = false
      const $dropdownBackdrop = this.$$(this.$refs.dropdownBackdrop)
      const $dropdown = this.$$(this.$refs.dropdownMenu)
      $dropdown.addClass('display-none')
      $dropdownBackdrop.removeClass('backdrop-in')
      this.detachEvents()
      this.$emit('onClose')
    }
  },
  beforeDestroy () {
    this.detachEvents()
  }
}
</script>

<style scoped>

</style>
