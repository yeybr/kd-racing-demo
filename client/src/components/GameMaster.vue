<template>
  <div class="game-master-panel">
    <div class="modal-overlay" v-show="showModal">
    </div>
    <div class="modal" v-show="showModal">
        <div class="content">
          Please select maximum two teams.
        </div>
        <div class="footer">
          <button type="button" class="btn ok-btn" @click="closeModal()">OK</button>
        </div>
    </div>
    <div class="header-section">
      <div class="title">
          <span class="red">G</span><span class="green">a</span><span class="yellow">m</span><span class="blue">e</span>
          <span class="red">M</span><span class="green">a</span><span class="yellow">s</span><span class="blue">t</span><span class="red">e</span><span class="green">r</span>
      </div>
    </div>
    <div v-show="state !== 'watching'" class="score-info waiting">
      <h3>Connecting...</h3>
    </div>
    <div class="game-action">
      <div v-show="state === 'watching' && !started" class="score-info waiting">
        <button type="button" class="game-btn btn" @click="startGame()">Start Game</button>
      </div>
      <div v-show="state === 'watching' && started && !compareMode" class="score-info waiting">
        <button type="button" class="game-btn btn" @click="stopGame()">Stop Game</button>
      </div>
      <div v-show="state === 'watching' && started && !compareMode" class="score-info waiting">
        <button type="button" class="game-btn btn" @click="compareGame()">Show Games</button>
      </div>
      <div v-show="state === 'watching' && started && compareMode" class="score-info waiting">
        <button type="button" class="game-btn btn" @click="exitCompareMode()">Back</button>
      </div>
    </div>
    <div v-show="state === 'watching' && !compareMode" class="waiting-players">
      <div class="title">Waiting Players ({{waitingPlayers.length}})</div>
      <div class="players">
        <div v-for="(player, index) in waitingPlayers" :key="player.id" class="player">
          {{player.gamerTag}}{{(index === waitingPlayers.length - 1) ? '': ','}}
        </div>
      </div>
    </div>
    <div v-show="state === 'watching' && !compareMode" class="teams">
      <div class="title">Teams</div>
      <div class="score-board">
        <div class="team" v-for="teamInfo in teams" :key="teamInfo.id" :id="teamInfo.id" @click="selectTeam">
          <div class="title">{{teamInfo.name}}</div>
          <div class="body">
            <template v-for="(player, index) in teamInfo.players">
              {{player.gamerTag}}{{(index === teamInfo.players.length - 1) ? '': ', '}}
            </template>
          </div>
        </div>
      </div>
    </div>
    <div v-if="state === 'watching' && compareMode" class="games-view">
    </div>
  </div>
</template>

