/**
 * @author kang.wang
 */
import IntlAirSearchResultPage from '../../views/products/intlair/intlair-search-result'
import IntlAirSearchLastResultPage from '../../views/products/intlair/intlair-search-last-result'
import IntlAirSearchCabinPage from '../../views/products/intlair/intlair-search-cabin'
import IntlAirBookPage from '../../views/products/intlair/intlair-book'

export default [{
  path: '/intlair/searchIntlAir',
  component: IntlAirSearchResultPage
}, {
  path: '/intlair/searchLastIntlAir',
  component: IntlAirSearchLastResultPage
}, {
  path: '/intlair/searchIntlAirCabin',
  component: IntlAirSearchCabinPage
}, {
  path: '/intlair/bookIntlAir',
  component: IntlAirBookPage
}]
