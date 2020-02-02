<template>
  <div ref="container" style="width:100%;height:100%;"></div>
</template>

<script>
  import 'ol/ol.css';
  import Map from 'ol/Map';
  import View from 'ol/View';
  import TileLayer from 'ol/layer/Tile';
  import VectorLayer from 'ol/layer/Vector';
  import OSM from 'ol/source/OSM';
  import VectorSource from 'ol/source/Vector';
  import GeoJSON from 'ol/format/GeoJSON';
  import Polygon from 'ol/geom/Polygon';
  import {transformExtent} from 'ol/proj.js';
  import {mapStyling} from '../service/MapStylingService';
  import Draw from "ol/interaction/Draw";
  import Style from 'ol/style/Style'
  import Stroke from 'ol/style/Stroke'
  import Fill from 'ol/style/Fill'

  export default {
    name: 'MapPanel',
    props: ['newFeatureGeoJson', 'featureGeoJsonToUpload', 'uploadedFeatureGeoJson', 'onDrawPolygon'],
    watch: {
      newFeatureGeoJson: function () {
        this.updateFeatures();
      },
      featureGeoJsonToUpload: function() {
        this.updateFeatures();
      },
      uploadedFeatureGeoJson: function() {
        this.updateFeatures();
      }
    },
    methods: {
      transform (extent) {
        return transformExtent(extent, 'EPSG:4326', 'EPSG:3857');
      },
      updateFeatures () {
        this.vectorSource.clear();
        this.updateFeaturesFromGeoJson();
      },
      updateFeaturesFromGeoJson () {
        if (this.newFeatureGeoJson) {
          this.addFeaturesFromGeoJson(this.newFeatureGeoJson, mapStyling.createPolygonStyle("red", 5, "rgba(255, 255, 255, 0.3)"))
        }
        if (this.featureGeoJsonToUpload) {
          const style = mapStyling.createPolygonStyle("red", 2, "rgba(255, 255, 255, 0.3)");
          this.featureGeoJsonToUpload.forEach(geoJson => this.addFeaturesFromGeoJson(geoJson, style));
        }
        if (this.uploadedFeatureGeoJson) {
          const style = mapStyling.createPolygonStyle("blue", 2, "rgba(255, 255, 255, 0.3)");
          this.uploadedFeatureGeoJson.forEach(geoJson => this.addFeaturesFromGeoJson(geoJson, style));
        }
      },
      addFeaturesFromGeoJson(geoJson, style) {
        const features = new GeoJSON().readFeatures(geoJson);
        console.log('Displaying GeoJSON features', features);
        if (features.length > 0) {
          features.forEach(f => {
            f.getGeometry().transform('EPSG:4326', 'EPSG:3857');
            f.setStyle(style);
          });
          this.vectorSource.addFeatures(features);
        }
      },
      scaleToFeatures() {
        const features = this.vectorSource.getFeatures();
        if (features.length > 0) {
          const extent = this.vectorSource.getExtent();
          this.map.getView().fit(extent, {size: this.map.getSize(), maxZoom: 10});
        } else if (!this.wmsData) {
          this.scaleToEurope();
        }
      },
      scaleToEurope() {
        const euExtent = this.transform([-27.68862, 33.59717, 43.90757, 71.97626]);
        this.map.getView().fit(euExtent, this.map.getSize());
      },
      addInteractions() {
        const me = this;
        const draw = new Draw({
          type: 'Polygon',
        });
        draw.on('drawend', function (e) {
          const currentFeature = e.feature; // this is the feature fired the event
          const options = {
            featureProjection: 'EPSG:3857',
            dataProjection: 'EPSG:4326'
          };
          const format = new GeoJSON(options);
          const geoJson = format.writeFeature(currentFeature);
          me.onDrawPolygon(geoJson);
        });
        this.map.addInteraction(draw);
      }
    },
    mounted () {
      const me = this;
      console.log('Mounted, starting map', this.$refs.container);

      this.vectorSource = new VectorSource({
        features: []
      });

      const vectorLayer = new VectorLayer({
        source: this.vectorSource,
        style: mapStyling.stylingFunction
      });

      this.map = new Map({
        target: this.$refs.container,
        layers: [
          new TileLayer({
            source: new OSM()
          }),
          vectorLayer
        ],
        view: new View({
          center: [0, 0],
          zoom: 2
        })
      });

      this.addInteractions();
      this.updateFeatures();
      this.scaleToEurope();
    },
  };
</script>
