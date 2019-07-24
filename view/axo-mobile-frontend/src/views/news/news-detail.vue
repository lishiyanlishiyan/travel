<template>
  <f7-page page-content>
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"></f7-nav-left>
      <f7-nav-title>
        {{newsDetail.title||$t('common.label.newsDetail')}}
      </f7-nav-title>
    </f7-navbar>
    <f7-card class="newsDetail">
      <f7-card-header>
        <f7-row>
          <f7-col>
            {{newsDetail.title}}
          </f7-col>
        </f7-row>
        <f7-row>
          <f7-col>
            {{newsDetail.createTime|date('YYYY-MM-DD HH:mm')}}
          </f7-col>
        </f7-row>
      </f7-card-header>
      <f7-card-content>
        <div v-html="newsDetail.content"></div>
      </f7-card-content>
      <f7-card-footer v-if="newsDetail.fileName && newsDetail.fileNameOld">
        <div>
          <f7-icon f7="attachment"></f7-icon>
          {{newsDetail.fileNameOld}}
        </div>
      </f7-card-footer>
    </f7-card>
  </f7-page>
</template>

<script>
import AdminNewsService from '../../services/news/AdminNewsService'

export default {
  name: 'news-detail',
  data () {
    const newsId = this.$f7route.params.id
    return {
      newsDetail: {},
      newsId
    }
  },
  mounted () {
    this.loadNewsDetail()
  },
  methods: {
    loadNewsDetail () {
      AdminNewsService.loadNewsDetail({
        id: this.newsId
      }).then(data => {
        if (data && data.resultData && data.resultData.newsDetail) {
          this.newsDetail = data.resultData.newsDetail
          this.calcLink()
        } else {
          this.$back()
        }
      })
    },
    calcLink () {
      this.$nextTick(() => {
        this.$$('.newsDetail a').addClass('external').attr('target', '_blank')
        //   .click(event => {
        //   event.preventDefault()
        // })
      })
    }
  }
}
</script>

<style scoped>

</style>
