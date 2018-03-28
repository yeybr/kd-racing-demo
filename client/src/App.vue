<template>
  <div id="app">
    <!-- <img src="./assets/logo.png"> -->
     <transition name="page" >
        <router-view class="view"></router-view>
      </transition>
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
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  font-family: Bangers;
}

body {
  background-image: url("assets/backgrounds.png");
  /*background-size: cover;*/
  /* height: 100%; */
  overflow-y: auto;
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

</style>
