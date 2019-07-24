/**
 * @author gary.fu
 */
import OrderDetailPage from '../../views/order/order-detail'
import OrderSubmitPage from '../../views/order/order-submit'
import OrderApprovePage from '../../views/order/order-approve'
import OrderIntlOptionSelectPage from '../../views/order/order-detail-intloption-select'
import OrderIntlDetail from '../../views/order/order-detail-inter'
import OrderDomDetail from '../../views/order/order-detail-domair'
import OrderHotelDetail from '../../views/order/order-detail-hotel'
import OrderTrainDetail from '../../views/order/order-detail-train'
export default [{
  path: '/order/detail/:taNo/:productType?',
  component: OrderDetailPage
}, {
  path: '/order/submit/:taNo',
  component: OrderSubmitPage
}, {
  path: '/order/approve/:taNo/:type',
  component: OrderApprovePage
}, {
  path: '/order/selectOption/:taNo/:packageId/:edit',
  component: OrderIntlOptionSelectPage
}, {
  path: '/order/inter/detail',
  component: OrderIntlDetail
}, {
  path: '/order/domair/detail',
  component: OrderDomDetail
}, {
  path: '/order/hotel/detail',
  component: OrderHotelDetail
}, {
  path: '/order/train/detail',
  component: OrderTrainDetail
}]
