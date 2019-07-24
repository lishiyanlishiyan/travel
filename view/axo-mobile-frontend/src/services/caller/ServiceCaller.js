import { $httpGet, $httpPost } from '../../utils/AxoUtils'
import clone from 'lodash/clone'
import { isObject } from 'lodash/lang'

export function callService (path, name, param, config) {
  return $httpPost(`/service/call?path=${path}&name=${name}`, param, config)
}

export function checkParams (path, name, config) {
  return $httpGet(`/service/checkParams?path=${path}&name=${name}`, {}, config)
}

export function getCallerMethod (path, name, cls = emptyParam()) {
  return (param, config) => {
    return callService(path, name, Object.assign(isObject(cls) ? cls : { '@class': cls }, param), config)
  }
}

export function defaultPageSetting (pageSize) {
  return clone({
    '@class': 'com.citsamex.app.spi.data.caller.common.PageSetting',
    pageNumber: 1,
    pageSize: pageSize || 20
  })
}

export function baseTaNoCompanyParam () {
  return clone({
    '@class': 'com.citsamex.app.spi.data.caller.request.order.BaseTaNoCompanyParam'
  })
}

export function baseCompanyParam () {
  return clone({
    '@class': 'com.citsamex.app.spi.data.caller.request.order.BaseCompanyParam'
  })
}

export function baseKeyWordsParam () {
  return clone({
    '@class': 'com.citsamex.app.spi.data.caller.request.master.BaseKeyWordsParam'
  })
}

export function baseIdentityParam () {
  return clone({
    '@class': 'com.citsamex.app.spi.data.caller.request.master.BaseIdentityParam'
  })
}

export function emptyParam () {
  return clone({
    '@class': 'com.citsamex.app.spi.data.base.AbstractServiceParam'
  })
}

export default {
  callService,
  getCallerMethod,
  checkParams
}
