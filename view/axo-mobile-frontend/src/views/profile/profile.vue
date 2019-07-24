<template>
  <f7-page :page-content="false">
    <f7-navbar>
      <f7-toolbar tabbar>
        <f7-link :key="profile.id"
                 v-for="profile in profiles"
                 :tab-link="'#index-profile-' + profile.id"
                 :tab-link-active="currentProfile===profile"
                 :text="profile.label"></f7-link>
      </f7-toolbar>
    </f7-navbar>
    <f7-tabs animated>
      <f7-page-content
        @tabShow="currentProfile=profileMap.personal"
        tab id="index-profile-personal" :tab-active="currentProfile===profileMap.personal">
        <personal-info v-if="currentProfile===profileMap.personal"/>
      </f7-page-content>
      <f7-page-content
        @tabShow="currentProfile=profileMap.mytrip"
        :tab-active="currentProfile===profileMap.mytrip"
        tab id="index-profile-mytrip">
        <my-trip :init-my-trip="currentProfile===profileMap.mytrip"/>
      </f7-page-content>
    </f7-tabs>
  </f7-page>
</template>

<script>
import MyTrip from './my-trip'
import PersonalInfo from './personal-info'
import fromPairs from 'lodash/fromPairs'

console.info('into profile')
export default {
  name: 'profile',
  components: { PersonalInfo, MyTrip },
  data () {
    console.info('data profile')
    const profiles = [{
      id: 'personal',
      label: this.$i18nBundle('profile.label.myProfile')
    }, {
      id: 'mytrip',
      label: this.$i18nBundle('order.label.myTrip')
    }]
    const profileMap = fromPairs(profiles.map(profile => [profile.id, profile]))
    const currentProfile = profiles[0]
    return {
      profiles,
      profileMap,
      currentProfile
    }
  },
  methods: {}
}
</script>

<style scoped>

</style>
