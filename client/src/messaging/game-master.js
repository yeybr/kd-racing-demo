import { UsersMessage, UsersAckMessage } from '@/messaging/messages.js';
import { TournamentMessage, TournamentsMessage } from '@/messaging/messages.js';;
import { publishMessageToTopic, parseReceivedMessage } from '@/messaging/messages.js';

export class GameMaster {
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
          this.subscribeToTopic('user/' + this.clientId);
        });
        this.session.on(solace.SessionEventCode.CONNECT_FAILED_ERROR, (sessionEvent) => {
          console.log('Connection failed to the message router: ' + sessionEvent.infoStr +
            ' - check correct parameter values and connectivity!');
            this.msgCallback({connected: false});
        });
        this.session.on(solace.SessionEventCode.DISCONNECTED, (sessionEvent) => {
          console.log('Disconnected: ' + sessionEvent.infoStr);
          this.msgCallback({connected: false});
          if (this.session !== null) {
            this.session.dispose();
            this.session = null;
          }
        });
        this.session.on(solace.SessionEventCode.SUBSCRIPTION_OK, (sessionEvent) => {
          var topicName = sessionEvent.correlationKey;
          console.log('Successfully subscribed to topic: ' + topicName);
          if (topicName === 'user/' + this.clientId) {
            this.register();
          }
        });
        this.session.on(solace.SessionEventCode.MESSAGE, (message) => {
          // console.log('Received message: "' + message.getBinaryAttachment() + '", details:\n' + message.dump());
          var topic = message.getDestination().getName();
          this.handleMessage(topic, message.getBinaryAttachment());
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

  unsubscribeToTopic(topic) {
    if (this.session !== null) {
      let solace = this.solaceApi;
      try {
        this.session.unsubscribe(
          solace.SolclientFactory.createTopicDestination(topic),
          true,
          topic,
          10000
        );
      } catch (e) {
        console.log('Unsubscribe failed.', e);
      }
    }
  }

  handleMessage(topic, message) {
    if (typeof message !== 'string') {
      console.log('Error: unexpected message type');
      return;
    }

    try {
      var messageInstance = parseReceivedMessage(topic, message);
      if (messageInstance !== null) {
        this.msgCallback(messageInstance);
      }
    } catch (e) {
      console.log('Error:', e);
    }
  }

  register() {
    console.log('Connect game master ' + this.username + ', clientId ' + this.clientId);

    var usersMessage = new UsersMessage(this.username, this.clientId, true);
    try {
      publishMessageToTopic('users', usersMessage, this.session, this.solaceApi);
    } catch (error) {
      console.log("Publish failed. error = ", error);
    }
   // TESTING CODE
  //  setTimeout(() => {
  //   this.msgCallback(this.simulateTeamResponse());
  //  }, 2000);
  }

  simulateTeamResponse() {
    let response = Object.assign(new TournamentMessage, {
      clientId: this.clientId,
      username: this.username,
      started: false,
      waitingPlayers: [
        {
          id: '10',
          name: 'Amy'
        },
        {
          id: '12',
          name: 'Nathan'
        }
      ],
      teams: [
        {
          teamId: '1',
          teamName: 'Team 1',
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
          ]
        },
        {
          teamId: '2',
          teamName: 'Team 2',
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
          ]
        }
      ]
    });
    return response;
  }

  // called by Scoreboard.vue destroy method
  unregister() {
    console.log('Let server know game master ' + this.username + ', clientId ' + this.clientId + ' becomes inactive');
  }

  startGame() {
    console.log('Send message to request to start game');

    var tournamentsMessage = new TournamentsMessage();
    try {
      publishMessageToTopic('tournaments', tournamentsMessage, this.session, this.solaceApi);
    } catch (error) {
      console.log("Publish failed. error = ", error);
    }
  }

  stopGame() {
    console.log('Send message to request to stop game');

    var tournamentsMessage = new TournamentsMessage('stopGames');
    try {
      publishMessageToTopic('tournaments', tournamentsMessage, this.session, this.solaceApi);
    } catch (error) {
      console.log("Publish failed. error = ", error);
    }
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
      this.msgCallback({connected: false});
    }
  }

}
