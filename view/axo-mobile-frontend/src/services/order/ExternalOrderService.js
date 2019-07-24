import { getCallerMethod } from '../caller/ServiceCaller'
import { ProductType } from '../../consts/OrderConsts'

const SERVICE_PATH = 'external/order'

const GET_EXTERNAL_ORDERS_PARAM = 'com.citsamex.app.spi.data.caller.request.order.rule.GetExternalOrdersParam'
const EXTERNAL_ORDER_AUTO_INPUT_PARAM = 'com.citsamex.app.spi.data.caller.request.order.external.ExternalOrderAutoInputParam'
const EXTERNAL_ORDER_SPLIT_PARAM = 'com.citsamex.app.spi.data.caller.request.order.rule.ExternalOrderSplitParam'
const GET_EXTERNAL_ORDER_DETAILS_PARAM = 'com.citsamex.app.spi.data.caller.request.order.rule.GetExternalOrderDetailsParam'

function getAutoExternalOrders (calcParam) {
  return (param, config) => {
    return new Promise((resolve, reject) => {
      this.getExternalOrders(Object.assign({}, calcParam, param), config).then((result) => {
        let items = []
        if (result.success && result.resultData) {
          items = result.resultData.externalOrders || []
        }
        resolve(items)
      }, reject)
    })
  }
}

function checkExternalOrderProduct (externalOrder, productType) {
  if (externalOrder) {
    console.info(externalOrder, productType)
    return (productType === ProductType.DomAir && externalOrder.domAirFlag) ||
      (productType === ProductType.Hotel && externalOrder.hotelFlag) ||
      (productType === ProductType.IntlAir && externalOrder.intlAirFlag) ||
      (productType === ProductType.Train && externalOrder.trainFlag)
  }
  return true
}

export default {
  getExternalOrders: getCallerMethod(SERVICE_PATH, 'getExternalOrders', GET_EXTERNAL_ORDERS_PARAM),
  getExternalOrderAutoInput: getCallerMethod(SERVICE_PATH, 'getExternalOrderAutoInput', EXTERNAL_ORDER_AUTO_INPUT_PARAM),
  splitExternalOrder: getCallerMethod(SERVICE_PATH, 'splitExternalOrder', EXTERNAL_ORDER_SPLIT_PARAM),
  getExternalOrderRuleDetails: getCallerMethod(SERVICE_PATH, 'getExternalOrderRuleDetails', GET_EXTERNAL_ORDER_DETAILS_PARAM),
  getAutoExternalOrders,
  checkExternalOrderProduct
}
