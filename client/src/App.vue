<template>
  <div id="app">
     <!-- <transition name="page" > -->
        <router-view class="view"></router-view>
      <!-- </transition> -->
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
        vpn: "msgvpn-8v6yiwwv9f",
        username: "solace-cloud-client",
        password: "ggtten25g11jnhht4tsevso1v9",
        url: "ws://vmr-mr8v6yiwiawh.messaging.solace.cloud:20584"
      }
    };
  }
};
</script>

<style>
#app {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  font-family: Bangers;
  height: 100%;
  display: flex;
  flex-direction: column;
}

body {
  background-image: url("assets/backgrounds.png");
  overflow-y: auto;
  height: 100%;
}

/* Transitions using the page hook */
page-enter-active, .page-leave-active {
  transition: all .30s ease-out;
}
.page-enter, .page-leave-active {
  opacity: 0;
  transform-origin: 50% 50%;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity .5s;
}
.fade-enter, .fade-leave-to /* .fade-leave-active below version 2.1.8 */ {
  opacity: 0;
}

/* Enter and leave animations can use different */
/* durations and timing functions.              */
.slide-fade-enter-active {
  transition: all .3s ease;
}
.slide-fade-leave-active {
  transition: all .8s cubic-bezier(1.0, 0.5, 0.8, 1.0);
}
.slide-fade-enter, .slide-fade-leave-to
/* .slide-fade-leave-active below version 2.1.8 */ {
  transform: translateX(10px);
  opacity: 0;
}

.titlebar {
  text-align: center;
  background: #bfa5a538;
  padding: 5px;
}

</style>
