<template>
  <f7-page page-content :infinite="true"
           @infinite="onInfinite"
           :infinite-preloader="config.hasMoreNews"
           :ptr="true" @ptr:refresh="onPtrRefresh">
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')" back-link-force></f7-nav-left>
      <f7-nav-title>
        {{$t('common.label.newsList')}}
      </f7-nav-title>
    </f7-navbar>
    <!--loading skeleton-->
    <div v-if="config.searching && searchNewsList.length===0">
      <f7-list class="skeleton-text" media-list>
        <f7-list-item :key="n" v-for="n in 10">
          <div slot="title">000000000000000000000000000000000</div>
          <div slot="subtitle">0000000000</div>
          <div slot="text">
            0000000000000000000000000000000000000000000000
            00000000000000000000
          </div>
        </f7-list-item>
      </f7-list>
    </div>
    <f7-list media-list>
      <f7-list-item v-if="!config.searching && searchNewsList.length===0">
        {{$t('common.msg.noresult')}}
      </f7-list-item>
      <f7-list-item v-for="news in searchNewsList"
                    :key="news.id"
                    @click="gotoDetail(news)"
                    link>
        <div slot="title">
          {{news.title}}
        </div>
        <div slot="subtitle">
          {{news.createTime|date('YYYY-MM-DD HH:mm')}}
        </div>
        <div slot="text">
          <div>
            {{news.content|summary}}
          </div>
        </div>
      </f7-list-item>
    </f7-list>
  </f7-page>
</template>

<script>
import merge from 'lodash/merge'
import concat from 'lodash/concat'
import AdminNewsService from '../../services/news/AdminNewsService'

export default {
  name: 'news',
  data () {
    const searchParam = AdminNewsService.getNewsListParam()
    return {
      config: {
        searching: false,
        hasMoreNews: false
      },
      searchParam,
      searchNewsList: []
    }
  },
  mounted () {
    this.doSearchNews(1)
  },
  methods: {
    onInfinite () {
      if (!this.config.searching && this.config.hasMoreNews) {
        this.doSearchNews(this.searchParam.pageSetting.pageNumber + 1, true)
      }
    },
    onPtrRefresh (event, done) {
      if (!this.config.searching) {
        this.doSearchNews(1).then(done)
      }
    },
    gotoDetail (news) {
      this.$goto(`/news/newsDetail/${news.id}`)
    },
    doSearchNews (pageNumber, append) {
      if (this.config.searching) {
        return
      }
      merge(this.searchParam, {
        pageSetting: {
          pageNumber
        }
      })
      console.info('search,.........', this.searchParam)
      this.config.searching = true
      if (!append) {
        this.$$('.page-content').scrollTo(0, 0)
      }
      return AdminNewsService.getNewsList(this.searchParam, { loading: !append }).then(data => {
        this.config.searching = false
        if (data && data.success && data.resultData) {
          const pageSetting = data.resultData.pageSetting
          Object.assign(this.searchParam.pageSetting, pageSetting || {})
          this.config.hasMoreNews = pageSetting.pageNumber < this.calcPageCount(pageSetting)
          this.storeNewsCounts(this.searchParam.language, pageSetting.totalCount)
          if (!append) {
            this.searchNewsList = data.resultData.newsDtos || []
          } else {
            this.searchNewsList = concat(this.searchNewsList, data.resultData.newsDtos || [])
          }
        }
      })
    },
    calcPageCount (pageSetting) { // news接口有点问题，没有返回页数
      return parseInt((pageSetting.totalCount + pageSetting.pageSize) / pageSetting.pageSize)
    },
    storeNewsCounts (lang, totalCount) {
      const newsCounts = {}
      newsCounts[`${lang}Loaded`] = true
      newsCounts[`${lang}`] = totalCount
      this.$store.dispatch('storeNewsCounts', newsCounts)
    }
  }
}
</script>

<style scoped>

</style>
