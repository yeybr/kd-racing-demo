<template>
  <div>
    <h1>{{msg}}</h1>
  </div>
</template>

<script>
import { GameMaster } from '@/messaging/game-master'
export default {
  name: 'scoreboard',
  created() {
    console.log('scoreboard created: data bind');
    this.masterMessenger = new GameMaster(this.$solace, this.$parent.appProps, this.handleMsg, this.handleStateChange);
    this.masterMessenger.join();
  },
  // mounted() {
  //   console.log('scoreboard mounted: dom element inserted');
  // },
  // beforeUpdate() {
  //   // add any customized code before DOM is re-render and patched based changes in data
  //   console.log('scoreboard beforeUpdate: data is changed, about to rerender dom');
  // },
  // updated() {
  //   console.log('scoreboard updated: dom is rerendered');
  // },
  destroyed() {
    // clean up any resource, such as close websocket connection, remove subscription
    console.log('scoreboard destroyed: dom removed');
    if (this.masterMessenger) {
      this.masterMessenger.leave();
      this.masterMessenger = null;
    }
  },

  // Underlying model
  data() {
    return {
      msg: 'Trouble Flipper Scoreboard'
    }
  },

  // any actions
  methods: {
    handleMsg: function(msg) {
      console.log(msg);
    },
    handleStateChange: function(state) {
      console.log(state);
    }
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
