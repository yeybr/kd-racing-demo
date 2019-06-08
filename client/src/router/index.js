import Vue from 'vue'
import Router from 'vue-router'
import Signin from '@/components/Signin'
import SignUp from '@/components/SignUp'
import RaceDashboard from '@/components/RaceDashboard'
import Game from '@/components/Game'
import Choose from '@/components/Choose'
import Scorebard from '@/components/Scoreboard'
import GameMaster from '@/components/GameMaster'

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
      path: '/choose',
      name: 'choose',
      component: Choose
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
    },
    {
      path: '/game-master',
      name: 'game-master',
      component: GameMaster
    },
    {
      path: '/signup',
      name: 'signup',
      component: SignUp
    }
    ,
    {
      path: '/racedashboard',
      name: 'racedashboard',
      component: RaceDashboard
    }
  ]
})
