<template>
  <f7-page>
    <f7-navbar title="Not found" back-link="Back"></f7-navbar>
    <f7-block strong>
      <p>Sorry</p>
      <p>Requested content not found.</p>
    </f7-block>
  </f7-page>
</template>
<script>
export default {
  mounted () {
    console.info(this.$f7route)
    if (this.$f7route.path.startsWith('/axo-mobile')) {
      this.$f7router.navigate('/', { ignoreCache: true, reloadCurrent: true })
    }
  }
}
</script>