<script>
import { UsersAckMessage, TournamentMessage, parseReceivedMessage } from '@/messaging/messages.js';
import { GameMaster } from '@/messaging/game-master';
import CommonUtils from './common-utils';
export default {
  name: 'gamemaster',
  mixins: [CommonUtils],
  beforeCreate() {
    document.body.className = 'mario';
  },
  created() {
    console.log('gamemaster created: data bound');

    // if (this.$route.query.username) {
      // this.username = this.$route.query.username;
      this.username = 'admin';
      // Retrive userInfo from local storage
      let clientId = null;
      // let userInfo = this.retrieveFromStorage('localStorage', 'trouble_flipper_game_master');
      // if (userInfo) {
      //   clientId = userInfo.clientId;
      //   if (userInfo.username !== this.username) {
      //     userInfo.username = this.username;
      //     this.saveIntoStorage('localStorage', 'trouble_flipper_game_master', userInfo);
      //   }
      // }
      this.clientId = clientId;
      this.masterMessenger = new GameMaster(this.$solace, this.$parent.appProps,
        {username: this.username, clientId: this.clientId},
        this.handleMsg.bind(this));
      this.masterMessenger.connect();
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
      started: false,
      compareMode: false,
      showModal: false,
      waitingPlayers: [],
      teams: [],
      selectedTeams: []
    }
  },

  // any actions
  methods: {
    handleMsg: function(msg) {
      console.log('Got message', msg);
      let newState = null;
      if (msg instanceof UsersAckMessage) {
        // user connect reply
        this.clientId = msg.clientId;
        this.username = msg.username;
        newState = 'waiting';
        this.handleStateChange(newState);
        return;
      } else if (msg instanceof TournamentMessage) {
        this.started = msg.started;
        this.updateArray(this.waitingPlayers, msg.waitingPlayers);
        this.updateArray(this.teams, msg.teams);
        newState = 'watching';
        this.handleStateChange(newState);
      } else if (msg.connected == false) {
        newState = 'connecting';
        this.handleStateChange(newState);
        return;
      }

    },
    handleStateChange: function(newState) {
      if (!newState) {
        return;
      }
      console.log("New state change", newState);
      let currentState = this.state;
      this.state = newState;

      // if (this.state === 'watching' && currentState !== 'watching') {
      //   console.log('Save username ' + this.username + ', clientId ' + this.clientId + ' to localStorage');
      //   this.saveIntoStorage('localStorage', 'trouble_flipper_game_master', { username: this.username, clientId: this.clientId });
      // } else
      if (this.state === "connecting") {
        this.cleanup();
      }
    },
    cleanup: function() {
      this.started = false;
      this.waitingPlayers.splice(0, this.waitingPlayers.length);
      this.teams.splice(0, this.teams.length);
    },
    startGame: function() {
      if (this.masterMessenger) {
        this.masterMessenger.startGame();
      }
    },
    stopGame: function() {
      if (this.masterMessenger) {
        this.masterMessenger.stopGame();
      }
    },
    selectTeam: function(event) {
      let teamId = event.currentTarget.getAttribute('id')
      let index = this.selectedTeams.indexOf(teamId);
      if (event.currentTarget.classList.contains('selected')) {
        event.currentTarget.classList.remove('selected');
        if (index >= 0) {
          this.selectedTeams.splice(index, 1);
        }
      } else {
        event.currentTarget.classList.add('selected');
        if (index < 0) {
          this.selectedTeams.push(teamId);
        }
      }
    },
    closeModal: function() {
      this.showModal = false;
    },
    compareGame: function() {
      console.log('selectedTeams', this.selectedTeams);
      if (this.selectedTeams.length === 0 || this.selectedTeams.length > 2) {
        this.showModal = true;
        return false;
      }
      if (this.masterMessenger) {
        this.selectedTeams.forEach(teamId => {
          this.masterMessenger.subscribeToTopic('team/' + teamId);
        });
      }
      this.compareMode = true;
    },
    exitCompareMode: function() {
      if (this.masterMessenger) {
        this.selectedTeams.forEach(teamId => {
          if (this.masterMessenger) {
            this.masterMessenger.unsubscribeToTopic('team/' + teamId);
            let teamElement = document.getElementById(teamId);
            if (teamElement) {
              teamElement.classList.remove('selected');
            }
          }
        });
      }
      this.selectedTeams.splice(0, this.selectedTeams.length);
      this.compareMode = false;
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

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 100%;
  background-color: transparent;
  opacity: 1;
  z-index: 1030;
}

.modal {
  background-color: white;
  z-index: 1050;
  border: 0;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
  top: 40%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 500px;
  height: 200px;
  display: flex;
  flex-direction: column;
}

.modal .content {
  padding: 40px 20px 20px 20px;
}
.modal .footer {
  padding: 20px 20px 40px 20px;
  font-size: 30px;
  display: flex;
  flex-direction: row;
  justify-content: flex-end;
}

.modal .footer .ok-btn {
  font-size: 20px;
  /* margin-bottom: 40px; */
  background: #006fea;
  border-radius: 4px;
  padding: 10px;
  /* margin: 40px 0; */
  color: white;
  width: 100px;
  cursor: pointer;
  min-height: 32px;
}

.game-master-panel .header-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.game-master-panel .header-section .title {
  font-size: 6vw;
}
.game-master-panel .header-section .user-info {
  font-weight: 500;
}

.game-master-panel .game-action {
  display: flex;
  flex-direction: row;
  justify-content: center;
  flex-wrap: wrap;
}

.game-master-panel .score-info.waiting {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-left: 10px;
  padding-right: 10px;
}
.game-master-panel .score-info.waiting label {
  margin-top: 40px;
  font-size: 8vw;
}
.game-master-panel .score-info.waiting .game-btn {
  font-size: 32px;
  margin-bottom: 40px;
  background: #006fea;
  border-radius: 4px;
  padding: 10px;
  margin: 40px 0;
  color: white;
  width: 200px;
  cursor: pointer;
  min-height: 68px;
}

.game-master-panel .waiting-players {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  margin: 15px;
}

.game-master-panel .waiting-players .title {
  font-size: 20px;
}

.game-master-panel .waiting-players .players {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
}

.game-master-panel .waiting-players .players .player {
  padding-right: 5px;
}

.game-master-panel .teams {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  margin: 15px;
}

.game-master-panel .teams .title {
  font-size: 20px;
}

.game-master-panel .teams .score-board {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  flex-wrap: wrap;
}

.game-master-panel .teams .score-board .team {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  margin: 15px;
  border-radius: 20px;
  border: solid 2px transparent;
  background: #924692;;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
  width: 400px;
  min-height: 90px;
}

.game-master-panel .teams .score-board .team.selected {
  border: solid 2px green;
}

.game-master-panel .teams .score-board .team .title {
  font-size: 20px;
  padding: 8px 16px;
  /* border-bottom: solid 2px #dedede; */
  border-bottom: groove 1px white;
  text-align: center;
  color: white;
}

.game-master-panel .teams .score-board .team .body {
  padding: 8px 16px 12px 16px;
  color: white;
}

.game-master-panel .games-view {
  display: flex;
  flex-direction: row;

}

.red {
  color: #d41345;
  text-shadow: -0.5vw -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000,
    1px 1px 0 #000;
}
.blue {
  color: rgb(5, 139, 255);
  text-shadow: -0.5vw -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000,
    1px 1px 0 #000;
}

.yellow {
  color: rgb(255, 252, 0);
  text-shadow: -0.5vw -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000,
    1px 1px 0 #000;
}
.green {
  color: rgb(37, 173, 33);
  text-shadow: -0.5vw -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000,
    1px 1px 0 #000;
}
</style>
