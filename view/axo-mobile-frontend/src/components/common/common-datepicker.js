/**
 * 参照list-input.js开发datepicker vue版
 * @author gary.fu
 */
import Utils from 'framework7-vue/utils/utils'
import Mixins from 'framework7-vue/utils/mixins'
import __vueComponentDispatchEvent from 'framework7-vue/runtime-helpers/vue-component-dispatch-event'
import __vueComponentProps from 'framework7-vue/runtime-helpers/vue-component-props'
import __vueComponentSetState from 'framework7-vue/runtime-helpers/vue-component-set-state'
import moment from 'moment'
import { calcDefaultMinDateStartDay } from './utils/common-utils'
import { isFunction } from 'lodash/lang'

export default {
  name: 'common-datepicker',
  model: {
    event: 'change'
  },
  props: Object.assign({
    id: [String, Number],
    media: String,
    tag: {
      type: String,
      default: 'div'
    },
    name: String,
    value: [String, Number, Array],
    defaultValue: [String, Number, Array],
    readonly: {
      type: Boolean,
      default: true
    },
    required: Boolean,
    disabled: Boolean,
    placeholder: String,
    inputId: [String, Number],
    size: [String, Number],
    accept: [String, Number],
    autocomplete: [String],
    autosave: String,
    multiple: Boolean,
    inputStyle: [String, Object],
    inputClassName: String,
    pattern: String,
    validate: [Boolean, String],
    tabindex: [String, Number],
    clearButton: Boolean,
    noFormStoreData: Boolean,
    noStoreData: Boolean,
    ignoreStoreData: Boolean,
    errorMessage: String,
    errorMessageForce: Boolean,
    info: String,
    label: [String, Number],
    inlineLabel: Boolean,
    floatingLabel: Boolean,
    // F7 Calendar/Datepicker配置参数
    calendarType: String,
    monthNames: Array,
    monthNamesShort: Array,
    dayNames: Array,
    dayNamesShort: Array,
    firstDay: { type: Number, default: 0 },
    weekendDays: Array,
    dateFormat: String,
    rangePicker: Boolean,
    rangePickerMinDays: Number,
    rangePickerMaxDays: Number,
    direction: String,
    routableModals: {
      type: Boolean,
      default: false
    },
    events: Array,
    toolbarCloseText: String,
    toolbarClearText: String,
    showClose: { type: Boolean, default: true },
    showClear: { type: Boolean, default: false },
    maxDate: [String, Date],
    minDate: [String, Date],
    disabledDates: Array,
    rangesClasses: Array,
    formatValue: Function,
    touchMove: { type: Boolean, default: true },
    closeOnSelect: { type: Boolean, default: true },
    weekHeader: { type: Boolean, default: true },
    monthSelector: { type: Boolean, default: true },
    yearSelector: { type: Boolean, default: true },
    openIn: String,
    scrollToInput: { type: Boolean, default: true },
    inputReadOnly: { type: Boolean, default: true },
    cssClass: String,
    closeByOutsideClick: { type: Boolean, default: true },
    toolbar: {
      type: Boolean,
      default: true
    },
    renderWeekHeader: Function,
    renderMonths: Function,
    renderMonth: Function,
    renderMonthSelector: Function,
    renderYearSelector: Function,
    renderHeader: Function,
    renderToolbar: Function,
    render: Function,
    on: Object,
    header: {
      type: Boolean,
      default: false
    },
    headerPlaceholder: String,
    defaultMinDateIncluded: { type: Boolean, default: false },
    defaultMinDateStart: { type: Number },
    open: {
      type: Boolean,
      default: false
    }
  }, Mixins.colorProps),

  data () {
    const props = __vueComponentProps(this)

    const state = (() => {
      const {
        value,
        defaultValue
      } = props
      return {
        inputFocused: false,
        inputInvalid: false,
        currentInputValue: typeof value === 'undefined' ? defaultValue : value
      }
    })()

    return {
      state
    }
  },

  render () {
    const _h = this.$createElement
    const self = this
    const {
      inputFocused,
      inputInvalid
    } = self.state
    const props = self.props
    const {
      id,
      style,
      className,
      media,
      tag,
      type,
      name,
      value,
      readonly,
      required,
      disabled,
      placeholder,
      inputId,
      size,
      accept,
      autocomplete,
      autosave,
      inputStyle,
      inputClassName,
      pattern,
      validate,
      tabindex,
      clearButton,
      noFormStoreData,
      noStoreData,
      ignoreStoreData,
      errorMessage,
      errorMessageForce,
      info,
      label,
      inlineLabel,
      floatingLabel
    } = props
    const createInput = () => {
      const needsValue = true
      const calcInputClassName = Utils.classNames(inputClassName || '', {
        'no-store-data': noFormStoreData || noStoreData || ignoreStoreData,
        'input-invalid': (errorMessage && errorMessageForce) || inputInvalid,
        'input-with-value': self.inputHasValue,
        'input-focused': inputFocused
      })
      return _h('input', {
        ref: 'inputEl',
        style: inputStyle,
        class: calcInputClassName,
        domProps: {
          value: needsValue ? value || self.state.currentInputValue : undefined,
          disabled,
          readOnly: readonly,
          required
        },
        on: {
          // focus: self.onFocus,
          // blur: self.onBlur,
          // input: self.onInput,
          change: self.onChange
        },
        attrs: {
          name: name,
          type: 'text',
          placeholder: placeholder,
          id: inputId,
          size: size,
          accept: accept,
          autocomplete: autocomplete,
          autoSave: autosave,
          pattern: pattern,
          validate: typeof validate === 'string' && validate.length ? validate : undefined,
          'data-validate': validate === true || validate === '' ? true : undefined,
          tabindex: tabindex,
          'data-error-message': errorMessageForce ? undefined : errorMessage
        }
      })
    }

    let inputEl = createInput()

    const hasErrorMessage = !!errorMessage || (self.$slots['error-message'] && self.$slots['error-message'].length)
    const ItemTag = tag
    return _h(ItemTag, {
      ref: 'el',
      style: style,
      class: Utils.classNames(className, {
        disabled
      }, Mixins.colorClasses(props)),
      attrs: {
        id: id
      }
    }, [this.$slots['root-start'], _h('div', {
      class: Utils.classNames('item-content item-input', {
        'inline-label': inlineLabel,
        'item-input-focused': inputFocused,
        'item-input-with-info': !!info || (self.$slots.info && self.$slots.info.length),
        'item-input-with-value': self.inputHasValue,
        'item-input-with-error-message': (hasErrorMessage && errorMessageForce) || inputInvalid,
        'item-input-invalid': (hasErrorMessage && errorMessageForce) || inputInvalid
      })
    }, [this.$slots['content-start'], (media || self.$slots.media) && _h('div', {
      class: 'item-media'
    }, [media && _h('img', {
      attrs: {
        src: media
      }
    }), this.$slots['media']]), _h('div', {
      class: 'item-inner'
    }, [this.$slots['inner-start'], (label || self.$slots.label) && _h('div', {
      class: Utils.classNames('item-title item-label', {
        'item-floating-label': floatingLabel
      })
    }, [label, this.$slots['label']]), _h('div', {
      class: Utils.classNames('item-input-wrap', {
        'input-dropdown': type === 'select'
      })
    }, [inputEl, this.$slots['input'], hasErrorMessage && errorMessageForce && _h('div', {
      class: 'item-input-error-message'
    }, [errorMessage, this.$slots['error-message']]), clearButton && _h('span', {
      class: 'input-clear-button'
    }), (info || self.$slots.info) && _h('div', {
      class: 'item-input-info'
    }, [info, this.$slots['info']])]), this.$slots['inner'], this.$slots['inner-end']]), this.$slots['content'], this.$slots['content-end']]), this.$slots['root'], this.$slots['root-end']])
  },

  computed: {
    inputHasValue () {
      const self = this
      const {
        value
      } = self.props
      const {
        currentInputValue
      } = self.state
      return typeof value === 'undefined' ? currentInputValue : value || value === 0
    },

    props () {
      return __vueComponentProps(this)
    }

  },
  watch: {
    'props.value': function watchValue (v) {
      const self = this
      const {
        value
      } = self.props
      if (!self.$f7) return
      self.setState({
        currentInputValue: value
      })
      self.updateInputOnDidUpdate = true
      const { params } = self.commonCalendar
      if (!params.rangePicker && !params.multiple) {
        if (!value || !moment(value).isValid()) {
          self.commonCalendar.setValue([])
        } else {
          self.commonCalendar.setValue([moment(value).toDate()])
        }
      }
    },
    'props.maxDate': function watchValue (v, ov) {
      if (v !== ov) {
        const self = this
        const { params } = self.commonCalendar
        params.maxDate = v
        calcDefaultMinDateStartDay(self.commonCalendar)
        self.commonCalendar.render()
      }
    },
    'props.minDate': function watchValue (v, ov) {
      if (v !== ov) {
        const self = this
        const { params } = self.commonCalendar
        params.minDate = v
        calcDefaultMinDateStartDay(self.commonCalendar)
        self.commonCalendar.render()
      }
    },
    'props.open': function (v, ov) {
      if (v !== ov) {
        this.commonCalendar[v ? 'open' : 'close']()
      }
    }
  },

  created () {
    const self = this
    self.onChange = self.onChange.bind(self)
    // self.onInput = self.onInput.bind(self)
    // self.onFocus = self.onFocus.bind(self)
    // self.onBlur = self.onBlur.bind(self)
    self.onInputNotEmpty = self.onInputNotEmpty.bind(self)
    self.onInputEmpty = self.onInputEmpty.bind(self)
    self.onInputClear = self.onInputClear.bind(self)
  },

  mounted () {
    const self = this
    const el = self.$refs.el
    if (!el) return
    self.$f7ready(f7 => {
      const {
        validate,
        value,
        defaultValue,
        routableModals,
        multiple,
        showClose,
        showClear,
        calendarType,
        monthNames,
        monthNamesShort,
        dayNames,
        dayNamesShort,
        firstDay,
        weekendDays,
        dateFormat,
        rangePicker,
        rangePickerMinDays,
        rangePickerMaxDays,
        direction,
        events,
        toolbarCloseText,
        toolbarClearText,
        maxDate,
        minDate,
        disabledDates,
        rangesClasses,
        formatValue,
        touchMove,
        closeOnSelect,
        openIn,
        scrollToInput,
        inputReadOnly,
        cssClass,
        closeByOutsideClick,
        toolbar,
        renderToolbar,
        renderWeekHeader,
        renderMonths,
        renderMonth,
        renderMonthSelector,
        renderYearSelector,
        renderHeader,
        render,
        header,
        on,
        headerPlaceholder,
        defaultMinDateStart,
        defaultMinDateIncluded,
        open
      } = self.props
      const inputEl = self.$refs.inputEl
      if (!inputEl) return
      inputEl.addEventListener('input:notempty', self.onInputNotEmpty, false)
      inputEl.addEventListener('input:empty', self.onInputEmpty, false)
      inputEl.addEventListener('input:clear', self.onInputClear, false)

      if ((validate || validate === '')) {
        setTimeout(() => {
          self.validateInput(inputEl)
        }, 0)
      }
      let calendarValue = []
      let calendarDefaultValue = []
      if (value && moment(value).isValid()) {
        calendarValue = [moment(value).toDate()]
      }
      if (defaultValue && moment(defaultValue).isValid()) {
        calendarDefaultValue = [moment(defaultValue).toDate()]
      }
      self.commonCalendar = f7.calendar.create(Utils.noUndefinedProps({
        inputEl,
        name,
        value: calendarValue,
        defaultValue: calendarDefaultValue,
        multiple,
        calendarType,
        monthNames,
        monthNamesShort,
        dayNames,
        dayNamesShort,
        firstDay,
        weekendDays,
        dateFormat,
        rangePicker,
        rangePickerMinDays,
        rangePickerMaxDays,
        direction,
        routableModals,
        events,
        toolbarCloseText,
        toolbarClearText,
        showClose,
        showClear,
        maxDate,
        minDate,
        disabledDates,
        rangesClasses,
        formatValue,
        touchMove,
        closeOnSelect,
        openIn,
        scrollToInput,
        inputReadOnly,
        cssClass,
        closeByOutsideClick,
        toolbar,
        renderWeekHeader,
        renderMonths,
        renderMonth,
        renderMonthSelector,
        renderYearSelector,
        renderHeader,
        render,
        header,
        headerPlaceholder,
        defaultMinDateStart,
        defaultMinDateIncluded,
        renderToolbar: renderToolbar || function () {
          const calendar = this
          const clearBtn = calendar.params.showClear ? `<a href="#" class="link calendar-clear">${calendar.params.toolbarClearText}</a>` : ''
          const closeBtn = calendar.params.showClose ? `<a href="#" class="link calendar-close">${calendar.params.toolbarCloseText}</a>` : ''
          return `
        <div class="toolbar no-shadow">
        <div class="toolbar-inner">
          ${clearBtn}
          ${calendar.renderMonthSelector()}
          ${calendar.renderYearSelector()}
          ${closeBtn}
        </div>
        </div>
      `.trim()
        },
        on: Object.assign(on || {}, {
          open () {
            const calendar = this
            const { $el, params } = this
            $el.find('.calendar-clear').click(() => calendar.setValue([]))
            $el.find('.calendar-close').once('click', () => calendar.close())
            if (on && isFunction(on.open)) {
              on.open.apply(this, arguments)
            }
            if (params.minDate && !calendar.getValue()[0]) {
              const minDate = moment(params.minDate).add(1, 'd')
              if (minDate.isAfter(moment(), 'M')) {
                calendar.setYearMonth(minDate.get('y'), minDate.get('M'), undefined)
              }
            }
            self.dispatchEvent('onOpen')
          },
          close () {
            self.dispatchEvent('onClose')
          }
        })
      }))
      calcDefaultMinDateStartDay(self.commonCalendar)
      if (open) {
        self.commonCalendar.open()
      }
    })
  },
  updated () {
    const self = this
    const {
      validate
    } = self.props
    const f7 = self.$f7
    if (!f7) return

    if (self.updateInputOnDidUpdate) {
      const inputEl = self.$refs.inputEl
      if (!inputEl) return
      self.updateInputOnDidUpdate = false

      if (validate || validate === '') {
        self.validateInput(inputEl)
      }
    }
  },

  beforeDestroy () {
    const self = this
    const inputEl = self.$refs.inputEl
    if (!inputEl) return
    inputEl.removeEventListener('input:notempty', self.onInputNotEmpty, false)
    inputEl.removeEventListener('input:empty', self.onInputEmpty, false)
    inputEl.removeEventListener('input:clear', self.onInputClear, false)
    self.commonCalendar.destroy()
  },

  methods: {
    validateInput (inputEl) {
      const self = this
      const f7 = self.$f7
      if (!f7 || !inputEl) return
      const validity = inputEl.validity
      if (!validity) return
      if (!validity.valid) {
        if (self.state.inputInvalid !== true) {
          self.setState({
            inputInvalid: true
          })
        }
      } else if (self.state.inputInvalid !== false) {
        self.setState({
          inputInvalid: false
        })
      }
    },
    onInputNotEmpty (event) {
      this.dispatchEvent('input:notempty inputNotEmpty', event)
    },
    onInputEmpty (event) {
      this.dispatchEvent('input:empty inputEmpty', event)
    },
    onInputClear (event) {
      this.dispatchEvent('input:clear inputClear', event)
    },
    onChange (event) {
      this.dispatchEvent('change', event.target.value)
    },

    dispatchEvent (events, ...args) {
      __vueComponentDispatchEvent(this, events, ...args)
    },

    setState (updater, callback) {
      __vueComponentSetState(this, updater, callback)
    }
  }
}
