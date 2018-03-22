<template>
  <div id="app">
    <!-- <img src="./assets/logo.png"> -->
    <router-view/>
  </div>
</template>

<script>
export default {
  name: "App",
  created() {
  },
  destroyed() {
  },
  data() {
    return {
      appProps: {
        vpn: "msgvpn-91b69335zj",
        username: "solace-cloud-client",
        password: "1a57th4am83cb5ali4rtlu4p55",
        url: "ws://mr-91b69335zj.messaging.solace.cloud:80"
      }
    };
  }
};
</script>

<style>
#app {
  font-family: "Avenir", Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
