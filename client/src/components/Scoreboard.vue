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
            <div class="score-table-body" id="scoretablebodycontent">
              <transition-group name="flip-list" >
                <div class="score-table-row" v-for="(player, index) in scoreboardInfo.players"  v-bind:key="player.gamerTag">
                  <div class="score-rank">
                    <span >{{index + 1}}</span>
                  </div>
                  <div class="userinfo" :class="player.character.type">
                    <div class="headshot">
                      <img :src="player.avatarLink">
                    </div>
                  </div>
                  <div class="id-user" :title="player.teamName">{{player.teamName}}
                    {{player.gamerTag}}
                  </div>
                  <div class="moves" style="position:relative;display:flex">
                    <img style="width:40px" src="../assets/starcoin.png">
                    <div class="labels" style="color:green"> X {{player.rightMoves}} </div>
                  </div>
                  <div class="moves" style="position:relative"><img style="width:40px" src="../assets/bowserhead-mario.png"><div class="labels" style="color:#d2c3c3"> X {{player.wrongMoves}} </div>
                  </div>
                </div>
              </transition-group>
            </div>
          </div>
        </div> <!--end of player score board-->
        <div class="score-table" id="team-score-table">
        <div> Team scoreboard : total teams {{scoreboardInfo.teams.length}}</div>
           <div class="score-table-body">
              <transition-group name="player-rank-list">
                <div style="display:flex; font-size: 32px;" class="score-table-row " v-for="(team, index) in scoreboardInfo.teams"  v-bind:key="index">
                  <div class="index">{{index + 1}}</div>
                  <div class="id-team">{{team.name}}</div>
                  <div class="completed">{{team.completed}}</div>
                </div>
              </transition-group>
            </div>
          </div> <!-- end of team score table-->
         </div>
    
    
   
      </div>

    <!-- <audio id="audio" autoplay controls src="https://archive.org/download/SuperMarioBros.ThemeMusic/SuperMarioBros.mp3">
      <p>If you are reading this, it is because your browser does not support the audio element.</p>
    </audio> -->
      <div style="font-size:;height: 100px">
    </div>
    </div>
  </div>
</template>

<script>
import Vue from "vue";

import {
  UsersAckMessage,
  TeamsMessage,
  parseReceivedMessage,
  PlayerRankMessage,
  PlayerListMessage
} from "@/messaging/messages.js";
import { Spectator } from "@/messaging/spectator";
import CommonUtils from "./common-utils";

