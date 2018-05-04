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
        <button type="button" class="back-btn btn" @click="exitCompareMode()">Back</button>
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
      <div class="game-view">
        <div class="header-section" v-if="team1">
          <!-- <div style="position:relative;background-image: url(static/backgrounds.png);"> -->
            <div class="titlebar">{{team1.teamInfo.teamName}}</div>
          <!-- </div> -->
          <div class="header-info">
            <div v-for="(player) in team1.teamInfo.players" v-if="player.character" :key="player.clientId"
              class="user-info" :class="player.character.type">
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
        <div v-if="team1 && team1.puzzlePicture" id="puzzle-area_team1" class="backgroundwhite">
          <div id="puzzle_team1" :style="team1.puzzleStyle">
            <transition-group name="puzzleswap" >
              <div v-for="(piece, i) in team1.puzzle" :index="piece.index" :key="piece.index" class="spot"
                :class="{selected: piece.selectedBy && !playerCharacterMap.get(piece.selectedBy),
                  selectedByMario: piece.selectedBy && playerCharacterMap.get(piece.selectedBy) === 'mario',
                  selectedByBowser: piece.selectedBy && playerCharacterMap.get(piece.selectedBy) === 'bowser',
                  selectedByPeach: piece.selectedBy && playerCharacterMap.get(piece.selectedBy) === 'peach',
                  selectedByYoshi: piece.selectedBy && playerCharacterMap.get(piece.selectedBy) === 'yoshi',
                  selectedByGoomba: piece.selectedBy && playerCharacterMap.get(piece.selectedBy) === 'goomba'}"
                :style="[team1.holderStyle]">
                <img :src="team1.puzzlePicture" :index="i" v-bind:style="piece.style"/>
                <div class="highlight"></div>
              </div>
            </transition-group>
            <div v-if="team1.win" id="win_team1">
              Winner!
              <div class="countdown">Next game starting in {{team1.countdown}}</div>
            </div>
          </div>
        </div>
        <div v-if="team1 && team1.puzzlePicture" class="rank-container backgroundwhite">
          <div class="rank">
            <div class="label">Team Rank</div>
            <div class="value">
              {{team1.teamRank.rank}}/{{team1.teamRank.totalTeam}}
            </div>
          </div>
        </div>
      </div>
      <div class="game-view">
        <div class="header-section" v-if="team2">
          <!-- <div style="position:relative;background-image: url(static/backgrounds.png);"> -->
            <div class="titlebar">{{team2.teamInfo.teamName}}</div>
          <!-- </div> -->
          <div class="header-info">
            <div v-for="(player) in team2.teamInfo.players" v-if="player.character" :key="player.clientId"
              class="user-info" :class="player.character.type">
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
        <div v-if="team2 && team2.puzzlePicture" id="puzzle-area_team2" class="backgroundwhite">
          <div id="puzzle_team2" :style="team2.puzzleStyle">
            <transition-group name="puzzleswap" >
              <div v-for="(piece, i) in team2.puzzle" :index="piece.index" :key="piece.index" class="spot"
                :class="{selected: piece.selectedBy && !playerCharacterMap.get(piece.selectedBy),
                  selectedByMario: piece.selectedBy && playerCharacterMap.get(piece.selectedBy) === 'mario',
                  selectedByBowser: piece.selectedBy && playerCharacterMap.get(piece.selectedBy) === 'bowser',
                  selectedByPeach: piece.selectedBy && playerCharacterMap.get(piece.selectedBy) === 'peach',
                  selectedByYoshi: piece.selectedBy && playerCharacterMap.get(piece.selectedBy) === 'yoshi',
                  selectedByGoomba: piece.selectedBy && playerCharacterMap.get(piece.selectedBy) === 'goomba'}"
                :style="[team2.holderStyle]">
                <img :src="team2.puzzlePicture" :index="i" v-bind:style="piece.style"/>
                <div class="highlight"></div>
              </div>
            </transition-group>
            <div v-if="team2.win" id="win_team2">
              Winner!
              <div class="countdown">Next game starting in {{team2.countdown}}</div>
            </div>
          </div>
        </div>
        <div v-if="team2 && team2.puzzlePicture" class="rank-container backgroundwhite">
          <div class="rank">
            <div class="label">Team Rank</div>
            <div class="value">
              {{team2.teamRank.rank}}/{{team2.teamRank.totalTeam}}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { UsersAckMessage, TournamentMessage, TeamsMessage, parseReceivedMessage, TeamRankMessage } from '@/messaging/messages.js';
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
      this.clientId = null;
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
      playerCharacterMap: new Map(),
      waitingPlayers: [],
      teams: [],
      selectedTeams: [],
      transitionName: "puzzleswap",
      team1: {
        win: false,
        puzzlePicture: '',
        holderStyle: {
          width: "0px",
          height: "0px"
        },
        puzzleStyle: '',
        backgroundwhite: false,
        countdown: 3,
        // transitionName: "puzzleswap",
        // From server
        puzzle: [],
        puzzleName: "",
        teamInfo: {
          teamId: "",
          teamName: "",
          players: [],
          totalTeam: 0,
          teamRank: 0,
          correctPieces: 0,
          completedGames: 0
        },
        teamRank: {
          rank: 0,
          totalTeam: 0
        }
      },
      team2: {
        win: false,
        puzzlePicture: '',
        holderStyle: {
          width: "0px",
          height: "0px"
        },
        puzzleStyle: '',
        backgroundwhite: false,
        countdown: 3,
        // transitionName: "puzzleswap",
        // From server
        gameWon: false,
        gameOver: false,
        puzzle: [],
        puzzleName: "",
        teamInfo: {
          teamId: "",
          teamName: "",
          players: [],
          correctPieces: 0,
          completedGames: 0
        },
        teamRank: {
          rank: 0,
          totalTeam: 0
        }
      }
    }
  },

  // any actions
  methods: {
    handleMsg: function(msg) {
      // console.log('Got message', msg);
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
      } else if (msg instanceof TeamsMessage) {
        if (!msg.teamId) {
          console.log('Team message does not have teamId');
          return;
        }
        let teamGame = this.findTeamGame(msg);
        if (!teamGame) {
          console.log('no team to update');
          return;
        }
        let teamMsg = {};
        teamMsg.gameWon = msg.gameWon;
        teamMsg.gameOver = msg.gameOver;
        teamMsg.puzzle = msg.puzzle;
        teamMsg.puzzleName = msg.puzzleName;
        let teamInfo = {};
        teamMsg.teamInfo = teamInfo;
        teamInfo.teamId = msg.teamId;
        teamInfo.teamName = msg.teamName;
        if (msg.players) {
          teamInfo.players = msg.players;
        } else {
          teamInfo.players = teamGame.players;
        }
        teamInfo.correctPieces = msg.correctPieces;
        teamInfo.completedGames = msg.completedGames;
        this.handleTeamsMessage(teamMsg, teamGame);
      } else if (msg instanceof TeamRankMessage) {
        if (!msg.teamId) {
          console.log('Team message does not have teamId');
          return;
        }
        let teamGame = this.findTeamGame(msg);
        if (!teamGame) {
          console.log('no team to update');
          return;
        }
        if (teamGame.teamRank) {
          teamGame.teamRank.rank = msg.rank;
          teamGame.teamRank.totalTeam = msg.totalTeams;
        }
      } else if (msg.connected == false) {
        newState = 'connecting';
        this.handleStateChange(newState);
        return;
      }

    },

    findTeamGame(msg) {
      let teamGame = null;
      if (this.team1.teamInfo && this.team1.teamInfo.teamId === msg.teamId) {
        teamGame = this.team1;
      } else if (this.team2.teamInfo && this.team2.teamInfo.teamId === msg.teamId) {
        teamGame = this.team2;
      } else if (!this.team1.teamInfo || !this.team1.teamInfo.teamId) {
        teamGame = this.team1;
      } else if (!this.team2.teamInfo || !this.team2.teamInfo.teamId) {
        teamGame = this.team2;
      }
      return teamGame;
    },

    handleTeamsMessage(msg, teamGame) {
      console.log('Team message', msg);
      teamGame.puzzleName = msg.puzzleName;
      if (teamGame.puzzleName) {
        teamGame.puzzlePicture = `static/${teamGame.puzzleName}`;
      } else {
        teamGame.puzzlePicture = '';
      }
      console.log('puzzlePicture', teamGame.puzzlePicture);
      if (msg.puzzle && msg.puzzle.length > 0) {
        let puzzleArea = document.getElementById("puzzle-area_team1");
        // console.log('puzzleArea', puzzleArea);
        if (puzzleArea) {
          this.updatePuzzleArea(msg, teamGame, puzzleArea);
        } else {
          setTimeout(() => {
            puzzleArea = document.getElementById("puzzle-area_team1");
            // console.log('puzzleArea', puzzleArea);
            this.updatePuzzleArea(msg, teamGame, puzzleArea);
          }, 50)
        }
      }

      if (msg.teamInfo) {
        if (msg.teamInfo.players) {
          let newPlayers = msg.teamInfo.players;
          newPlayers.forEach((player) => {
            if (player.character) {
              player.avatarLink = `static/${player.character.type}-mario.jpg`;
              player.powerMoves = player.character.superPower;
            }
            this.playerCharacterMap.set(player.clientName, player.character ? player.character.type : '');
          });
          console.log('playerCharacterMap', this.playerCharacterMap);
        }
        this.updateData(teamGame.teamInfo, msg.teamInfo);
      }
    },

    updatePuzzleArea: function(msg, teamGame, puzzleArea) {
      let pieces = msg.puzzle;
      // assume is 5 x 5
      let size = Math.sqrt(msg.puzzle.length);
      if (puzzleArea) {
        var square = puzzleArea.offsetHeight;
        if (puzzleArea.offsetHeight > puzzleArea.offsetWidth) {
          square = puzzleArea.offsetWidth;
        }
        teamGame.puzzleStyle = `width: ${square}px; height: ${square}px;`;

        var splits = 100/size;
        var movePercent = splits / 100;
        var unit = square * movePercent;
        teamGame.holderStyle.width = unit + "px";
        teamGame.holderStyle.height = unit + "px";

        for (var i = 0; i < size * size; ++i) {
          pieces[i].style = `width: ${square}px; margin-left: -${unit *
              (pieces[i].index % size)}px; margin-top: -${unit * Math.floor(pieces[i].index / size)}px;`;
        }
      }
      this.updateArray(teamGame.puzzle, pieces);
      this.checkWinCondition(msg.gameWon, teamGame);
    },

    checkWinCondition: function(gameWon, teamGame) {
      teamGame.win = gameWon;
      if (teamGame.win) {
        console.log("WINNER!");
        if (teamGame.timer) {
          clearInterval(teamGame.timer);
        }
        teamGame.timer = setInterval(() => {
          teamGame.countdown--;
          if (teamGame.countdown === 0) {
            teamGame.countdown = 3;
            clearInterval(teamGame.timer);
            teamGame.timer = null;
          }
        }, 1000);
      }
    },

    resetGameTeams: function(gameTeam) {
      if (gameTeam.timer) {
        clearInterval(gameTeam.timer);
        gameTeam.timer = null;
      }
      let newValue = {
        win: false,
        puzzlePicture: '',
        holderStyle: {
          width: "0px",
          height: "0px"
        },
        puzzleStyle: '',
        backgroundwhite: false,
        countdown: 3,
        transitionName: "puzzleswap",
        // From server
        gameWon: false,
        gameOver: false,
        puzzle: [],
        puzzleName: "",
        teamInfo: {
          teamId: "",
          teamName: "",
          players: [],
          correctPieces: 0,
          completedGames: 0
        },
        teamRank: {
          rank: 0,
          totalTeam: 0
        }
      };
      this.updateData(gameTeam, newValue)
    },

    handleStateChange: function(newState) {
      if (!newState) {
        return;
      }
      console.log("New state change", newState);
      let currentState = this.state;
      this.state = newState;
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
      this.playerCharacterMap.clear();
      this.resetGameTeams(this.team1);
      this.resetGameTeams(this.team2);

      if (this.masterMessenger) {
        this.selectedTeams.forEach(teamId => {
          this.masterMessenger.subscribeToTopic('team/' + teamId);
          this.masterMessenger.subscribeToTopic('score/' + teamId);
          this.masterMessenger.queryGame(teamId);
        });
      }
      this.compareMode = true;
    },
    exitCompareMode: function() {
      this.resetGameTeams(this.team1);
      this.resetGameTeams(this.team2);
      if (this.masterMessenger) {
        this.selectedTeams.forEach(teamId => {
          if (this.masterMessenger) {
            this.masterMessenger.unsubscribeToTopic('team/' + teamId);
            this.masterMessenger.unsubscribeToTopic('score/' + teamId);
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

/* .backgroundwhite {
  background: white;
} */

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

.game-master-panel .score-info.waiting .back-btn {
  font-size: 24px;
  background: #006fea;
  border-radius: 4px;
  padding: 10px;
  margin: 20px 0 10px 0;
  color: white;
  width: 100px;
  cursor: pointer;
  min-height: 48px;
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
  justify-content: space-between;
}

.game-master-panel .games-view .game-view {
  flex: 1 1 0;
  position: relative;
}

/* header section */
.game-view .header-section {
  /* padding: 15px; */
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.game-view .titlebar {
  text-align: center;
  background: transparent;
  padding: 5px;
  font-size: 2vw;
}

.game-view .header-info {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
}
.game-view .header-info .game-info {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.game-view .header-info .game-info .info {
  padding: 0px;
}

/* .game-view .header-info .me.user-info:active .headshot {
  color: grey;
  transform: scale(1.2);
} */

/* .game-view .profile {
  border: 1px #b9acac9e solid;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
  display: inline-block;
} */
.game-view .user-info {
  font-size: 1vw;
  position: relative;
  padding: 1vw;
}

.game-view .user-info .name {
  text-align: center;
}

.game-view .headshot {
  width: 3vw;
  height: 3vw;
  overflow: hidden;
  border-radius: 18vw;
  position: relative;
  box-shadow: 0px 0px 1px 3px rgba(0, 0, 0, 0.1);
  background: white;
}

.game-view .headshot img {
  position: absolute;
}

.game-view .powers {
  display: flex;
  flex-direction: row;
}

.game-view .power {
  width: 5px;
  height: 5px;
  background: green;
  margin: 0 1px;
}

.game-view .peach .headshot img {
  width: 2vw;
  left: 0.6vw;
  /* top: -1vw; */
}

.game-view .peach {
  color: pink;
}

.game-view .mario .headshot img {
  width: 2vw;
  left: 0.7vw;
  /* top: -3vw; */
}

.game-view .mario {
  color: #006fea;
}

.game-view .yoshi .headshot img {
  width: 2vw;
  left: 0.5vw;
  top: 0.1vw;
}

.game-view .yoshi {
  color: rgb(19, 207, 19);
}

.game-view .bowser .headshot img {
  width: 2vw;
  left: 0.6vw;
  /* top: -2vw; */
}

.game-view .bowser {
  color: rgb(252, 206, 0);
}

.game-view .goomba .headshot img {
  width: 2vw;
  top: 0.6vw;
  left: 0.6vw;
}

.game-view .goomba {
  color: rgb(194, 86, 23);
}


/* puzzle section */
#puzzle-area_team1 {
  flex: 1 1 0;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

#puzzle_team1 {
  min-height: 300px;
  position: relative;
}

#puzzle-area_team2 {
  flex: 1 1 0;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

#puzzle_team2 {
  min-height: 300px;
  position: relative;
}

/* .game-view .hidden-image {
  position: fixed;
  top: 200%;
} */
.game-view .puzzle div {
  display: inline;
}
.game-view .spot {
  display: inline-block;
  overflow: hidden;
  vertical-align: top;
  border: solid 1px grey;
  box-sizing: border-box;
  position: relative;
  transition: 0.6s;
  transform-style: preserve-3d;
}
.game-view .spot.selected {
  border: solid 1px red;
  background: #6d6d67;
  opacity: .7;
  background: red;
  border: 3px solid blue;
 /* transform:  rotateY(360deg); */
 /* transform: translateY(30px); */

}

.game-view .spot.selectedByMario {
  border: 3px solid #006fea;
}

.game-view .spot.selectedByMario img {
  box-shadow: 10px 10px 10px 10px #006fea inset;
}

.game-view .spot.selectedByMario .highlight {
  box-shadow: inset 0 0 20px #006fea;
}

.game-view .spot.selectedByBowser {
  border: 3px solid rgb(252, 206, 0);
}

.game-view .spot.selectedByBowser img {
  box-shadow: 10px 10px 10px 10px rgb(252, 206, 0);
}

.game-view .spot.selectedByBowser .highlight {
  box-shadow: inset 0 0 20px rgb(252, 206, 0);
}

.game-view .spot.selectedByPeach {
  border: 3px solid pink;
}

.game-view .spot.selectedByPeach img {
  box-shadow: 10px 10px 10px 10px pink;
}

.game-view .spot.selectedByPeach .highlight {
  box-shadow: inset 0 0 20px pink;
}

.game-view .spot.selectedByYoshi {
  border: 3px solid rgb(19, 207, 19);
}

.game-view .spot.selectedByYoshi img {
  box-shadow: 10px 10px 10px 10px rgb(19, 207, 19);
}

.game-view .spot.selectedByYoshi .highlight {
  box-shadow: inset 0 0 20px rgb(19, 207, 19);
}

.game-view .spot.selectedByGoomba {
  border: 3px solid rgb(194, 86, 23);
}

.game-view .spot.selectedByGoomba img {
  box-shadow: 10px 10px 10px 10px rgb(194, 86, 23);
}

.game-view .spot.selectedByGoomba .highlight {
  box-shadow: inset 0 0 20px rgb(194, 86, 23);
}

.game-view .spot.swapped {
    transform:  rotateY(360deg);
}
.game-view .spot.selected img {
  box-shadow: 10px 10px 10px 10px red inset;
}

#win_team1 {
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

#win_team1 .countdown {
  font-size: 30px;
  color: #ffee01;
}

#win_team2 {
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

#win_team2 .countdown {
  font-size: 30px;
  color: #ffee01;
}

.game-view .highlight {
  position: absolute;
  top: 0;
  bottom: 0;
  right: 0;
  left: 0;
}
.game-view .selected .highlight {
  box-shadow: inset 0 0 20px red;
}
.game-view .selectedByMe .highlight {
  box-shadow: inset 0 0 20px yellow;
}

.game-view .rank-container {
  position: relative;
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  align-items: stretch;
  min-height: 32px;
  /* border-top: 1px solid grey; */
}
.game-view .rank-container > div {
  padding-top: 3px;
  border-right: 1px solid grey;
}

.game-view .rank-container div:last-child {
  border-right: none;
}
.game-view .rank-container .rank {
  flex: 1;
  text-align: center;
}
.game-view .rank-container .statusbar {
  flex: 1 1 auto;
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  position: relative;
}
.game-view .rank-container .label {
  line-height: 12px;
  font-size: 12px;
}
.game-view .rank-container .value {
  font-size: 24px;
  line-height: 24px;
}

.puzzleswap-enter-active, .puzzleswap-leave-active {
  transition: opacity .5s;
}
.puzzleswap-enter, .puzzleswapleave-to /* .fade-leave-active below version 2.1.8 */ {
  opacity: 0;
}
.puzzleswap-move {
  transition: transform 1s;
  border: 2px  solid red;
  /* opacity: .6;
  border: 2px  solid red; */

}

.puzzleswapblue-move {
  transition: transform 1s;
  border: 2px  solid blue;
  /* opacity: .6;
  border: 2px  solid red; */

}
</style>
