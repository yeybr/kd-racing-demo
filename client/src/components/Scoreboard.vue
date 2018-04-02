<template>
  <div class="score-panel">
    <div class="title">
      <h1>{{msg}}</h1>
      <!-- <div class="user-info">User: {{username}}</div> -->
    </div>
    <div v-show="state !== 'watching'" class="score-info waiting">
      <h3>Connecting...</h3>
    </div>
    <div v-show="state === 'watching'" class="score-board">
      <div class="game" v-for="gameInfo in scoreboardInfo.games" :key="gameInfo.id">
        <span class="info">Puzzle: {{gameInfo.gameName}}</span>
        <span class="info">Team: {{gameInfo.teamName}}</span>
        <div class="info">Players:
          <template v-for="(player, index) in gameInfo.players">
            {{player.name}}{{(index === gameInfo.players.length - 1) ? '': ', '}}
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { GameMaster } from '@/messaging/game-master';
import Utils from './Utils.vue';
export default {
  name: 'scoreboard',
  mixins: [Utils],
  created() {
    console.log('scoreboard created: data bound');

    // if (this.$route.query.username) {
      // this.username = this.$route.query.username;
      // this.isMaster = this.$route.query.isMaster;
      this.username = 'admin';
      this.isMaster = true;
      this.masterMessenger = new GameMaster(this.$solace, this.$parent.appProps,
        {username: this.username, isMaster: this.isMaster, client: null},
        this.handleMsg.bind(this));
      this.masterMessenger.connect();
    // } else {
    //   this.$router.push({
    //     name: 'signin'
    //   });
    // }
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
      this.masterMessenger.unregister();
      this.masterMessenger.disconnect();
      this.masterMessenger = null;
    }
  },

  // Underlying model
  data() {
    return {
      msg: 'Trouble Flipper Scoreboard',
      state: 'connecting',
      client: "",
      username: "",
      isMaster: false,
      scoreboardInfo: {
        teams: [
        ]
      }
    }
  },

  // any actions
  methods: {
    handleMsg: function(msg) {
      console.log('Got message', msg);
      this.client = msg.client;
      this.username = msg.username;
      if (msg.scoreboardInfo) {
        this.updateData(this.scoreboardInfo, msg.scoreboardInfo);
      }
      this.handleStateChange(msg);
    },
    handleStateChange: function(msg) {
      if (msg.state) {
        console.log('State change', msg);
        this.state = msg.state;
      }
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

.score-panel {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  padding: 15px;
}
.score-panel .title {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.score-panel .title .user-info {
  font-weight: 500;
}
.score-panel .score-info.waiting {
  padding: 15px 0px;
  align-self: center;
}
.score-panel .score-board {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
}

.score-panel .score-board .game {
  margin: 15px;
  padding: 15px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  flex-wrap: wrap;
  border: solid 1px grey;
  width: 400px;
}
</style>
