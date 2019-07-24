import moment from 'moment'
import AirConsts from '../consts/AirConsts'

/**
 * 定义一些Axo专用filter，方便数据展示
 * @param Vue
 * @param options
 */
function install (Vue, options = {}) {
  Vue.filter('cabinType', cabinType => {
    return Vue.$i18nBundle(AirConsts.CABIN_TYPE_MAP[cabinType])
  })
  Vue.filter('cabinTypeI', cabinTypeI => {
    return Vue.$i18nBundle(AirConsts.CABIN_TYPE_I_MAP[cabinTypeI]) || Vue.$i18nBundle('air.label.cabinMixed')
  })
  Vue.filter('duration', duration => {
    if (duration) {
      const hour = moment.duration(duration).hours()
      const minute = moment.duration(duration).minutes()
      let hourStr = ''
      let minuteStr = ''
      if (hour !== 0) {
        hourStr = `${hour}h`
      }
      if (minute !== 0) {
        minuteStr = `${minute}m`
      }
      return `${hourStr}${minuteStr}`
    }
  })
}

export default {
  install
}
