<template>
  <div id='game' class="game-panel" :class="{backgroundwhite: backgroundwhite}">
    <div v-show="state === 'playing'" class="header-section">
      <div style="position:relative;background-image: url(static/backgrounds.png);">
        <div class="titlebar">{{teamInfo.teamName}}</div>
      </div>
      <div class="header-info">
        <div class="user-info">
          <div class="name-tag">{{username}} </div>
          <div class="profile" :style="styleAvatar" @click="power">
          </div>
        </div>
        <div class="others-info">
          <div v-for="(player, i) in otherPlayers"  :index="i" :key="player.clientId" class="others-info-profile" :style="[player.styleAvatar]">
            {{player.username}}
          </div>
        </div>
      </div>
    </div>
    <div class="game-stats">
      <div v-show="state === 'waiting'" class="status waiting">
        <label>Waiting for game to start...</label>
        <button type="button" class="start-btn btn" @click="startGame()">Start Game!</button>
      </div>
      <div v-show="state === 'connecting'" class="status waiting">
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
        <div class="heromug goomba" data_id="goomba" @click="pickAvatar">
          <div class="heroselect"></div>
          <div class="nametag">Goomba</div>
        </div>
        <div class="heromug yoshi"  data_id="yoshi" @click="pickAvatar">
          <div class="heroselect"></div>
          <div class="nametag" hero-name="bowser">Yoshi</div>
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
    <div v-if="state === 'playing'" id="puzzle-area" class="backgroundwhite">
      <div id="puzzle" :style="puzzleStyle">
        <transition-group name="puzzleswap" >
          <div  v-for="(piece, i) in puzzle" @click="select" :index="piece.index" :key="piece.index" class="spot" :class="{selected: piece.selected}" :style="[holderStyle]">
            <img :src="puzzlePicture" :index="i" v-bind:style="piece.style"/>
            <div class="highlight"></div>
          </div>
        </transition-group>
        <div v-if="win" id="win">
          Winner!
          <div class="countdown">Next game starting in {{countdown}}</div>
        </div>
      </div>
    </div>
    <!-- <div v-if="state === 'playing'" class="status">
      <span class="stats">Time Remaining: {{timeRemaining}}</span>
    </div> -->
    <!-- shape="M50,3l12,36h38l-30,22l11,36l-31-21l-31,21l11-36l-30-22h38z"		 -->
    <div v-if="state === 'playing'" class="rank-container backgroundwhite">
      <div class="rank">
        <div class="label">Team Rank</div>
        <div class="value">
          {{teamInfo.teamRank}}/{{teamInfo.totalTeam}}
        </div>
      </div>
      <div class="statusbar">
        <template v-if="timeForEachMove > 0">
          <loading-progress
            :progress="progress"
            :indeterminate="indeterminate"
            :counter-clockwise="counterClockwise"
            shape="circle"
            size="20"
            height="40"
            width="40"
            fill-duration="1"
          />
        </template>
      </div>
      <div class="rank">
        <div class="label">Overall Rank</div>
        <div class="value">
          {{rank}}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { UsersAckMessage, TeamsMessage, parseReceivedMessage } from '@/messaging/messages.js';
