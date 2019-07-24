/**
 * @author gary.fu
 */
import { emptyParam, getCallerMethod } from '../caller/ServiceCaller'

// service path
const SERVICE_PATH = 'online/feedback'

const FEEDBACK_PARAM = 'com.citsamex.app.spi.data.caller.request.feedback.FeedbackParam'

export default {
  doFeedback: getCallerMethod(SERVICE_PATH, 'doFeedback', FEEDBACK_PARAM),
  loadAvailableFeedbackTypes: getCallerMethod(SERVICE_PATH, 'loadAvailableFeedbackTypes', emptyParam())
}