export default {
  name: "scoreboard",
  mixins: [CommonUtils],
  beforeCreate() {
    document.body.className = "scoreboard";
  },
  created() {
    console.log("scoreboard created: data bound");

    // if (this.$route.query.username) {
    // this.username = this.$route.query.username;
    this.username = "spectator";
    this.clientId = null;
    this.spectatorMessenger = new Spectator(
      this.$solace,
      this.$parent.appProps,
      { username: this.username, clientId: this.clientId },
      this.handleMsg.bind(this)
    );
    this.spectatorMessenger.connect();

    // window.setInterval((function() {
    //     let currentPos = document.getElementById("scoretablebodycontent").offsetTop;
    //     // document.getElementById("scoretablebodycontent").scrollTop += 100;
    //     document.getElementById("scoretablebodycontent").scrollBy({
    //       top: 100, // could be negative value
    //       left: 0,
    //       behavior: 'smooth'
    //     });
    //     let newPos = document.getElementById("scoretablebodycontent").offsetTop;
    //     if (currentPos == newPos) {
    //         document.getElementById("scoretablebodycontent").scrollBy({
    //         top: -100, // could be negative value
    //         left: 0,
    //         behavior: 'smooth'
    //     });
    //     }

    //   }), 5000);

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
    console.log("scoreboard destroyed: dom removed");
    if (this.spectatorMessenger) {
      this.spectatorMessenger.unregister();
      this.spectatorMessenger.disconnect();
      this.spectatorMessenger = null;
    }
  },

  // Underlying model
  data() {
    return {
      msg: "Trouble Flipper Scoreboard",
      state: "connecting",
      scoreboardInfo: {
        teams: [],
        players: []
      }
    };
  },

  // any actions
  methods: {
    handleMsg: function(msg) {
      console.log("Got message", msg);
      // this.scoreboardInfo.players = msg.players;
      if (msg.players) {
        this.updateData(this.scoreboardInfo.players, msg.players);
        this.scoreboardInfo.players.forEach(function(player) {
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
        this.state = "watching";
      }
    },
    ordinal_suffix_of: function(i) {
      var j = i % 10,
        k = i % 100;
      if (j == 1 && k != 11) {
        return i + "st";
      }
      if (j == 2 && k != 12) {
        return i + "nd";
      }
      if (j == 3 && k != 13) {
        return i + "rd";
      }
      return i + "th";
    }
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<!-- Enable sas by using lang="scss" -->
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
  width: 100%;
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
  overflow-y: scroll;
  position: relative;
}
.score-table-body .completed {
    flex: 1;
    text-align: right;  

}
.score-table-body .game-name {
  font-size: 12px;
  align-self: flex-end;
  padding-left: 30px;
}
.score-table-body-content {
  animation: MoveUpDown 1s linear infinite;
  position: absolute;
  left: 0;
  bottom: 0;
}

#player-score-table,
#team-score-table {
  /* background: rgba(226, 30, 30, 0.14); */
  color: white;
}

.headshot {
  width: 50px;
  height: 50px;
  overflow: hidden;
  border-radius: 25px;
  position: relative;
  box-shadow: 0px 0px 1px 3px rgba(0, 0, 0, 0.1);
  background: white;
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
  width: 120px;
  left: -40px;
  top: 0px;
}

.peach {
  color: pink;
}

.mario .headshot img {
  width: 55px;
  left: 0px;
  top: -7px;
}

.mario {
  color: #006fea;
}

.yoshi .headshot img {
  width: 70px;
  left: -10px;
  top: 4px;
}

.yoshi {
  color: rgb(19, 207, 19);
}

.bowser .headshot img {
  width: 100px;
  left: -5px;
  top: -5px;
}

.bowser {
  color: rgb(252, 206, 0);
}

.goomba .headshot img {
  width: 65px;
  left: 0px;
  top: 4px;
}

.goomba {
  color: rgb(194, 86, 23);
}

.score-title {
  font-size: 4vw;
  text-align: center;
  margin: 1vw 20vw;
  border-radius: 10vw;
}

.score-table-row {
  display: flex;
  align-items: center;
  border-radius: 30px;
  height: 60px;
  padding: 5px;
  margin: 0.5vw 2vw;

  padding: 0.3vw 1vw;
  margin: 0.5vw 2vw;
  border: 2px solid #b3a6a6;
  box-shadow: 0.5vw -1px 3px #565151, 9px 2px 0 #c3acac, 5px 1px 0 #9c9292,
    10px 3px 0px 1px #a99191;
  text-shadow: 4px -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 black,
    1px 1px 0 #000;
}

.score-table {
  width: 50%;
 
}
.score-table-row .score-rank {
 
        font-size: 32px;
    padding: 0 10px;
    width: 40px;
    align-self: center;
}
.score-table-row .id-user {
    font-size: 32px;
    padding: 0 .5em;
    flex: 1;
    white-space: nowrap; 
    overflow: hidden;
    text-overflow: ellipsis;
    /* max-width: 350px; */

/* .score-table-row .id {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  font-size: 32px; */
}

.score-table-row .id-team {
    font-size: 32px;
    padding: 0 .5em;
    white-space: nowrap; 
    overflow: hidden;
    text-overflow: ellipsis;
    /* max-width: 400px; */

/* .score-table-row .id {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  font-size: 32px; */
}


.score-table-row .index {
  width: 40px;
  padding-left: 10px;
}
#player-score-table .score-table-row {
  background: #924692e0;
}
#team-score-table .score-table-row {
  background: rgba(37, 173, 33, 0.9);
}
.score-table-row > .moves {
  flex-basis: 20%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}
.moves img {
  opacity: 0.6;
}
.score-table-row .labels {
  font-size: 30px;
  align-self: center;
  /* position:absolute;
  top: calc(2vw - 15px);
  left: 2vw; */
}
.red {
  color: #d41345;
  text-shadow: -0.5vw -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000,
    1px 1px 0 #000;
}
.flip-list-move {
  transition: transform 1s;
}

.flip-list-enter,
.flip-list-leave-to {
  opacity: 0;
  transform: rotateY(90deg);
}

.bounce-enter-active {
  animation: bounce-in 0.5s;
}
.bounce-leave-active {
  animation: bounce-in 0.5s reverse;
}
@keyframes bounce-in {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.5);
  }
  100% {
    transform: scale(1);
  }
}

.component-fade-enter-active,
.component-fade-leave-active {
  transition: opacity 0.3s ease;
}
.component-fade-enter,
.component-fade-leave-to {
  opacity: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s;
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}

.scores-panel {
  display: flex;
  align-items: start;
  justify-content: center;
  width: 100%;
}


#audio {
  position: fixed;
  bottom: 10px;
  width: 50%;
  margin: 0 auto;
}
</style>

