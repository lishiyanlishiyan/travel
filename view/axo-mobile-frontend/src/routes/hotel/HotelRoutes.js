/**
 * @author gary.fu
 */
import HotelSearchResultPage from '../../views/products/hotel/hotel-search-result'
import HotelDetailPage from '../../views/products/hotel/hotel-detail'
import HotelBookPage from '../../views/products/hotel/hotel-book'

export default [{
  path: '/hotel/doSearchHotel',
  component: HotelSearchResultPage
}, {
  path: '/hotel/hotelDetail',
  component: HotelDetailPage
}, {
  path: '/hotel/toBookHotel',
  component: HotelBookPage
}]
