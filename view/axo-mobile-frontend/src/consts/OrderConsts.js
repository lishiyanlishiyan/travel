export const ProductType = {
  Order: 'order',
  Hotel: 'hotel',
  DomAir: 'domair',
  IntlAir: 'intlair',
  Train: 'train',
  Others: 'others',
  Visa: 'visa',
  CarRental: 'car-rental',
  Company: 'company',
  Profile: 'profile',
  CompanyProducts: 'companyProducts',
  AllProducts: 'allProducts',
  CompanyInfo: 'companyInfo'
}

export const Products = [{
  'id': 1,
  'nameCN': '国内机票',
  'nameEN': 'Dom Air',
  'productType': ProductType.DomAir
}, {
  'id': 2,
  'nameCN': '国际机票',
  'nameEN': 'Intl Air',
  'productType': ProductType.IntlAir
}, {
  'id': 5,
  'nameCN': '酒店',
  'nameEN': 'Hotel',
  'productType': ProductType.Hotel
}, {
  'id': 7,
  'nameCN': '火车',
  'nameEN': 'Train',
  'productType': ProductType.Train
}, {
  'id': 4,
  'nameCN': '其他',
  'nameEN': 'Others',
  'productType': ProductType.Others
}]

export const SearchProductMap = {
  others: 'existOthers',
  intlair: 'existIntlAir',
  domair: 'existDomAir',
  hotel: 'existHotel',
  visa: 'existVisa',
  train: 'existTrain',
  'domair-refund': 'existDomAirRefund',
  'domair-rebook': 'existDomAirRebook',
  'car-rental': 'existCarRental'
}

export default {
  BASIC_SEARCH_PARAM_KEY: 'Order/basicSearchParam',
  SEARCH_STATUS_KEY: 'Order/searchStatus',
  EXTERNAL_ORDER_AUTO_INPUT_KEY: 'Order/externalOrderAutoInput',
  ORDER_SEARCH_STATUS_KEY: 'Order/orderSearchStatus'
}