import { Player } from "@/messaging/player";
import CommonUtils from "./common-utils";
export default {
  name: "game",
  mixins: [CommonUtils],
  // lifecycle callbacks
  created() {
    console.log("game created: data bound");
    this.indeterminate = false;
    this.counterClockwise = true;

    if (this.$route.query.username) {
      this.username = this.$route.query.username;
      // Retrive userInfo from local storage
      let userInfo = this.retrieveFromStorage(
        "localStorage",
        "trouble_flipper_player"
      );
      let clientId = null;
      if (userInfo) {
        clientId = userInfo.clientId;
        if (userInfo.username !== this.username) {
          userInfo.username = this.username;
          this.saveIntoStorage(
            "localStorage",
            "trouble_flipper_player",
            userInfo
          );
        }
      }

      this.clientId = clientId;
      // DO NOT initialize playerMessenger in data() function; otherwise all its memebers will become reactive
      // including the solace API. We don't want solace API's data structure to be injected with Observer stuff,
      // it causes SolaceClientFactory.init() to fail
      this.playerMessenger = new Player(
        this.$solace,
        this.$parent.appProps,
        { username: this.username, clientId: this.clientId },
        this.handleMsg.bind(this)
      );
      this.playerMessenger.connect();
    } else {
      this.$router.push({
        name: "signin"
      });
    }
  },
  mounted() {},
  // beforeUpdate() {
  //   // add any customized code before DOM is re-render and patched based changes in data
  //   console.log('game beforeUpdate: data is changed, about to rerender dom');
  // },
  // updated() {
  //   console.log('game updated: dom is rerendered');
  // },
  destroyed() {
    // clean up any resource, such as close websocket connection, remove subscription
    console.log("game destroyed: dom removed");
    this.stopCountDown();
    if (this.playerMessenger) {
      this.playerMessenger.unregister();
      this.playerMessenger.disconnect();
      this.playerMessenger = null;
    }
  },

  // Underlying model
  data() {
    let avatarLink = "";
    return {
      // UI only
      title: "Trouble Flipper",
      state: "connecting",
      win: false,
      timeRemaining: 0, // timing remaining for current move before shuffle
      timeForEachMove: 0,
      clientId: "",
      username: this.username,
      avatarLink: avatarLink,
      rank: 0,
      puzzlePicture: 'static/puzzle1.png',
      holderStyle: {
        width: "0px",
        height: "0px"
      },
      puzzleStyle: '',
      backgroundwhite: false,
      selected: null,
      styleAvatar: {
        "background-image": avatarLink,
        "background-size": "contain",
        "background-repeat": "no-repeat",
        "background-position": "center",
        height: "15vh",
        width: "25vw"
      },
      // From server
      puzzle: [],
      teamInfo: {
        teamId: "",
        teamName: "",
        players: [],
        puzzleName: "",
        totalTeam: 0,
        teamRank: 0,
        timeAllowedForEachMove: 0
      },
      countdown: 3
    };
  },

  computed: {
    progress: function() {
      if (typeof this.timeRemaining === "number" && typeof this.timeForEachMove === "number" && this.timeForEachMove > 0) {
        return this.timeRemaining / this.timeForEachMove;
      }
      return 0;
    },
    otherPlayers: function() {
      if (Array.isArray(this.teamInfo.players) && this.teamInfo.players.length > 0) {
        return this.teamInfo.players.filter((player) => {
          return player.clientId !== this.clientId;
        })
      }
    }
  },

  // any actions
  methods: {
    getRandomInt: function(max) {
      return Math.floor(Math.random() * Math.floor(max));
    },
    handleMsg: function(msg) {
      console.log("Got message", msg);
      let newState = null;
      if (msg instanceof UsersAckMessage) {
        // user connect reply
        this.clientId = msg.clientId;
        this.username = msg.username;
        newState = 'waiting';
        this.handleStateChange(newState);
        return;
      } else if (msg instanceof TeamsMessage) {
        let teamMsg = {};
        teamMsg.gameWon = msg.gameWon;
        if (msg.puzzle) {
          teamMsg.puzzle = msg.puzzle;
        }
        let teamInfo = {
          teamName: 'Team 1',
          puzzleName: 'puzzle3',
          timeAllowedForEachMove: 10,
          totalTeam: 5,
        };
        teamMsg.teamInfo = teamInfo;
        if (msg.teamId) {
          teamInfo.teamId = msg.teamId;
        } else {
          teamInfo.teamId = this.teamInfo.teamId;
        }
        if (msg.players) {
          teamInfo.players = msg.players;
        } else {
          teamInfo.players = this.teamInfo.players;
        }
        this.handleTeamsMessage(teamMsg);
      } else if (msg.connected == false) {
        newState = 'connecting';
        this.handleStateChange(newState);
        return;
      }
    },
    handleTeamsMessage: function(msg) {
      console.log('teamMsg', msg);
      let newState = null;
      if (msg.puzzle && msg.puzzle.length > 0) {
        if (this.puzzle.length === 0) {
          // must be first team message, transition to select avatar
          newState = 'start';
        }
        let pieces = msg.puzzle;
        // assume is 5 x 5
        let size = Math.sqrt(msg.puzzle.length);
        var square = 412;
        if (window.innerWidth < square) {
          square = window.innerWidth;
        }
        this.puzzleStyle = `width: ${square}px; height: ${square}px;`;

        var splits = Math.floor(99 / size);
        var movePercent = splits / 100;
        var unit = square * movePercent;
        this.holderStyle.width = unit + "px";
        this.holderStyle.height = unit + "px";

        for (var i = 0; i < size * size; ++i) {
          pieces[i].style = `width: ${square}px; margin-left: -${unit *
              (pieces[i].index % size)}px; margin-top: -${unit * Math.floor(pieces[i].index / size)}px;`;
          pieces[i].selected = false;
        }
        this.updateArray(this.puzzle, pieces);
        this.selected = null;
        if (newState !== 'start') {
          this.checkWinCondition(msg.gameWon);
        }
      }
      if (msg.teamInfo) {
        if (msg.teamInfo.players) {
          let newPlayers = msg.teamInfo.players;
          let me = null;
          newPlayers.forEach((player) => {
            if (player.avatar) {
              newState = 'playing';
              let style = {
                "background-image": `url("static/${player.avatar}-mario.jpg")`,
                "background-size": "contain",
                "background-repeat": "no-repeat",
                "background-position": "center"
              };
              player.styleAvatar = style;
              player.avatarLink = `url("static/${player.avatar}-mario.jpg")`;
            }
            if (player.clientId === this.clientId) {
              me = player;
            }
          });
          if (me) {
            this.character = me.avatar;
            this.avatarLink = `url("static/${me.avatar}-mario.jpg")`;
            this.styleAvatar["background-image"] = this.avatarLink;
            this.rank = me.rank;
          }
        }
        this.updateData(this.teamInfo, msg.teamInfo);
        if (this.teamInfo.puzzleName) {
          this.puzzlePicture = `static/${this.teamInfo.puzzleName}.png`;
        }
      }
      this.handleStateChange(newState);
    },
    handleStateChange: function(newState) {
      if (!newState) {
        return;
      }
      console.log("New state change", newState);
      let currentState = this.state;
      this.state = newState;
      if (currentState !== "playing" && this.state === "playing") {
        this.backgroundwhite = true;
        this.timeForEachMove =
          this.teamInfo && this.teamInfo.timeAllowedForEachMove
            ? this.teamInfo.timeAllowedForEachMove
            : 0;
        this.timeRemaining = this.timeForEachMove;
        this.startCountDown();
      } else if (this.state !== "playing") {
        this.stopCountDown();
        if (this.state === "waiting") {
          console.log(
            "Save username " +
              this.username +
              ", clientId " +
              this.clientId +
              " to localStorage"
          );
          this.saveIntoStorage("localStorage", "trouble_flipper_player", {
            username: this.username,
            clientId: this.clientId
          });
        } else if (this.state === "connecting") {
          this.cleanupGame();
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
        this.playerMessenger.pickAvatar(
          event.currentTarget.getAttribute("data_id")
        );
      }
    },
    startCountDown: function() {
      this.stopCountDown();
      if (this.timeForEachMove > 0) {
        console.log("start count down timer", this.timeRemaining);
        this.countdownTimer = setInterval(() => {
          this.timeRemaining--;
          if (this.timeRemaining <= 0) {
            // console.log("no time left", this.timeRemaining);
            this.stopCountDown();
            this.randomSwap();
          }
        }, 1000);
      }
    },
    stopCountDown: function() {
      if (this.countdownTimer) {
        console.log("stop countdownTimer");
        clearInterval(this.countdownTimer);
        this.countdownTimer = null;
      }
    },
    cleanupGame: function() {
      // clear puzzle
      this.puzzle.splice(0, this.puzzle.length);
      if (this.teamInfo) {
        this.teamInfo.players.splice(0, this.teamInfo.players.length);
        this.teamInfo.teamId = '';
        this.teamInfo.teamName = '';
      }
    },
    randomSwap: function() {
      console.log("random swap");
      var piece1 = this.puzzle[this.getRandomInt(9)];
      var piece2 = this.puzzle[this.getRandomInt(9)];
      this.swap(piece1, piece2);
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
      let piece1 = {index: a.index};
      let piece2 = {index: b.index};
      let pieces = this.puzzle.map((piece) => {
        return {index: piece.index};
      });
      this.playerMessenger.swap(piece1, piece2, pieces);
    },
    power: function() {
      if (this.character === "peach") {
        console.log("Peach Heal power activated!");
        this.playerMessenger.peachHeal("mario");
      } else if (this.character === "mario" && this.selected) {
        let puzzlePiece = {index: this.selected.index};
        this.playerMessenger.starPower(puzzlePiece);
      }
    },
    checkWinCondition: function(gameWon) {
      this.win = gameWon;
      if (this.win) {
        console.log("WINNER!");
        this.stopCountDown();
        var i = setInterval(() => {
          this.countdown--;
          if (this.countdown === 0) {
            this.countdown = 3;
            clearInterval(i);
          }
        }, 1000);
      } else {
        // reset timeRemaining
        this.timeRemaining = this.timeForEachMove;
        this.startCountDown();
      }
    },
    starPath(x, y, size, points) {
      // const angle = Math.PI / points;
      // const starCoords = _.map(_.range(2 * points), (index) => {
      //   const length = index % 2 === 0 ? size : size / 2;
      //   return "L " + (length * Math.sin(angle * (index + 1)) + x) + ", " +
      //     (length * Math.cos(angle * (index + 1)) + y);
      // });
      // const path = starCoords.toString();
      return "M " + (x + size) + "," + (y + size) + " " + path + "z";
    }
  }
};

try {
  locOrientation =
    screen.lockOrientation ||
    screen.mozLockOrientation ||
    screen.msLockOrientation ||
    screen.orientation.lock;
  locOrientation("landscape");
} catch (e) {
  //ignore
}
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
  flex: 1 1 auto;
}

