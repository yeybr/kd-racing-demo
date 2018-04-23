import { UsersMessage, UsersAckMessage, TournamentsMessage, TeamsMessage, publishMessageToTopic, parseReceivedMessage, RankMessage } from '@/messaging/messages.js';
import { SwapMessage } from '@/messaging/messages.js';
import { StarPowerMessage, PeachHealMessage } from './messages';
export class Player {
  constructor(solaceApi, appProps, userInfo, msgCallback) {
    this.solaceApi = solaceApi;
    this.appProps = appProps;
    this.username = userInfo.username;
    this.clientId = userInfo.clientId;
    // upon receiving a message, call this callback with json content, so that UI can get updated
    this.msgCallback = msgCallback;
    this.session = null;
  }

  connect() {
    // create session, publisher, subscriber
    try {
      if (!this.session) {
        // console.log('Creating the connection', this.solaceApi, this.appProps);
        let solace = this.solaceApi;
        var factoryProps = new solace.SolclientFactoryProperties();
        factoryProps.profile = solace.SolclientFactoryProfiles.version10;
        solace.SolclientFactory.init(factoryProps);
        // enable logging to JavaScript console at WARN level
        // NOTICE: works only with 'solclientjs-debug.js'
        solace.SolclientFactory.setLogLevel(solace.LogLevel.WARN);
        this.session = solace.SolclientFactory.createSession({
          url: this.appProps.url,
          vpnName: this.appProps.vpn,
          userName: this.appProps.username,
          password: this.appProps.password,
          clientName: this.clientId || ''
        });
        this.session.on(solace.SessionEventCode.UP_NOTICE, (sessionEvent) => {
          this.clientId = this.session.getSessionProperties().clientName;
          console.log('Successfully connected with clientId ' + this.clientId);
          this.subscribeToTopic('user/' + this.clientId);
          this.subscribeToTopic('score/' + this.clientId);
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
          console.log('Received message: "' + message.getBinaryAttachment() + '", details:\n' + message.dump());
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

  handleMessage(topic, message) {
      if (typeof message !== 'string') {  
        console.log('Error: unexpected message type');
        return;
      }

      try {
        var messageInstance = parseReceivedMessage(topic, message);
        if (messageInstance instanceof TeamsMessage) {
          // set team topic
          this.teamTopic = topic;
          this.gameTopic = 'games/' + messageInstance.teamId;
          this.subscribeToTopic('score/' + messageInstance.teamId);
        }
        if (messageInstance !== null) {
          this.msgCallback(messageInstance);
        
        }
      } catch (e) {
        console.log('Error:', e);
      }
  }

  register() {
    console.log('Connect player ' + this.username + ', clientId ' + this.clientId);
    /*
    publish request to players, clientId is optional. If clientId is present, the server should resume
    the user's game if it is active
    {
      clientId: 1,
      username: test
    }
    reply:
      {
        clientId: 1,
        username: test
      }
    subscribe to
    players/<clientId>
    players/<teamid>
    */

    var usersMessage = new UsersMessage(this.username, this.clientId);
    try {
      publishMessageToTopic('users', usersMessage, this.session, this.solaceApi);
    } catch (error) {
      console.log("Publish failed. error = ", error);
    }
  }

  // called by Game.vue destroy method
  unregister() {
    console.log('Let server know player ' + this.username + ', clientId ' + this.clientId + ' becomes inactive');
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

  pickAvatar(avatar) {
    console.log('Send message to request the selected avatar ' + avatar);

    // REMOVE TESTING CODE
    setTimeout(() => {
      this.msgCallback(this.simulatePickAvatarResponse(avatar));
    }, 0);
  }

  // when integrating with the server, make sure the attributes that are not
  // available in the server message are provided with hardcoded value.
  simulatePickAvatarResponse(avatar) {
    let msg = Object.assign(new TeamsMessage, {
      // players with avatar
      players: [
        {
          clientId: this.clientId,
          username: this.username,
          avatar: avatar
        },
        {
          clientId: '1',
          username: 'Kevin',
          avatar: 'peach'
        },
        {
          clientId: '2',
          username: 'Rob',
          avatar: 'yoshi'
        },
        {
          clientId: '5',
          username: 'Roland',
          avatar: 'bowser'
        },
        {
          clientId: '6',
          username: 'Bob',
          avatar: 'goomba'
        },
      ]
    });
    return msg;
  }

  swap(piece1, piece2, puzzle) {
    console.log('publish swap message to ' + this.gameTopic, piece1, piece2);

    // The response from server is not the whole puzzle, but echo back the request
    var swapMessage = new SwapMessage(piece1, piece2, this.clientName); 
    try {
      publishMessageToTopic(this.gameTopic, swapMessage, this.session, this.solaceApi);
    } catch (error) {
      console.log("Publish failed. error = ", error);
    }
  }

  starPower(puzzlePiece) {
    var starPowerMessage = new StarPowerMessage(puzzlePiece);
    try {
      publishMessageToTopic(this.gameTopic + "/starPower", starPowerMessage, this.session, this.solaceApi);
    } catch (error) {
      console.log("Publish failed. error = ", error);
    }
  }

  peachHeal(character) {
    var peachHealMessage = new PeachHealMessage(character);
    try {
      publishMessageToTopic(this.gameTopic + "/peachHeal", peachHealMessage, this.session, this.solaceApi);
    } catch (error) {
      console.log("Publish failed. error = ", error);
    }
  }

  troubleFlipper() {
    try {
      publishMessageToTopic(this.gameTopic + "/troubleFlipper", {}, this.session, this.solaceApi);
    } catch (error) {
      console.log("Publish failed. error = ", error);
    }
  }

  yoshiGuard() {
    try {
      publishMessageToTopic(this.gameTopic + "/yoshiGuard", {}, this.session, this.solaceApi);
    } catch (error) {
      console.log("Publish failed. error = ", error);
    }
  }

  greenShell() {
    try {
      publishMessageToTopic(this.gameTopic + "/greenShell", {}, this.session, this.solaceApi);
    } catch (error) {
      console.log("Publish failed. error = ", error);
    }
  }

  disconnect() {
    // session disconnect
    console.log('Disconect');
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
