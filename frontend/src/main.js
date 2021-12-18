import Vue from 'vue'
import VueRouter from "vue-router"
import router from "@/router"
import App from './App.vue'
import { BootstrapVue, BootstrapVueIcons  } from 'bootstrap-vue'
import VueClipboard from "vue-clipboard2"

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(VueRouter)
Vue.use(BootstrapVue)
Vue.use(BootstrapVueIcons)
Vue.use(VueClipboard)

Vue.config.productionTip = false

new Vue({
    router,
    render: h => h(App),
}).$mount('#app')
