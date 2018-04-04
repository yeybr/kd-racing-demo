import { UsersMessage, publishMessageToTopic } from '@/messaging/messages.js';

export class Player {
  constructor(solaceApi, appProps, userInfo, msgCallback) {
    this.solaceApi = solaceApi;
    this.appProps = appProps;
    this.username = userInfo.username;
    this.client = userInfo.client;
    // upon receiving a message, call this callback with json content, so that UI can get updated
    this.msgCallback = msgCallback;
    this.session = null;
  }

  connect() {
    // create session, publisher, subscriber
    try {
      if (!this.session) {
        console.log('Creating the connection', this.solaceApi, this.appProps);
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
          password: this.appProps.password
        });
        this.session.on(solace.SessionEventCode.UP_NOTICE, (sessionEvent) => {
          console.log('Successfully connected and ready to send and receive messages.');
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
        this.session.on(solace.SessionEventCode.MESSAGE, (message) => {
          console.log('Received message: "' + message.getBinaryAttachment() + '", details:\n' + message.dump());
          this.handleMessage(message.getBinaryAttachment());
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

  handleMessage(jsonMessage) {
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

  register() {
    console.log('Connect player ' + this.username + ', client ' + this.client);
    /*
    publish request to players, client is optional. If client is present, the server should resume
    the user's game if it is active
    {
      client: 1,
      username: test
    }
    reply:
      {
        client: 1,
        username: test
      }
    subscribe to
    players/<client>
    players/<teamid>
    */

    // REMOVE TESTING CODE
    setTimeout(()=> {
      let msg = {
        state: 'waiting',
        client: '1',
        username: this.username,
      };
      this.msgCallback(msg);
    }, 1000);

    // TODO (Brandon):
    //
    // * Where the heck do you get the clientId??... hard code for now...
    //
    /*
    var usersMessage = new UsersMessage(this.username, '1');
    try {
      publishMessageToTopic('users', usersMessage, this.session, this.solaceApi);
    } catch (error) {
      console.log("Publish failed.");
    }
    // Mocked server response... remove later
    setTimeout(() => {
      var usersAckMessage = new UsersAckMessage(UsersAckMessage.SUCCESS);
      this.msgCallback(usersAckMessage);
    }, 2000);
    */
  }

  // called by Game.vue destroy method
  unregister() {
    console.log('Let server know player ' + this.username + ', client ' + this.client + ' becomes inactive');
  }

  startGame() {
    console.log('Send message to request to start game');

    // REMOVE TESTING CODE
    let msg = {
      state: 'start',
      client: '1',
      username: this.username,
      gameInfo: {
        gameId: '1',
        gameName: 'Mario',
        teamId: '1',
        teamName: 'Team 1',
        win: false,
        players: [
          {
            id: '1',
            name: 'Kevin',
            avatar: 'peach'
          },
          {
            id: '2',
            name: 'Rob',
            avatar: 'yoshi'
          },
          {
            id: '5',
            name: 'Roland',
            avatar: 'toad',
          },
          {
            id: '5',
            name: 'Bob',
            avatar: 'goomba',
          },
        ],
        stats: {
          total: 16,
          finished: 0,
          totalMoves: 0,
          correctMoves: 0
        }
      }
    };
    this.msgCallback(msg);
  }

  pickAvatar(avatar) {
    console.log('Send message to request the selected avatar ' + avatar);

    // REMOVE TESTING CODE
    let msg = {
      state: 'playing',
      client: '1',
      username: this.username,
      gameInfo: {
        gameId: '1',
        gameName: 'Mario',
        teamId: '1',
        teamName: 'Team 1',
        avatar: avatar,
        win: false,
        players: [
          {
            id: '1',
            name: 'Kevin',
            avatar: 'peach'
          },
          {
            id: '2',
            name: 'Rob',
            avatar: 'yoshi'
          },
          {
            id: '5',
            name: 'Roland',
            avatar: 'toad',
          },
          {
            id: '5',
            name: 'Bob',
            avatar: 'goomba',
          },
        ],
        stats: {
          total: 16,
          finished: 0,
          totalMoves: 0,
          correctMoves: 0
        }
      }
    };
    this.msgCallback(msg);
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
