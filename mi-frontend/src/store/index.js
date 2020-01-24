import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

export const store = new Vuex.Store({
  state: {
    geojson: [],
  },
  getters: {
    geojson (state) {
      return state.geojson;
    }
  },
  mutations: {
  },
  actions: {
  }
});
