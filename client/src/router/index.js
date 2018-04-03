import Vue from 'vue'
import Router from 'vue-router'
import Signin from '@/components/Signin'
import Game from '@/components/Game'
import Choose from '@/components/Choose'
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
    // {
    //   path: '/choose',
    //   name: 'choose',
    //   component: Choose
    // },
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
