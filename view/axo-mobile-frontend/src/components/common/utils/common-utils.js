import { isFunction, isNumber } from 'lodash/lang'
import map from 'lodash/map'
import range from 'lodash/range'
import padStart from 'lodash/padStart'
import isEqual from 'lodash/isEqual'
import moment from 'moment'
import find from 'lodash/find'

export function calcDefaultMinDateStartDay (calendar) {
  let { params } = calendar
  let { minDate, maxDate, rangePicker, multiple, defaultMinDateStart, defaultMinDateIncluded } = params
  if (!(rangePicker || multiple) && isNumber(defaultMinDateStart)) {
    let currentDate = calendar.getValue()[0]
    let defaultMinDate = currentDate
    if (minDate) {
      let addDays = defaultMinDateIncluded && currentDate && !moment(currentDate).isBefore(minDate) ? 0 : defaultMinDateStart
      defaultMinDate = moment(minDate).add(addDays, 'd')
    }
    if (maxDate && moment(maxDate).isBefore(defaultMinDate)) {
      defaultMinDate = maxDate
    }
    if (currentDate && moment(currentDate).isAfter(defaultMinDate)) {
      defaultMinDate = currentDate
    }
    if (defaultMinDate) {
      calendar.setValue([moment(defaultMinDate).toDate()])
    }
  }
}

/**
 * 有些条件下F7的picker设置值无效
 * @param picker
 * @param forceValues
 */
export function setPickerValue (picker, forceValues) {
  const newValue = forceValues || []
  const newDisplayValue = []
  if (isEqual(picker.getValue(), newValue)) {
    return
  }
  if (newValue.length > 0) {
    let cols = picker.params.cols
    for (let i = 0; i < cols.length; i += 1) {
      let col = cols[i]
      let index = (col.values || []).indexOf(newValue[i])
      if (col.displayValues) {
        newDisplayValue.push(col.displayValues[index])
      } else {
        newDisplayValue.push(col.values[index])
      }
    }
  }
  if (newValue.indexOf(undefined) >= 0) {
    return
  }
  picker.setValue(newValue)
  picker.value = newValue
  picker.displayValue = newDisplayValue
  if (picker.inputEl) {
    picker.$inputEl.val(picker.formatValue())
    picker.$inputEl.trigger('change')
  }
}

export function simpleDatePickerValue (date) {
  const today = date || new Date()
  return [today.getFullYear() + '', padStart(today.getMonth() + 1, 2, '0'), padStart(today.getDate(), 2, '0')]
}

export function simpleDatePickerConfig (dateFormat, start = 1900, end = 2101) {
  const getItems = (max) => {
    const end = max || 31
    return map(range(1, end + 1), i => padStart(i, 2, '0'))
  }
  const getDaysByMonthAndYear = (month, year) => {
    let intDay = new Date(year, parseInt(month) + 1 - 1, 1)
    let d = new Date(intDay - 1)
    return getItems(d.getDate())
  }
  const initMonthes = getItems(12)
  const initYears = map(range(start, end), y => y.toString())
  const onChange = (picker, values, displayValues) => {
    if (picker.cols.length === 3) {
      let days = getDaysByMonthAndYear(picker.cols[1].value, picker.cols[0].value)
      let currentValue = picker.cols[2].value
      if (parseInt(currentValue) > days.length) {
        currentValue = padStart(days.length, 2, '0')
      }
      if (picker.cols[2].values.length !== days.length) {
        picker.cols[2].replaceValues(getItems(days.length))
      }
      picker.cols[2].setValue(currentValue)
    }
  }

  const _config = {
    formatValue: function (p, values, displayValues) {
      if (values && values.length && values[0]) {
        let year = values[0]
        let month = values[1]
        let day = values[2]
        return dateFormat
          .replace(/yyyy/g, year)
          .replace(/yy/g, (year + '').substring(2))
          .replace(/MM/g, month)
          .replace(/dd/g, day)
      }
    },
    cols: [{// Years
      values: initYears,
      onChange
    }]
  }
  dateFormat.indexOf('M') !== -1 && _config.cols.push({// Months
    values: initMonthes,
    onChange
  })
  dateFormat.indexOf('d') !== -1 && _config.cols.push({// Days
    values: getItems()
  })
  return _config
}

export function renderSelectPageItem (autocomplete, items) {
  return renderSelectItem(items, 3, autocomplete.params.valueProperty, autocomplete.params.textProperty)
}

