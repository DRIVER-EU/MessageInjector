<template>
    <v-layout row justify-center>
        <!--<v-btn color="primary" dark @click.native.stop="dialog = true">Open Dialog</v-btn>-->
        <v-dialog v-model="dialog" max-width="600">
            <v-card class="organisationPopup" style="height: calc(100vh - 160px);">
                <v-card-title class="headline">
                    Upload CAP
                </v-card-title>
                <v-card-text>
                    <div style="overflow-y:scroll;position:absolute;top:70px;bottom:60px;left:0px;right:0px;">
                        <div style="margin: 0px 20px;">
                            <v-text-field v-model="topicName" label="Topic Name"></v-text-field>
                            <v-textarea label="CAP Message" v-model="capData" :auto-grow="true"></v-textarea>
                        </div>
                    </div>
                </v-card-text>
                <v-card-actions style="position:absolute;bottom:0px;height:60px;left:0px;right:0px;">
                    <v-btn flat="flat" @click="cancel">Cancel</v-btn>
                    <v-spacer></v-spacer>
                    <v-btn flat="flat" @click.native="upload">Upload</v-btn>
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
        topicName: '',
        capData: ''
      }
    },
    methods: {
      reset: function() {
        this.topicName = '';
        this.capData = '';
      },
      cancel: function () {
        this.dialog = false
        this.reset();
      },
      upload: function () {
        console.log('Uploading', this.capData)
        fetchService.performPostXml('sendXMLMessage/CAP?cgorName=' + this.topicName, this.capData).then(() => {
          this.cancel();
        }).catch(ex => console.log(ex))
      }
    },
    created () {
      const vm = this
      eventBus.$on(EventName.CAP_POPUP, function (value) {
        vm.dialog = value.open
      })
    }
  }
</script>
