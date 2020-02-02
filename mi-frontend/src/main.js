import Vue from 'vue';
import App from './App';
import router from './router';
import Vuetify from 'vuetify';
import {store} from './store';
import 'vuetify/dist/vuetify.min.css';
import axios from 'axios';
import VueAxios from 'vue-axios';
import 'vis/dist/vis.css';
import Toolbar from './components/Toolbar';
import Urls from './constants/Urls';
import VueLoadImage from 'vue-load-image';
import DatetimePicker from 'vuetify-datetime-picker';
import 'vuetify-datetime-picker/src/stylus/main.styl';
import 'vuetify-stylus-fixed-table-header/index.styl';
import MapPanel from './components/MapPanel';
import GeoJsonUploadPopup from './components/GeoJsonUploadPopup'
import AddPolygonPanel from './components/AddPolygonPanel'
import JsonTree from './components/JsonTree'

export const eventBus = new Vue();
store.eventBus = eventBus;
window.eventBus = eventBus;

Vue.use(VueAxios, axios.create({
  baseURL: Urls.HTTP_BASE
}));
store.axios = Vue.prototype.axios;

Vue.use(Vuetify, {
  theme: {
    primary: '#FDB836',
    secondary: '#b0bec5',
    tertiary: '#fff8dc7a',
    accent: '#8c9eff',
    error: '#b71c1c'
  }
});
Vue.use(DatetimePicker);

Vue.config.productionTip = false;

Vue.component('add-polygon-panel', AddPolygonPanel);
Vue.component('geo-json-upload-popup', GeoJsonUploadPopup);
Vue.component('json-tree', JsonTree);
Vue.component('map-panel', MapPanel);
Vue.component('vue-load-image', VueLoadImage);
Vue.component('toolbar', Toolbar);

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
});
