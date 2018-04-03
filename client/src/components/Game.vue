<template>
  <div class="game-panel">
    <div v-if="state === 'playing'" class="header-section">
      <h1>{{title}}</h1>
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
          <div class="name-tag">{{username}} </div>
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
        <label>Waiting for game to start...</label>
        <button type="button" class="start-btn btn" @click="startGame()">Start Game!</button>
      </div>
      <div v-else-if="state === 'connecting'" class="status waiting">
        <label>Connecting...</label>
      </div>
    </div>
    <div v-if="state === 'start'" id="choose">
      <div class="titlebar">Choose your character</div>
      <div class="heros">
        <div class="heromug bowser" data_id="bowser" @click="pickAvatar">
        <div class="heroselect" ></div>
        <div class="nametag" hero-name="bowser">Bowser</div>
        </div>
        <div class="heromug yoshi"  data_id="yoshi" @click="pickAvatar">
          <div class="heroselect"></div>
              <div class="nametag" hero-name="bowser">Yoshi</div>
        </div>
        <div class="heromug toad" data_id="toad" @click="pickAvatar">
          <div class="heroselect"></div>
              <div class="nametag">Toad</div>
        </div>
        <div class="heromug peach" data_id="peach" @click="pickAvatar">
          <div class="heroselect"></div>
          <div class="nametag">Peach</div>
        </div>
        <div class="heromug mario" data_id="mario" @click="pickAvatar">
          <div class="heroselect"></div>
          <div class="nametag">Mario</div>
        </div>
      </div>
    </div>
    <div v-if="state === 'playing'" id="puzzle-area">
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
    <div v-if="state === 'playing'" class="status">
      <span class="stats">Time Remaining: {{timeRemaining}}</span>
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
      this.username = this.$route.query.username;
      // Retrive userInfo from local storage
      let userInfo = this.retrieveFromStorage('localStorage', 'trouble_flipper_player');
      let client = null;
      if (userInfo) {
        client = userInfo.client;
        if (userInfo.username !== this.username) {
          userInfo.username = this.username;
          this.saveIntoStorage('localStorage', 'trouble_flipper_player', userInfo);
        }
      }
      this.client = client;
      // DO NOT initialize playerMessenger in data() function; otherwise all its memebers will become reactive
      // including the solace API. We don't want solace API's data structure to be injected with Observer stuff,
      // it causes SolaceClientFactory.init() to fail
      this.playerMessenger = new Player(this.$solace, this.$parent.appProps,
        {username: this.username, client: this.client},
        this.handleMsg.bind(this));
      this.playerMessenger.connect();
    } else {
      this.$router.push({
        name: 'signin'
      });
    }
  },
  // mounted() {
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
    this.stopCountDown();
    if (this.playerMessenger) {
      this.playerMessenger.unregister();
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
    var movePercent = splits / 100;
    var unit = square * movePercent;
    for (var i = 0; i < size * size; ++i) {
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
    let avatarLink = '';
    console.log(puzzlePicture);
    return {
      size: size,
      holderStyle: holderStyle,
      puzzleStyle: puzzleStyle,
      puzzlePicture: puzzlePicture,
      title: 'Trouble Flipper',
      state: 'connecting',
      timeRemaining: 5, // timing remaining for current move before shuffle
      timeForEachMove: 5,
      client: '',
      username: this.username,
      avatarLink: avatarLink,
      gameInfo: {
        id: '',
        name: '',
        teamId: '',
        teamName: '',
        timeAllowedForEachMove: 5,
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
      this.client = msg.client;
      this.username = msg.username;
      if (msg.gameInfo) {
        this.updateData(this.gameInfo, msg.gameInfo);
      }
      if (this.gameInfo.avatar) {
        this.avatarLink = `url("static/${this.gameInfo.avatar}-mario.jpg")`;
        this.styleAvatar['background-image'] = this.avatarLink;
      }
      this.handleStateChange(msg);
    },
    handleStateChange: function(msg) {
      if (msg.state) {
        console.log('State change', msg);
        let currentState = this.state;
        this.state = msg.state;
        if (currentState !== 'playing' && this.state === 'playing') {
          this.timeForEachMove = (this.gameInfo && this.gameInfo.timeAllowedForEachMove) ? this.gameInfo.timeAllowedForEachMove : 5;
          this.timeRemaining = this.timeForEachMove;
          this.startCountDown();
        } else if (this.state !== 'playing') {
          this.stopCountDown();
          if (this.state === 'waiting') {
            console.log('Save username ' + this.username + ', client ' + this.client + ' to localStorage');
            this.saveIntoStorage('localStorage', 'trouble_flipper_player', { username: this.username, client: this.client });
          }
        }
      }
    },
    startGame: function() {
      if (this.playerMessenger) {
        this.playerMessenger.startGame();
      }
    },
    pickAvatar: function(event) {
      console.log("Play with " + event.currentTarget.getAttribute("data_id"));
      if (this.playerMessenger) {
        this.playerMessenger.pickAvatar(event.currentTarget.getAttribute("data_id"));
      }
    },
    startCountDown: function() {
      console.log('start count down timer', this.timeRemaining);
      this.stopCountDown();
      this.countdownTimer = setInterval(()=> {
        this.timeRemaining--;
        if (this.timeRemaining <= 0) {
          console.log('no time left', this.timeRemaining);
          this.stopCountDown();
          this.randomSwap();
        }
      }, 1000);
    },
    stopCountDown: function() {
      if (this.countdownTimer) {
        console.log('stop countdownTimer');
        clearInterval(this.countdownTimer);
        this.countdownTimer = null;
      }
    },
    randomSwap: function() {
      console.log('random swap');
      // TODO: send message to server to request random swap
      var piece1 = this.puzzle[this.getRandomInt(9)];
      var piece2 = this.puzzle[this.getRandomInt(9)];
      this.swap(piece1, piece2);
      this.$forceUpdate();
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
        this.stopCountDown();
      } else {
        // reset timeRemaining
        this.timeRemaining = this.timeForEachMove;
        this.startCountDown();
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
  height: 100%;
  flex: 1 1 auto;
}

/* header section */
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

/* game stats/status section */
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
.stats {
  padding: 15px 25px 15px 15px;
}
.status.waiting {
  align-self: stretch;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.status.waiting label {
  margin-top: 40px;
  font-size: 8vw;
}
.status.waiting .start-btn {
  font-size: 32px;
  margin-bottom: 80px;
  background: #006fea;
  border-radius: 4px;
  padding: 10px;
  margin: 40px 0;
  color: white;
  width: 200px;
  cursor: pointer;
}

/* puzzle section */
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
  background: rgba(0, 0, 0, 0.2);
  text-shadow: -1px 0 black, 0 1px black, 1px 0 black, 0 -1px black;
  z-index: 2;
  display: flex;
  justify-content: center;
  align-items: center;
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

/* TODO: move avatar selection to its own component */
.titlebar {
  text-align: center;
  background: #bfa5a538;
  padding: 5px;
}
.heros {
  display: flex;
  flex-flow: row wrap;
  justify-content: center;
}
.heromug {
  position:relative;
  border: 1px #f56f6f9e solid;
  border-radius: 4vw;
  min-width: 30vw;
  min-height: 28vh;

  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
  background-repeat: no-repeat;
  background-position: center;
  background-size: contain;
  margin: 2vw;
  background-color:white;
}

.heroselect {
  min-height: 28vh;
  min-width: 30vw;
  border-radius: 4vw;
}
.heromug > .heroselect:hover{
  background-color: #ff00003b;
}

.heromug:hover {
  box-shadow: 0 5px 55px rgba(0,0,0,0.3);
}

.heromug.bowser {
  background-image: url(../assets/bowser-mario.jpg);
}

.heromug.yoshi {
  background-image: url(../assets/yoshi-mario.jpg);
}
.heromug.peach {
  background-image: url(../assets/peach-mario.jpg);
}

.heromug.toad {
  background-image: url(../assets/toad-mario.jpg);
}

.heromug.mario {
  background-image: url(../assets/mario-mario.jpg);
}

.heromug > .nametag {
  position: absolute;
  bottom: 2vw;
  background: #3e1d0c66;
  width: 100%;
  text-align: center;
  color: white;
}
</style>
