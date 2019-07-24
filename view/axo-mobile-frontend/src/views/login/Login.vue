<template>
  <f7-page login-screen>
    <f7-login-screen-title>{{$t('common.label.loginTitle')}}</f7-login-screen-title>
    <f7-block>
      <f7-list form>
        <f7-list-input
          clear-button
          name="companyCode"
          v-validate="'required'"
          :data-vv-as="$t('common.label.companyCode')"
          type="text"
          :placeholder="$t('common.label.companyCode')"
          :value="loginParam.companyCode"
          @input="loginParam.companyCode = $event.target.value">
          <f7-icon f7="business" slot="media"></f7-icon>
        </f7-list-input>
        <f7-list-input
          clear-button
          name="username"
          v-validate="'required'"
          :data-vv-as="$t('common.label.loginName')"
          type="text"
          :placeholder="$t('common.label.loginName')"
          :value="loginParam.loginName"
          @input="loginParam.loginName = $event.target.value">
          <f7-icon f7="person" slot="media"></f7-icon>
        </f7-list-input>
        <f7-list-input
          clear-button
          name="password"
          v-validate="'required'"
          :data-vv-as="$t('common.label.password')"
          type="password"
          :placeholder="$t('common.label.password')"
          :value="loginParam.password"
          @input="loginParam.password = $event.target.value">
          <f7-icon f7="lock" slot="media"></f7-icon>
        </f7-list-input>
        <li v-if="errors">
          <common-form-errors :value="errorMessage||errors.all()"/>
        </li>
        <f7-block>
          <f7-row>
            <f7-col>
              <f7-button fill large :disabled="!formValidator.isFormValid" type="submit" @click="signIn">
                {{$t('common.label.login')}}
              </f7-button>
            </f7-col>
          </f7-row>
        </f7-block>
      </f7-list>
      <f7-block>
        <f7-row>
          <f7-col>
            <f7-link @click="gotoOnlineSso()" v-if="$isOnlineSsoEnabled()">{{$t('common.label.desktopVersion')}}</f7-link>
          </f7-col>
        </f7-row>
        <f7-row>
          <f7-col width="50">
            <template v-show="ssoConfigList.length>0" v-for="ssoConfig in ssoConfigList">
              <f7-link :key="ssoConfig.url" :href="ssoConfig.url" external target="_self">
                {{$t('login.msg.ssoLogin')}}
                <span v-if="ssoConfigList.length>1">
                  {{`[${ssoConfig.key.replace('mobile@', '')}]`}}
                </span>
              </f7-link>
            </template>
          </f7-col>
          <f7-col width="50">
            <f7-segmented raised>
              <f7-button :key="locale.locale" v-for="locale in localeList" :active="$isLocale(locale.locale)"
                         @click="$changeLocale(locale.locale);$f7router.refreshPage()">{{locale.label}}
              </f7-button>
            </f7-segmented>
          </f7-col>
        </f7-row>
      </f7-block>
    </f7-block>
  </f7-page>
</template>

<script>
import debounce from 'lodash/debounce'
import LoginService from '../../services/login/LoginService'

const LOGIN_PARAM_KEY = 'axo-mobile-login-param'
const EXTERNAL_SSO_TYPES = ['saml', 'oauth', 'cas']

export default {
  data () {
    this.$localStorage.addProperty(LOGIN_PARAM_KEY, Object)
    const loginParam = this.$localStorage.get(LOGIN_PARAM_KEY, {
      companyCode: '',
      loginName: '',
      password: ''
    })
    return {
      formValidator: {},
      config: {},
      loginParam,
      errorMessage: '',
      ssoConfigList: []
    }
  },
  computed: {
    localeList () {
      return this.$getSupportLocales()
    }
  },
  watch: {
    'loginParam.companyCode': debounce(function (v) {
      console.info('company code .... ', v)
      this.ssoConfigList = []
      if (v) {
        this.loadSsoLoginConfig(v)
      }
    }, 500)
  },
  methods: {
    signIn ($event) {
      LoginService.login(this.loginParam).then(data => {
        this.$localStorage.set(LOGIN_PARAM_KEY, {
          ...this.loginParam,
          password: ''
        })
        this.loginResponse(data)
      })
      $event.preventDefault()
    },
    loginResponse (data) {
      if (data.success && data.resultData) {
        this.$store.dispatch('loginResult', data.resultData)
        let index = '/'
        const ssoParams = data.resultData.ssoParams
        if (ssoParams) {
          if (ssoParams.gotoPath) {
            index = ssoParams.gotoPath
          } else if (ssoParams.taNo) {
            index = `/order/detail/${ssoParams.taNo}`
          }
          if (ssoParams.externalOrderNo) {
            this.$store.dispatch('Order/storeSsoExternalOrderNo', ssoParams.externalOrderNo)
          }
        }
        this.gotoIndex(index)
      } else {
        this.errorMessage = data.message || data.error
      }
    },
    gotoIndex (index) {
      this.$goto(this.$f7router.history[0] || '/', {
        force: true,
        ignoreCache: true,
        clearPreviousHistory: true
      }).then(() => {
        if (index !== '/') {
          this.$goto(index)
        }
      })
    },
    loadSsoLoginConfig (companyCode) {
      LoginService.ssoLoginConfig(companyCode, { loading: false }).then(data => {
        if (data && data.success && data.resultData && data.resultData.length > 0) {
          const ssoConfigList = data.resultData.filter(ssoConfig => this.$checkItemIndex(EXTERNAL_SSO_TYPES, ssoConfig.type) > -1)
          ssoConfigList.forEach(ssoConfig => {
            ssoConfig.url = ssoConfig.url || `${process.env.VUE_APP_API_GATEWAY}/sso/${ssoConfig.type}?companyId=${ssoConfig.companyCode.toLowerCase()}&ssoKey=${ssoConfig.key}`
          })
          this.ssoConfigList = ssoConfigList
          console.info(this.ssoConfigList)
        }
      })
    },
    gotoOnlineSso: LoginService.gotoOnlineSso
  },
  mounted () {
    const globalConfig = this.$store.getters.globalConfig
    if (globalConfig.isLoginIn) {
      this.gotoIndex('/')
      return
    }
    this.$initFormValidate()
    this.$nextTick(() => {
      if (this.$f7route && this.$f7route.path.startsWith('/sso&access_token')) {
        const query = this.$utils.parseUrlQuery(this.$f7route.path.replace('/sso&', '/sso?'))
        if (query.access_token) {
          LoginService.loginByToken(query.access_token, { loading: this.$t('login.msg.ssoLogining') }).then(data => {
            this.loginResponse(data)
          })
        }
      }
    })
    if (this.loginParam.companyCode && !this.$isCordova()) {
      this.loadSsoLoginConfig(this.loginParam.companyCode)
    }
  }
}
</script>
