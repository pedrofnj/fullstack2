import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPersist from 'pinia-plugin-persistedstate'
import 'vuetify/styles'
import App from '../App.vue'
import router from './router'
import vuetify from './vuetify'

const pinia = createPinia()
pinia.use(piniaPersist)

createApp(App).use(pinia).use(router).use(vuetify).mount('#app')
