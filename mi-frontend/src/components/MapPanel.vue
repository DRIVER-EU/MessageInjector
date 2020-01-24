<template>
  <div ref="container" style="width:100%;height:100%;"></div>
</template>

<script>
  import 'ol/ol.css';
  import Map from 'ol/Map';
  import View from 'ol/View';
  import Feature from 'ol/Feature';
  import TileLayer from 'ol/layer/Tile';
  import VectorLayer from 'ol/layer/Vector';
  import OSM from 'ol/source/OSM';
  import TileWMS from 'ol/source/TileWMS';
  import VectorSource from 'ol/source/Vector';
  import GeoJSON from 'ol/format/GeoJSON';
  import Circle from 'ol/geom/Circle';
  import Point from 'ol/geom/Point';
  import Polygon from 'ol/geom/Polygon';
  import Style from 'ol/style/Style';
  import Icon from 'ol/style/Icon';
  import Control from 'ol/control/Control';
  import {transformExtent} from 'ol/proj.js';
  import {mapStyling} from '../service/MapStylingService';
  import Urls from '../constants/Urls';
  import Draw from "ol/interaction/Draw";

  export default {
    name: 'MapPanel',
    props: ['geojson'],
    watch: {
      geojson: function () {
      },
    },
    methods: {
      transform (extent) {
        return transformExtent(extent, 'EPSG:4326', 'EPSG:3857');
      },
      updateFeatures () {
        this.vectorSource.clear();
        this.updateFeaturesFromGeoJson();
        this.scaleToFeatures();
      },
      updateFeaturesFromGeoJson () {
        if (this.geojson) {
          const features = new GeoJSON().readFeatures(this.geojson);
          console.log('Displaying GeoJSON features', features);
          if (features.length > 0) {
            features.forEach(f => f.getGeometry().transform('EPSG:4326', 'EPSG:3857'));
            this.vectorSource.addFeatures(features);
          }
        }
      },
      scaleToFeatures() {
        const features = this.vectorSource.getFeatures();
        if (features.length > 0) {
          const extent = this.vectorSource.getExtent();
          this.map.getView().fit(extent, {size: this.map.getSize(), maxZoom: 10});
        } else if (!this.wmsData) {
          const euExtent = this.transform([-27.68862, 33.59717, 43.90757, 71.97626]);
          this.map.getView().fit(euExtent, this.map.getSize());
        }
      },
      addInteractions() {
        const draw = new Draw({
          source: this.vectorSource,
          type: 'Polygon'
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
    },
  };
</script>
