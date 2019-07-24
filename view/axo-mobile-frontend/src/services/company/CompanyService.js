import { getCallerMethod, baseCompanyParam } from '../caller/ServiceCaller'

// service path
const SERVICE_PATH = 'profile/company'
const GET_COMPANY_CITY_ADDRESS_LIST_PARAM = 'com.citsamex.app.spi.data.caller.request.company.GetCompanyCityAddressListParam'

const CompanyService = {
  loadCompanySsoConfig: getCallerMethod(SERVICE_PATH, 'loadCompanySsoConfig', baseCompanyParam()),
  getAvailableCompanyCityAddresses: getCallerMethod(SERVICE_PATH, 'getAvailableCompanyCityAddresses', GET_COMPANY_CITY_ADDRESS_LIST_PARAM)
}
export default CompanyService
