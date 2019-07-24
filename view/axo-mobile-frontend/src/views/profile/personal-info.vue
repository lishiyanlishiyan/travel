<template>
  <f7-page>
    <f7-list media-list>
      <f7-list-item
        :title="$i18nMsg(globalConfig.currentLoginUser.nameCN, globalConfig.currentLoginUser.nameEN)"
        :subtitle="globalConfig.currentLoginUser.email">
        <f7-icon slot="media" size="44px" f7="person"></f7-icon>
      </f7-list-item>
    </f7-list>
    <f7-block-title>{{$t('profile.label.myProfile')}}</f7-block-title>
    <f7-list>
      <f7-list-item link="/personal-data" :title="$t('profile.label.basicPersonalInfo')">
        <f7-icon slot="media" f7="person"></f7-icon>
      </f7-list-item>
      <f7-list-item link="/id-info" :title="$t('profile.label.certInfo')">
        <f7-icon slot="media" f7="favorites_fill"></f7-icon>
      </f7-list-item>
      <f7-list-item link="/travel-secretary" :title="$t('profile.label.travelArranger')">
        <f7-icon slot="media" f7="persons"></f7-icon>
      </f7-list-item>
      <f7-list-item link="/acting-approver" :title="$t('profile.label.alternativeApprover')">
        <f7-icon slot="media" f7="person_round"></f7-icon>
      </f7-list-item>
      <f7-list-item link="/memberships" :title="$t('profile.label.membership')">
        <f7-icon slot="media" f7="favorites_alt_fill"></f7-icon>
      </f7-list-item>
      <f7-list-item link="/credit-card" :title="$t('profile.label.creditCard')">
        <f7-icon slot="media" f7="card_fill"></f7-icon>
      </f7-list-item>
      <f7-list-item link="/travel-prefer" :title="$t('profile.label.travelPreference')">
        <f7-icon slot="media" f7="heart_fill"></f7-icon>
      </f7-list-item>
    </f7-list>
    <f7-block-title>{{$t('profile.label.setting')}}</f7-block-title>
    <f7-list>
      <f7-list-item link="#" :title="$t('profile.label.changeLanguage')" smart-select
                    :smart-select-params="{openIn: 'sheet', closeOnSelect: true}">
        <label>
          <select @change="$changeLocaleInternal($event.target.value)">
            <option :key="index"
                    v-for="(locale, index) in localeList"
                    :value="locale.locale"
                    :selected="$isLocale(locale.locale)">
              {{locale.label}}
            </option>
          </select>
        </label>
        <f7-icon slot="media" f7="world"></f7-icon>
      </f7-list-item>
      <f7-list-item link :title="$t('common.label.contactCitsgbt')"
                    @click="showContactInfo">
        <f7-icon slot="media" f7="phone_fill"></f7-icon>
      </f7-list-item>
      <f7-list-item link="/feedback" :title="$t('profile.label.feedbackLabel')">
        <f7-icon slot="media" f7="message_fill"></f7-icon>
      </f7-list-item>
      <f7-list-item link="/dev" v-if="developmentFlag" :title="$t('profile.label.developmentTool')">
        <f7-icon slot="media" f7="world_fill"></f7-icon>
      </f7-list-item>
    </f7-list>
    <f7-list>
      <f7-list-button @click="$logout" class="logout" :title="$t('profile.label.loginOut')">
        <f7-icon slot="media" f7="exit"></f7-icon>
      </f7-list-button>
    </f7-list>
  </f7-page>

</template>

<script>
import { mapState } from 'vuex'
import { ProductType } from '../../consts/OrderConsts'

export default {
  name: 'personal-info',
  data () {
    const developmentFlag = process.env.NODE_ENV === 'development'
    return {
      developmentFlag
    }
  },
  computed: {
    localeList () {
      return this.$getSupportLocales()
    },
    ...mapState(['globalConfig'])
  },
  mouted () {
    // console.log(globalConfig)
  },
  methods: {
    $changeLocaleInternal (locale) {
      this.$changeLocale(locale)
      this.$f7router.refreshPage()
    },
    showContactInfo () {
      const companyInfo = this.$defaultSetup(ProductType.CompanyInfo)
      this.$coreShowPopup(this.$i18nMsg(companyInfo.contactRemarkCN, companyInfo.contactRemarkEN), this.$t('common.label.contactCitsgbt'))
    }
  }
}
</script>
<style scoped>

</style>
