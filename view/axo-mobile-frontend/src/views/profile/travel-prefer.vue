<template>
  <f7-page :page-content="false">
    <f7-navbar>
      <f7-nav-left :back-link="$t('common.label.back')"/>
      <f7-nav-title :title="$t('profile.label.travelPreference')"/>
      <f7-nav-right>
        <f7-link @click="save">{{$t('common.label.save')}}</f7-link>
      </f7-nav-right>
    </f7-navbar>
    <f7-list inline-labels no-hairlines-md style="margin-top:60px">
      <f7-list-item :title="$t('profile.label.preferSeat')" v-if="travelPreference.userId" smart-select
                    :smart-select-params="{openIn: 'sheet'}">
        <label>
          <select name="preferSeat"
                  :value="travelPreference.seatPreferenceCode"
                  @change="travelPreference.seatPreferenceCode= $event.target.value"
          >
            <option v-for="(val,index) in seatList "
                    :key="index"
                    :value="val.code"
                    :selected="val.nameCN"
            >
              {{$i18nMsg(val.nameCN,val.nameEN)}}
            </option>
          </select>
        </label>
      </f7-list-item>
      <f7-list-item :title="$t('profile.label.preferFood')" v-if="travelPreference.userId"
                    smart-select :smart-select-params="{openIn: 'sheet'}">
        <label>
          <select name="preferFood"
                  :value="travelPreference.mealPreferenceCode"
                  @change="travelPreference.mealPreferenceCode= $event.target.value"
          >
            <option v-for="(val,index) in mealList "
                    :key="index"
                    :value="val.code"
                    :selected="val.nameCN">
              {{$i18nMsg(val.nameCN,val.nameEN)}}
            </option>
          </select>
        </label>
      </f7-list-item>

    </f7-list>
  </f7-page>
</template>

<script>
import ProfileService from '../../services/profile/ProfileService'
import { mapGetters } from 'vuex'
import cloneDeep from 'lodash/cloneDeep'

export default {
  data () {
    return {
      seatList: [],
      mealList: [],
      travelPreference: {},
      changeData: {
        userId: '',
        travelPreference: []
      }
    }
  },
  computed: {
    ...mapGetters(['loginResult', 'currentLoginUser'])
  },
  mounted () {
    ProfileService.getSeatPrefTypes().then(res => {
      this.seatList = res.resultData.seatPrefs
      console.log(res)
    })
    ProfileService.getMealPrefTypes().then(res => {
      this.mealList = res.resultData.mealPrefs
      console.log(res)
    })
    this.loadPref()
  },
  methods: {
    loadPref () {
      let { userId } = this.currentLoginUser
      ProfileService.getPreference({ userId }).then(res => {
        if (res.resultData.travelPreference) {
          this.travelPreference = res.resultData.travelPreference
        } else {
          // res.resultData.travelPreference = new {Object({})}
          res.resultData.travelPreference = this.newTravelpre()
          this.travelPreference = res.resultData.travelPreference
        }
        console.log(res)
        console.log(this.travelPreference)
      })
    },
    newTravelpre () {
      let { userId } = this.$store.getters.currentLoginUser
      return {
        id: '',
        mealPreferenceCode: '',
        mealPreferenceNameCN: '',
        mealPreferenceNameEN: '',
        mealPreferenceRemark: '',
        seatPreferenceCode: '',
        seatPreferenceNameCN: '',
        seatPreferenceNameEN: '',
        seatPreferenceRemark: '',
        userId
      }
    },
    save () {
      this.changeData.travelPreference = cloneDeep(this.travelPreference)
      this.changeData.userId = this.travelPreference.userId
      console.log(this.changeData)
      ProfileService.updatePreference(this.changeData).then(res => {
        console.log(res)
        if (res.success) {
          let updateTravelPreference = this.travelPreference
          this.travelPreference = updateTravelPreference
          console.log(updateTravelPreference)
          this.loadPref()
        } else {
          this.$coreError(res.message)
        }
      })
    }
  }
}
</script>
