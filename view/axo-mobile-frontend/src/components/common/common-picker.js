/**
 * 参照list-input.js开发picker, 另外实现简单日期选择功能
 * @author gary.fu
 */
import Utils from 'framework7-vue/utils/utils'
import Mixins from 'framework7-vue/utils/mixins'
import __vueComponentDispatchEvent from 'framework7-vue/runtime-helpers/vue-component-dispatch-event'
import __vueComponentProps from 'framework7-vue/runtime-helpers/vue-component-props'
import __vueComponentSetState from 'framework7-vue/runtime-helpers/vue-component-set-state'
import moment from 'moment'
import { isArray, isFunction } from 'lodash/lang'
import debounce from 'lodash/debounce'
import { setPickerValue, simpleDatePickerConfig, simpleDatePickerValue } from './utils/common-utils'

export default {
  name: 'common-picker',
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
    inputStyle: [String, Object],
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
    // picker相关
    twoWayBind: { type: Boolean, default: true },
    datePicker: Boolean,
    dateFormat: { type: String, default: 'yyyy-MM-dd' },
    dateStartYear: { type: Number, default: 2000 },
    dateEndYear: { type: Number, default: 2100 },
    pickerTitle: String,
    showClear: { type: Boolean, default: false },
    showClose: { type: Boolean, default: true },
    toolbarClearText: String,
    rotateEffect: {
      type: Boolean,
      default: false
    },
    momentumRatio: Number,
    updateValuesOnMomentum: Boolean,
    updateValuesOnTouchmove: { type: Boolean, default: true },
    freeMode: { type: Boolean, default: false },
    formatValue: Function,
    cols: Array,
    openIn: String,
    scrollToInput: { type: Boolean, default: true },
    inputReadOnly: { type: Boolean, default: true },
    cssClass: String,
    closeByOutsideClick: { type: Boolean, default: true },
    toolbar: { type: Boolean, default: true },
    toolbarCloseText: String,
    routableModals: { type: Boolean, default: false },
    url: String,
    view: Object,
    renderToolbar: Function,
    render: Function,
    on: Object
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
    console.info('rerender................')
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
    const createInputHidden = () => {
      return _h('input', {
        ref: 'inputElHidden',
        domProps: {
          value: value || self.state.currentInputValue,
          disabled,
          readOnly: readonly,
          required
        },
        on: {
          change: self.onChange
        },
        attrs: {
          name: name,
          type: 'hidden',
          id: inputId
        }
      })
    }
    const createInput = () => {
      const inputClassName = Utils.classNames({
        'no-store-data': noFormStoreData || noStoreData || ignoreStoreData,
        'input-invalid': (errorMessage && errorMessageForce) || inputInvalid,
        'input-with-value': self.inputHasValue,
        'input-focused': inputFocused
      })
      return _h('input', {
        ref: 'inputEl',
        style: inputStyle,
        class: inputClassName,
        domProps: {
          // value: value,
          disabled,
          readOnly: readonly,
          required
        },
        on: {
          // focus: self.onFocus,
          // blur: self.onBlur,
          // input: self.onInput,
          // change: self.onChange
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
    let inputElHidden = createInputHidden()

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
    }, [inputElHidden, inputEl, this.$slots['input'], hasErrorMessage && errorMessageForce && _h('div', {
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
    'props.value': function watchValue (v, ov) {
      const self = this
      const {
        value,
        datePicker,
        twoWayBind
      } = self.props
      if (!self.$f7) return
      self.setState({
        currentInputValue: value
      })
      self.updateInputOnDidUpdate = true
      if (!twoWayBind) {
        return
      }
      let values = []
      if (value) {
        values = isArray(value) ? value : [value]
        if (datePicker) {
          values = simpleDatePickerValue(value ? moment(value).toDate() : value)
        }
      }
      setPickerValue(self.commonPicker, values)
    }
  },

  created () {
    const self = this
    self.onChange = self.onChange.bind(self)
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
        datePicker,
        dateFormat,
        dateStartYear,
        dateEndYear,
        pickerTitle,
        showClose,
        showClear,
        rotateEffect,
        momentumRatio,
        updateValuesOnMomentum,
        updateValuesOnTouchmove,
        freeMode,
        formatValue,
        cols,
        openIn,
        scrollToInput,
        inputReadOnly,
        cssClass,
        closeByOutsideClick,
        toolbar,
        toolbarCloseText,
        toolbarClearText,
        url,
        view,
        renderToolbar,
        render,
        on
      } = self.props
      const inputEl = self.$refs.inputEl
      if (!inputEl) return
      inputEl.addEventListener('input:clear', self.onInputClear, false)

      if ((validate || validate === '')) {
        setTimeout(() => {
          self.validateInput(inputEl)
        }, 0)
      }
      let values = []
      if (value) {
        values = isArray(value) ? value : [value]
      }
      let defaultValues = []
      if (defaultValue) {
        defaultValues = isArray(defaultValue) ? defaultValue : [defaultValue]
      }
      let configCols = cols
      let configFormatValue = formatValue
      if (datePicker) {
        let pickerConfig = simpleDatePickerConfig(dateFormat, dateStartYear, dateEndYear)
        configCols = pickerConfig.cols
        configFormatValue = pickerConfig.formatValue
        if (value) {
          values = simpleDatePickerValue(value ? moment(value).toDate() : value)
        }
      }
      self.commonPicker = f7.picker.create(Utils.noUndefinedProps({
        inputEl,
        name,
        value: values,
        defaultValue: defaultValues,
        routableModals,
        toolbarCloseText,
        toolbarClearText,
        pickerTitle,
        showClose,
        showClear,
        rotateEffect,
        momentumRatio,
        updateValuesOnMomentum,
        updateValuesOnTouchmove,
        freeMode,
        formatValue: configFormatValue || function () {
          const picker = this
          const { value, displayValue } = picker
          return displayValue ? displayValue.join(' ') : value.join(' ')
        },
        cols: configCols,
        openIn,
        scrollToInput,
        inputReadOnly,
        cssClass,
        closeByOutsideClick,
        toolbar,
        url,
        view,
        render,
        // onChange,
        renderToolbar: renderToolbar || function () {
          const picker = this
          const clearBtn = picker.params.showClear ? `<a href="#" class="link picker-clear">${picker.params.toolbarClearText}</a>` : ''
          const closeBtn = picker.params.showClose ? `<a href="#" class="link picker-close">${picker.params.toolbarCloseText}</a>` : ''
          return `<div class="toolbar no-shadow">
            <div class="toolbar-inner">
              <div class="left">
                ${clearBtn}
              </div>
              <div class="center title">
              ${picker.params.pickerTitle || ''}
              </div>
              <div class="right">
                ${closeBtn}
              </div>
            </div>
          </div>`.trim()
        },
        on: Object.assign(on || {}, {
          open () {
            const picker = this
            const { $el } = this
            $el.find('.picker-clear').click(() => {
              picker.setValue(self.datePicker ? simpleDatePickerValue() : [])
            })
            $el.find('.picker-close').once('click', () => picker.close())
            if (datePicker && !picker.getValue()[0]) {
              picker.setValue(simpleDatePickerValue())
            }
            if (on && isFunction(on.open)) {
              on.open.apply(this, arguments)
            }
          },
          change: debounce(() => {
            const values = self.commonPicker.getValue()
            const $inputElHidden = self.$$(self.$refs.inputElHidden)
            if (datePicker) {
              $inputElHidden.val(self.commonPicker.formatValue(values))
            } else {
              $inputElHidden.val(values.join(' '))
            }
            $inputElHidden.change()
          }, 300)
        })
      }))
    })
  },
  updated () {
    console.info('updated..............')
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
    inputEl.removeEventListener('input:clear', self.onInputClear, false)
    self.commonPicker.destroy()
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
    onInputClear (event) {
      this.setState({
        currentInputValue: ''
      })
      this.dispatchEvent('input:clear inputClear', '')
      setPickerValue(this.commonPicker, [])
    },
    onChange (event) {
      let value = event.target.value
      let { datePicker } = this
      if (!datePicker && this.commonPicker && this.commonPicker.params.cols.length > 1) {
        value = this.commonPicker.getValue()
      }
      this.dispatchEvent('change', value)
    },
    dispatchEvent (events, ...args) {
      __vueComponentDispatchEvent(this, events, ...args)
    },

    setState (updater, callback) {
      __vueComponentSetState(this, updater, callback)
    }
  }
}
