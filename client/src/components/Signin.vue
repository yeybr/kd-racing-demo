<template>

    <!-- <ons-gesture-detector> -->
  <div id="signon" v-on:swipeleft="gochoose" v-on:swiperight="goscoreboard" v-on:swipebottom="gogamemaster">
      <img class="mario" src="../assets/mario.png"/>
    <!-- submit button -->
    <div class="form-group">
      <label for="usr"><span class="red">E</span><span class="green">n</span><span class="yellow">t</span><span class="blue">e</span><span class="red">r</span> your gamer tag</label>
      <input type="text" class="form-control" id="usr" v-model="username">
    </div>
    <button type="button" class="go-btn btn" @click="signon()">Go!</button>
    <audio  controls autoplay src="https://archive.org/download/SuperMarioBros.ThemeMusic/SuperMarioBros.mp3">
    
<p>If you are reading this, it is because your browser does not support the audio element.     </p>
</audio>
  </div>
      <!-- </ons-gesture-detector> -->

</template>

<script>
import CommonUtils from './common-utils';
export default {
  name: "signin",
  mixins: [CommonUtils],
  // lifecycle callbacks
  created() {
    // instance created and data bound
    // looking for existing player information from local storage and prepopulate username
    let userInfo = this.retrieveFromStorage('localStorage', 'trouble_flipper_player');
    if (userInfo && userInfo.username) {
      this.username = userInfo.username;
    }
  },
  // mounted() {
  //   console.log('signin mounted: dom element inserted');
  // },
  // beforeUpdate() {
  //   // add any customized code before DOM is re-render and patched based changes in data
  //   console.log('signin beforeUpdate: data is changed, about to rerender dom');
  // },
  // updated() {
  //   console.log('signin updated: dom is rerendered');
  // },
  destroyed() {
    // clean up any resource, such as close websocket connection, remove subscription
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
      usertype: "player"
    };
  },

  // any actions
  methods: {
    gochoose: function(event) {
      this.$router.push('game');
    },
    goscoreboard: function(event) {
      this.$router.push({
          name: 'scoreboard',
          query: {
            username: this.username,
            isMaster: this.usertype === 'master'
          }
        });
    },
    gogamemaster: function(event) {
      console.log("go as master login");
    },
    signon: function(event) {
      console.log(this.username + ", " + this.password + ", " + this.usertype);
      if (this.usertype === 'player') {
        this.$router.push({
          name: 'game',
          query: {
            username: this.username
          }
        });
      } else {
        this.$router.push({
          name: 'scoreboard',
          query: {
            username: this.username,
            isMaster: this.usertype === 'master'
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
#signon {
  height: 100%;
  flex: 1;
  align-self: stretch;
  display: flex;
  flex-direction: column;
  align-items: center;
}
label {
  font-size: 8vw;
}
.center {
  text-align: center;
  width: 100%;
}

.form-group {
  margin: 0 40px;
}

input {
  font-size: 32px;
  font-family: Roboto;
  text-align: center;
}

.mario {
  width: 200px;
  padding: 40px 0;
}

.go-btn {
  font-size: 32px;
  margin-bottom: 80px;
  background: #006fea;
  border-radius: 4px;
  padding: 10px;
  margin: 40px 0;
  color: white;
  width: 100px;
  cursor: pointer;
}


.red {
      color: #d41345;
    text-shadow: -10px -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000, 1px 1px 0 #000;
}
.blue {
    color: rgb(0, 75, 187);
    text-shadow: -10px -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000, 1px 1px 0 #000;
}

.yellow {
    color: rgb(226, 210, 120);
    text-shadow: -10px -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000, 1px 1px 0 #000;
}
.green {
    color: rgb(37, 173, 33);
    text-shadow: -10px -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000, 1px 1px 0 #000;
}
</style>
