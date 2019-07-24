/**
 * 定义一些framework7的默认配置
 * <br>解决framework7没有国际化问题
 * @author gary.fu
 */
import cn from './messages_cn'
import en from './messages_en'
import get from 'lodash/get'

const getCn = key => get(cn, key)
const getEn = key => get(en, key)

function smartSelectRenderItem (item, index) {
  const { leftRadio, overflowEnable } = this.params
  let itemHtml
  if (item.isLabel) {
    itemHtml = `<li class="item-divider">${item.groupLabel}</li>`
  } else {
    itemHtml = `
        <li class="${item.className || ''}">
          <label class="${leftRadio ? item.inputType : 'item-' + item.inputType} item-content">
            <input type="${item.inputType}" name="${item.inputName}" value="${item.value}" ${item.selected ? 'checked' : ''}/>
            <i class="icon icon-${item.inputType}"></i>
            ${item.hasMedia ? `
              <div class="item-media">
                ${item.icon ? `<i class="icon ${item.icon}"></i>` : ''}
                ${item.image ? `<img src="${item.image}">` : ''}
              </div>
            ` : ''}
            <div class="item-inner">
              <div class="${overflowEnable ? '' : 'item-title'}${item.color ? ` color-${item.color}` : ''}">${leftRadio ? '&nbsp;' : ''}${item.text}</div>
            </div>
          </label>
        </li>
      `
  }
  return itemHtml
}

function smartSelectRenderItems () {
  const ss = this
  const renderItemRemark = ss.params.renderItemRemark || function () {
    return ''
  }
  const itemsHtml = `
      ${ss.items.map((item, index) => `${ss.renderItem(item, index)}`).join('')}
      ${renderItemRemark.call(ss, ss.items)}
    `
  return itemsHtml
}

const F7Config = {
  zh_CN: {
    calendar: {
      toolbarCloseText: getCn('common.label.close'),
      toolbarClearText: getCn('common.label.clear'),
      monthNames: ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'],
      dayNames: ['周日', '周一', '周二', '周三', '周四', '周五', '周六'],
      dayNamesShort: ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    },
    picker: {
      toolbarCloseText: getCn('common.label.finish'),
      toolbarClearText: getCn('common.label.clear')
    },
    smartSelect: {
      pageBackLinkText: getCn('common.label.back'),
      sheetCloseLinkText: getCn('common.label.close'),
      popupCloseLinkText: getCn('common.label.close'),
      renderItem: smartSelectRenderItem,
      renderItems: smartSelectRenderItems
    },
    autocomplete: {
      popupCloseLinkText: getCn('common.label.close'),
      pageBackLinkText: getCn('common.label.back'),
      searchbarPlaceholder: getCn('common.msg.search'),
      searchbarDisableText: getCn('common.label.cancel'),
      searchbarSearchText: getCn('common.label.search'),
      notFoundText: getCn('common.msg.noresult')
    },
    dialog: {
      buttonOk: getCn('common.label.confirm'),
      buttonCancel: getCn('common.label.cancel')
    },
    toast: {
      closeButtonText: getCn('common.label.close')
    }
  },
  en_US: {
    calendar: {
      toolbarClearText: getEn('common.label.clear')
    },
    picker: {
      toolbarClearText: getEn('common.label.clear')
    },
    smartSelect: {
      renderItem: smartSelectRenderItem,
      renderItems: smartSelectRenderItems
    },
    autocomplete: {
      searchbarPlaceholder: getEn('common.msg.search'),
      notFoundText: getEn('common.msg.noresult'),
      searchbarSearchText: getEn('common.label.search')
    },
    toast: {
      closeButtonText: getEn('common.label.close')
    }
  }
}

export function initF7DefaultConfig (f7) {
  // 修改一些默认配置
  Object.assign(f7.params.smartSelect, {
    routableModals: false
  })
  Object.assign(f7.params.calendar, {})
  // 先把英文版label相关保存下来
  storeEnF7Config(f7)
}

export function storeEnF7Config (f7) {
  Object.keys(F7Config.zh_CN).forEach(key => {
    const _config = F7Config.zh_CN[key]
    if (_config) {
      Object.keys(_config).forEach(configKey => {
        F7Config.en_US[key] = F7Config.en_US[key] || {}
        F7Config.en_US[key][configKey] = f7.params[key][configKey] || F7Config.en_US[key][configKey]
      })
    }
  })
}

export function calcF7Language (f7, locale) {
  Object.keys(F7Config[locale]).forEach(key => {
    const _config = F7Config[locale][key]
    if (_config) {
      Object.keys(_config).forEach(configKey => {
        f7.params[key][configKey] = _config[configKey]
      })
    }
  })
}
