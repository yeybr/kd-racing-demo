<template>
<div id="choose" class="choose-panel">
  <!-- <div class="titlebar">Choose your character</div>
  <div class="heros">
    <div class="heromug bowser" data_id="bowser" @click="goplay">
    <div class="heroselect" ></div>
    <div class="nametag" hero-name="bowser">Bowser</div>
    </div>
    <div class="heromug yoshi"  data_id="yoshi" @click="goplay">
      <div class="heroselect"></div>
      <div class="nametag" hero-name="bowser">Yoshi</div>
    </div>
    <div class="heromug goomba" data_id="goomba" @click="goplay">
      <div class="heroselect"></div>
      <div class="nametag">Goomba</div>
    </div>
    <div class="heromug peach" data_id="peach" @click="goplay">
      <div class="heroselect"></div>
      <div class="nametag">Peach</div>
    </div>
    <div class="heromug mario" data_id="mario" @click="goplay">
      <div class="heroselect"></div>
      <div class="nametag">Mario</div>
    </div>
  </div> -->
  <div class="titlebar">View connection logs</div>
  <div class="log-panel">
    <textarea id="logs" v-bind:value="logs" rows=18></textarea>
  </div>
  <div class="action-panel">
    <button class="button btn-secondary" @click="clearLogs">Clear</button>
  </div>
 </div>
</template>

<script>
import Router from "vue-router";
import { Rankings, TextareaLogger } from "@/messaging/rankings";

export default {
  name: "Choose",
  // lifecycle callbacks
  created() {
    console.log("choose created: data bind");
    this.username = "testuser";
    this.clientId = null;
    this.messagener = new Rankings(
      this.$solace,
      this.$parent.appProps,
      { username: this.username, clientId: this.clientId },
      this.handleMsg.bind(this),
      new TextareaLogger(this)
    );
    this.messagener.connect();
  },
  destroyed() {
    // clean up any resource, such as close websocket connection, remove subscription
    console.log("choose destroyed: dom removed");
  },

  // Underlying model
  data() {
    return {
      msg: "Welcome to join Trouble Flipper!",
      user_type_list: [
        { text: "Player", value: "player" },
        { text: "Spectator", value: "spectator" },
        { text: "Game Master", value: "master" }
      ],
      username: "",
      password: "",
      usertype: "player",
      logs: ""
    };
  },

  // any actions
  methods: {
    clearLogs: function() {
      this.logs = "";
    },
    handleMsg: function(msg) {
      console.log("Got message", msg);
    },
    goplay: function(event) {
      var username = this.$route.query.username;
      console.log("play with " + event.currentTarget.getAttribute("data_id"));
      this.$router.push({
        name: "game",
        query: {
          username: username
            ? username
            : event.currentTarget.getAttribute("data_id"),
          avatar: event.currentTarget.getAttribute("data_id")
        }
      });
    },
    gosignin: function(event) {
      this.$router.push("signin");
    },
    game: function(event) {
      console.log(this.username + ", " + this.password + ", " + this.usertype);
      if (this.usertype === "player") {
        this.$router.push({
          name: "game",
          query: {
            username: this.username
          }
        });
      } else {
        this.$router.push({
          name: "scoreboard",
          params: {
            isMaster: this.usertype === "master"
          }
        });
      }
    },
    signout: function(event) {}
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

.choose-panel {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-content: center;
  overflow-y: auto;
}

.choose-panel .log-panel {
  margin: 20px;
  flex: 1;
}

.choose-panel .log-panel textarea {
  width: 100%;
  font-family: Arial, Helvetica, sans-serif;
}

.choose-panel .action-panel {
  margin: 0px 20px 20px 20px;
  text-align: right;
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
label {
  font-size: 36px;
}
.page__content {
  padding-top: 200px !important;
}
ul {
  display: block;
}
ul li {
  display: block;
  padding: 10px;
}

.center {
  text-align: center;
  width: 100%;
}

.form-group {
  margin: 0 40px;
}
</style>
