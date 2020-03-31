<template>
    <v-layout row justify-center>
        <v-dialog v-model="dialog" max-width="600">
            <v-card class="organisationPopup" style="height: calc(100vh - 160px);">
                <v-card-title class="headline">Evaluation Logging message</v-card-title>
                <v-card-text>
                    <div style="overflow-y:scroll;position:absolute;top:70px;bottom:60px;left:0px;right:0px;">
                        <div style="margin: 0px 20px;">
                            <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
                              <v-btn slot="activator">Level</v-btn>
                              <v-card>
                                <v-list>
                                  <v-list-tile @click="levelClicked('DEBUG')">DEBUG</v-list-tile>
                                  <v-list-tile @click="levelClicked('INFO')">INFO</v-list-tile>
                                  <v-list-tile @click="levelClicked('WARN')">WARN</v-list-tile>
                                  <v-list-tile @click="levelClicked('ERROR')">ERROR</v-list-tile>
                                  <v-list-tile @click="levelClicked('CRITICAL')">CRITICAL</v-list-tile>
                                  <v-list-tile @click="levelClicked('SILLY')">SILLY</v-list-tile>
                                </v-list>
                              </v-card>
                            </v-menu>
                            <v-text-field v-model="level" label="Level" disabled=true></v-text-field>
                            <v-text-field v-model="message" label="Log Message"></v-text-field>
                        </div>
                    </div>
                </v-card-text>
                <v-card-actions style="position:absolute;bottom:0px;height:60px;left:0px;right:0px;">
                    <v-btn flat="flat" @click="cancel">Cancel</v-btn>
                    <v-spacer></v-spacer>
                    <v-btn flat="flat" @click.native="send">Send</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
    </v-layout>
</template>
<script>
  import {eventBus} from '../main'
  import EventName from '../constants/EventName'
  import {fetchService} from '../service/FetchService'

  export default {
    data () {
      return {
        dialog: false,
        level: 'INFO',
        message: ''
      }
    },
    methods: {
      reset: function() {
        this.level = 'INFO',
        this.message = ''
      },
      levelClicked(level) {
        this.level = level;
      },
      cancel: function () {
        this.dialog = false
        this.reset();
      },
      send: function () {
        console.log('Sending')
        fetchService.performPostJson('sendEvalLogMsg?level=' + this.level, this.message).then(() => {
          this.cancel();
        }).catch(ex => console.log(ex))
      }
    },
    created () {
      const vm = this
      eventBus.$on(EventName.EVAL_LOG_POPUP, function (value) {
        vm.dialog = value.open
      })
    }
  }
</script>
