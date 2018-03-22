<template>
  <div class="game-panel">
    <div class="title">
      <h1>{{msg}}</h1>
    </div>
    <div class="game-info">
      <h2>Puzzle - {{gameInfo.name}}</h2>
      <div class="status">
        <span class="stats">Player: {{$route.query.username}}</span>
        <span class="stats">Total: {{gameInfo.stats.total}}</span>
        <span class="stats">Finished: {{gameInfo.stats.finished}}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { Player } from '@/messaging/player'
export default {
  name: "game",
  // lifecycle callbacks
  created() {
    console.log('game created: data bind');
    // DO NOT initialize playerMessenger in data() function; otherwise all its memebers will become reactive
    // including the solace API. We don't want solace API's data structure to be injected with Observer stuff,
    // it causes SolaceClientFactory.init() to fail
    this.playerMessenger = new Player(this.$solace, this.$parent.appProps, this.handleMsg, this.handleStateChange);
    this.playerMessenger.join();
  },
  // mounted() {
  //   console.log('game mounted: dom element inserted');
  // },
  // beforeUpdate() {
  //   // add any customized code before DOM is re-render and patched based changes in data
  //   console.log('game beforeUpdate: data is changed, about to rerender dom');
  // },
  // updated() {
  //   console.log('game updated: dom is rerendered');
  // },
  destroyed() {
    // clean up any resource, such as close websocket connection, remove subscription
    console.log('game destroyed: dom removed');
    if (this.playerMessenger) {
      this.playerMessenger.leave();
      this.playerMessenger = null;
    }
  },

  // Underlying model
  data() {
    return {
      msg: "Trouble Flipper",
      gameInfo: {
        id: "1",
        name: "Cars",
        players: [
          {
            id: 1,
            name: "Kevin"
          },
          {
            id: 2,
            name: "Rob"
          }
        ],
        stats: {
          total: 25,
          finished: 5
        }
      }
    };
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
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<!-- Enable sas by using lang="scss" -->
<style scoped>
h1,
h2 {
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
  color: #1dacfc;
}

game-panel {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  padding: 15px;
  .title {
  }
  .game-info {
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    .status {
      display: flex;
      flex-direction: row;
      .stats {
        padding: 15px 25px 15px 15px;
      }
    }
  }
  .game-board {
    flex: 1 1 0;
  }
}
</style>
