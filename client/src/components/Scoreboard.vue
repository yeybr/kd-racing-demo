<template>

  <div class="score1-panel">
      <div class="titlebar"><div class="score-title red">Leader Board</div></div>
    <div class="title">
      <!-- <h1>{{msg}}</h1> -->
      <!-- <div class="user-info">User: {{username}}</div> -->
    </div>
    <div class="score-panel">
      <div v-show="state !== 'watching'" class="score-info waiting">
        <h3>Connecting...</h3>
      </div>
      <div v-show="state === 'watching'" class="score-board">
        <div class="scores-panel" style="display:flex" >
            <div id="player-score-table" class="score-table">

              <div class="info">
              <div> Players scoreboard : total players {{scoreboardInfo.players.length}}</div>
              <div class="score-table-body">
                <transition-group name="flip-list" >
                  <div class="score-table-row" v-for="(player, index) in scoreboardInfo.players"  v-bind:key="player.gamerTag">
                    <div>
                     <span >{{index}}</span>
                    </div>
                    <div class="headshot" >
                        <img :src="player.avatarLink">
                      </div>

                    <div style="font-size:3vw" class="id">
                       {{player.gamerTag}}
                    </div>

                  <div class="moves" style="position:relative"><img style="width:5vw" src="../assets/starcoin.png"><div class="labels" style="color:green"> {{player.rightMoves}} </div>
                     </div>

                  <div class="moves" style="position:relative"><img style="width:5vw" src="../assets/ghost-mario.png"><div class="labels" style="color:red"> {{player.wrongMoves}} </div>
                     </div>

                  </div>
                </transition-group>
              </div>
          </div>


          </div> <!--end of player score board-->
        <div  class="score-table" id="team-score-table">
           <div> Team scoreboard : total teams {{scoreboardInfo.teams.length}}</div>
           <div class="score-table-body">
                <transition-group name="player-rank-list">
                  <div class="score-table-row " v-for="(team, index) in scoreboardInfo.teams"  v-bind:key="index">
                    <div style="font-size:3vw;display:flex;align-items: center;" >
                        <div style="flex-grow:2">{{index}} {{team.name}}</div>
                        <div style="padding-left: 2em; font-size:.5em" > {{team.game}} </div>
                    </div>
                </div>
                </transition-group>
              </div>
          </div> <!-- end of team score table-->
      </div>

    </div>

      <!-- <div class="game" v-for="gameInfo in scoreboardInfo.games" :key="gameInfo.id">
          <span class="info">Puzzle: {{gameInfo.gameName}}</span>
          <span class="info">Team: {{gameInfo.teamName}}</span>
          <div class="info">Players:
            <template v-for="(player, index) in gameInfo.players">
              {{player.name}}{{(index === gameInfo.players.length - 1) ? '': ', '}}
            </template>
          </div>
      </div> -->
    </div>
  </div>

</template>

<script>
import { UsersAckMessage, TeamsMessage, parseReceivedMessage, PlayerRankMessage, PlayerListMessage } from '@/messaging/messages.js';
import { Spectator } from '@/messaging/spectator';
import CommonUtils from './common-utils';
export default {
  name: 'scoreboard',
  mixins: [CommonUtils],
  created() {
    console.log('scoreboard created: data bound');

    // if (this.$route.query.username) {
      // this.username = this.$route.query.username;
      this.username = 'spectator';
      // Retrive userInfo from local storage
      let userInfo = this.retrieveFromStorage('localStorage', 'trouble_flipper_spectator');

      let clientId = null;
      if (userInfo) {
        clientId = userInfo.clientId;
        if (userInfo.username !== this.username) {
          userInfo.username = this.username;
          this.saveIntoStorage('localStorage', 'trouble_flipper_spectator', userInfo);
        }
      }
      this.clientId = clientId;
      this.spectatorMessenger = new Spectator(this.$solace, this.$parent.appProps,
        {username: this.username, clientId: this.clientId},
        this.handleMsg.bind(this));
      this.spectatorMessenger.connect();
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
    if (this.spectatorMessenger) {
      this.spectatorMessenger.unregister();
      this.spectatorMessenger.disconnect();
      this.spectatorMessenger = null;
    }
  },

  // Underlying model
  data() {
    return {
      msg: 'Trouble Flipper Scoreboard',
      state: 'connecting',
      scoreboardInfo: {
        teams: [
        ],
        players: [

        ]
      }
    }
  },

  // any actions
  methods: {
    handleMsg: function(msg) {
      console.log('Got message', msg);
      // this.scoreboardInfo.players = msg.players;

      if (msg.players) {
        this.updateData(this.scoreboardInfo.players, msg.players);
        this.scoreboardInfo.players.forEach(function(player) {
          console.log(player);
          player.avatarLink = `static/${player.character.type}-mario.jpg`;
        });
      }
      if (msg.teams) {
        this.updateData(this.scoreboardInfo.teams, msg.teams);
      }
      this.handleStateChange(msg);

    },
    handleStateChange: function(msg) {
      let currentState = this.state;
      if (msg.state) {
        this.state = msg.state;
      } else {
        this.state = 'watching';
      }

      if (currentState !== 'watching' && this.state === 'watching') {
        console.log('Save username ' + this.username + ', clientId ' + this.clientId + ' to localStorage');
        this.saveIntoStorage('localStorage', 'trouble_flipper_spectator', { username: this.username, clientId: this.clientId });
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

.score-table-body {
     height: 80vh;
    overflow-y: scroll;
}
#player-score-table, #team-score-table {
background: rgba(226, 30, 30, 0.14);
color: white;
}

.headshot {
 width: 5vw;
  height: 5vw;
  overflow: hidden;
  border-radius: 3vw;
  position: relative;
  box-shadow: 0px 0px 1px 3px rgba(0, 0, 0, 0.1);
      margin: 0 2vw 0 0;
}


.headshot img {
  position: absolute;
  width: 5vw;

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
  width: 5vw;
  left: -1vw;
  top: -1vw;
}

.mario {
  color: #006fea;
}

.yoshi .headshot img {
  left: -1vw;
  width: 5vw;
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


.score-title {
  font-size: 8vw;
      text-align: center;
    background: ##fe113;
    margin: 1vw 20vw;
    border-radius: 10vw;

}

.score-table-row {
  display: flex;
      border-radius: 10vw;
    background: #924692;
    padding: 1vw;
    margin: 1vw;
}
.score-table-row > .id {
  flex-basis: 40%;
}
.score-table-row > .moves {
  flex-basis: 20%;

}
.moves img {
  opacity: .6;
}
.score-table-row  .labels {
  font-size: 30px;
  position:absolute;
  top: calc(2vw - 15px);
  left: 2vw;
}
.red {
  color: #d41345;
  text-shadow: -0.5vw -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000,
    1px 1px 0 #000;
}
.flip-list-move {
  transition: transform 1s;
}

.scores-panel {
      display: flex;
    align-items: start;
    justify-content: center;
    width: 100%;
}

.score-table {
    flex-basis: 50%;
}

</style>

<style lang="css">
body {
  background-image: url("../assets/scorebackground.png");
  overflow-y: auto;
  background-size: cover;
}

</style>
