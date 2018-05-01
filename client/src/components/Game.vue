<template>
  <div id='game' class="game-panel" :class="{backgroundwhite: backgroundwhite}">
    <div class="modal" v-show="chooseHeal">
      <div class="text">Choose a player</div>
      <div class="content">
        <div v-for="(player) in otherPlayers" v-if="player.character" :key="player.clientId" class="user-info" :class="player.character.type">
          <div class="headshot" @click="heal(player)">
            <img :src="player.avatarLink">
          </div>
        </div>
      </div>
    </div>
    <div v-show="state === 'playing'" class="header-section">
      <div style="position:relative;background-image: url(static/backgrounds.png);">
        <div class="titlebar">{{teamInfo.teamName}}</div>
      </div>
      <div class="header-info">
        <div class="me user-info" :class="characterType">
          <div class="headshot" @click="power()">
            <img :src="avatarLink">
          </div>
          <div class="name">{{username}}</div>
          <div class="powers">
            <div v-for="n in powerMoves" :key="n" class="power"></div>
          </div>
        </div>
        <div v-for="(player) in otherPlayers" v-if="player.character" :key="player.clientId" class="user-info" :class="player.character.type">
          <div class="headshot">
            <img :src="player.avatarLink">
          </div>
          <div class="name">{{player.gamerTag}}</div>
          <div class="powers">
            <div v-for="n in player.powerMoves" :key="n" class="power"></div>
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
        <div class="heromug bowser" :class="{disabled: bowserUsed }" data_id="bowser" @click="pickCharacter">
          <div class="heroselect" ></div>
          <div class="nametag" hero-name="bowser">Bowser</div>
        </div>
        <div class="heromug goomba" :class="{disabled: goombaUsed}" data_id="goomba" @click="pickCharacter">
          <div class="heroselect"></div>
          <div class="nametag">Goomba</div>
        </div>
        <div class="heromug yoshi" :class="{disabled: yoshiUsed}" data_id="yoshi" @click="pickCharacter">
          <div class="heroselect"></div>
          <div class="nametag" hero-name="yoshi">Yoshi</div>
        </div>
        <div class="heromug peach" :class="{disabled: peachUsed}" data_id="peach" @click="pickCharacter">
          <div class="heroselect"></div>
          <div class="nametag">Peach</div>
        </div>
        <div class="heromug mario" :class="{disabled: marioUsed}" data_id="mario" @click="pickCharacter">
          <div class="heroselect"></div>
          <div class="nametag">Mario</div>
        </div>
      </div>
    </div>
    <div v-if="state === 'playing'" id="puzzle-area" class="backgroundwhite">
      <div id="puzzle" :style="puzzleStyle">
        <transition-group name="puzzleswap" >
          <div  v-for="(piece, i) in puzzle" @click="select" :index="piece.index" :key="piece.index" class="spot" :class="{selected: piece.selectedBy && piece.selectedBy != clientId, selectedByMe: piece.selectedBy == clientId}" :style="[holderStyle]">
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
    <div v-if="state === 'playing'" class="rank-container backgroundwhite">
      <div class="rank">
        <div class="label">Team Rank</div>
        <div class="value">
          {{teamRank.rank}}/{{teamRank.totalTeam}}
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
          {{rank}}/{{totalPlayers}}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { UsersAckMessage, TeamsMessage, parseReceivedMessage, PlayerRankMessage, TeamRankMessage } from '@/messaging/messages.js';
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
      totalPlayers: 0,
      puzzlePicture: 'static/puzzle1.png',
      holderStyle: {
        width: "0px",
        height: "0px"
      },
      puzzleStyle: '',
      backgroundwhite: false,
      styleAvatar: {
        "background-image": avatarLink,
        "background-size": "contain",
        "background-repeat": "no-repeat",
        "background-position": "center",
        height: "15vh",
        width: "25vw"
      },
      character: null,
      characterType: "",
      powerMoves: 0,
      players: [],
      chooseHeal: false,
      // From server
      puzzle: [],
      puzzleName: "",
      teamInfo: {
        teamId: "",
        teamName: "",
        players: [],
        totalTeam: 0,
        teamRank: 0,
        timeAllowedForEachMove: 0
      },
      teamRank: {
        rank: 0,
        totalTeam: 0
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
          return player.clientName !== this.clientId;
        })
      }
    },
    marioUsed: function() {
      return !(Array.isArray(this.teamInfo.availableCharacters) && this.teamInfo.availableCharacters.indexOf('mario') >= 0);
    },
    bowserUsed: function() {
      return !(Array.isArray(this.teamInfo.availableCharacters) && this.teamInfo.availableCharacters.indexOf('bowser') >= 0);
    },
    peachUsed: function() {
      return !(Array.isArray(this.teamInfo.availableCharacters) && this.teamInfo.availableCharacters.indexOf('peach') >= 0);
    },
    yoshiUsed: function() {
      return !(Array.isArray(this.teamInfo.availableCharacters) && this.teamInfo.availableCharacters.indexOf('yoshi') >= 0);
    },
    goombaUsed: function() {
      return !(Array.isArray(this.teamInfo.availableCharacters) && this.teamInfo.availableCharacters.indexOf('goomba') >= 0);
    }
  },

  // any actions
  methods: {
    getRandomInt: function(max) {
      return Math.floor(Math.random() * Math.floor(max));
    },
    handleMsg: function(msg) {
      // console.log("Got message", msg);
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
        if (msg.puzzleName) {
          teamMsg.puzzleName = msg.puzzleName;
        }
        let teamInfo = {
          timeAllowedForEachMove: 10
        };
        teamMsg.teamInfo = teamInfo;
        if (msg.teamId) {
          teamInfo.teamId = msg.teamId;
        } else {
          teamInfo.teamId = this.teamInfo.teamId;
        }
        if (msg.teamName) {
          teamInfo.teamName = msg.teamName;
        } else {
          teamInfo.teamName = this.teamInfo.teamName;
        }
        if (msg.players) {
          teamInfo.players = msg.players;
        } else {
          teamInfo.players = this.teamInfo.players;
        }
        teamInfo.availableCharacters = msg.availableCharacters || [];
        this.handleTeamsMessage(teamMsg);
      } else if (msg.connected == false) {
        newState = 'connecting';
        this.handleStateChange(newState);
        return;
      }  else if (msg instanceof TeamRankMessage) {
         if (this.teamRank) {
           this.teamRank.rank = msg.rank;
           this.teamRank.totalTeam = msg.totalTeams;
         }
      } else if (msg instanceof PlayerRankMessage) {
          this.rank = msg.rank;
          this.totalPlayers = msg.totalPlayers;
      }
    },
    handleTeamsMessage: function(msg) {
      console.log('teamMsg', msg);
      let newState = null;
      if (msg.puzzleName) {
        this.puzzleName = msg.puzzleName;
      }
      if (msg.puzzle && msg.puzzle.length > 0) {
        // if (this.puzzle.length === 0) {
        //   // must be first team message, transition to select avatar
        //   newState = 'start';
        // }
        let pieces = msg.puzzle;
        // assume is 5 x 5
        let size = Math.sqrt(msg.puzzle.length);
        var square = 412;
        if (window.innerWidth < square) {
          square = window.innerWidth;
        }
        this.puzzleStyle = `width: ${square}px; height: ${square}px;`;

        var splits = 100/size;
        var movePercent = splits / 100;
        var unit = square * movePercent;
        this.holderStyle.width = unit + "px";
        this.holderStyle.height = unit + "px";

        for (var i = 0; i < size * size; ++i) {
          pieces[i].style = `width: ${square}px; margin-left: -${unit *
              (pieces[i].index % size)}px; margin-top: -${unit * Math.floor(pieces[i].index / size)}px;`;
        }
        this.updateArray(this.puzzle, pieces);

        if (newState !== 'start') {
          this.checkWinCondition(msg.gameWon);
        }
      }
      if (msg.teamInfo) {
        if (msg.teamInfo.players) {
          let newPlayers = msg.teamInfo.players;
          let me = null;
          newPlayers.forEach((player) => {
            if (player.character) {
              player.avatarLink = `static/${player.character.type}-mario.jpg`;
              player.powerMoves = player.character.superPower;
            }
            if (player.clientName === this.clientId) {
              me = player;
            }
          });
          if (me) {
            this.character = me.character;
            if (me.character) {
              this.characterType = this.character.type;
              this.avatarLink = `static/${me.character.type}-mario.jpg`;
              this.styleAvatar["background-image"] = this.avatarLink;
              this.powerMoves = me.powerMoves;
            }
          }
        }
        this.updateData(this.teamInfo, msg.teamInfo);
        if (this.puzzleName) {
          this.puzzlePicture = `static/${this.puzzleName}.png`;
        }
      }
      if (!this.character) {
        // must be first team message, transition to select avatar
        newState = 'start';
      } else {
        newState = 'playing';
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
    stopGame: function() {
      if (this.playerMessenger) {
        this.playerMessenger.stopGame();
      }
    },
    pickCharacter: function(event) {
      let character = event.currentTarget.getAttribute("data_id")
      if (this.teamInfo.availableCharacters && this.teamInfo.availableCharacters.indexOf(character) >= 0) {
        console.log("Play with " + character);
        if (this.playerMessenger) {
          this.playerMessenger.pickCharacter(character);
        }
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
      let isAlreadySelectedByMe = this.puzzle.find(p => {
        return p.selectedBy == this.clientId;
      });
      let index = e.target.previousElementSibling.attributes.index.value;

      // Selection validity check:
      //
      // If you have already selected a piece, then the valid choices are either
      // the piece you have already selected (to unselect it), or a non-selected
      // piece.
      //
      // If you have no already selected a piece, then the valid choices are any
      // non-selected piece.
      //
      let selectedPiece = {index: this.puzzle[index].index, selectedBy: this.puzzle[index].selectedBy};
      let isSelectionValid = isAlreadySelectedByMe ?
        (selectedPiece.selectedBy == this.clientId) || !selectedPiece.selectedBy :
        !selectedPiece.selectedBy;

      if (!isSelectionValid) {
        return;
      }

      // Handle the final selection case
      //
      if (isAlreadySelectedByMe) {
        let isNewSelectionAlreadySelectedByMe = (selectedPiece.selectedBy == this.clientId);

        // Handles the unselection case
        //
        if (isNewSelectionAlreadySelectedByMe) {
          selectedPiece.selectedBy = "";
          this.playerMessenger.selectPiece(selectedPiece);
        }
        // Handle the swap case
        //
        else {
          this.swap(isAlreadySelectedByMe, selectedPiece);
        }
      }
      // Handle the initial selection case
      //
      else {
        selectedPiece.selectedBy = this.clientId;
        this.playerMessenger.selectPiece(selectedPiece);
      }
    },
    swap: function(a, b) {
      // console.log(a.index, b.index);
      let piece1 = {index: a.index, selectedBy: a.selectedBy};
      let piece2 = {index: b.index, selectedBy: b.selectedBy};
      let pieces = this.puzzle.map((p) => {
        return {index: p.index, selectedBy: p.selectedBy};
      });
      this.playerMessenger.swap(piece1, piece2, pieces);
    },
    power: function() {
      if (this.powerMoves) {let type = this.character.type;
      if (type === "peach") {

        this.chooseHeal = true;
      } else if (type === "mario" ) {
        let mySelectedPiece = this.puzzle.find(p => {
        return p.selectedBy ==this.clientId;
        });
        if (mySelectedPiece) {
        this.playerMessenger.starPower(mySelectedPiece);
        }
      } else if (type === "bowser") {
        this.playerMessenger.troubleFlipper();
      } else if (type === "yoshi") {
        this.playerMessenger.yoshiGuard();
      } else if (type === "goomba") {
        this.playerMessenger.greenShell();}
      }
    },
    heal: function(player) {
      console.log(player);
      this.chooseHeal = false;
      this.playerMessenger.peachHeal(player.character.type);
    },
    // TODO (BTO): We may want to move the stop countdown call to the swap and
    //             make it player-specific instead of team-wide...
    //
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

.modal .text {
  position: fixed;
  top: 20%;
  font-size: 40px;
  color: white;
  font-family: Bangers;
  width: 100vw;
  text-align: center;
}
.modal .content {
  position: fixed;
  top: 30%;
  padding: 4vw;
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
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
  flex-wrap: nowrap;
}
.header-info .game-info {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.header-info .game-info .info {
  padding: 0px;
}

.header-info .me.user-info:active .headshot {
  color: grey;
  transform: scale(1.2);
}
.profile {
  border: 1px #b9acac9e solid;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
  display: inline-block;
}
.user-info {
  font-size: 4vw;
  position: relative;
  padding: 1vw;
}

.user-info .name {
  text-align: center;
}

.headshot {
  width: 18vw;
  height: 18vw;
  overflow: hidden;
  border-radius: 18vw;
  position: relative;
  box-shadow: 0px 0px 1px 3px rgba(0, 0, 0, 0.1);
  background: white;
}

.headshot img {
  position: absolute;
}

.powers {
  display: flex;
  flex-direction: row;
}

.power {
  width: 5px;
  height: 5px;
  background: green;
  margin: 0 1px;
}

.peach .headshot img {
  width: 50vw;
  left: -19vw;
  top: -1vw;
}

.peach {
  color: pink;
}

.mario .headshot img {
  width: 20vw;
  left: -1vw;
  top: -3vw;
}

.mario {
  color: #006fea;
}

.yoshi .headshot img {
  width: 24vw;
  left: -3vw;
  top: 2vw;
}

.yoshi {
  color: rgb(19, 207, 19);
}

.bowser .headshot img {
  left: -1vw;
  width: 35vw;
  top: -2vw;
}

.bowser {
  color: rgb(252, 206, 0);
}

.goomba .headshot img {
  width: 20vw;
  top: 2vw;
  left: 2vw;
}

.goomba {
  color: rgb(194, 86, 23);
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
.spot.selectedByMe {
  border: solid 1px yellow;
}
/* .spot.swapped {
    transform:  rotateY(360deg);
} */
.spot.selected img {
  box-shadow: 10px 10px 10px 10px red inset;
}
.spot.selectedByMe img {
  box-shadow: 10px 10px 10px 10px yellow inset;
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
.selectedByMe .highlight {
  box-shadow: inset 0 0 20px yellow;
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
  cursor: pointer;
}

.heromug.disabled {
  opacity: 0.5;
}

.heromug.disabled .heroselect {
  cursor: default;
}

.heromug:not(.disabled) > .heroselect:hover {
  background-color: #ff00003b;
}

.heromug:not(.disabled):hover {
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
