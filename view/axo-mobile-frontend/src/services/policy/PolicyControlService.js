import Vue from 'vue'
import { $coreWarning, $coreAlert, $coreConfirm } from '../../utils/AxoUtils'
import { $i18nMsg } from '../../utils/AxoMessages'
import extend from 'lodash/extend'
import merge from 'lodash/merge'
import find from 'lodash/find'
import { isObject, isFunction } from 'lodash/lang'

const defaults = {
  // 是否显示禁止继续操作的提示框
  showForbidden: true,
  // 是否显示信息提示框
  showInfo: true,
  // 是否显示reasonCode选择(如果需要后处理ReasonCode，可以先设置false)
  showReasonCode: true,
  // 把提交页面审批变化信息和信息提示一起显示
  showSubmitPageInfo: false,
  // 把提交审批变化信息和信息提示一起显示
  showSubmitInfo: false,
  showWaiting: true,
  ajaxConfig: {
    type: 'POST',
    dataType: 'json',
    error: function () {
      if (console) {
        console.error('策略加载错误')
      }
    }
  }
}

function doPolicyControl (method, data, callback, eachCallback, config) {
  return method(data).then((data) => {
    if (data.success) {
      let policyControlData = data.resultData
      if (policyControlData && !policyControlData.hasOwnProperty('allPassed')) {
        policyControlData = policyControlData.policyControlData || policyControlData.policyControlResult
      }
      handlePolicyControl(policyControlData, callback, eachCallback, config)
    } else {
      $coreAlert(data.message)
    }
  })
}

/**
 * 解析策略控制
 * @param policyControlData
 * @param callback
 * @param eachCallback
 */
function handlePolicyControl (policyControlData, callback, eachCallback, config) {
  if (isObject(eachCallback) && !config) {
    config = eachCallback
    eachCallback = null
  }
  const _config = extend({
    callback: callback,
    eachCallback: eachCallback
  }, defaults, config || {})
  if (!policyControlData.allPassed) {
    // 策略控制执行
    if (policyControlData.forbiddenToGo) {
      handleForbiddenToGoHandler(policyControlData)
    } else {
      handleInfoHandler(policyControlData, function () {
        handleReasonCodeHandler(policyControlData, callback, eachCallback, _config)
      }, _config)
    }
  } else {
    if (isFunction(callback)) {
      callback(policyControlData)
    }
  }
}

/**
 * 禁止继续处理
 * @param resultData
 */
function handleForbiddenToGoHandler (resultData) {
  // 禁止继续处理
  const message = handlerToMessage(resultData.forbiddenInfoList)
  $coreWarning(message)
}

function handleInfoHandler (resultData, okCallback, config) {
  const messageList = []
  // 弹出提示信息
  if (config) {
    if (config.showInfo) {
      merge(messageList, resultData.infoHandlerList)
    }
    if (config.showSubmitPageInfo === true) {
      merge(messageList, resultData.submitPageHandlerList)
    }
    if (config.showSubmitInfo === true) {
      merge(messageList, resultData.approvalHandlerList)
    }
  }
  if (messageList.length > 0) {
    let message = handlerToMessage(messageList)
    $coreConfirm(message).then(okCallback)
  } else {
    if (isFunction(okCallback)) {
      okCallback()
    }
  }
}

