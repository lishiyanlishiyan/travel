/**
 * @author gary.fu
 */
import DomAirSearchResultPage from '../../views/products/domair/domair-search-result'
import DomAirSearchLastResultPage from '../../views/products/domair/domair-search-last-result'
import DomAirBookPage from '../../views/products/domair/domair-book'

export default [{
  path: '/domair/doSearchDomAir',
  component: DomAirSearchResultPage
}, {
  path: '/domair/doSearchLastDomAir',
  component: DomAirSearchLastResultPage
}, {
  path: '/domair/toBookDomAir',
  component: DomAirBookPage
}]
