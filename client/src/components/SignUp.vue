<template>
		<!-- <ons-gesture-detector> -->
	<div id="signon"  v-on:swipeleft="gochoose" v-on:swiperight="goscoreboard" v-on:swipebottom="gogamemaster">		
		<!-- v-if="state == 'waiting' -->
		<div id="selectingTeam" >    
				<!-- submit button -->
				<div class="form-group">

					<div class="team-select-grid">
						
						<div class="a">{{ games[0].teams[0].teamInfo.value }} vs {{ games[0].teams[1].teamInfo.value}}</div>
				
						<button ctype="button" class="b go-btn btn" @click="readyToBattle(games[0].teams[0].teamInfo.text, games[0].teams[1].teamInfo.text)">Ready</button>
						
						<select class="c" v-model="selectedTeam[0]" v-on:change="teamSelected(0, selectedTeam[0], 0, 'home')" >
							<option v-for="option in registeredTeams" 
								v-if="!option.disabled" v-bind:value="option.value">
								{{ option.text }}
							</option>
						</select>
						
						<select class="d" v-model="selectedTeam[1]" v-on:change="teamSelected(1, selectedTeam[1], 0, 'away')" >
							<option v-for="option in registeredTeams" 
									v-if="!option.disabled" v-bind:value="option.value">
								{{ option.text }}
							</option>
						</select>
						
						
						
					</div>
					<div class="team-select-grid">
						
						<div class="a">{{ games[1].teams[0].teamInfo.value }} vs {{ games[1].teams[1].teamInfo.value}}</div>
				
						<button ctype="button" class="b go-btn btn" @click="readyToBattle(games[1].teams[0].teamInfo.text, games[1].teams[1].teamInfo.text)">Ready</button>
						
						<select class="c" v-model="selectedTeam[3]" v-on:change="teamSelected(3, selectedTeam[3], 1, 'home')" >
							<option v-for="option in registeredTeams" 
								v-if="!option.disabled" v-bind:value="option.value">
								{{ option.text }}
							</option>
						</select>
						
						<select class="d" v-model="selectedTeam[4]" v-on:change="teamSelected(4, selectedTeam[4], 1, 'away')" >
							<option v-for="option in registeredTeams" 
									v-if="!option.disabled" v-bind:value="option.value">
								{{ option.text }}
							</option>
						</select>		
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
		<!-- //v-if="state != 'starting' -->
			<div class="game-container"  >
				 <div v-for="(game, index) in games">
					<h1>Game {{index + 1}} - {{ game.teams[0].teamInfo.value }} vs {{ game.teams[1].teamInfo.value}} </h1>             
					  
					<div class="team-status">
						<h1> {{ game.teams[0].teamInfo.value }}'attacks</h1>

						<div class="attack-grid">
							<div class="attack-item" v-bind:class="{disabled: attack.used}" v-for="attack in game.teams[0].attacks">
								{{ attack.name }} 
							</div>
						</div>	
					</div>
					<div class="team-status">
						<h1>{{ game.teams[1].teamInfo.value}}'s attacks</h1>
						<div class="attack-grid">
							<div class="attack-item" v-bind:class="{disabled: attack.used}" v-for="attack in game.teams[1].attacks">
								{{ attack.name }}
							</div>
						</div>
					</div>
					      
				</div>
				<div></div>
					
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
			msg: "Welcome to join Trouble Racers!",
			state: "waiting",
			

			game1team1: '',
			user_type_list: [
				{ text: "Player", value: "player" },
				{ text: "Spectator", value: "spectator" },
				{ text: "Game Master", value: "master" }
			],
			username: "",
			password: "",
			usertype: "player",
			selectedTeam: ['', '', '', ''],
			games: [{id: 0, teams: [{id: "home", teamInfo: {}, attacks: [] }, 
									{id: "away", teamInfo: {}, attacks: [] }]}, 
					{id: 1, teams: [{id: "home", teamInfo: {}, attacks: [] }, 
									{id: "away", teamInfo: {}, attacks: [] }]}], 
			registeredTeams: [
				// {text: 'Team 1', value: '1', disabled: false},
				// {text: 'Team 2', value: '2', disabled: false},
				// {text: 'Team 3', value: '3', disabled: false},
				// {text: 'Team 4', value: '4', disabled: false}
			],
			attackoptions: [
				{'name': 'Broken Wheel', id: 1, used: false},
				{'name': 'Spin out', id: 2, used: false},
				{'name': 'Reverse control', id: 3, used: false}
			]

		};
	},

	// any actions
	methods: {
		teamSelected: function(selectedTeamIndex, teamClientName, gameIndex, teamId) {
			debugger;	 //gameIndex 0, teamId: home/away
			if (teamClientName) {
				let game = this.games.find(game=> {
					return game.id === gameIndex;
				});
				let team = game.teams.find(team=> {
					return team.id === teamId;
				});
				let prevClientName = team.teamInfo.value;
				//reset previous selection 
				let prevTeam = this.registeredTeams.forEach(registeredTeam => {		
					return registeredTeam.value === prevClientName;
				});
				if (prevTeam) {
					prevTeam.disabled = false;
				}
				

				//if find the changed clientName
				let newTeam = this.registeredTeams.find(registeredTeam => {		
					return registeredTeam.value === teamClientName;
				});
				if (newTeam) {
					newTeam.disabled = true;
					team.teamInfo = newTeam;
					team.attacks = this.attackoptions.slice();
				}	
			}
			

		},
		handleMsg: function(msg) {

			debugger;
			console.log('Got message', msg);
			if (msg && msg.clientName && msg.team) {
				let team = this.registeredTeams.find(t => {
							return t.value === msg.team;
				});

				switch (msg.action) {
					
					case "attack":
						let attackSuccess = false;
						//find the attck for the team, if not used send attack to car
						if (team) {
							let playteam = null;
							for (game in this.games) {
								debugger;
								playteam = game.teams.find(team => {
									teamInfo.value === msg.team; });
								if (playteam) {
									break;
								}
							}
							if (playteam) {
								let attack = playteam.attacks.find(attack => {
									return attack.id === msg.attackId;
								});
								if (attack.used)  {
									attackSuccess = false;
								} else {
									attackSuccess = true;
									attack.used = true;
									let attackAction = {}; //todo
									this.dashboardMessenger.sendAttack(msg.clientName, msg.opponent, attackAction);
								}
							}
							
						} else {
							console.log("no attack occurred");
						}
						break;
					case "register":
						
						
						if (team) {
							console.log("team already registered: " + msg.team);
							this.dashboardMessenger.clientRegistered(msg.clientName, false);
						} else {
							let newOption = {text: msg.clientName, value: msg.team, disabled: false};
							this.registeredTeams.push(newOption);
							this.dashboardMessenger.clientRegistered(msg.clientName, true);
						}	
						break;
					default:

				}

				
				
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
  grid-template-columns: auto auto;
  /* background-color: #2196F3; */
  padding: 10px;
}
.team-select-grid {
	display: grid;
	grid-template-columns: auto auto auto;
	grid-gap: 10px;
	/* background-color: #2196F3; */
	padding: 10px;
	grid-auto-rows: 35px;
}
.attack-grid {
	display: grid;
	grid-template-columns: auto auto auto;
	grid-gap: 10px;
	/* background-color: #2196F3; */
	padding: 10px;
	grid-auto-rows: auto;
}
.a {
	grid-column: 1 / span 2;
}
.b {
	grid-column: 3 ;
	grid-row: 1 / span 2;
	margin: auto;
}
.c {
	grid-column: 1 ;
	grid-row: 2 ;
}
.d {
	grid-column: 2 ;
	grid-row: 2 ;
}
.attack-item {
	background: #e4e0ea;
    border-radius: 4px;
    margin: auto;
    padding: 20px;
	
}
.attack-item.disabled {
	background: black;
}
.team-status {
	background: #ff00002b;
    padding: 20px;
    margin: 20px;
}
</style>
