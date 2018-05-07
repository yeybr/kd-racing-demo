import { parseReceivedMessage } from '@/messaging/messages.js';

export class Rankings {
  constructor(solaceApi, appProps, userInfo, msgCallback, logger) {
    this.solaceApi = solaceApi;
    this.appProps = appProps;
    this.username = userInfo.username;
    this.clientId = userInfo.clientId;
    // upon receiving a message, call this callback with json content, so that UI can get updated
    this.msgCallback = msgCallback;
    this.session = null;
    this.logger = logger || new solace.ConsoleLogImpl();
  }

  connect() {
    // create session, publisher, subscriber
    try {
      if (!this.session) {
        // console.log('Creating the connection', this.solaceApi, this.appProps);
        let solace = this.solaceApi;
        var factoryProps = new solace.SolclientFactoryProperties();
        factoryProps.profile = solace.SolclientFactoryProfiles.version7;
        if (this.logger) {
          factoryProps.logger =  this.logger;
        }
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
          this.logger.info('Successfully connected with clientId ' + this.clientId +
            ', protocol in use ' + sessionProperties.transportProtocolInUse);
          this.subscribeToTopic('test/>');
        });
        this.session.on(solace.SessionEventCode.CONNECT_FAILED_ERROR, (sessionEvent) => {
          this.logger.error('Connection failed to the message router: ' + sessionEvent.infoStr +
            ' - check correct parameter values and connectivity!', sessionEvent);
          this.msgCallback({connected: false});
        });
        this.session.on(solace.SessionEventCode.DISCONNECTED, (sessionEvent) => {
          this.logger.info('Disconnected: ' + sessionEvent.infoStr, sessionEvent);
          this.msgCallback({connected: false});
          if (this.session !== null) {
            this.session.dispose();
            this.session = null;
          }
        });
        this.session.on(solace.SessionEventCode.SUBSCRIPTION_OK, (sessionEvent) => {
          var topicName = sessionEvent.correlationKey;
          this.logger.info('Successfully subscribed to topic: ' + topicName);
        });
        this.session.on(solace.SessionEventCode.MESSAGE, (message) => {
          // console.log('Received message: "' + message.getBinaryAttachment() + '", details:\n' + message.dump());
          var topic = message.getDestination().getName();
          this.handleMessage(topic, message.getBinaryAttachment());
        });
        this.session.connect();
        this.logger.info('Connecting');
      }
    } catch (e) {
      this.logger.error('Connect fails', e);
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
      this.msgCallback({connected: false});
    }
  }

}

// this should be bound to the route initiazling the the logger
export class TextareaLogger {
  constructor(parent) {
    this.parent = parent;
  }

  updateLogs(args) {
    if (this.parent && this.parent.logs !== undefined && this.parent.logs !== null) {
      if (this.parent.logs !== "") {
        this.parent.logs += "\n";
      }
      args.forEach(arg => {
        this.parent.logs += arg.toString();
      });
    }
  }

  generateArgs(args) {
    let temp = [];
    for (let i = 0; i < args.length; i++) {
      temp[i] = args[i];
    }
    return temp;
  }

  trace() {
    try {
      let args = ['TRACE: '].concat(this.generateArgs(arguments));
      this.updateLogs(args);
    } catch (e) {
      console.log('logging exception', e);
    }
  }

  debug() {
    try {
      let args = ['DEBUG: '].concat(this.generateArgs(arguments));
      this.updateLogs(args);
    } catch (e) {
      console.log('logging exception', e);
    }
  }

  info() {
    try {
      let args = ['INFO: '].concat(this.generateArgs(arguments));
      this.updateLogs(args);
    } catch (e) {
      console.log('logging exception', e);
    }
  }

  warn() {
    try {
      let args = ['WARN: '].concat(this.generateArgs(arguments));
      this.updateLogs(args);
    } catch (e) {
      console.log('logging exception', e);
    }
  }

  error() {
    try {
      let args = ['ERROR: '].concat(this.generateArgs(arguments));
      this.updateLogs(args);
    } catch (e) {
      console.log('logging exception', e);
    }
  }

  fatal() {
    try {
      let args = ['FATAL: '].concat(this.generateArgs(arguments));
      this.updateLogs(args);
    } catch (e) {
      console.log('logging exception', e);
    }
  }
}
