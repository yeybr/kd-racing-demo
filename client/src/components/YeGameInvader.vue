<template>
    <ons-gesture-detector>
  <div id="invader" > test
    </div>
      </ons-gesture-detector>

</template>

<script>



import Phaser from 'Phaser'

export default {
  name: "invader",
  // lifecycle callbacks
  created() {
    // instance created and data bound
  },
  mounted() {
      console.log('signin mounted: dom element inserted');
      let self=this;
       this.game = new Phaser.Game({type: Phaser.AUTO,
        parent: 'phaser-example',
        width: 640,
        height: 360,
        scene: {
          init: function init() {
  this.playerSpeed = 1.5;
  this.enemySpeed = 2;
  this.enemyMaxY = 280;
  this.enemyMinY = 80;
},
            preload: function preload ()
            {
              self.preload(this);

            },
            create:   function create ()
              {
                self.create(this);               
              },
              update:   function update ()
              {
                self.update(this);               
              },
              gameOver:   function gameOver ()
              {
                self.gameOver(this);               
              },
        }});


  },
  // beforeUpdate() {
  //   // add any customized code before DOM is re-render and patched based changes in data
  //   console.log('signin beforeUpdate: data is changed, about to rerender dom');
  // },
  // updated() {
  //   console.log('signin updated: dom is rerendered');
  // },
  destroyed() {
    // clean up any resource, such as close websocket connection, remove subscription
    this.game.destroy();
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
    preload (phaser) {
            phaser.load.image('bullet', 'static/invaders/bullet.png');
          phaser.load.image('enemyBullet', 'static/invaders/enemy-bullet.png');
          phaser.load.image('invader', 'static/invaders/invader32x32x4.png', 32, 32);
          phaser.load.image('ship', 'static/invaders/player.png');
          phaser.load.image('kaboom', 'static/invaders/explode.png', 128, 128);
          phaser.load.image('starfield', 'static/invaders/starfield.png');
          


    },
    create(phaser) {
      // var logo = phaser.add.image(400, 150, 'logo');

      //           phaser.tweens.add({
      //                     targets: logo,
      //                     y: 450,
      //                     duration: 2000,
      //                     ease: 'Power2',
      //                     yoyo: true,
      //                     loop: -1
      //                 });
      // background
        let bg = phaser.add.sprite(0, 0, 'starfield');

        // change origin to the top-left of the sprite
        bg.setOrigin(0, 0);

        // player
        phaser.player = phaser.add.sprite(40, 20, 'ship');

        // scale down
//        phaser.player.setScale(0.5);

        // goal
        phaser.treasure = phaser.add.sprite(phaser.sys.game.config.width - 80, phaser.sys.game.config.height / 2, 'kaboom');
        // phaser.treasure.setScale(0.6);

        // group of enemies
        phaser.enemies = phaser.add.group({
          key: 'invader',
          repeat: 5,
          setXY: {
            x: 110,
            y: 100,
            stepX: 80,
            stepY: 20
          }
        });

        // scale enemies
        Phaser.Actions.ScaleXY(phaser.enemies.getChildren(), -0.5, -0.5);

        // set speeds
        Phaser.Actions.Call(phaser.enemies.getChildren(), function(enemy) {
          enemy.speed = Math.random() * 2 + 1;
        }, phaser);

        // player is alive
        phaser.isPlayerAlive = true;

    },
    update(phaser) {
                  // only if the player is alive
        if (!phaser.isPlayerAlive) {
          return;
        }

        // check for active input
        if (phaser.input.activePointer.isDown) {
          // player walks
          phaser.player.x += phaser.playerSpeed;
        }

        //treasure collision
        if (Phaser.Geom.Intersects.RectangleToRectangle(phaser.player.getBounds(), phaser.treasure.getBounds())) {
          this.gameOver(phaser);
        }

        // enemy movement and collision
        let enemies = phaser.enemies.getChildren();
        let numEnemies = enemies.length;

        for (let i = 0; i < numEnemies; i++) {

          // move enemies
          enemies[i].y += enemies[i].speed;

          // reverse movement if reached the edges
          if (enemies[i].y >= phaser.enemyMaxY && enemies[i].speed > 0) {
            enemies[i].speed *= -1;
          } else if (enemies[i].y <= phaser.enemyMinY && enemies[i].speed < 0) {
            enemies[i].speed *= -1;
          }

          // enemy collision
          if (Phaser.Geom.Intersects.RectangleToRectangle(phaser.player.getBounds(), enemies[i].getBounds())) {
            this.gameOver(phaser);
            break;
          }
        }

    },

  gameOver(phaser) {

  // flag to set player is dead
  phaser.isPlayerAlive = false;

  // shake the camera
  phaser.cameras.main.shake(500);

  // fade camera
  phaser.time.delayedCall(250, function() {
    phaser.cameras.main.fade(250);
  }, [], phaser);

  // restart game
  phaser.time.delayedCall(500, function() {
    phaser.scene.manager.bootScene(phaser);
  }, [], phaser);

  // reset camera effects
  phaser.time.delayedCall(600, function() {
    phaser.cameras.main.resetFX();
  }, [], phaser);
},
    gochoose: function(event) {
      this.$router.push('choose');
    },
    goscoreboard: function(event) {
      this.$router.push({
          name: 'scoreboard',
          query: {
            username: this.username,
            isMaster: this.usertype === 'master'
          }
        });
    },
    gogamemaster: function(event) {
      console.log("go as master login");
    },
    signon: function(event) {
      console.log(this.username + ", " + this.password + ", " + this.usertype);
      if (this.usertype === 'player') {
        this.$router.push({
          name: 'choose',
          query: {
            username: this.username
          }
        });
      } else {
        this.$router.push({
          name: 'scoreboard',
          query: {
            username: this.username,
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
  height: 100%;
  flex: 1;
  align-self: stretch;
  display: flex;
  flex-direction: column;
  align-items: center;
}
label {
  font-size: 8vw;
}
.center {
  text-align: center;
  width: 100%;
}

.form-group {
  margin: 0 40px;
}

input {
  font-size: 32px;
  font-family: Roboto;
  text-align: center;
}

.mario {
  width: 200px;
  padding: 40px 0;
}

.go-btn {
  font-size: 32px;
  margin-bottom: 80px;
  background: #006fea;
  border-radius: 4px;
  padding: 10px;
  margin: 40px 0;
  color: white;
  width: 100px;
  cursor: pointer;
}
</style>
