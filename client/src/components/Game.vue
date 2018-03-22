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
export default {
  name: "game",
  // lifecycle callbacks
  created() {
    console.log('game created');
  },
  mounted() {
    console.log('game mounted');
  },
  beforeUpdate() {
    // add customized jquery code before dom is re-render and patch when data changes
    console.log('game beforeUpdate');
  },
  updated() {
    console.log('game updated');
  },
  destroyed() {
    // clean up any network resource, such as close websocket connection
    console.log('game destroyed');
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
