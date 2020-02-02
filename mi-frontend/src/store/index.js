import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

export const store = new Vuex.Store({
  state: {
    newPolygonGeoJson: null,
    polygonGeoJsonToUpload: [],
    uploadedPolygonGeoJson: [],
  },
  getters: {
    newPolygonGeoJson (state) {
      return state.newPolygonGeoJson;
    }
  },
  mutations: {
    SET_NEW_POLYGON_GEO_JSON (state, geoJson) {
      state.newPolygonGeoJson = geoJson;
    },
    ADD_POLYGON_GEO_JSON_TO_UPLOAD (state, geoJson) {
      state.polygonGeoJsonToUpload.push(geoJson);
    },
    CLEAR_POLYGON_GEO_JSON_TO_UPLOAD (state) {
      state.polygonGeoJsonToUpload = [];
    },
    MARK_POLYGON_GEO_JSON_AS_UPLOADED (state) {
      state.uploadedPolygonGeoJson = [...state.uploadedPolygonGeoJson, ...state.polygonGeoJsonToUpload];
      state.polygonGeoJsonToUpload = [];
    }
  },
  actions: {
    setNewPolygonGeoJson (context, geoJson) {
      context.commit('SET_NEW_POLYGON_GEO_JSON', geoJson)
    },
    addPolygonGeoJsonToUpload (context, geoJson) {
      context.commit('ADD_POLYGON_GEO_JSON_TO_UPLOAD', geoJson)
    },
    clearPolygonGeoJsonToUpload (context) {
      context.commit('CLEAR_POLYGON_GEO_JSON_TO_UPLOAD')
    },
    markPolygonGeoJsonAsUploaded (context) {
      context.commit('MARK_POLYGON_GEO_JSON_AS_UPLOADED')
    }
  }
});
