import { getCallerMethod } from '../caller/ServiceCaller'

const SERVICE_PATH = 'billing/product'

const BILLING_VALIDATE_QUERY_PARAM = 'com.citsamex.app.spi.data.caller.request.order.billing.BillingValidateQueryParam'

export default {
  validateProductBillingType: getCallerMethod(SERVICE_PATH, 'validateProductBillingType', BILLING_VALIDATE_QUERY_PARAM)
}
