import { default as solace } from '@/lib/solclient.js'

export default {
  install: function(Vue, options) {
    Object.defineProperty(Vue.prototype, '$solace', { value: solace });
  }
}
