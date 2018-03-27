<template>
 <ons-gesture-detector  v-on:swiperight="gosignin">
<div id="choose">
<div class="titlebar">Choose your character</div>

<div class="heros">
  <div class="heromug bowser" data_id="bowser" @click="goplay">       
  <div class="heroselect" ></div>
  <div class="nametag" hero-name="bowser">Bowser</div>
  </div>
  <div class="heromug yoshi"  data_id="yoshi" @click="goplay">
    <div class="heroselect"></div>
        <div class="nametag" hero-name="bowser">Yoshi</div>
  </div>
  <div class="heromug toad" data_id="toad" @click="goplay">
    <div class="heroselect"></div>
        <div class="nametag">Toad</div>
  </div>
  <div class="heromug peach" data_id="peach" @click="goplay">
    <div class="heroselect"></div>
    <div class="nametag">Peach</div>
  </div>

   <div class="heromug mario" data_id="mario" @click="goplay">
    <div class="heroselect"></div>
    <div class="nametag">Mario</div>
  </div> 
 </div>
 </div>
     </ons-gesture-detector>
</template>

<script>
import Router from 'vue-router'

export default {
  name: "Choose",
  // lifecycle callbacks
  created() {
    console.log('signin created: data bind');
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
    console.log('signin destroyed: dom removed');
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
    goplay: function(event) {
      console.log("play with " + event.currentTarget.getAttribute("data_id"));
     this.$router.push({
          name: 'game',
          query: {
            username: event.currentTarget.getAttribute("data_id"),
            avatar: event.currentTarget.getAttribute("data_id")
          }
        });
    },
    gosignin: function(event) {
       this.$router.push('signin');
    },
    game: function(event) {
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
          params: {
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
.titlebar {
      text-align: center;
    background: #bfa5a538;
    padding: 5px;
}
</style>
