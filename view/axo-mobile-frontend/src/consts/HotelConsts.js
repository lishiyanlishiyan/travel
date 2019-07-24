const HOTEL_TYPE_LIST = [{
  id: 'D',
  label: 'hotel.label.dom'
}, {
  id: 'I',
  label: 'hotel.label.intl'
}]

const HOTEL_TAB_LIST = [{
  id: 'corp',
  resultType: '1',
  label: 'hotel.label.corp'
}, {
  id: 'amex',
  resultType: '2',
  label: 'hotel.label.amex'
}, {
  id: 'other',
  resultType: '3',
  label: 'hotel.label.all'
}, {
  id: 'favorite',
  resultType: '5',
  label: 'hotel.label.favorite'
}]

const HOTEL_STARS_LIST = [{
  id: '0,1,2',
  label: 'hotel.label.starLevel2'
}, {
  id: '3',
  label: 'hotel.label.starLevel3'
}, {
  id: '4',
  label: 'hotel.label.starLevel4'
}, {
  id: '5',
  label: 'hotel.label.starLevel5'
}]

const HOTEL_PRICES_LIST = [{
  id: '0-300',
  label: 'hotel.label.priceLevel1'
}, {
  id: '301-500',
  label: 'hotel.label.priceLevel2'
}, {
  id: '501-800',
  label: 'hotel.label.priceLevel3'
}, {
  id: '801-',
  label: 'hotel.label.priceLevel4'
}, {
  id: 'cityBudget',
  label: 'hotel.label.cityBudget'
}]

const HOTEL_DISTANCE_LIST = [{
  id: '5',
  label: 'common.label.unlimited'
}, {
  id: '0.5',
  label: 'hotel.label.distance1'
}, {
  id: '2',
  label: 'hotel.label.distance2'
}, {
  id: '5',
  label: 'hotel.label.distance3'
}]

const HOTEL_SPECIAL_NEEDS = [{
  id: 'Non Smoking Room',
  label: 'hotel.label.nonSmokingRoom'
}, {
  id: 'Smoking Room',
  label: 'hotel.label.smokingRoom'
}, {
  id: 'Twin Bed',
  label: 'hotel.label.twinBed'
}, {
  id: 'King Bed',
  label: 'hotel.label.kingBed'
}]

const HOTEL_TIME_OPTIONS = ['00:00:00', '01:00:00', '02:00:00', '03:00:00', '04:00:00',
  '05:00:00', '06:00:00', '07:00:00', '08:00:00', '09:00:00',
  '10:00:00', '11:00:00', '12:00:00', '13:00:00', '14:00:00',
  '15:00:00', '16:00:00', '17:00:00', '18:00:00', '19:00:00',
  '20:00:00', '21:00:00', '22:00:00', '23:00:00', '23:59:00']

export default {
  HOTEL_TYPE_LIST,
  HOTEL_TAB_LIST,
  HOTEL_STARS_LIST,
  HOTEL_PRICES_LIST,
  HOTEL_SPECIAL_NEEDS,
  HOTEL_TIME_OPTIONS,
  HOTEL_DISTANCE_LIST
}
