/**
 * @author gary.fu
 */
import DomAirSearchPage from '../views/products/domair/domair-search'
import IntlAirSearchPage from '../views/products/intlair/intlair-search'
import HotelSearchPage from '../views/products/hotel/hotel-search'
import TrainSearchPage from '../views/products/train/train-search'
import OtherPage from '../views/products/others/others'

export default [{
  path: '/search/domair',
  component: DomAirSearchPage
}, {
  path: '/search/intlair',
  component: IntlAirSearchPage
}, {
  path: '/search/hotel',
  component: HotelSearchPage
}, {
  path: '/search/train',
  component: TrainSearchPage
}, {
  path: '/search/others',
  component: OtherPage
}]
