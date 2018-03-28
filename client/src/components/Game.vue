<template>
  <div class="game-panel">
    <div class="header-section">
      <h1>{{msg}}</h1>
      <div class="header-info">
        <div class="game-info">
          <span class="info">Puzzle: {{gameInfo.gameName}}</span>
          <span class="info">Team: {{gameInfo.teamName}}</span>
          <div class="info">Players:
            <template v-for="(player, index) in gameInfo.players">
              {{player.name}}{{(index === gameInfo.players.length - 1) ? '': ', '}}
            </template>
          </div>
        </div>
        <div class="user-info">
          <div class="name-tag">{{userName}} </div>
          <div class="profile" :style="styleAvatar" >
          </div>
        </div>
      </div>
    </div>
    <div class="game-stats">
      <div v-if="state === 'playing'" class="status">
        <span class="stats">Progress: {{gameInfo.stats.finished}} / {{gameInfo.stats.total}}</span>
        <span class="stats">Total Moves: {{gameInfo.stats.totalMoves}}</span>
        <span class="stats">Correct Moves: {{gameInfo.stats.correctMoves}}</span>
      </div>
      <div v-else-if="state === 'waiting'" class="status waiting">
        <h3>Waiting for game to start...</h3>
      </div>
      <div v-else class="status waiting">
        <h3>Connecting...</h3>
      </div>
    </div>
    <div v-show="state === 'playing'" id="puzzle-area">
      <div id="puzzle" :style="puzzleStyle">
        <div v-for="(piece, i) in puzzle" @click="select" :index="piece.index" :key="piece.index" class="spot" :class="{selected: piece.selected}" :style="holderStyle">
          <img :src="puzzlePicture" :index="i" v-bind:style="piece.style"/>
          <div class="highlight"></div>
        </div>
        <div v-if="gameInfo.win" id="win">
          Winner!
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Player } from '@/messaging/player';
import Utils from './Utils.vue';
export default {
  name: "game",
  mixins: [Utils],
  // lifecycle callbacks
  created() {
    console.log('game created: data bound');
    if (this.$route.query.username) {
      this.userName = this.$route.query.username;
      // DO NOT initialize playerMessenger in data() function; otherwise all its memebers will become reactive
      // including the solace API. We don't want solace API's data structure to be injected with Observer stuff,
      // it causes SolaceClientFactory.init() to fail
      this.playerMessenger = new Player(this.$solace, this.$parent.appProps, this.userName,
        this.handleMsg.bind(this), this.handleStateChange.bind(this));
      this.playerMessenger.connect();
    } else {
      this.$router.push({
          name: 'signin'
      });
    }
  },
  mounted() {
    var that = this;
    // this.interval = setInterval(function() {
    //   var piece1 = this.puzzle[this.getRandomInt(9)];
    //   var piece2 = this.puzzle[this.getRandomInt(9)];
    //   this.swap(piece1, piece2);
    //   this.$forceUpdate();
    // }.bind(this), 5000);
    console.log(this.interval);
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
    var size = this.getRandomInt(3) + 3;
    var splits = Math.floor(99 / size);
    var pieces = [];
    var square = 412;
    if (window.innerWidth < square) {
      square = window.innerWidth;
    }
    for (var i = 0; i < size * size; ++i) {
      var movePercent = splits / 100;
      var unit = square * movePercent;
      pieces.push({
        index: i,
        style: `width: ${square}px; margin-left: -${unit * (i % size)}px; margin-top: -${unit * Math.floor(i / size)}px;`,
        selected: false
      });
    }
    var holderStyle = `width: ${unit}px; height: ${unit}px;`;
    var puzzleStyle = `width: ${square}px; height: ${square}px;`;
    console.log("pieces done");
    this.shuffle(pieces);
    console.log("shuffle");

    let random = this.getRandomInt(4) + 1;
    let puzzlePicture = `static/puzzle${random}.png`;
    let avatarLink = `url("static/${this.$route.query.avatar}-mario.jpg")`;
    console.log(puzzlePicture);
    return {
      size: size,
      holderStyle: holderStyle,
      puzzleStyle: puzzleStyle,
      puzzlePicture: puzzlePicture,
      msg: 'Trouble Flipper',
      state: 'connecting',
      userId: '',
      userName: this.userName,
      avatarLink: avatarLink,
      gameInfo: {
        id: '',
        name: '',
        teamId: '',
        teamName: '',
        win: false,
        players: [
        ],
        stats: {
          total: 0,
          finished: 0,
          totalMoves: 0,
          correctMoves: 0
        }
      },
      puzzle: pieces,
      selected: null,
      styleAvatar: {
        'background-image': avatarLink,
        'background-size': 'contain',
        'background-repeat': 'no-repeat',
        'background-position': 'center',
        'height': '6vh',
        'width': '8vw'
      }
    };
  },

  // any actions
  methods: {
    getRandomInt: function(max) {
      return Math.floor(Math.random() * Math.floor(max));
    },
    handleMsg: function(msg) {
      console.log('Got message', msg);
      if (msg.state) {
        this.state = msg.state;
      }
      this.userId = msg.userId;
      this.userName = msg.userName;
      if (msg.gameInfo) {
        this.updateData(this.gameInfo, msg.gameInfo);
      }
    },
    handleStateChange: function(msg) {
      console.log('State change', msg);
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
    },
    select: function(e) {
      let isAlreadySelected = this.puzzle.find(p => {
        return p.selected;
      });
      let index = e.target.previousElementSibling.attributes.index.value;
      this.selected = this.puzzle[index];
      this.selected.selected = true;
      if (isAlreadySelected) {
        this.swap(isAlreadySelected, this.selected);
      }
    },
    swap: function(a, b) {
      console.log(a.index, b.index);
      let aIndex = this.puzzle.findIndex(p => {
        return p.index == a.index;
      });
      let bIndex = this.puzzle.findIndex(p => {
        return p.index == b.index;
      });
      var temp = this.puzzle[aIndex];
      this.puzzle[aIndex] = this.puzzle[bIndex];
      this.puzzle[bIndex] = temp;
      a.selected = false;
      b.selected = false;
      this.checkWinCondition();
    },
    checkWinCondition: function() {
      this.gameInfo.win = this.puzzle.reduce((result, piece, i) => {
        return result && piece.index == i;
      }, true);
      if (this.gameInfo.win) {
        console.log("WINNER!");
        console.log(this.interval);
        clearInterval(this.interval);
      }
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
.header-section {
  padding: 15px;
}
.header-info {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  padding: 10px 10px;
}
.header-info .game-info {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.header-info .game-info .info {
  padding: 0px;
}
.game-stats {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  padding: 0px 10px;
}
.status {
  display: flex;
  flex-direction: row;
}
.status.waiting {
  justify-content: center;
}
.stats {
  padding: 15px 25px 15px 15px;
}
#puzzle-area {
  flex: 1 1 auto;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
#puzzle {
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
  overflow: hidden;
  vertical-align: top;
  border: solid 1px grey;
  box-sizing: border-box;
  position: relative;
}

.spot.selected {
  border: solid 1px red;
}

.spot.selected img {
  box-shadow: 10px 10px 10px 10px red inset;
}

#win {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  color: #006fea;
  font-size: 100px;
  vertical-align: middle;
  text-align: center;
  height: 100%;
  line-height: 100%;
  padding: 40% 0;
  background: rgba(0, 0, 0, 0.2);
}
.profile {
  border: 1px #b9acac9e solid;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
  display:inline-block;
}
.user-info {
  font-size: 3vw;
  position: relative;
  padding: 0px 10px;
}
.name-tag {
  text-align: center;
  background: grey;
  color: white;
  width: 8vw;
}
.highlight {
  position: absolute;
  top: 0;
  bottom: 0;
  right: 0;
  left: 0;
}

.selected .highlight {
  box-shadow: inset 0 0 20px red;
}
</style>
