<template>
  <v-app>
    <toolbar>
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
        <v-btn slot="activator">
          <v-icon left>all_inbox</v-icon>
          Reports
        </v-btn>
      </v-menu>
    </toolbar>
    <main style="height: 100%">
      <v-layout column justify-space-between fill-height>
        <div style="position:absolute;top:64px;bottom:0px;left:0px;right:0px;">
          <v-layout row wrap fill-height>
            <div ref="mainFrame" style="position:absolute;top:0px;bottom:0px;left:0px;right:400px;">
              <map-panel />
            </div>
            <div ref="detailsFrame" style="position:absolute;top:0px;bottom:0px;right:0px;width:400px;">
              <details-panel style="height: 100%; overflow: auto;" :onWidthChange="setDetailsWidth"/>
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
  </v-app>
</template>
<script>
  import {eventBus} from '../main';
  import EventName from '../constants/EventName';

  export default {
    name: 'MainPage',
    data: () => ({
      snackbar: {
        visible: false,
        text: ""
      }
    }),
    methods: {
      openUploadPopup () {
        eventBus.$emit(EventName.UPLOAD_POPUP, true);
      },
      setDetailsWidth(width) {
        const mainFrame = this.$refs.mainFrame;
        const detailsFrame = this.$refs.detailsFrame;
        mainFrame.style.right = width + "px";
        detailsFrame.style.width = width + "px";
      }
    },
  };
</script>
