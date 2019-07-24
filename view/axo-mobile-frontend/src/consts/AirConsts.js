const CABIN_TYPE_MAP = {
  'A': 'air.label.cabinA',
  'F': 'air.label.cabinF',
  'C': 'air.label.cabinC',
  'Y': 'air.label.cabinY'
}

const CABIN_TYPE_I_MAP = {
  'FIRST': 'air.label.cabinF',
  'BUSINESS': 'air.label.cabinC',
  'ECONOMY': 'air.label.cabinY',
  'PREMIUM_FIRST': 'air.label.cabinFP',
  'PREMIUM_BUSINESS': 'air.label.cabinCP',
  'PREMIUM_ECONOMY': 'air.label.cabinYP'
}

const FLIGHT_TAB_MAP = {
  '1': 'air.label.lowFlight',
  '2': 'air.label.threeAgreementFlight',
  '3': 'air.label.allFlight'
}

const FLIGHT_KEY_MAP = {
  '1': 'lowestFlights',
  '2': 'corporateFlights',
  '3': 'domAirFlights'
}

const FLIGHT_ALLIANCE_MAP = {
  '1': 'air.label.skyteam',
  '2': 'air.label.oneworld',
  '3': 'air.label.staralliance'
}

const AIR_SORT_LIST = []

export default {
  CABIN_TYPE_MAP,
  CABIN_TYPE_I_MAP,
  FLIGHT_TAB_MAP,
  AIR_SORT_LIST,
  FLIGHT_KEY_MAP,
  FLIGHT_ALLIANCE_MAP
}
