<template>
		<!-- <ons-gesture-detector> -->
	<div id="signon"  v-on:swipeleft="gochoose" v-on:swiperight="goscoreboard" v-on:swipebottom="gogamemaster">		
		<div id="selectingTeam" v-if="state == 'waiting'">    
				<!-- submit button -->
				<div class="form-group">

					<div>
					<div>
						<template  v-for="option in options" v-if="!option.disabled">
							<input type="radio"
									v-bind:name="game1"
									v-model="game1team1" 
									v-on:change="teamSelected(option.value, 'g1t1')"
									v-bind:id="option.value"
      								v-bind:value="option.text">		
							<label  v-bind:for="option.value">{{option.text}} {{option.value}}</label>

						</template>
						</div>
						test
						<div>
					<template  v-for="option in options"  v-if="!option.disabled">

							<input type="radio"
									v-bind:name="g1t2"
									v-model="game1team2" 
									v-on:change="teamSelected(option.value, 'g1t2')"
									v-bind:id="option.value"
      								v-bind:value="option.text">		
							<label  v-bind:for="option.value">{{option.text}} {{option.value}}</label>

						</template>
					
					</div>
						<select v-model="game1team1" >
							<option v-for="option in options" v-if="!option.disabled" v-bind:value="option.value">
							
								{{ option.text }}

							</option>
						</select>
						<select v-model="game1[1]">
							<option v-for="option in options" v-if="!option.disabled" v-bind:value="option.value">
	
								{{ option.text }}
								
							</option>
					</select>
						<span>{{ game1[0] }} vs {{ game1[1]}}</span>
						<button type="button" class="go-btn btn" @click="readyToBattle(game1[0], game1[1])">Ready</button>

					</div>
					<div>
						<select v-model="game2[0]">
							<option v-for="option in options"  v-if="!option.disabled" v-bind:value="option.value">
								
								{{ option.text }}
								
							</option>
					</select>
						<select v-model="game2[1]">
							<option v-for="option in options" v-if="!option.disabled" v-bind:value="option.value">
									
								{{ option.text }}
								
							</option>
					</select>
						<span>{{ game2[0] }} vs {{ game2[1]}}</span>

						<button type="button" class="go-btn btn" @click="readyToBattle(game2[0], game2[1])">Ready</button>
					</div>

					<!-- <input type="text" class="form-control" id="usr" v-model="username"> -->
				</div>
			

				<div @click="startGame()">

						<!-- <img  class="mario" src="https://66.media.tumblr.com/463774ad1eac68fd818e1bfa49978fdf/tumblr_pfsmbsMN7i1rpr5dc_250.gif"/>       -->
				<!-- <img  class="mario" src="https://66.media.tumblr.com/806f6094d319c9ec10a45538f035f17c/tumblr_ou44ky7EPx1uuhxwpo5_400.gif"/>       -->
				<img  class="mario" src="https://66.media.tumblr.com/11b1dc86d72049e34fd91903127a5762/tumblr_ou44ky7EPx1uuhxwpo2_400.gif"/>      

					<button type="button" class="go-btn btn">Go!</button>
				</div>
		<!-- <img  class="mario" src="../assets/mario-running.gif"/> -->       
		</div>
			<div class="game-container"  v-if="state != 'starting'">
				<div>
					<h1>Game 1</h1>             
					Team {{ game1[0] }} vs Team {{ game1[1] }}         
				</div>
				<div></div>
				<div>
					<h1>Game 2</h1>
					Team {{ game2[0] }} vs Team {{ game2[1] }}         
				</div>		
		</div>
	</div>

			<!-- </ons-gesture-detector> -->
</template>

<script>
import { UsersAckMessage, TournamentMessage, TeamsMessage, parseReceivedMessage, TeamRankMessage } from '@/messaging/messages.js';
import { Dashboard } from '@/messaging/dashboard';
import CommonUtils from "./common-utils";