function coreShowReasonCode (resultHandler, reasonCodeOkClick) {
  if (Vue.f7) {
    console.info(resultHandler)
    const cancelText = Vue.$i18nBundle('common.label.cancel')
    const confirmText = Vue.$i18nBundle('common.label.confirm')
    Vue.f7.popup.create({
      content: `
            <div class="popup policy-reason-code">
              <div class="page">
                <div class="navbar">
                  <div class="navbar-inner">
                    <div class="left"><a href="#" class="link popup-close">${cancelText}</a></div>
                    <div class="title">${$i18nMsg(resultHandler.infoCN, resultHandler.infoEN)}</div>
                    <div class="right"><a href="#" class="link popup-confirm">${confirmText}</a></div>
                  </div>
                </div>
                <div class="page-content">
                   <div class="list">
                    <ul>
      ${resultHandler.reasonCodes.map((reasonCode, i) => {
    return `<li><label class="radio item-content">
                        <input type="radio" name="popupPolicyReasonCode" value="${reasonCode.code}" ${i === 0 && resultHandler.reasonCodes.length === 1 ? 'checked' : ''} />
                        <i class="icon icon-radio"></i>
                        <div class="item-inner">
                          <div class="item-title">&nbsp;${$i18nMsg(reasonCode.infoCN, reasonCode.infoEN)}</div>
                        </div>
                      </label>
                    </li>`
  }).join('')}
                    </ul>
                    ${resultHandler.remarkCN || resultHandler.remarkEN ? `
                      <ul>
                        <li class="list-group-title">${Vue.$i18nBundle('common.label.remark')}</li>
                        <li>
                            <div class="item-content">${$i18nMsg(resultHandler.remarkCN, resultHandler.remarkEN)}</div>
                        </li>
                      </ul>
                    ` : ''}
                      <ul class="policyRCError display-none text-color-red">
                        <li>
                            <div class="item-content">${Vue.$i18nBundle('common.msg.policyReasonCode')}</div>
                        </li>
                      </ul>
                  </div>
                </div>
              </div>
            </div>
          `.trim(),
      on: {
        closed () {
          this.destroy()
        },
        opened (popup) {
          popup.$el.find('.popup-confirm').click(e => {
            const selectedCode = popup.$el.find('input[name=popupPolicyReasonCode]:checked').val()
            console.info(selectedCode)
            popup.$el.find('.policyRCError').addClass('display-none')
            if (selectedCode) {
              const reasonCode = find(resultHandler.reasonCodes, { code: selectedCode })
              reasonCodeOkClick(reasonCode)
              popup.close()
            } else {
              popup.$el.find('.policyRCError').removeClass('display-none')
            }
          })
          console.info('open', popup)
        }
      }
    }).open()
  }
}

function handleReasonCodeHandler (resultData, callback, callback1, config) {
  const reasonCodes = []
  const reasonCodesHandlers = []
  if (!config.showReasonCode || !resultData.reasonCodeHandlerList || resultData.reasonCodeHandlerList.length === 0) {
    callback(resultData, reasonCodes, resultData.reasonCodeHandlerList)
    return
  }

  function showReasonCode (reasonCodeHandler, i) {
    if (i === resultData.reasonCodeHandlerList.length) {
      callback(resultData, reasonCodes, reasonCodesHandlers)
    } else {
      const reasonCodeOkClick = function (reasonCode) {
        if (isFunction(callback1)) {
          callback1(reasonCode, reasonCodeHandler)
        }
        reasonCodes.push(reasonCodeHandler.id + '#' + reasonCode.id)
        reasonCodesHandlers.push(reasonCodeHandler)
        showReasonCode(resultData.reasonCodeHandlerList[++i], i)
      }
      coreShowReasonCode(reasonCodeHandler, reasonCodeOkClick)
    }
  }

  showReasonCode(resultData.reasonCodeHandlerList[0], 0)
}

function handlerToMessage (handlerList) {
  let result = ''
  if (handlerList && handlerList.length > 0) {
    handlerList.forEach((obj, num) => {
      if (!obj.showInfo) {
        if (handlerList.length > 1) {
          result += (num + 1) + '. '
        }
        result += $i18nMsg(obj.infoCN, obj.infoEN)
        result += (num === handlerList.length - 1) ? '' : '<br>'
      }
    })
  }
  const left = handlerList.length > 1 ? 'text-align-left' : ''
  return `<div class="${left}">${result}</div>`
}

export default {
  doPolicyControl,
  handlePolicyControl
}
