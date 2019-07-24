<template>
  <f7-page page-content>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"/>
      <f7-nav-title>{{$t('common.label.order')}}</f7-nav-title>
    </f7-navbar>
    <f7-list inline-labels no-hairlines-md>
      <f7-list-input  type="textarea" :value="taApprovalParam.comment" @input="changeComment($event.target)">
        <div slot="label">
          <span>{{$t('common.label.remark')}}</span>
        </div>
      </f7-list-input>
    </f7-list>
    <f7-block>
      <f7-row>
        <f7-col>
          <f7-button fill large  @click="approveOrder">
            {{approveType==='a'?$t('common.label.approve'):$t('common.label.reject')}}
          </f7-button>
        </f7-col>
      </f7-row>
    </f7-block>
  </f7-page>
</template>

<script>
import OrderService from '../../services/order/OrderService'
import { mapGetters } from 'vuex'

export default {
  name: 'order-approve',
  data: function () {
    const currentTaNo = this.$f7route.params.taNo
    const approveType = this.$f7route.params.type
    let taApprovalParam = { taNo: currentTaNo, comment: '' }
    return {
      currentTaNo, approveType, taApprovalParam
    }
  },
  computed: {
    ...mapGetters(['currentLoginUser'])
  },
  methods: {
    approveOrder () {
      let self = this
      if (self.taApprovalParam.comment && self.taApprovalParam.comment.length > 1500) {
        self.$coreError(self.$i18nBundle('common.msg.approveCommont'))
        return
      }
      let method = 'rejectTa'
      if (self.approveType === 'a') {
        method = 'approveTa'
      }
      let { userId } = this.currentLoginUser
      self.taApprovalParam.approverId = userId
      self.taApprovalParam.approveDate = new Date()
      self.taApprovalParam.syncFlag = true
      OrderService[method](self.taApprovalParam).then(data => {
        if (data && data.success) {
          self.$back({ force: true })
        } else if (data) {
          self.$coreError(data.message)
        }
      })
    },
    changeComment (target) {
      let self = this
      self.taApprovalParam.comment = target.value
    }
  }
}
</script>

<style scoped>

</style>
