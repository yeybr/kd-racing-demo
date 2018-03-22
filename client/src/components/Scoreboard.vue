<template>
  <div>
    <h1>{{msg}}</h1>
  </div>
</template>

<script>
export default {
  name: 'scoreboard',
  // lifecycle callbacks
  created() {
    console.log('scoreboard created');
  },
  mounted() {
    console.log('scoreboard mounted');
  },
  beforeUpdate() {
    // add customized jquery code before dom is re-render and patch when data changes
    console.log('scoreboard beforeUpdate');
  },
  updated() {
    console.log('scoreboard updated');
  },
  destroyed() {
    // clean up any network resource, such as close websocket connection
    console.log('scoreboard destroyed');
  },

  // Underlying model
  data() {
    return {
      msg: 'Trouble Flipper Scoreboard'
    }
  },

  // any actions
  methods: {
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<!-- Enable sas by using lang="scss" -->
<style scoped>
h1, h2 {
  font-weight: normal;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #1DACFC;
}
</style>
