import { UsersMessage, UsersAckMessage, TournamentsMessage, TeamsMessage, publishMessageToTopic, parseReceivedMessage, TeamRankMessage, PlayerRankMessage } from '@/messaging/messages.js';
import { SwapMessage,  PickCharacterMessage } from '@/messaging/messages.js';
import { StarPowerMessage, PeachHealMessage } from './messages';
export class Rankings {
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
          this.subscribeToTopic('score/>');
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
          // if (topicName === 'user/' + this.clientId) {
          //   this.register();
          // }
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
        }
        if (messageInstance !== null) {
          this.msgCallback(messageInstance);
        }
      } catch (e) {
        console.log('Error:', e);
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
