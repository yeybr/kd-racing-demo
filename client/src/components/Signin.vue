<template>
    <!-- <img width="280" height="80" src="../assets/logo_wegrow.png"> -->
  <div id="signon">
    <!-- Username -->
    <label class="center">Enter your gamer tag</label>

    <!-- submit button -->
    <div class="form-group">
      <label for="usr">Name:</label>
      <input type="text" class="form-control" id="usr">
    </div>
  </div>
</template>

<script>
export default {
  name: "signin",
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
#signon {
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
