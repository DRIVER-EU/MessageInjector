<template>
  <v-flex>
    <v-card v-show="!hideTitle" class="detailsPanel">
      <v-card-title class="justify-space-between primary--text">
        Properties
        <div></div>
      </v-card-title>
      <v-card-text>
        Hello
      </v-card-text>
    </v-card>
  </v-flex>
</template>

<script>
  export default {
    components: {},
    name: 'DetailsPanel',
    props: ['hideTitle', 'onWidthChange'],
    data: function () {
      return {
        isWide: false
      };
    },
    computed: {
      record: function () {
        const record = this.$store.state.record;
        console.log('Displaying details', record);
        return record || {};
      },
      currentProperties() {
        return { recordID: this.record.id, record: this.record };
      }
    },
    methods: {
      toggleWidth() {
        const newWidth = this.isWide ? 400 : 800;
        this.isWide = !this.isWide;
        this.onWidthChange(newWidth);
      }
    },
    created: function () {
    }
  };
</script>
