<template>
    <v-layout row justify-center>
        <v-dialog v-model="dialog" max-width="600">
            <v-card class="organisationPopup" style="height: calc(100vh - 160px);">
                <v-card-title class="headline">Phase Message</v-card-title>
                <v-card-text>
                    <div style="overflow-y:scroll;position:absolute;top:70px;bottom:60px;left:0px;right:0px;">
                        <div style="margin: 0px 20px;">
                            <v-text-field v-model="msg.phaseId" label="Message ID"></v-text-field>
                            <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
                              <v-btn slot="activator">Type</v-btn>
                              <v-card>
                                <v-list>
                                  <v-list-tile @click="typeClicked('CALL')">CALL</v-list-tile>
                                  <v-list-tile @click="typeClicked('ACTION')">ACTION</v-list-tile>
                                  <v-list-tile @click="typeClicked('MESSAGE')">MESSAGE</v-list-tile>
                                  <v-list-tile @click="typeClicked('MAIL')">MAIL</v-list-tile>
                                  <v-list-tile @click="typeClicked('TWEET')">TWEET</v-list-tile>
                                </v-list>
                              </v-card>
                            </v-menu>
                            <v-text-field v-model="msg.type" label="Type" disabled=true></v-text-field>
                            <v-text-field v-model="msg.title" label="Title"></v-text-field>
                            <v-text-field v-model="msg.headline" label="Headline"></v-text-field>
                            <v-text-field v-model="msg.description" label="Description"></v-text-field>
                            <v-text-field v-model="msg.rolePlayerName" label="Role Player Name"></v-text-field>
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
          msgId: '',
          type: '',
          title: '',
          headline: '',
          description: '',
          rolePlayerName: '',
          comment: ''
        }
      }
    },
    methods: {
      reset: function() {
        this.msg = {
          msgId: '',
          type: '',
          title: '',
          headline: '',
          description: '',
          rolePlayerName: '',
          comment: ''
        }
      },
      typeClicked(type) {
        this.msg.type = type;
      },
      cancel: function () {
        this.dialog = false
        this.reset();
      },
      send: function () {
        console.log('Sending')
        fetchService.performPostJson('sendRolePlayerMessage', this.msg).then(() => {
          this.cancel();
        }).catch(ex => console.log(ex))
      }
    },
    created () {
      const vm = this
      eventBus.$on(EventName.ROLE_POPUP, function (value) {
        vm.dialog = value.open
      })
    }
  }
</script>
