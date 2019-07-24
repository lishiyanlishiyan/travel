const TRAIN_TYPE_MAP = {
  'G/C': 'train.label.typeGC',
  'D': 'train.label.typeD',
  'Z': 'train.label.typeZ',
  'T': 'train.label.typeT',
  'K': 'train.label.typeK',
  '0': 'train.label.typeO'
}

const TRAIN_STATION_TYPE_MAP = {
  '1': 'train.label.stationType1',
  '3': 'train.label.stationType3',
  '4': 'train.label.stationType4',
  '2': 'train.label.stationType2'
}

const TRAIN_SEAT_TYPE_MAP = {
  '无座': 'NOS',
  '硬座': 'HAS',
  '硬卧下': 'HABD',
  '软座': 'SOS',
  '软卧下': 'SOBD',
  '动卧下': 'DSOBD',
  '商务座': 'BUS',
  '特等座': 'SUS',
  '一等座': 'FIS',
  '二等座': 'SES',
  '高级软卧下': 'ADSBD',
  '高级动卧下': 'DADSBD'
}

export default {
  TRAIN_TYPE_MAP,
  TRAIN_STATION_TYPE_MAP,
  TRAIN_SEAT_TYPE_MAP
}
