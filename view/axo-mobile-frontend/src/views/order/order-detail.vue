<template>
  <f7-page page-content :with-subnavbar="!!(order.statusCode<5 && orderStatusList)">
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')" :back-link-force="!!productType" back-link-url="/order"/>
      <f7-nav-title>{{$t('common.label.order')}}: {{currentTaNo}}</f7-nav-title>
      <f7-subnavbar class="orderStatus" v-if="order.statusCode<5 && orderStatusList">
        <f7-segmented raised>
          <f7-button :key="orderStatus.code"
                     v-html="$i18nMsg(orderStatus.descriptionCN, orderStatus.descriptionEN)"
                     :class="{'done': order.statusCode>orderStatus.code || order.tcStatusCode=='2'}"
                     :active="order.statusCode===orderStatus.code && order.tcStatusCode!='2'"
                     v-for="orderStatus in orderStatusList">
          </f7-button>
          <f7-button :class="{'done': order.tcStatusCode=='2'}"
                     v-html="$t('order.label.completed')"></f7-button>
        </f7-segmented>
      </f7-subnavbar>
    </f7-navbar>
    <order-head :order="order" v-if="order && order.taNo"/>
    <f7-card v-if="order.existDomAir">
      <f7-card-header>{{$t('air.label.dom')}}</f7-card-header>
      <f7-card-content>
        <f7-row style="align-items: center;">
          <f7-col width="80">
            <f7-row v-for="(segment,idx) in domAirSegments" :key="segment.id" no-gap class="flightInfo">
              <f7-col width="50">
                <f7-chip color="green" v-if="idx===0 && domAirSegments.length<=2" :text="$t('air.label.departure')"/>
                <f7-chip color="blue" v-if="idx!==0 && domAirSegments.length<=2" :text="$t('air.label.return')"/>
                <f7-chip color="blue" v-if="domAirSegments.length>2" :text="$t('air.label.segment')+(idx+1)"/>
                {{segment.deptDate|date('YYYY-MM-DD')}}
              </f7-col>
                <f7-col width="50">{{segment.deptPort?$i18nMsg(segment.deptPort.cityNameCN, segment.deptPort.cityNameEN):segment.deptPortCode}}→{{segment.arrivalPort?$i18nMsg(segment.arrivalPort.cityNameCN, segment.arrivalPort.cityNameEN):segment.arrivalPortCode}}</f7-col>
            </f7-row>
          </f7-col>
          <f7-col width="20" v-if="order.price && order.price.domAirPrice">
            {{'¥'+order.price.domAirPrice}}
          </f7-col>
        </f7-row>
      </f7-card-content>
      <f7-card-footer>
        <f7-link @click="toDomAir()">{{$t('common.label.showMore')}}</f7-link>
      </f7-card-footer>
    </f7-card>
    <f7-card v-if="order.existIntlAir || order.existIntlFlight">
      <f7-card-header>{{$t('air.label.intl')}}</f7-card-header>
      <f7-card-content :key="productOrder.id" v-for="productOrder in order.newIntlAirList">
        <f7-row style="align-items: center;">
          <f7-col width="80">
            <f7-row no-gap class="flightInfo" :key="airline.id" v-for="(airline,idx) in productOrder.airlineList">
              <f7-col width="50">
                <f7-chip color="green" v-if="idx===0 && productOrder.airlineList.length<=2" :text="$t('air.label.departure')"/>
                <f7-chip color="blue" v-if="idx!==0 && productOrder.airlineList.length<=2" :text="$t('air.label.return')"/>
                <f7-chip color="blue" v-if="productOrder.airlineList.length>3" :text="$t('air.label.segment')+(idx+1)"/>
                {{airline.deptDate|date('YYYY-MM-DD')}}
              </f7-col>
              <f7-col width="50">
                {{airline.deptCityEntity?$i18nMsg(airline.deptCityEntity.nameCN,airline.deptCityEntity.nameEN):airline.deptCity}}→{{airline.arrivalCityEntity?$i18nMsg(airline.arrivalCityEntity.nameCN,airline.arrivalCityEntity.nameEN):airline.arrivalCity}}
              </f7-col>
            </f7-row>
          </f7-col>
          <f7-col width="20" v-if="order.price && order.price.intPrice">
            {{'¥'+order.price.intPrice}}
          </f7-col>
        </f7-row>
      </f7-card-content>
      <f7-card-footer>
        <f7-link @click="toIntlAir()">{{$t('common.label.showMore')}}</f7-link>
      </f7-card-footer>
    </f7-card>
    <f7-card v-if="order.existHotel">
      <f7-card-header>{{$t('common.label.hotel')}}</f7-card-header>
      <f7-card-content>
        <!--<f7-row>
          <f7-col width="30">
            {{$t('hotel.label.city')}}
          </f7-col>
          <f7-col width="70">
            {{$i18nMsg(hotel.hotelNationNameCN, hotel.hotelNationNameEN)}}-{{$i18nMsg(hotel.hotelCityNameCN, hotel.hotelCityNameEN)}}
          </f7-col>
        </f7-row>
        <f7-row>
          <f7-col width="30">
            {{$t('hotel.label.hotelName')}}
          </f7-col>
          <f7-col width="70">
              <span v-if="hotel.hotelId===-1">
                <span v-if="hotel.hotelName">{{hotel.hotelName}}</span>
                <span v-if="!hotel.hotelName">{{$i18nMsg(hotel.hotelNameCN,hotel.hotelNameEN)}}</span>
              </span>
            <span v-if="hotel.hotelId!==-1">{{$i18nMsg(hotel.hotelNameCN,hotel.hotelNameEN)}}</span>
            <f7-chip v-if="hotel.corporateFlag==='1'" :text="$t('common.label.corp')" color="blue"/>
          </f7-col>
        </f7-row>
        <f7-row>
          <f7-col>
            {{$date(hotel.checkinDate, 'MM-DD')}}
          </f7-col>
          <f7-col>
            {{hotel.totalStay}} {{$t('hotel.label.stayPer')}}
            <f7-chip v-if="hotel.orderStatus===0" :text="$t('common.label.bookCanceled')" color="red"/>
          </f7-col>
          <f7-col>
            {{$date(hotel.checkoutDate, 'MM-DD')}}
          </f7-col>
        </f7-row>-->
        <f7-row style="align-items: center;">
          <f7-col width="80">
            <f7-row no-gap :key="hotel.id" v-for="hotel in order.hotelList">
              <f7-col width="50">
                <f7-chip color="gray" :text="(hotel.totalStay)+$t('hotel.label.stayPer')"/>
                {{$date(hotel.checkinDate, 'YYYY-MM-DD')}}
              </f7-col>
              <f7-col width="50">
                {{$i18nMsg(hotel.hotelNationNameCN, hotel.hotelNationNameEN)}}
                -
                {{$i18nMsg(hotel.hotelCityNameCN, hotel.hotelCityNameEN)}}
                <f7-chip v-if="hotel.orderStatus===0" :text="$t('common.label.bookCanceled')" color="red"/>
              </f7-col>
              <f7-col width="100" class="text-color-gray" style="padding-left: 34px; font-size: 12px; margin: -5px 0 5px;">
                <span v-if="hotel.hotelId===-1">
                  <span v-if="hotel.hotelName">{{hotel.hotelName}}</span>
                  <span v-if="!hotel.hotelName">{{$i18nMsg(hotel.hotelNameCN,hotel.hotelNameEN)}}</span>
                </span>
                <span v-if="hotel.hotelId!==-1">{{$i18nMsg(hotel.hotelNameCN,hotel.hotelNameEN)}}</span>
              </f7-col>
            </f7-row>
          </f7-col>
          <f7-col width="20" v-if="order.price && order.price.hotelPrice">
            {{order.price.hotelPrice|currency('￥')}}
          </f7-col>
        </f7-row>
      </f7-card-content>
      <f7-card-footer>
        <f7-link @click="toProduct('hotel')">{{$t('common.label.showMore')}}</f7-link>
      </f7-card-footer>
    </f7-card>
    <f7-card v-if="order.existTrain">
      <f7-card-header>{{$t('common.label.train')}}</f7-card-header>
      <f7-card-content :key="productOrder.id" v-for="productOrder in order.trainList">
        {{productOrder.id}}
      </f7-card-content>
      <f7-card-footer>
        <f7-link @click="toProduct('train')">{{$t('common.label.showMore')}}</f7-link>
      </f7-card-footer>
    </f7-card>
    <f7-list v-if="order.approvers && order.approvers.length>0">
      <f7-list-item accordion-item :title="$t('order.label.approvers')">
        <f7-accordion-content>
          <div :key="approver.id" v-for="(approver,index) in order.approvers" class="approver">
            <f7-block strong>
              <f7-row no-gap>
                <f7-col width="5">{{index+1}}.</f7-col>
                <f7-col width="25">{{$i18nMsg(approver.apprNameCN,approver.apprNameEN)}}</f7-col>
                <f7-col width="40">{{approver.appDate|date('YY-MM-DD HH:mm')}}</f7-col>
                <f7-col width="20" v-if="approver.status==='N'"><f7-chip :text="$i18nMsg(approver.statusNameCN,approver.statusNameEN)" color="yellow"/></f7-col>
                <f7-col width="20" v-if="approver.status==='A'"><f7-chip :text="$i18nMsg(approver.statusNameCN,approver.statusNameEN)" color="green"/></f7-col>
                <f7-col width="20" v-if="approver.status==='R'"><f7-chip :text="$i18nMsg(approver.statusNameCN,approver.statusNameEN)" color="red"/></f7-col>
                <f7-col width="10"><f7-link v-if="approver.memo" :text="$t('common.label.remark')" @click="$coreShowPopup(approver.memo)"/></f7-col>
              </f7-row>
            </f7-block>
          </div>
        </f7-accordion-content>
      </f7-list-item>
    </f7-list>
    <f7-list v-if="orderErrorMessage">
      <li>
        <common-form-errors :value="orderErrorMessage" />
      </li>
    </f7-list>
    <f7-fab position="right-bottom" slot="fixed" v-if="order.orderButton && !orderErrorMessage">
      <f7-icon f7="add"></f7-icon>
      <f7-icon f7="close"></f7-icon>
      <f7-fab-buttons position="top">
        <f7-fab-button :label="$t('order.label.continueBook')" v-if="order.orderButton.continueFlag"
                       @click="$goto('/continue-book/' + order.taNo)">
          <f7-icon f7="add_round"/>
        </f7-fab-button>
        <f7-fab-button :label="$t('common.label.submitOrder')" v-if="order.orderButton.submitBtn" @click="toSubmitOrder()">
          <f7-icon f7="document_check"/>
        </f7-fab-button>
        <f7-fab-button :label="$t('common.label.approve')" v-if="order.orderButton.approvalBtn"
                       @click="toApproveOrder('a')">
          <f7-icon f7="check"/>
        </f7-fab-button>
        <f7-fab-button :label="$t('common.label.reject')" v-if="order.orderButton.rejectBtn"
                       @click="toApproveOrder('r')">
          <f7-icon f7="close"/>
        </f7-fab-button>
        <f7-fab-button :label="$t('order.label.cancel')" v-if="order.orderButton.cancelBtn" @click="cancelOrder()">
          <f7-icon f7="trash"/>
        </f7-fab-button>
      </f7-fab-buttons>
    </f7-fab>
  </f7-page>
