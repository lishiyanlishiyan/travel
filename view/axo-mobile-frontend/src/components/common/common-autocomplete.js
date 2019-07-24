/**
 * 参照list-input.js开发autocomplete
 * @author gary.fu
 */
import Utils from 'framework7-vue/utils/utils'
import Mixins from 'framework7-vue/utils/mixins'
import __vueComponentDispatchEvent from 'framework7-vue/runtime-helpers/vue-component-dispatch-event'
import __vueComponentProps from 'framework7-vue/runtime-helpers/vue-component-props'
import __vueComponentSetState from 'framework7-vue/runtime-helpers/vue-component-set-state'
import { isArray, isFunction, isObject } from 'lodash/lang'
import debounce from 'lodash/debounce'
import cloneDeep from 'lodash/cloneDeep'
import find from 'lodash/find'
import { renderAutocompleteItem, renderSelectPageItem, reRenderSelectedItem } from './utils/common-utils'

export default {
  name: 'common-autocomplete',
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
    value: [String, Number, Array, Object],
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
    // autocomplete相关新参数
    selectPageDataItems: { type: [Array] },
    autocompleteConfig: { type: Object },
    autocompleteDataItems: { type: [Array] },
    opened: { type: Boolean, default: false },
    // autocomplete原参数
    openIn: { type: String, default: 'popup' },
    source: { type: Function },
    limit: { type: Number },
    preloader: { type: Boolean, default: true },
    preloaderColor: { type: String, default: 'white' },
    valueProperty: { type: String, default: 'id' },
    textProperty: { type: String, default: 'label' },
    textPropertyFun: { type: Function },
    // Standalone Autocomplete Parameters
    requestSourceOnOpen: { type: Boolean },
    // openerEl
    popupCloseLinkText: { type: String },
    pageBackLinkText: { type: String },
    pageTitle: { type: String, default: '' },
    searchbarPlaceholder: { type: String },
    searchbarDisableText: { type: String },
    searchbarSearchText: { type: String },
    notFoundText: { type: String },
    multiple: { type: Boolean, default: false },
    closeOnSelect: { type: Boolean },
    autoFocus: { type: Boolean, default: false },
    animate: { type: Boolean, default: true },
    navbarColorTheme: { type: String },
    formColorTheme: { type: String },
    routableModals: { type: Boolean, default: false },
    url: { type: String },
    view: { type: Object },
    // Dropdown Autocomplete Parameters
    // inputEl
    inputEvents: { type: String },
    highlightMatches: { type: Boolean, default: true },
    typeahead: { type: Boolean, default: false },
    dropdownPlaceholderText: { type: String },
    updateInputValueOnSelect: { type: Boolean, default: true },
    expandInput: { type: Boolean, default: false },
    dropdownContainerEl: { type: String },
    // Render functions
    renderDropdown: Function,
    renderPage: Function,
    renderPopup: Function,
    renderItem: Function,
    renderSearchbar: Function,
    renderNavbar: Function,
    renderSelectPage: Function, // 新加一种SelectPage选择模式，进入的时候选择
    // Events
    on: Object
  }, Mixins.colorProps),

  data () {
    const props = __vueComponentProps(this)
    const state = (() => {
      const {
        value,
        valueProperty,
        textProperty
      } = props
      return {
        inputFocused: false,
        inputInvalid: false,
        currentInputValue: isObject(value) ? value[valueProperty] : value,
        currentInputLabel: isObject(value) ? value[textProperty] : value
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
      floatingLabel,
      pageTitle,
      openIn
    } = props
    const createInputHidden = () => {
      return _h('input', {
        ref: 'inputElHidden',
        domProps: {
          value: self.state.currentInputValue,
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
          value: self.state.currentInputLabel,
          disabled,
          readOnly: openIn !== 'dropdown',
          required
        },
        on: {
          change: function () {
          }
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
    return _h(tag, {
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
    }, [_h('div', { // 创建一个item-title的div，防止不同autocomplete模式报错
      class: Utils.classNames('display-none', 'item-title')
    }, [pageTitle]), inputElHidden, inputEl, this.$slots['input'], hasErrorMessage && errorMessageForce && _h('div', {
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
        value,
        valueProperty
      } = self.props
      const {
        currentInputValue
      } = self.state
      return typeof value === 'undefined' ? currentInputValue : (isObject(value) ? value[valueProperty] : value)
    },
    props () {
      return __vueComponentProps(this)
    }

  },
  watch: {
    'props.value': function watchValue (v) {
      const self = this
      const {
        value,
        valueProperty,
        textProperty
      } = self.props
      if (!self.$f7) return
      self.setState({
        currentInputValue: isObject(value) ? value[valueProperty] : '',
        currentInputLabel: isObject(value) ? value[textProperty] : ''
      })
      self.updateInputOnDidUpdate = true
      reRenderSelectedItem(self.commonAutocomplete, value)
      if (value !== self.commonAutocomplete.value[0]) {
        this.dispatchEvent('change', value)
      }
    },
    'props.selectPageDataItems': function watchValue (v) {
      const self = this
      const {
        selectPageDataItems
      } = self.props
      if (!self.$f7) return
      self.commonAutocomplete.params.selectPageDataItems = selectPageDataItems
    },
    'props.opened': function (opened) {
      if (opened) {
        this.commonAutocomplete.open()
      } else {
        this.commonAutocomplete.close()
      }
    }
  },

  created () {
    const self = this
    self.onChange = self.onChange.bind(self)
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
        selectPageDataItems,
        opened,
        openIn,
        source,
        limit,
        preloader,
        preloaderColor,
        valueProperty,
        textProperty,
        textPropertyFun,
        requestSourceOnOpen,
        popupCloseLinkText,
        pageBackLinkText,
        pageTitle,
        searchbarPlaceholder,
        searchbarDisableText,
        searchbarSearchText,
        notFoundText,
        multiple,
        closeOnSelect,
        autoFocus,
        animate,
        navbarColorTheme,
        formColorTheme,
        routableModals,
        url,
        view,
        inputEvents,
        highlightMatches,
        typeahead,
        dropdownPlaceholderText,
        updateInputValueOnSelect,
        expandInput,
        dropdownContainerEl,
        renderDropdown,
        renderPage,
        renderPopup,
        renderItem,
        renderSearchbar,
        renderNavbar,
        renderSelectPage,
        on
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
      self.commonAutocomplete = f7.autocomplete.create(Utils.noUndefinedProps({
        inputEl,
        openerEl: inputEl,
        name,
        value: value ? [value] : undefined,
        openIn,
        limit,
        preloader,
        preloaderColor,
        valueProperty,
        textProperty,
        textPropertyFun,
        requestSourceOnOpen,
        popupCloseLinkText,
        pageBackLinkText,
        pageTitle,
        searchbarPlaceholder,
        searchbarDisableText,
        searchbarSearchText,
        notFoundText,
        multiple,
        closeOnSelect,
        autoFocus,
        animate,
        navbarColorTheme,
        formColorTheme,
        routableModals,
        url,
        view,
        inputEvents,
        highlightMatches,
        typeahead,
        dropdownPlaceholderText,
        updateInputValueOnSelect,
        expandInput,
        dropdownContainerEl,
        renderDropdown,
        selectPageDataItems,
        source: source || function (query, render) {
          const ac = this
          const { $el } = ac
          if (query) {
            $el.find('.select-page-values').hide()
          } else {
            $el.find('.select-page-values').show()
          }
          let autocompleteDataItems = self.autocompleteDataItems
          let autocompleteConfig = self.autocompleteConfig
          if (isArray(autocompleteDataItems)) {
            let results = []
            for (let i = 0; i < autocompleteDataItems.length; i++) {
              let autocompleteItem = autocompleteDataItems[i]
              let match = true
              if (query !== 0) {
                if (isObject(autocompleteItem)) {
                  match = autocompleteItem[this.params.textProperty] && autocompleteItem[this.params.textProperty].toLowerCase().indexOf(query.toLowerCase()) >= 0
                } else {
                  match = autocompleteItem.toLowerCase().indexOf(query.toLowerCase()) >= 0
                }
              }
              if (match) {
                results.push(autocompleteItem)
              }
            }
            render(results)
          } else if (autocompleteConfig) {
            if (autocompleteConfig.url) {
              // 暂未实现
            } else if (isFunction(autocompleteConfig.searchMethod)) {
              const searchAutocomplete = (query) => {
                const queryParam = {}
                const keyWordKey = autocompleteConfig.keyWordKey || 'keyWords'
                queryParam[keyWordKey] = query
                if (preloader) {
                  ac.preloaderShow()
                  ac.searchbar.backdropShow()
                }
                const resultItems = autocompleteConfig.searchMethod(queryParam, { loading: false })
                if (isArray(resultItems)) {
                  ac.preloaderHide()
                  render(resultItems)
                } else {
                  resultItems.then((response) => {
                    ac.preloaderHide()
                    ac.searchbar.backdropHide()
                    if (response) {
                      if (queryParam[keyWordKey] !== query) {
                        searchAutocomplete(query)
                      } else {
                        render(response)
                      }
                    }
                  })
                }
              }
              if (!ac.internalSearchAutocomplete) {
                ac.internalSearchAutocomplete = debounce(searchAutocomplete, 300)
              }
              if (inputEvents === 'search') {
                searchAutocomplete(query)
              } else {
                if (query.length !== 0) {
                  ac.internalSearchAutocomplete(query)
                }
              }
            }
          }
        },
        renderSelectPage: renderSelectPage || function () {
          const ac = this
          let selectPageHtml = ''
          const dataItems = ac.params.selectPageDataItems
          if (dataItems && dataItems.length > 0) {
            selectPageHtml += '<div class="margin-vertical select-page-values">'
            for (let i = 0; i < dataItems.length; i++) {
              let dataItem = dataItems[i]
              if (dataItem.items && dataItem.items.length > 0) {
                selectPageHtml += `<div class="block-title margin-vertical">${dataItem.label}</div>
                  <div class="list">
                    <ul class="padding">
                        ${renderSelectPageItem(ac, dataItem.items)}
                    </ul>
                  </div>`
              }
            }
            selectPageHtml += '</div>'
          }
          return selectPageHtml
        },
        renderPage: renderPage || function () {
          const ac = this
          return `
            <div class="page page-with-subnavbar autocomplete-page" data-name="autocomplete-page">
              ${ac.renderNavbar()}
              <div class="searchbar-backdrop"></div>
              <div class="page-content">
                <div class="margin-vertical list autocomplete-list autocomplete-found autocomplete-list-${ac.id} ${ac.params.formColorTheme ? `color-theme-${ac.params.formColorTheme}` : ''}">
                  <ul></ul>
                </div>
                <div class="margin-vertical list autocomplete-not-found">
                  <ul>
                    <li class="item-content"><div class="item-inner"><div class="item-title">${ac.params.notFoundText}</div></div></li>
                  </ul>
                </div>
                <div class="margin-vertical list autocomplete-values">
                  <ul class="${isFunction(ac.params.textPropertyFun) ? 'media-list' : ''}"></ul>
                </div>
                ${ac.params.renderSelectPage.apply(ac)}
              </div>
            </div>
          `.trim()
        },
        renderPopup,
        renderItem: renderItem || renderAutocompleteItem,
        renderSearchbar: renderSearchbar || function () {
          const ac = this
          let searchBarHTML = `
            <form class="searchbar">
              <div class="searchbar-inner">
                <div class="searchbar-input-wrap">
                  <input type="search" placeholder="${ac.params.searchbarPlaceholder}"/>
                  <i class="searchbar-icon"></i>
                  <span class="input-clear-button"></span>
                </div>
                ${inputEvents === 'search' ? `<div class="right"><a href="#" class="searchbar-search-button link">${ac.params.searchbarSearchText}</a></div>` : `<span class="searchbar-disable-button">${ac.params.searchbarDisableText}</span>`}
              </div>
            </form>
          `.trim()
          return searchBarHTML
        },
        renderNavbar,
        on: Object.assign({
          open () {
            const ac = this
            const { $el } = ac
            const dataItems = ac.params.selectPageDataItems
            $el.find('.select-data-item').click((event) => {
              const $target = self.$$(event.target)
              const value = $target.attr('data-value')
              if (value && dataItems && dataItems.length > 0) {
                const filter = {}
                filter[valueProperty] = value
                let item = {}
                dataItems.forEach(dataItem => {
                  item = find(dataItem.items, filter)
                })
                reRenderSelectedItem(ac, item, true)
              }
            })
            if (inputEvents === 'search') {
              ac.searchbar.off('search')
              $el.find('.searchbar-search-button').click(function () {
                const query = ac.searchbar.query
                ac.source(query)
              })
            }
            self.$emit('open')
          },
          change (value) {
            const $inputEl = self.$$(self.$refs.inputEl)
            const $inputElHidden = self.$$(self.$refs.inputElHidden)
            const { valueProperty, textProperty } = this.params
            $inputElHidden.val(value[0][valueProperty]).change()
            $inputEl.val(value[0][textProperty]).change()
            self.setState({
              currentInputValue: value[0][valueProperty],
              currentInputLabel: value[0][textProperty]
            })
          },
          closed (ac) {
            ac.internalSearchAutocomplete = null
            self.$emit('closed')
          }
        }, on || {})
      }))
      if (opened) {
        self.commonAutocomplete.open()
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
    self.commonAutocomplete.destroy()
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
      this.setState({
        currentInputValue: '',
        currentInputLabel: ''
      })
      this.dispatchEvent('change', null)
    },
    onChange (event) {
      this.dispatchEvent('change', cloneDeep(this.commonAutocomplete.value[0]))
    },
    dispatchEvent (events, ...args) {
      __vueComponentDispatchEvent(this, events, ...args)
    },

    setState (updater, callback) {
      __vueComponentSetState(this, updater, callback)
    }
  }
}
