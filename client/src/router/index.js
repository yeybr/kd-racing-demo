import Vue from 'vue'
import Router from 'vue-router'
import Signin from '@/components/Signin'
import Game from '@/components/Game'
import Scorebard from '@/components/Scoreboard'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      redirect: '/signin'
    },
    {
      path: '/signin',
      name: 'signin',
      component: Signin
    },
    {
      path: '/game',
      name: 'game',
      component: Game
    },
    {
      path: '/scoreboard',
      name: 'scoreboard',
      component: Scorebard
    }
  ]
})
