<template>
  <f7-nav-right>
    <f7-link icon-f7="home_fill" icon-size="25" @click="backHomePage"/>
  </f7-nav-right>
</template>

<script>
export default {
  name: 'common-home',
  methods: {
    backHomePage ($event) {
      this.$backHomePage()
      $event.preventDefault()
    }
  }
}
</script>

<style scoped>

</style>
