<template>
  <f7-block style="margin: 15px 0;">
  <f7-row v-if="currentBtns && currentBtns.length>0">
    <f7-col>
      <f7-button fill class="button-large" v-if="currentBtns.length===1" @click="currentBtns[0].click(productOrder)">{{currentBtns[0].title}}</f7-button>
      <f7-button fill class="button-large" v-if="currentBtns.length>1" @click="showMoreBtns(refName)">{{$t('common.label.operate')}}</f7-button>
      <f7-actions :ref="refName">
        <f7-actions-group>
          <f7-actions-button :key="btn.title" v-for="btn in currentBtns"  @click="btn.click(productOrder)">{{btn.title}}</f7-actions-button>
        </f7-actions-group>
        <f7-actions-group>
          <f7-actions-button color="red">{{$t('common.label.cancel')}}</f7-actions-button>
        </f7-actions-group>
      </f7-actions>
    </f7-col>
  </f7-row>
  </f7-block>
</template>
<script>
export default {
  name: 'order-product-button',
  props: {
    currentBtns: {
      type: Array
    },
    refName: {
      type: String
    },
    productOrder: {
      type: Object
    }
  },
  data () {
    return { }
  },
  methods: {
    showMoreBtns (btnRef) {
      this.$refs[btnRef].open()
    }
  },
  mounted () {

  }
}
</script>

<style scoped>

</style>
