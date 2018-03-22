import { default as solace } from '@/lib/solclient.js'

export default {
  install: function(Vue, options) {
    console.log(solace);
    Object.defineProperty(Vue.prototype, '$solace', { value: solace });
  }
}
