<template>
  <div class="game-master-panel">
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
import CommonUtils from './common-utils';
export default {
  name: 'gamemaster',
  mixins: [CommonUtils],
  created() {
    console.log('gamemaster created: data bound');

    // if (this.$route.query.username) {
      // this.username = this.$route.query.username;
      this.username = 'admin';
      // Retrive userInfo from local storage
      let userInfo = this.retrieveFromStorage('localStorage', 'trouble_flipper_game_master');
      let clientId = null;
      if (userInfo) {
        clientId = userInfo.clientId;
        if (userInfo.username !== this.username) {
          userInfo.username = this.username;
          this.saveIntoStorage('localStorage', 'trouble_flipper_game_master', userInfo);
        }
      }
      this.clientId = clientId;
      this.masterMessenger = new GameMaster(this.$solace, this.$parent.appProps,
        {username: this.username, clientId: this.clientId},
        this.handleMsg.bind(this));
      this.masterMessenger.connect();
    // } else {
    //   this.$router.push({
    //     name: 'signin'
    //   });
    // }
  },
  // mounted() {
  //   console.log('gamemaster mounted: dom element inserted');
  // },
  // beforeUpdate() {
  //   // add any customized code before DOM is re-render and patched based changes in data
  //   console.log('gamemaster beforeUpdate: data is changed, about to rerender dom');
  // },
  // updated() {
  //   console.log('gamemaster updated: dom is rerendered');
  // },
  destroyed() {
    // clean up any resource, such as close websocket connection, remove subscription
    console.log('game master destroyed: dom removed');
    if (this.masterMessenger) {
      this.masterMessenger.unregister();
      this.masterMessenger.disconnect();
      this.masterMessenger = null;
    }
  },

  // Underlying model
  data() {
    return {
      msg: 'Trouble Flipper Game Master',
      state: 'connecting',
      clientId: "",
      username: "",
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
      this.clientId = msg.clientId;
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
        if (this.state === 'watching') {
          console.log('Save username ' + this.username + ', clientId ' + this.clientId + ' to localStorage');
          this.saveIntoStorage('localStorage', 'trouble_flipper_game_master', { username: this.username, clientId: this.clientId });
        }
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

.game-master-panel {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  padding: 15px;
}
.game-master-panel .title {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.game-master-panel .title .user-info {
  font-weight: 500;
}
.game-master-panel .score-info.waiting {
  padding: 15px 0px;
  align-self: center;
}
.game-master-panel .score-board {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
}

.game-master-panel .score-board .game {
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