export default {
	name: "signup",
	mixins: [CommonUtils],
	// lifecycle callbacks
	beforeCreate() {
		document.body.className = 'mario';
	},
	created() {
		console.log("sign up created: data bound");

		// if (this.$route.query.username) {
		// this.username = this.$route.query.username;
		this.username = "dashboard";
		this.clientId = null;
		debugger;
		// console.log(this.$solace);
		// let $solace = this.$solace;
		// let appProps = this.$parent.appProps;
		// let userName = this.username;
		// let clientId = this.clientId;

		this.dashboardMessenger = new Dashboard(
			this.$solace,
			this.$parent.appProps,
			{ username: this.userName, clientId: this.clientId },
			this.handleMsg.bind(this)
		);
		this.dashboardMessenger.connect();

		// window.setInterval((function() {
		//     let currentPos = document.getElementById("scoretablebodycontent").offsetTop;
		//     // document.getElementById("scoretablebodycontent").scrollTop += 100;
		//     document.getElementById("scoretablebodycontent").scrollBy({
		//       top: 100, // could be negative value
		//       left: 0,
		//       behavior: 'smooth'
		//     });
		//     let newPos = document.getElementById("scoretablebodycontent").offsetTop;
		//     if (currentPos == newPos) {
		//         document.getElementById("scoretablebodycontent").scrollBy({
		//         top: -100, // could be negative value
		//         left: 0,
		//         behavior: 'smooth'
		//     });
		//     }

		//   }), 5000);

		// } else {
		//   this.$router.push({
		//     name: 'signin'
		//   });
		// }
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
	},

	// Underlying model
	data() {
		return {
			msg: "Welcome to join Trouble Flipper!",
			state: "waiting",
			teamList: [],
			game1team1: '',
			user_type_list: [
				{ text: "Player", value: "player" },
				{ text: "Spectator", value: "spectator" },
				{ text: "Game Master", value: "master" }
			],
			username: "",
			password: "",
			usertype: "player",
			game1: ['', ''],
			game2: ['', ''],
			options: [ 
				{text: 'Team 1', value: '1', disabled: false},
				{text: 'Team 2', value: '2', disabled: false},
				{text: 'Team 3', value: '3', disabled: false},
				{text: 'Team 4', value: '4', disabled: false}
			
			],
			attackoptions: [
				{'name': 'Broken Wheel', id: 1},
				{'name': 'Spin out', id: 2},
				{'name': 'Reverse control', id: 3}
			],
			teams: [

			]

		};
	},

	// any actions
	methods: {
		teamSelected: function(teamId, teamSelection) {
			debugger;	
			if (teamId) {
				if (teamSelection === "g1t1") {
					this.game1[0] = this.game1team1;
				} else if (teamSelection === "g1t1") {
					this.game1[1] = this.game1team2;
				}
				

				//reset all teams disabled

				this.options.forEach(element => {
					
					element.disabled = false;
				});
				debugger;
				let o = this.options.find(function(option) {
						return option.value === teamId;
				});
				if (o) {
					o.disabled = true;
				}
			}
			

		},
		handleMsg: function(msg) {

			console.log('Got message', msg);
			debugger;
			if (msg && msg.clientName && msg.team) {
				let newOption = {text: msg.clientName, value: msg.team, disabled: false};
				this.options.push(newOption);
				this.dashboardMessenger.clientRegistered(msg.clientName);
			}
		},
		startGame: function() {
				this.dashboardMessenger.startGame();
				this.state = "starting";
				// this.$router.push({
				//   name: "racedashboard",
				//   query: {
				//   }
				// });
		},
		readyToBattle: function(clientName, opponent) {
				this.dashboardMessenger.clientOpponentRegistered(clientName, opponent);

		},
		gochoose: function(event) {
			this.$router.push("game");
		},
		goscoreboard: function(event) {
			this.$router.push({
				name: "scoreboard",
				query: {
					username: this.username,
					isMaster: this.usertype === "master"
				}
			});
		},
		gogamemaster: function(event) {
			console.log("go as master login");
		},
		signon: function(event) {
			console.log(this.username + ", " + this.password + ", " + this.usertype);
			if (this.usertype === "player") {
				this.$router.push({
					name: "game",
					query: {
						username: this.username
					}
				});
			} else {
				this.$router.push({
					name: "scoreboard",
					query: {
						username: this.username,
						isMaster: this.usertype === "master"
					}
				});
			}
		},
		signout: function(event) {}
	},
	goracedashboard: function(event) {
			this.$router.push({
				name: "racedashboard",
				query: {
					username: this.username,
					isMaster: this.usertype === "master"
				}
			});
		},
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
	font-size: 40px;
}
.center {
	text-align: center;
	width: 100%;
}

.form-group {
	margin: 100px auto 0 auto;
	text-align: center;
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

.go-action .label {
	position: absolute;
	bottom: 0;
	font-size: 32px;
	color: #006fea;
	left: calc(50% - 16px);

}
.go-action:hover  {
	border-radius: 300px;
	border: 1px solid #758291;
		box-shadow: -3px -1px 3px #afa1df,  -9px 2px 0 #afa1df, 5px 1px 0 #afa1df,
		10px 3px 0px 1px #afa1df;
}
.go-action  {
	position: relative;
	
	border-radius: 300px;
	border: 1px solid #758291;
		box-shadow: 3px -1px 3px #565151, 9px 2px 0 #c3acac, 5px 1px 0 #9c9292,
		10px 3px 0px 1px #a99191;
}
.go-btn {
	font-size: 32px;
	margin-bottom: 80px;
	background: #006fea;
	border-radius: 40px;
	padding: 10px;
	margin: 40px 0;
	color: white;
	width: 100px;
	min-height: 70px;
}

.red {
	color: #d41345;
	text-shadow: -3px -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000,
		1px 1px 0 #000;
}
.blue {
	color: rgb(5, 139, 255);
	text-shadow: -3px -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000,
		1px 1px 0 #000;
}

.yellow {
	color: rgb(255, 252, 0);
	text-shadow: -3px -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000,
		1px 1px 0 #000;
}
.green {
	color: rgb(37, 173, 33);
	text-shadow: -3px -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000,
		1px 1px 0 #000;
}
select {

	width: 100px;
}

.game-container {
  display: grid;
  width: 90vw;
  grid-template-columns: auto 20px auto;
  /* background-color: #2196F3; */
  padding: 10px;
}

</style>
