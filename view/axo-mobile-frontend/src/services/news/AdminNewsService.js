import { baseIdentityParam, defaultPageSetting, getCallerMethod } from '../caller/ServiceCaller'
import store from '../../store'
import { $getSupportLocale } from '../../utils/AxoUtils'

const SERVICE_PATH = 'admin/news'

const GET_NEWS_LIST = 'com.citsamex.app.spi.data.caller.request.admin.GetNewsListParam'

const getNewsList = getCallerMethod(SERVICE_PATH, 'getNewsList', GET_NEWS_LIST)
const loadNewsDetail = getCallerMethod(SERVICE_PATH, 'loadNewsDetail', baseIdentityParam())

function getNewsListParam (pageSize) {
  const globalConfig = store.getters.globalConfig
  const currentLoginUser = store.getters.currentLoginUser
  let spLocale = $getSupportLocale(globalConfig.currentLocale)
  return {
    language: spLocale ? spLocale.language : 'cn',
    effectOnly: true,
    published: 1,
    companyCode: currentLoginUser.companyCode,
    pageSetting: defaultPageSetting(pageSize)
  }
}

export default {
  getNewsList,
  getNewsListParam,
  loadNewsDetail
}
