<template>
  <f7-page :page-content="false">
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"/>
      <f7-nav-title :title="$t('profile.label.feedbackTitle')"/>
    </f7-navbar>
    <f7-list inline-labels form class="page-content">
      <f7-list-item smart-select :smart-select-params="{openIn: 'popup', closeOnSelect: true}"
                    :class="errors.first($t('profile.label.feedbackType'))?'text-color-red':'text-color-blue'"
                    :title="$t('profile.label.feedbackType')">
        <select :value="feedback.typeId" @change="feedback.typeId=$event.target.value"
                :data-vv-name="$t('profile.label.feedbackType')"
                :placeholder="$t('profile.label.feedbackType')" v-validate="'required'">
          <option value="">{{$t('common.label.selectOption')}}</option>
          <optgroup :key="feedbackType.id" :label="$i18nMsg(feedbackType.nameCn, feedbackType.nameEn)"
                    v-for="feedbackType in feedbackTypes">
            <option :key="subType.id" :value="subType.id" :data-display-as="$i18nMsg(subType.nameCn, subType.nameEn)"
                    v-for="subType in feedbackType.children">
              {{$i18nMsg(subType.nameCn, subType.nameEn)}}
            </option>
          </optgroup>
        </select>
      </f7-list-item>
      <f7-list-input :value="feedback.content" @input="feedback.content=$event.target.value"
                     :class="errors.first($t('profile.label.feedbackContent'))?'text-color-red':'text-color-blue'"
                     :label="$t('profile.label.feedbackContent')"
                     type="textarea" :placeholder="$t('profile.label.feedbackContent')"
                     :data-vv-name="$t('profile.label.feedbackContent')" v-validate="'required'"/>
      <f7-button large fill @click="doFeedback" :disabled="!formValidator.isFormValid"
                 :text="$t('common.label.submit')"/>
    </f7-list>
  </f7-page>
</template>
<script>

import FeedbackService from '../services/feedback/FeedbackService'

export default {
  data () {
    const feedbackParam = this.newFeedbackParam()
    return {
      feedbackParam,
      feedback: feedbackParam.feedback,
      feedbackTypes: [],
      formValidator: {}
    }
  },
  mounted () {
    this.$initFormValidate()
    this.loadFeedbackTypes()
  },
  methods: {
    newFeedbackParam () {
      const currentLoginUser = this.$store.getters.currentLoginUser
      return {
        userId: currentLoginUser.userId,
        companyCode: currentLoginUser.companyCode,
        systemKey: 'AXO-MOBILE',
        feedback: {
          typeId: null,
          content: '',
          feedbackUrl: window.location.href,
          userAgent: window.navigator.userAgent
        }
      }
    },
    doFeedback () {
      console.info(this.feedbackParam)
      FeedbackService.doFeedback(this.feedbackParam).then(data => {
        if (data && data.success) {
          this.$coreAlert(this.$t('common.msg.submitSuccess')).then(() => {
            this.$back()
          })
        } else {
          this.$coreError(data.message)
        }
      })
    },
    calcFeedbackTypes (typeList, parent) {
      const result = []
      typeList.forEach(feedbackType => {
        if ((!parent && !feedbackType.parentId) || (parent && parent.id === feedbackType.parentId)) {
          result.push(feedbackType)
          feedbackType.children = this.calcFeedbackTypes(typeList, feedbackType)
        }
      })
      return result
    },
    loadFeedbackTypes () {
      FeedbackService.loadAvailableFeedbackTypes({}, { loading: false }).then(data => {
        this.feedbackTypes = this.calcFeedbackTypes(data.resultData.typeList)
        console.info('feedback type........', this.feedbackTypes)
      })
    }
  }
}
</script>
