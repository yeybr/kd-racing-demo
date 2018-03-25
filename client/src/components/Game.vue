<template>
  <div class="game-panel">
    <div class="title">
      <h1>{{msg}}</h1>
      <div class="user-info">User: {{userName}}</div>
    </div>
    <div class="game-info">
      <template v-if="state === 'playing'">
        <div class="status">
          <span class="stats">Puzzle: {{gameInfo.stats.gameName}}</span>
          <span class="stats">Team: {{gameInfo.teamName}}</span>
          <div class="stats">Players:
            <template v-for="(player, index) in gameInfo.players">
              {{player.name}}{{(index === gameInfo.players.length - 1) ? '': ', '}}
            </template>
          </div>
          <span class="stats">Progress: {{gameInfo.stats.finished}} / {{gameInfo.stats.total}}</span>
          <span class="stats">Total Moves: {{gameInfo.stats.totalMoves}}</span>
          <span class="stats">Correct Moves: {{gameInfo.stats.correctMoves}}</span>
        </div>
      </template>
      <template v-else-if="state === 'waiting'">
        <h2>Waiting for game to start...</h2>
      </template>
      <template v-else>
        <h2>Connecting...</h2>
      </template>
    </div>
    <div v-show="state === 'playing'" class="game-board">
      Game Board
    </div>
    <div id="puzzle">
      <div v-for="piece in puzzle" :key="piece.index" class="spot">
        <img src="../assets/puzzle.png" v-bind:style="piece.style"/>
      </div>
    </div>
    <div class="hidden-image">
      <img src="../assets/puzzle.png"/>
    </div>
  </div>
</template>

<script>
import { Player } from '@/messaging/player'
export default {
  name: "game",
  // lifecycle callbacks
  created() {
    console.log('game created: data bound');
    if (this.$route.query.username) {
      // DO NOT initialize playerMessenger in data() function; otherwise all its memebers will become reactive
      // including the solace API. We don't want solace API's data structure to be injected with Observer stuff,
      // it causes SolaceClientFactory.init() to fail
      this.playerMessenger = new Player(this.$solace, this.$parent.appProps, this.$route.query.username,
        this.handleMsg.bind(this), this.handleStateChange.bind(this));
      this.playerMessenger.connect();
    } else {
      this.$router.push({
          name: 'signin'
      });
    }
  },
  mounted() {
    var puzzle = new Image;
    puzzle.src = "../assets/puzzle.png";
    console.log(puzzle);
  },
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
      this.playerMessenger.disconnect();
      this.playerMessenger = null;
    }
  },

  // Underlying model
  data() {
    var pieces = [];
    for (var i = 0; i < 9; ++i) {
      pieces.push({
        index: i,
        width: "400px",
        test: "kevin",
        style: `width: 400px; margin-left: ${i % 3 * -32}vw; margin-top: ${Math.floor(i / 3) * -32}vw;`
      });
    }
    console.log("pieces done");
    this.shuffle(pieces);
    console.log("shuffle");
    return {
      msg: 'Trouble Flipper',
      state: 'connecting',
      userId: '',
      userName: this.$route.query.username,
      gameInfo: {
        id: "1",
        name: "Yoshi",
        teamId: '',
        teamName: '',
        players: [
        ],
        stats: {
          gameId: '',
          gameName: '',
          total: 0,
          finished: 0,
          totalMoves: 0,
          correctMoves: 0
        }
      },
      puzzle: pieces
    };
  },

  // any actions
  methods: {
    handleMsg: function(msg) {
      console.log(msg, this);
      if (msg.state) {
        this.state = msg.state;
      }
      this.userId = msg.userId;
      this.userName = msg.userName;
      if (msg.gameInfo) {
        this.gameInfo.teamId = msg.gameInfo.teamId;
        this.gameInfo.teamName = msg.gameInfo.teamName;
        let currentPlayers = this.gameInfo.players;
        let newPlayers = msg.gameInfo.players;
        if (currentPlayers.length <= newPlayers.length) {
          for (let i = 0; i < currentPlayers.length; i++) {
            Object.assign(currentPlayers[i], newPlayers[i]);
          }
          if (currentPlayers.length < newPlayers.length) {
            currentPlayers.push(...newPlayers.slice(currentPlayers.length));
          }
        } else {
          for (let i = 0; i < newPlayers.length; i++) {
            Object.assign(currentPlayers[i], newPlayers[i]);
          }
          currentPlayers.splice(newPlayers.length, (newPlayers.length - currentPlayers.length));
        }
        if (msg.gameInfo.stats) {
          this.gameInfo.stats.gameId = msg.gameInfo.stats.gameId;
          this.gameInfo.stats.gameName = msg.gameInfo.stats.gameName;
          this.gameInfo.stats.total = msg.gameInfo.stats.total;
          this.gameInfo.stats.finished = msg.gameInfo.stats.finished;
          this.gameInfo.stats.totalMoves = msg.gameInfo.stats.totalMoves;
          this.gameInfo.stats.correctMoves = msg.gameInfo.stats.correctMoves;
        }
      }
    },
    handleStateChange: function(msg) {
      console.log('state change', msg);
      this.state = msg.state;
    },
    shuffle: function(array) {
      var currentIndex = array.length, temporaryValue, randomIndex;

      // While there remain elements to shuffle...
      while (0 !== currentIndex) {

        // Pick a remaining element...
        randomIndex = Math.floor(Math.random() * currentIndex);
        currentIndex -= 1;

        // And swap it with the current element.
        temporaryValue = array[currentIndex];
        array[currentIndex] = array[randomIndex];
        array[randomIndex] = temporaryValue;
      }

      return array;
    }
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<!-- Enable sass by using lang="scss" -->
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

.game-panel {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  width: 100%;
}
.title {
  padding: 15px;
}
.game-info {
  padding: 15px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  
}
.game-board {
  flex: 1 1 0;
}
.status {
  display: flex;
  flex-direction: row;
}
.stats {
  padding: 15px 25px 15px 15px;
}

#puzzle {
  position: absolute;
  bottom: 0;
  width: 100%;
  height:60%;
  text-align: center;
  border: solid 1px grey;
}
.hidden-image {
  position: fixed;
  top: 200%;
}
.puzzle div {
  display: inline;
}
.spot {
  display: inline-block;
  width: calc(32vw);
  height: calc(32vw);
  overflow: hidden;
  vertical-align: top;
  border: solid 1px grey;
}

</style>
