<template>
    <v-flex>
        <v-card v-if="hasPolygonGeoJson">
            <v-toolbar flat color="white">
                <v-toolbar-title>Properties</v-toolbar-title>
                <v-spacer></v-spacer>
                <v-dialog v-model="dialog" max-width="500px">
                    <template v-slot:activator="{ on }">
                        <v-btn color="primary" dark class="mb-2" v-on="on">New Property</v-btn>
                    </template>
                    <v-card>
                        <v-card-title>
                            <span class="headline">{{ formTitle }}</span>
                        </v-card-title>
                        <v-card-text>
                            <v-container grid-list-md>
                                <v-layout wrap>
                                    <v-flex xs12 sm12 md6>
                                        <v-text-field v-model="editedItem.key" label="Key" ref="keyField"></v-text-field>
                                    </v-flex>
                                    <v-flex xs12 sm12 md6>
                                        <v-text-field v-model="editedItem.value" label="Value"></v-text-field>
                                    </v-flex>
                                </v-layout>
                            </v-container>
                        </v-card-text>
                        <v-card-actions>
                            <v-spacer></v-spacer>
                            <v-btn color="blue darken-1" flat @click="close">Cancel</v-btn>
                            <v-btn color="blue darken-1" flat @click="save">Save</v-btn>
                        </v-card-actions>
                    </v-card>
                </v-dialog>
            </v-toolbar>
            <v-data-table
                    :headers="headers"
                    :items="properties"
                    :hide-default-footer="true"
                    hide-actions
            >
                <template v-slot:items="props">
                    <td>{{ props.item.key }}</td>
                    <td>{{ props.item.value }}</td>
                    <td class="justify-center layout px-0">
                        <v-icon small class="mr-2" @click="editItem(props.item)">
                            edit
                        </v-icon>
                        <v-icon small @click="deleteItem(props.item)">
                            delete
                        </v-icon>
                    </td>
                </template>
                <template v-slot:no-data>
                    No properties.
                </template>
            </v-data-table>
            <!--
            <div style="font-family: Courier; font-size: 10px; margin: 20px 0px;">
                {{polygonWithPropertiesGeoJson}}
            </div>
            -->
            <div style="display: flex;">
                <v-btn slot="activator" @click="discardPolygon">
                    Discard
                </v-btn>
                <v-spacer></v-spacer>
                <v-btn slot="activator" @click="addPolygonToUpload">
                    Add to upload
                </v-btn>
            </div>
        </v-card>
        <v-card v-else class="detailsPanel">
            <v-card-text>
                Draw polygon on map to get started or perform upload via toolbar button.
            </v-card-text>
        </v-card>
    </v-flex>
</template>

<script>
  import {store} from '../store'

  export default {
    components: {},
    name: 'AddPolygonPanel',
    props: ['hideTitle', 'onWidthChange'],
    data: function () {
      return {
        dialog: false,
        isWide: false,
        headers: [
          {text: 'Key', value: 'key'},
          {text: 'Value', value: 'value', sortable: false},
        ],
        properties: [],
        editedIndex: -1,
        editedItem: {
          key: '',
          value: '',
        },
        defaultItem: {
          key: '',
          value: '',
        },
      }
    },
    computed: {
      formTitle () {
        return this.editedIndex === -1 ? 'New Property' : 'Edit Property'
      },
      hasPolygonGeoJson: function () {
        return !!this.$store.state.newPolygonGeoJson
      },
      polygonWithPropertiesGeoJson: function () {
        return this.getPolygonWithPropertiesGeoJson()
      },
    },
    methods: {
      getPropertyValue (value) {
        /*
        const isNumberic = !isNaN(value);
        if (isNumberic) {
          return parseFloat(value);
        } else {
          return value;
        }
        */
        return value
      },
      getPolygonWithPropertiesGeoJson () {
        const geojson = this.$store.state.newPolygonGeoJson
        if (geojson) {
          const propertyMap = {}
          this.properties.forEach(p => propertyMap[p.key] = this.getPropertyValue(p.value))
          const polygon = JSON.parse(geojson)
          polygon.bbox = null;
          polygon.properties = propertyMap;
          return JSON.stringify(polygon);
        } else {
          return geojson
        }
      },
      addPolygonToUpload () {
        store.dispatch('addPolygonGeoJsonToUpload', this.getPolygonWithPropertiesGeoJson())
        store.dispatch('setNewPolygonGeoJson', null)
        this.properties = [];
      },
      discardPolygon () {
        store.dispatch('setNewPolygonGeoJson', null)
      },
      toggleWidth () {
        const newWidth = this.isWide ? 400 : 800
        this.isWide = !this.isWide
        this.onWidthChange(newWidth)
      },
      editItem (item) {
        this.editedIndex = this.properties.indexOf(item)
        this.editedItem = Object.assign({}, item)
        this.dialog = true
        this.$nextTick(() => this.$refs.keyField.$el.focus())
      },
      deleteItem (item) {
        const index = this.properties.indexOf(item)
        this.properties.splice(index, 1)
      },
      close () {
        this.dialog = false
        setTimeout(() => {
          this.editedItem = Object.assign({}, this.properties)
          this.editedIndex = -1
        }, 300)
      },
      save () {
        if (this.editedIndex > -1) {
          Object.assign(this.properties[this.editedIndex], this.editedItem)
        } else {
          this.properties.push(this.editedItem)
        }
        this.close()
      }
    },
    created: function () {
    }
  }
</script>
