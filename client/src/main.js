// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
// Webpack CSS and plugin import
import 'onsenui/css/onsenui.css'
import 'onsenui/css/onsen-css-components.css'
import VueOnsen from 'vue-onsenui'
// import SolclientjsPlugin from './plugins/solclientjsPlugin'

Vue.config.productionTip = false;
// Vue.use(SolclientjsPlugin);
Vue.use(VueOnsen);

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>'
})
