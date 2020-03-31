<template>
    <v-layout row justify-center>
        <v-dialog v-model="dialog" max-width="600">
            <v-card class="organisationPopup" style="height: calc(100vh - 160px);">
                <v-card-title class="headline">Session Mgmt Message</v-card-title>
                <v-card-text>
                    <div style="overflow-y:scroll;position:absolute;top:70px;bottom:60px;left:0px;right:0px;">
                        <div style="margin: 0px 20px;">
                            <v-text-field v-model="msg.sessionId" label="Session ID"></v-text-field>
                            <v-text-field v-model="msg.sessionName" label="Session name"></v-text-field>
                            <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
                              <v-btn slot="activator">State</v-btn>
                              <v-card>
                                <v-list>
                                  <v-list-tile @click="stateClicked('Initializing')">Initializing</v-list-tile>
                                  <v-list-tile @click="stateClicked('Started')">Started</v-list-tile>
                                  <v-list-tile @click="stateClicked('Stopped')">Stopped</v-list-tile>
                                  <v-list-tile @click="stateClicked('Closed')">Closed</v-list-tile>
                                </v-list>
                              </v-card>
                            </v-menu>
                            <v-text-field v-model="msg.state" label="State" disabled=true></v-text-field>
                            
                            <v-text-field v-model="msg.trialId" label="Trial ID"></v-text-field>
                            <v-text-field v-model="msg.trialName" label="Trial Name"></v-text-field>
                            <v-text-field v-model="msg.scenarioId" label="Scenario ID"></v-text-field>
                            <v-text-field v-model="msg.scenarioName" label="Scenario Name"></v-text-field>
                            <v-text-field v-model="msg.comment" label="Comment"></v-text-field>
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
        msg: {
          trialId: '',
          trialName: '',
          scenarioId: '',
          scenarioName: '',
          sessionId: '',
          sessionName: '',
          state: ''
        }
      }
    },
    methods: {
      reset: function() {
        this.msg = {
          trialId: '',
          trialName: '',
          scenarioId: '',
          scenarioName: '',
          sessionId: '',
          sessionName: '',
          state: ''
        }
      },
      stateClicked(state) {
        this.msg.state = state;
      },
      cancel: function () {
        this.dialog = false
        this.reset();
      },
      send: function () {
        console.log('Sending')
        fetchService.performPostJson('sendSessionMessage', this.msg).then(() => {
          this.cancel();
        }).catch(ex => console.log(ex))
      }
    },
    created () {
      const vm = this
      eventBus.$on(EventName.SESSION_POPUP, function (value) {
        vm.dialog = value.open
      })
    }
  }
</script>
