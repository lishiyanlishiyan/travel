<template>
  <f7-page ptr @ptr:refresh="ptrRefresh">
    <f7-list class="myTrip">
      <f7-list-item v-if="timelineMap.length===0">
        {{$t('common.msg.noresult')}}
      </f7-list-item>
      <div v-for="[timelineKey, timelines] in timelineMap" :key="timelineKey">
        <div class="block-title">{{timelineKey}}</div>
        <div class="timeline axo">
          <div class="timeline-item" :key="index" v-for="(timeline, index) in timelines">
            <div class="timeline-item-date">{{timeline.date|date('MM-DD')}}
              <small>{{timeline.date|date('YYYY')}}</small>
            </div>
            <div class="timeline-item-divider"></div>
            <div class="timeline-item-content">
              <!--eslint-disable-next-line-->
              <div class="timeline-item-inner" :key="domAirOrder.id" v-if="timeline.domAirList.length"
                   v-for="domAirOrder in timeline.domAirList">
                <timeline-domair :product-order="domAirOrder"/>
              </div>
              <div class="timeline-item-inner" :key="intlAirOrder.id" v-for="intlAirOrder in timeline.intlAirList">
                <div class="timeline-item-time">
                  <f7-icon size="25" material="flight" color="blue"/>
                </div>
              </div>
              <div class="timeline-item-inner" :key="trainOrder.id" v-for="trainOrder in timeline.trainList">
                <timeline-train :product-order="trainOrder"/>
              </div>
              <div class="timeline-item-inner" :key="carRentalOrder.id" v-for="carRentalOrder in timeline.carRentalList">
                <div class="timeline-item-time">
                  <f7-icon size="25" material="local_taxi" color="blue"/>
                </div>
              </div>
              <div class="timeline-item-inner" :key="otherOrder.id" v-for="otherOrder in timeline.otherList">
                <div class="timeline-item-time">
                  <f7-icon size="25" f7="document_fill" color="blue"/>
                </div>
              </div>
              <div class="timeline-item-inner" :key="hotelOrder.id" v-for="hotelOrder in timeline.hotelList">
                <timeline-hotel :product-order="hotelOrder"/>
              </div>
            </div>
          </div>
        </div>
      </div>
    </f7-list>
  </f7-page>
</template>

<script>
import { mapGetters } from 'vuex'
import moment from 'moment'
import OrderService from '../../services/order/OrderService'
import TimelineDomair from './timeline/timeline-domair'
import TimelineTrain from './timeline/timeline-train'
import TimelineHotel from './timeline/timeline-hotel'

const orderTimelineKey = 'Order/orderTimelineList'
export default {
  name: 'my-trip',
  components: { TimelineHotel, TimelineTrain, TimelineDomair },
  props: {
    initMyTrip: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      timelineMap: [],
      timelineList: []
    }
  },
  mounted () {
  },
  watch: {
    initMyTrip: function () {
      this.timelineList = this[orderTimelineKey]
      if (!this.timelineList || this.timelineList.length === 0) {
        this.searchOrderTimeline()
      } else {
        this.calcOrderTimeline()
      }
    }
  },
  computed: {
    ...mapGetters(['currentLoginUser', orderTimelineKey])
  },
  methods: {
    ptrRefresh (event, done) {
      this.searchOrderTimeline().then(done)
    },
    searchOrderTimeline () {
      return OrderService.searchOrderTimeline({
        userId: this.currentLoginUser.userId,
        companyCode: this.currentLoginUser.companyCode
      }).then(data => {
        if (data && data.success && data.resultData) {
          this.timelineList = data.resultData.timelineList || []
          this.calcOrderTimeline()
          this.$store.dispatch('Order/storeOrderTimelineList', this.timelineList)
        }
      })
    },
    calcOrderTimeline () {
      const timelineMap = new Map()
      this.timelineList.forEach(timeline => {
        let monthKey = moment(timeline.date).format(this.$i18nMsg('YYYY年M月', 'MMMMM YYYY'))
        let timelines = timelineMap.get(monthKey) || []
        timelineMap.set(monthKey, timelines)
        timelines.push(timeline)
      })
      this.timelineMap = [...timelineMap]
    }
  }
}
</script>

<style scoped>

</style>
