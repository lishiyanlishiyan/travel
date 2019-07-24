import PersonalData from '../../views/profile/personal-data.vue'
import IdInfo from '../../views/profile/id-info.vue'
import TravleSecretary from '../../views/profile/travel-secretary.vue'
import ActingApprover from '../../views/profile/acting-approver.vue'
import Memberships from '../../views/profile/memberships.vue'
import CreditCard from '../../views/profile/credit-card.vue'
import TarvelPrefer from '../../views/profile/travel-prefer.vue'

export default [{
  path: '/personal-data',
  component: PersonalData
}, {
  path: '/id-info',
  component: IdInfo
}, {
  path: '/travel-secretary',
  component: TravleSecretary
}, {
  path: '/acting-approver',
  component: ActingApprover
}, {
  path: '/memberships',
  component: Memberships
}, {
  path: '/credit-card',
  component: CreditCard
}, {
  path: '/travel-prefer',
  component: TarvelPrefer
}]