export function renderSelectItem (items, colCount = 2, valueProperty = 'id', textProperty = 'label') {
  let html = ''
  const colCls = 'col-' + parseInt(100 / colCount)
  for (let i = 0; i < items.length; i++) {
    let item = items[i]
    if (i % colCount === 0) {
      html += '<li class="margin-bottom"><div class="row left">'
    }
    html += `<label class="${colCls}"><a href="#" 
                class="button button-outline select-data-item" 
                data-label="${item[textProperty]}" 
                data-value="${item[valueProperty]}">
                ${item[textProperty]}
            </a></label>`
    if (i % colCount === colCount - 1 || i === items.length - 1) {
      if (i === items.length - 1) {
        for (let j = 0; j < colCount - i % colCount; j++) {
          html += `<div class="${colCls}"/>`
        }
      }
      html += '</div></li>'
    }
  }
  return html
}

export function reRenderSelectedItem (ac, value, updateChange) {
  ac.value = value ? [value] : []
  if (updateChange) {
    ac.updateValues()
    ac.emit('local::change autocompleteChange', ac.value)
  }
}

export function groupByCount (items, count) {
  const result = []
  items.forEach((item, index) => {
    let idx = parseInt(index / count)
    if (result[idx]) {
      result[idx].push(item)
    } else {
      result[idx] = [item]
    }
  })
  return result
}

export function renderAutocompleteItem (item, index) {
  const ac = this
  let itemHtml
  let textFun = (item) => {
    return `<div class="item-title">${item.text}</div>`
  }
  if (isFunction(ac.params.textPropertyFun)) {
    const filter = {}
    filter[ac.params.valueProperty] = item.value
    let data = find(ac.items, filter)
    if (!data) {
      data = find(ac.values, filter)
    }
    item.data = data
    textFun = ac.params.textPropertyFun
  }
  const itemValue = item.value && typeof item.value === 'string' ? item.value.replace(/"/g, '&quot;') : item.value
  if (ac.params.openIn !== 'dropdown') {
    itemHtml = `
        <li class="${isFunction(ac.params.textPropertyFun) ? 'media-item' : ''}">
          <label class="item-${item.inputType} item-content">
            <input type="${item.inputType}" name="${item.inputName}" value="${itemValue}" ${item.selected ? 'checked' : ''}>
            <i class="icon icon-${item.inputType}"></i>
            <div class="item-inner">
              ${textFun(item, index)}
            </div>
          </label>
        </li>
      `
  } else if (!item.placeholder) {
    // Dropdown
    itemHtml = `
        <li>
          <label class="item-radio item-content" data-value="${itemValue}">
            <div class="item-inner">
              ${textFun(item, index)}
            </div>
          </label>
        </li>
      `
  } else {
    // Dropwdown placeholder
    itemHtml = `
        <li class="autocomplete-dropdown-placeholder">
          <label class="item-content">
            <div class="item-inner">
               ${textFun(item, index)}
            </div>
          </label>
        </li>
      `
  }
  return itemHtml.trim()
}

export function calcConditionItemSelected (conditionItem) {
  if (conditionItem.items) {
    let hasSelected = false
    conditionItem.items.forEach(item => {
      calcConditionItemSelected(item)
      if (!item.isUnlimited && (item.selected || item.hasSelected)) {
        hasSelected = true
      }
    })
    conditionItem.hasSelected = hasSelected
  }
}

export function clearConditionItemsSelected (conditionItems) {
  if (conditionItems) {
    conditionItems.forEach(conditionItem => {
      if (conditionItem.items) {
        conditionItem.items.forEach(item => {
          clearConditionItemsSelected(conditionItem.items)
          if (item.hasOwnProperty('selected')) {
            item.selected = false
          }
        })
        if (conditionItem.hasOwnProperty('hasSelected')) {
          conditionItem.hasSelected = false
        }
      }
    })
  }
}

export function calcConditionFilter (conditionItems, id) {
  const conditionItem = find(conditionItems, { id })
  let results = []
  if (conditionItem) {
    if (conditionItem.items[0] && conditionItem.items[0].items) {
      conditionItem.items.forEach(item => {
        results = results.concat(item.items.filter(_item => _item.selected).map(_item => _item.id))
      })
    } else {
      results = conditionItem.items.filter(item => item.selected).map(item => item.id)
    }
  }
  return results
}

export default {
  calcDefaultMinDateStartDay,
  simpleDatePickerValue,
  simpleDatePickerConfig,
  setPickerValue,
  renderSelectPageItem,
  reRenderSelectedItem,
  groupByCount,
  renderAutocompleteItem,
  calcConditionItemSelected,
  clearConditionItemsSelected,
  calcConditionFilter
}
