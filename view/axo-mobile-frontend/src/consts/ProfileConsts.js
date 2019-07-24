const ROLE_MAP = {
  '1': 'TRAVELLER',
  '2': 'APPROVER',
  '3': 'ARRANGER',
  '4': 'ADMINISTRATOR',
  '5': 'SYS_MANAGER',
  '6': 'TRACK_ORDERS',
  '7': 'INDIVIDUAL_REGISTRATION'
}

const TRAVELLER_ATTR_MAP = {
  cert: {
    itemsKey: 'userCerts',
    selectKey: 'selectedCert'
  },
  creditCard: {
    itemsKey: 'userCreditCards',
    selectKey: 'selectedCreditCard'
  },
  membership: {
    itemsKey: 'memberships',
    selectKey: 'selectedMembership'
  }
}

const USER_BASIC_TRAVELLER_MAPPING = {
  userNameCN: 'nameCN',
  userNameEN: 'nameEN',
  userName: 'nameCN',
  givenName: 'givenNameEN',
  surname: 'surNameEN',
  userEmail: 'email',
  userMobile: 'mobile',
  gender: 'gender',
  userId: 'userId',
  custNo: 'custNo',
  companyCode: 'companyCode'
}

export default {
  ROLE_MAP,
  TRAVELLER_ATTR_MAP,
  USER_BASIC_TRAVELLER_MAPPING
}