</template>

<script>
import { mapGetters } from 'vuex'
import OrderService from '../../services/order/OrderService'
import { ProductType } from '../../consts/OrderConsts'
import PolicyService from '../../services/policy/PolicyControlService'
import OrderHead from './order-head'
export default {
  name: 'order-detail',
  components: { OrderHead },
  data () {
    const currentTaNo = this.$f7route.params.taNo
    const productType = this.$f7route.params.productType
    const companyDefaultSetup = this.$defaultSetup(ProductType.Company)
    const orderStatusList = companyDefaultSetup.orderStatusList
    const intlDefaultSetup = this.$defaultSetup(ProductType.IntlAir)
    const intlQCMode = intlDefaultSetup.qcMode
    const hotelDefaultSetup = this.$defaultSetup(ProductType.Hotel)
    const onHoldBookEnable = hotelDefaultSetup.onHoldBookEnable
    const bothAppr = companyDefaultSetup.majorApproverApproveEnable
    const pressApproveEnable = companyDefaultSetup.pressApproveEnable
    const continueBookEnable = companyDefaultSetup.continueBookEnable
    let openProduct = {}
    let domAirSegments = []
    return { currentTaNo, productType, orderStatusList, order: {}, intlQCMode, onHoldBookEnable, bothAppr, pressApproveEnable, continueBookEnable, openProduct, domAirSegments, orderErrorMessage: '' }
  },
  computed: {
    ...mapGetters(['loginResult', 'currentLoginUser'])
  },
  mounted () {
    if (!this.currentTaNo) {
      this.$back()
    } else {
      this.loadTaDetails()
    }
  },
  methods: {
    defaultOpen (order, product) {

    },
    toSubmitOrder () {
      let self = this
      let paramObj = { taNo: self.currentTaNo, companyCode: self.order.companyCode }
      OrderService.toSubmitOrder(paramObj).then(data => {
        if (data && data.success) {
          if (data.resultData.policyControlResult) {
            PolicyService.handlePolicyControl(data.resultData.policyControlResult, function () {
              paramObj.policyApplied = true
              OrderService.toSubmitOrder(paramObj).then(data => {
                if (data && data.success) {
                  self.$store.dispatch('Order/storeSubmitPageData', data)
                  self.$goto('/order/submit/' + self.order.taNo)
                } else {
                  self.$coreError(data.message)
                }
              })
            })
          } else {
            self.$store.dispatch('Order/storeSubmitPageData', data)
            self.$goto('/order/submit/' + self.order.taNo)
          }
        } else {
          self.$coreError(data.message)
        }
      })
    },
    toApproveOrder (approveType) {
      let self = this
      self.$goto('/order/approve/' + self.order.taNo + '/' + approveType)
    },
    cancelOrder () {
      let self = this
      self.$coreConfirm(self.$t('order.msg.cancel')).then(function () {
        let { userId } = self.currentLoginUser
        let param = { taNo: self.currentTaNo, userId, needCancelPNR: true, syncFlag: true }
        OrderService.cancelTa(param).then(data => {
          if (data && data.success) {
            window.location.reload()
          } else if (data) {
            self.$coreError(data.message)
          }
        })
      })
    },
    initOrderButton (order) {
      if (order) {
        let orderStatus = Number(order.statusCode)
        let creator = order.createBy
        let { userId, userRoles } = this.currentLoginUser
        let historyFlag = order.historyFlag && order.historyFlag === 1
        let onlineFlag = !order.tcUserId
        let currentCreatorFlag = creator === userId
        let cancelBtn = !historyFlag && currentCreatorFlag && orderStatus < 4 && onlineFlag
        let intlLockFlag = this.calIntlIsLock(order)
        let hasProductFlag = this.calHasProduct(order)
        let submitBtn = !historyFlag && currentCreatorFlag && orderStatus === 1 && !intlLockFlag && hasProductFlag && onlineFlag
        let issueTicketBtn = !historyFlag && currentCreatorFlag && onlineFlag &&
          orderStatus > 1 && orderStatus < 5 && order.issueReqStatus === 'N'
        let hotelOnHoldBtn = false
        if (issueTicketBtn) {
          if (orderStatus === 4 && this.onHoldBookEnable) {
            hotelOnHoldBtn = true
            issueTicketBtn = false
          }
        }
        let isCurrentApprover = this.calIsCurrentApprover(order.approvers, orderStatus, userId, this.bothAppr)
        let hasApproveFlag = this.calHasApproverRole(userRoles)
        let rejectBtn = !historyFlag && isCurrentApprover && hasApproveFlag && onlineFlag
        let approvalBtn = !historyFlag && isCurrentApprover && hasApproveFlag && onlineFlag
        let pressApproveBtn = (orderStatus === 2 || orderStatus === 3) && this.pressApproveEnable &&
          !isCurrentApprover && !historyFlag && currentCreatorFlag && onlineFlag
        let continueFlag = (this.continueBookEnable || !order.externalLinkNo) && currentCreatorFlag && !historyFlag && onlineFlag && !order.existVisa && orderStatus < 2
        order.orderButton = { cancelBtn, submitBtn, issueTicketBtn, hotelOnHoldBtn, rejectBtn, approvalBtn, currentCreatorFlag, pressApproveBtn, continueFlag }
      }
    },
    calHasProduct (order) {
      return order.existDomAir || order.existHotel || order.existIntlAir || order.existIntlFlight ||
        order.existOther || order.existTrain || order.existVisa || order.existCarRental
    },
    calHasApproverRole (roleList) {
      let hasApproveFlag = false
      if (roleList && roleList.length > 0) {
        for (let role of roleList) {
          if (role.roleId === '2') {
            hasApproveFlag = true
            break
          }
        }
      }
      return hasApproveFlag
    },
    calIsCurrentApprover (approverList, orderStatus, loginUser, isBothAppr) {
      let isCurrentApprover = false
      if (orderStatus === 2 || orderStatus === 3) {
        if (approverList && approverList.length > 0) {
          for (let approver of approverList) {
            if (approver.isApp === 'S') {
              let apprTo = approver.appTo
              if (isBothAppr) {
                isCurrentApprover = loginUser === approver.appMain || loginUser === apprTo
              } else {
                isCurrentApprover = (!apprTo && loginUser === approver.appMain) || loginUser === apprTo
              }
              if (isCurrentApprover) {
                break
              }
            }
          }
        }
      }
      return isCurrentApprover
    },
    calIntlIsLock (order) {
      let lockFlag = false
      let self = this
      if (order.existIntlAir || order.existIntlFlight) {
        if (!self.intlQCMode && order.newIntlAirList) {
          for (let intlAir of order.newIntlAirList) {
            if (intlAir.packageType === 'NORMAL' || intlAir.packageType === 'REQUEST') {
              lockFlag = intlAir.lockFlag === 1
              if (lockFlag) {
                break
              }
            }
          }
        }
      }
      return lockFlag
    },
    loadTaDetails () {
      OrderService.getTADetailForAuth({ taNo: this.currentTaNo }).then(data => {
        if (data && data.success && data.resultData && data.resultData.taDetail) {
          const order = this.order = data.resultData.taDetail
          this.$store.dispatch('Order/storeOrderDetail', this.order)
          this.initOrderButton(this.order)
          this.domAirSegments = this.initDomAirSegment(this.order.newDomAirList)
          if (order.historyFlag !== 1 && order.tcUserId) {
            this.orderErrorMessage = this.$t('order.msg.offlineOrder')
          } else if (order.historyFlag === 1) {
            this.orderErrorMessage = this.$t('order.msg.historyOrder')
          }
        } else {
          this.$coreError(this.$t('order.msg.accessDenied')).then(() => {
            this.$back()
          })
        }
      })
    },
    toIntlAir () {
      this.$goto('/order/inter/detail')
    },
    toDomAir () {
      this.$goto('/order/domair/detail')
    },
    toProduct (productType) {
      this.$goto(`/order/${productType}/detail`)
    },
    initDomAirSegment (domAirList) {
      return OrderService.initDomAirSegment(domAirList)
    }
  }
}
</script>

<style scoped>

</style>
