import { UsersMessage, UsersAckMessage, TournamentsMessage, TeamsMessage, publishMessageToTopic, parseReceivedMessage, RankMessage } from '@/messaging/messages.js';

export class Spectator {
  constructor(solaceApi, appProps, userInfo, msgCallback) {
    this.solaceApi = solaceApi;
    this.appProps = appProps;
    this.username =  userInfo.username;
    this.clientId = userInfo.clientId;
    // upon receiving a message, call this callback with json content, so that UI can get updated
    this.msgCallback = msgCallback;
    this.session = null;
  }

  connect() {
    // create session, publisher, subscriber
    // handle session events, messages
    try {
      if (!this.session) {
        // console.log("Creating the connection", this.solaceApi, this.appProps);
        let solace = this.solaceApi;
        var factoryProps = new solace.SolclientFactoryProperties();
        factoryProps.profile = solace.SolclientFactoryProfiles.version7;
        solace.SolclientFactory.init(factoryProps);
        this.session = solace.SolclientFactory.createSession({
          url: this.appProps.url,
          vpnName: this.appProps.vpn,
          userName: this.appProps.username,
          password: this.appProps.password,
          clientName: this.clientId || ''
        });
        this.session.on(solace.SessionEventCode.UP_NOTICE, (sessionEvent) => {
          let sessionProperties = this.session.getSessionProperties();
          this.clientId = sessionProperties.clientName;
          console.log('Successfully connected with clientId ' + this.clientId +
            ', protocol in use ' + sessionProperties.transportProtocolInUse);
          this.register();

        });
        this.session.on(solace.SessionEventCode.CONNECT_FAILED_ERROR, (sessionEvent) => {
          console.log('Connection failed to the message router: ' + sessionEvent.infoStr +
            ' - check correct parameter values and connectivity!');
          this.msgCallback({state: 'connecting'});
        });
        this.session.on(solace.SessionEventCode.DISCONNECTED, (sessionEvent) => {
          console.log('Disconnected: ' + sessionEvent.infoStr);
          this.msgCallback({state: 'connecting'});
          if (this.session !== null) {
            this.session.dispose();
            this.session = null;
          }
        });
        this.session.on(solace.SessionEventCode.SUBSCRIPTION_OK, (sessionEvent) => {
          var topicName = sessionEvent.correlationKey;
          console.log('Successfully subscribed to topic: ' + topicName);
          if (topicName === 'score/players') {
            this.msgCallback({state: 'watching'});
          }
        });
        this.session.on(solace.SessionEventCode.MESSAGE, (message) => {
          // console.log('Received message: "' + message.getBinaryAttachment() + '", details:\n' + message.dump());
          this.handleMessage(message.getDestination(), message.getBinaryAttachment());
        });
        this.session.connect();
      }
    } catch (e) {
      console.log('Connect fails', e);
      if (this.session) {
        this.disconnect();
      }
    }
  }

  handleMessage(destination, jsonMessage) {

    if (jsonMessage) {
      let msg = null;
      if (typeof jsonMessage === 'string') {
        try {
          msg = JSON.parse(jsonMessage);
        } catch (e) {
          console.log('cannot parse message', e);
        }
      } else if (typeof jsonMessage === 'object') {
        msg = jsonMessage;
      } else {
        console.log('unknown message type',  jsonMessage);
      }
      if (msg) {
        // process message future if needed, such as adding new state value
        this.msgCallback(msg);
      }
    }
  }
  subscribeToTopic(topic) {
    if (this.session !== null) {
      let solace = this.solaceApi;
      try {
        this.session.subscribe(
          solace.SolclientFactory.createTopicDestination(topic),
          true,
          topic,
          10000
        );
      } catch (e) {
        console.log('Subscribe failed.', e);
      }
    }
  }
  register() {
    console.log('Connect spectator ' + this.username + ', clientId ' + this.clientId);
    // this.subscribeToTopic('score/>');
    // this.subscribeToTopic('team/>');
    // this.subscribeToTopic('user/>');
    this.subscribeToTopic('score/players');
    console.log('subscribing to players ranking');

   // TESTING CODE
  //  setTimeout(() => {
  //   this.msgCallback(this.simulateTeamResponse());
  //  }, 2000);
  }

  simulateTeamResponse() {
    return {
      state: 'watching',
      clientId: this.clientId,
      username: this.username,
      scoreboardInfo: {
        games: [
          {
            gameId: '1',
            gameName: 'Mario',
            teamId: '1',
            teamName: 'Team 1',
            win: false,
            players: [
              {
                id: '1',
                name: 'Kevin'
              },
              {
                id: '2',
                name: 'Rob'
              },
              {
                id: '5',
                name: 'Roland'
              },
            ],
            stats: {
              total: 16,
              finished: 0,
              totalMoves: 0,
              correctMoves: 0
            }
          },
          {
            gameId: '2',
            gameName: 'Yoshi',
            teamId: '2',
            teamName: 'Team 2',
            win: false,
            players: [
              {
                id: '3',
                name: 'Brandon'
              },
              {
                id: '4',
                name: 'Abhishek'
              },
              {
                id: '6',
                name: 'Ye'
              },
              {
                id: '7',
                name: 'Monica'
              }
            ],
            stats: {
              total: 16,
              finished: 0,
              totalMoves: 0,
              correctMoves: 0
            }
          }
        ]
      }
    }
  }

  // called by Scoreboard.vue destroy method
  unregister() {
    console.log('Let server know spectator ' + this.username + ', clientId ' + this.clientId + ' becomes inactive');
  }

  disconnect() {
    // session disconnect
    console.log("Disconect");
    try {
      if (this.session) {
        this.session.disconnect();
        this.session = null;
      }
    } catch (e) {
      console.log('Disconnect fails', e);
      this.msgCallback({state: 'connecting'});
    }
  }

}
