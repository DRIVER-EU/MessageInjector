<template>
  <v-app>
    <toolbar>
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
        <v-btn slot="activator">
          <v-icon left>bar_chart</v-icon>SimpleMessages</v-btn>
        <v-card>
          <v-list>
            <v-list-tile @click="openPhaseMsgPopup()">
              <v-icon left>cloud_upload</v-icon>
              Phase Msg
            </v-list-tile>
            <v-list-tile @click="openSessionMgmtPopup()">
              <v-icon left>cloud_upload</v-icon>
              Session Mgmt
            </v-list-tile>
            <v-list-tile @click="openRolePlayerMsgPopup()">
              <v-icon left>cloud_upload</v-icon>
              Role Player Msg
            </v-list-tile>
            <v-list-tile @click="openPhaseOSTStageMsgPopup()">
              <v-icon left>cloud_upload</v-icon>
              OST Stage change
            </v-list-tile>
            <v-list-tile @click="openLogMsgPopup()">
              <v-icon left>cloud_upload</v-icon>
              Log Msg
            </v-list-tile>
            <v-list-tile @click="openEvalLogMsgPopup()">
              <v-icon left>cloud_upload</v-icon>
              Evaluation Log Msg
            </v-list-tile>
          </v-list>
        </v-card>
      </v-menu>
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
        <v-btn class="toolbarButton" slot="activator" @click="openCapUploadPopup">
          <v-icon left>cloud_upload</v-icon>
          CAP Upload
        </v-btn>
        <v-btn :disabled="!isGeometryUploadPossible" class="toolbarButton" slot="activator" @click="openGeoJsonUploadPopup">
          <v-badge overlap :color="!isGeometryUploadPossible ? 'rgba(0,0,0,0.26)' : 'rgba(0, 0, 0, 0.87)'">
            <template v-slot:badge>
              <span>{{numberOfPolygonsToUpload}}</span>
            </template>
            <v-icon left>cloud_upload</v-icon>
          </v-badge>
          Geometry Upload
        </v-btn>
      </v-menu>
    </toolbar>
    <main style="height: 100%">
      <v-layout column justify-space-between fill-height>
        <div style="position:absolute;top:64px;bottom:0px;left:0px;right:0px;">
          <v-layout row wrap fill-height>
            <div ref="mainFrame" style="position:absolute;top:0px;bottom:0px;left:0px;right:400px;">
              <map-panel :on-draw-polygon="handlePolygon"
                         :new-feature-geo-json="newFeatureGeoJson"
                         :feature-geo-json-to-upload="featureGeoJsonToUpload"
                         :uploaded-feature-geo-json="uploadedFeatureGeoJson"
              />
            </div>
            <div ref="detailsFrame" style="position:absolute;top:0px;bottom:0px;right:0px;width:400px;">
              <add-polygon-panel style="height: 100%; overflow: auto;" :onWidthChange="setDetailsWidth"/>
            </div>
          </v-layout>
        </div>
      </v-layout>
      <v-snackbar v-model="snackbar.visible" :top="true" :timeout="0" color="error">
        {{snackbar.text}}
        <v-btn flat @click="snackbar.visible = false">
          Close
        </v-btn>
      </v-snackbar>
    </main>
    <geo-json-upload-popup/>
    <cap-upload-popup/>
    <phase-msg-popup/>
    <log-msg-popup/>
    <eval-log-msg-popup/>
    <role-player-msg-popup/>
    <session-msg-popup/>
  </v-app>
</template>
<script>
  import {eventBus} from '../main';
  import EventName from '../constants/EventName';
  import {store} from '../store'

  export default {
    name: 'MainPage',
    data: () => ({
      snackbar: {
        visible: false,
        text: ""
      }
    }),
    computed: {
      numberOfPolygonsToUpload() {
        return store.state.polygonGeoJsonToUpload.length;
      },
      newFeatureGeoJson() {
        return store.state.newPolygonGeoJson;
      },
      featureGeoJsonToUpload() {
        return store.state.polygonGeoJsonToUpload;
      },
      uploadedFeatureGeoJson() {
        return store.state.uploadedPolygonGeoJson;
      },
      isGeometryUploadPossible() {
        return store.state.polygonGeoJsonToUpload.length > 0 && !store.state.newPolygonGeoJson;
      }
    },
    methods: {
      openGeoJsonUploadPopup () {
        eventBus.$emit(EventName.GEO_JSON_POPUP, {open: true});
      },
      openCapUploadPopup () {
        eventBus.$emit(EventName.CAP_POPUP, {open: true});
      },
      openPhaseMsgPopup() {
        eventBus.$emit(EventName.PHASE_POPUP, {open: true});
      },
      openSessionMgmtPopup() {
        eventBus.$emit(EventName.SESSION_POPUP, {open: true});
      },
      openRolePlayerMsgPopup() {
        eventBus.$emit(EventName.ROLE_POPUP, {open: true});
      },
      openPhaseOSTStageMsgPopup() {
        eventBus.$emit(EventName.OST_POPUP, {open: true});
      },
      openLogMsgPopup() {
        eventBus.$emit(EventName.LOG_POPUP, {open: true});
      },
      openEvalLogMsgPopup() {
        eventBus.$emit(EventName.EVAL_LOG_POPUP, {open: true});
      },
      setDetailsWidth(width) {
        const mainFrame = this.$refs.mainFrame;
        const detailsFrame = this.$refs.detailsFrame;
        mainFrame.style.right = width + "px";
        detailsFrame.style.width = width + "px";
      },
      handlePolygon(geoJson) {
        store.dispatch('setNewPolygonGeoJson', geoJson);
      }
    },
  };
</script>
