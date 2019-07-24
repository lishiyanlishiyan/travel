/**
 * @author gary.fu
 *
 * 封装一些Framework7-Vue中不存在的framework7控件为Vue插件
 */
import CommonDatePicker from './common-datepicker'
import CommonAutocomplete from './common-autocomplete'
import CommonPicker from './common-picker'
import CommonDropdown from './common-dropdown'
import CommonHome from './common-home'
import CommonDropdownFilter from './common-dropdownfilter'
import CommonFormErrors from './common-form-errors'
import CommonFAB from './common-fab'
// import CommonPanelFilter from './common-panelfilter'
import CommonFilterPlugin from './common-filters'

export default {
  name: 'CommonPlugin',
  install (Vue, params = {}) {
    Vue.component('common-datepicker', CommonDatePicker)
    Vue.component('common-picker', CommonPicker)
    Vue.component('common-autocomplete', CommonAutocomplete)
    Vue.component('common-dropdown', CommonDropdown)
    // Vue.component('common-panelfilter', CommonPanelFilter)
    Vue.component('common-home', CommonHome)
    Vue.component('common-dropdownfilter', CommonDropdownFilter)
    Vue.component('common-form-errors', CommonFormErrors)
    Vue.component('common-fab', CommonFAB)
    Vue.use(CommonFilterPlugin)
  }
}
