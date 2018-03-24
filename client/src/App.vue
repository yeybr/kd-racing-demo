<template>
  <div id="app">
    <!-- <img src="./assets/logo.png"> -->
    <router-view/>
  </div>
</template>

<script>
export default {
  name: 'App',
  created() {
    console.log("Creating the connection", this.$solace);
    var solace = this.$solace;
     var factoryProps = new solace.SolclientFactoryProperties();
        factoryProps.profile = solace.SolclientFactoryProfiles.version10;
        solace.SolclientFactory.init(factoryProps);
        // enable logging to JavaScript console at WARN level
        // NOTICE: works only with "solclientjs-debug.js"
        solace.SolclientFactory.setLogLevel(solace.LogLevel.WARN);
        this.session = solace.SolclientFactory.createSession({
          // solace.SessionProperties
          url:      this.url,
          vpnName:  this.vpn,
          userName: this.username,
          password: this.password
        });
        this.session.connect();

  },
  destroyed() {
    this.session.dispose();
  },
  data() {
    return {
      session: null,
      vpn: "msgvpn-91b69335zj",
      username: "solace-cloud-client",
      password: "1a57th4am83cb5ali4rtlu4p55",
      url: "ws://mr-91b69335zj.messaging.solace.cloud:80"
    }
  }
}
</script>

<style>
#app {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
  font-family: Bangers;
}

.page__background {
    background-image: url("assets/background.png");
    background-size: cover;
}
</style>
