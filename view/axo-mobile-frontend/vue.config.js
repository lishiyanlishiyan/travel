module.exports = {
  publicPath: process.env.APP_CONTEXT_PATH,
  outputDir: 'dist',
  filenameHashing: true,
  productionSourceMap: true, // 生产环境是否产生sourceMap？
  configureWebpack: config => {
    if (process.env.NODE_ENV === 'production') {
      // 为生产环境修改配置...
    } else {
      // 为开发环境修改配置...
    }
  },
  devServer: {
    // open: process.platform === 'darwin',
    host: '0.0.0.0',
    port: 8080,
    https: false,
    hotOnly: false,
    disableHostCheck: true
    // See https://github.com/vuejs/vue-cli/blob/dev/docs/cli-service.md#configuring-proxy
    // 服务器端配置org.springframework.web.filter.CorsFilter之后，测试环境也不再使用proxy配置
    // proxy: {
    //   '/axo-mobile-open': {
    //     target: 'http://127.0.0.1:7888/axo-mobile-open', // 需要请求的地址
    //     // target: 'http://10.181.57.212:7888/axo-mobile-open', // 需要请求的地址
    //     changeOrigin: true, // 是否跨域
    //     pathRewrite: {
    //       '^/axo-mobile-open': '/'
    //     }
    //   }
    // }
  }
}
