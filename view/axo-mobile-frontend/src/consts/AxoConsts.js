const COUNT_HELPER = {
  count: 0,
  increment () {
    this.count++
    return this
  },
  decrement () {
    this.count--
    return this
  },
  isHide () {
    return this.count <= 0
  }
}

const TIME_CONFIG_REGEXP = /(\d+)([dhmws]*)/

export default {
  COUNT_HELPER,
  TIME_CONFIG_REGEXP
}
