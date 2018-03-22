<template>
  <v-ons-page id="signon">
    <!-- <img width="280" height="80" src="../assets/logo_wegrow.png"> -->

    <!-- Toolbar -->
    <v-ons-toolbar>
      <div class="center">Sign On</div>
    </v-ons-toolbar>

    <!-- Username -->
    <p>
      <v-ons-input v-model="username" modifier="underbar" placeholder="Username" float></v-ons-input>
    </p>

    <!-- Password -->
    <p>
      <v-ons-input v-model="password" type="password" modifier="underbar" placeholder="Password" float></v-ons-input>
    </p>

    <v-ons-list>
      <v-ons-list-item v-for="item in user_type_list" :key="item.value" :value="item.value" tappable>
        <label class="left">
          <v-ons-radio
            :input-id="'radio-' + item.value"
            :value="item.value"
            v-model="usertype">
          </v-ons-radio>
        </label>
        <label :for="'radio-' + item.value" class="center">
          {{ item.text }}
        </label>
      </v-ons-list-item>
    </v-ons-list>

    <!-- submit button -->
    <p style="text-align: center">
      <v-ons-button @click="signon()" style="margin: 6px 0">
        Sign On
      </v-ons-button>
    </p>
  </v-ons-page>
</template>

<script>
export default {
  name: "signin",
  // lifecycle callbacks
  created() {
    console.log('signin created');
  },
  mounted() {
    console.log('signin mounted');
  },
  beforeUpdate() {
    // add customized jquery code before dom is re-render and patch when data changes
    console.log('signin beforeUpdate');
  },
  updated() {
    console.log('signin updated');
  },
  destroyed() {
    // clean up any network resource, such as close websocket connection
    console.log('signin destroyed');
  },

  // Underlying model
  data() {
    return {
      msg: "Welcome to join Trouble Flipper game!",
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
</style>
