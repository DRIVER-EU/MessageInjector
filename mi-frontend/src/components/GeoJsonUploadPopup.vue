<template>
  <v-layout row justify-center>
    <!--<v-btn color="primary" dark @click.native.stop="dialog = true">Open Dialog</v-btn>-->
    <v-dialog v-model="dialog" max-width="600">
      <v-card class="organisationPopup" style="height: calc(100vh - 160px);">
        <v-card-title class="headline">
          Upload Geometries
        </v-card-title>
        <v-card-text>
          <div style="overflow-y:scroll;position:absolute;top:70px;bottom:60px;left:0px;right:0px;">
            <div style="margin: 0px 20px;">
              <v-text-field v-model="topicName" label="Topic Name"></v-text-field>
              <json-tree :json="featureCollectionGeoJson"/>
            </div>
          </div>
        </v-card-text>
        <v-card-actions style="position:absolute;bottom:0px;height:60px;left:0px;right:0px;">
          <v-btn flat="flat" @click="cancel">Cancel</v-btn>
          <v-spacer></v-spacer>
          <v-btn flat="flat" @click="clear">Clear all</v-btn>
          <v-btn flat="flat" @click.native="upload">Upload</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-layout>
</template>
<script>
  import {eventBus} from '../main';
  import EventName from '../constants/EventName';
  import {store} from '../store';
  import GeometryFactory from 'jsts/org/locationtech/jts/geom/GeometryFactory'
  import GeoJSONReader from 'jsts/org/locationtech/jts/io/GeoJSONReader'
  import GeoJSONWriter from 'jsts/org/locationtech/jts/io/GeoJSONWriter'
  import {fetchService} from '../service/FetchService'

  export default {
    data () {
      return {
        dialog: false,
        topicName: "",
      };
    },
    computed: {
      featureCollectionGeoJson: function() {
        const geoJsonPolygons = this.$store.state.polygonGeoJsonToUpload;
        if (geoJsonPolygons && geoJsonPolygons.length > 0) {
          const features = [];
          geoJsonPolygons.forEach(geoJsonPolygon => {
            features.push(JSON.parse(geoJsonPolygon));
          });
          const featureCollection = {type: "FeatureCollection", bbox: null, features: features};
          return JSON.stringify(featureCollection);
        } else {
          return JSON.stringify({});
        }
      },
    },
    methods: {
      multiPolygonGeoJson: function() {
        const geoJsonPolygons = this.$store.state.polygonGeoJsonToUpload;
        if (geoJsonPolygons && geoJsonPolygons.length > 0) {
          const factory = new GeometryFactory();
          const reader = new GeoJSONReader();
          const writer = new GeoJSONWriter();
          const geometries = [];
          geoJsonPolygons.forEach(geoJsonPolygon => {
            const polygon = reader.read(geoJsonPolygon);
            geometries.push(polygon.geometry);
          });
          const multiPolygon = factory.createMultiPolygon(geometries);
          return JSON.stringify(writer.write(multiPolygon));
        } else {
          return JSON.stringify({});
        }
      },
      reset: function() {
        this.topicName = '';
      },
      clear: function () {
        store.dispatch('clearPolygonGeoJsonToUpload')
        this.cancel();
      },
      cancel: function () {
        this.dialog = false;
        this.reset();
      },
      upload: function() {
        console.log("Uploading", this.featureCollectionGeoJson);
        fetchService.performPostJson('sendGeoJson?cgorName=' + this.topicName, this.featureCollectionGeoJson).then(() => {
          store.dispatch('markPolygonGeoJsonAsUploaded')
          this.cancel();
        }).catch(ex => console.log(ex));
      }
  },
    created () {
      const vm = this;
      eventBus.$on(EventName.GEO_JSON_POPUP, function (value) {
        vm.dialog = value.open;
      });
    }
  };
</script>