.backgroundwhite {
  background: white;
}

/* header section */
.header-section {
  /* padding: 15px; */
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
  display: inline-block;
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
  width: 100%;
}

.others-info {
  display: flex;
  width: 60vw;
  align-content: center;
  align-items: center;
  flex-wrap: wrap;
}
.others-info-profile {
  /* width: 20vw;
    height: 12vh; */
  width: 25vw;
  height: 8vh;
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
  transition: 0.6s;
  transform-style: preserve-3d;
}
.spot.selected {
  border: solid 1px red;
}
/* .spot.swapped {
    transform:  rotateY(360deg);
} */
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
  flex-direction: column;
}

#win .countdown {
  font-size: 30px;
  color: #ffee01;
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
  font-size: 4vh;
}
.heros {
  display: flex;
  flex-flow: row wrap;
  justify-content: center;
}
.heromug {
  position: relative;
  border: 1px #f56f6f9e solid;
  border-radius: 4vw;
  min-width: 30vw;
  min-height: 28vh;

  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
  background-repeat: no-repeat;
  background-position: center;
  background-size: contain;
  margin: 2vw;
  background-color: white;
}

.heroselect {
  min-height: 28vh;
  min-width: 30vw;
  border-radius: 4vw;
}
.heromug > .heroselect:hover {
  background-color: #ff00003b;
}

.heromug:hover {
  box-shadow: 0 5px 55px rgba(0, 0, 0, 0.3);
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

.heromug.goomba {
  background-image: url(../assets/goomba-mario.jpg);
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
.rank-container {
  position: relative;
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  align-items: stretch;
  min-height: 60px;
  border-top: 1px solid grey;
}
.rank-container > div {
  padding-top: 3px;
  border-right: 1px solid grey;
}

.rank-container div:last-child {
  border-right: none;
}
.rank-container .rank {
  flex: 1;
  text-align: center;
}
.vue-progress-path .progress {
  stroke: red;
}
.rank-container .statusbar {
  flex: 1 1 auto;
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  position: relative;
}
.rank-container .label {
  line-height: 12px;
  font-size: 12px;
}
.rank-container .value {
  font-size: 40px;
  line-height: 40px;
}

</style>
