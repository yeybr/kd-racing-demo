<template>
  <div class="score-panel">
    <div class="title">
      <h1>{{msg}}</h1>
      <div class="user-info">User: {{userName}}</div>
    </div>
    <div class="score-info">
      <div v-show="state !== 'watching'">
        <h2>Connecting...</h2>
      </div>
    </div>
    <div v-show="state === 'watching'" class="score-board">
      Score Board
    </div>
  </div>
</template>

<script>
import { GameMaster } from '@/messaging/game-master'
export default {
  name: 'scoreboard',
  created() {
    console.log('scoreboard created: data bound');
    if (this.$route.query.username) {
      this.masterMessenger = new GameMaster(this.$solace, this.$parent.appProps, this.$route.query.username,
        this.$route.query.isMaster,
        this.handleMsg.bind(this), this.handleStateChange.bind(this));
      this.masterMessenger.connect();
    } else {
      this.$router.push({
        name: 'signin'
      });
    }
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
      this.masterMessenger.disconnect();
      this.masterMessenger = null;
    }
  },

  // Underlying model
  data() {
    return {
      msg: 'Trouble Flipper Scoreboard',
      state: 'connecting',
      userId: "",
      userName: this.$route.query.username,
      scoreboardInfo: {
        teams: [
        ]
      }
    }
  },

  // any actions
  methods: {
    handleMsg: function(msg) {
      console.log(msg);
      if (msg.state) {
        this.state = msg.state;
      }
      this.userId = msg.userId;
      this.userName = msg.userName;

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
</style>
