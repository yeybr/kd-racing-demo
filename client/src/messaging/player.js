import { UsersMessage, UsersAckMessage, publishMessageToTopic } from '@/messaging/messages.js';

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
          this.register();
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

    // REMOVE TESTING CODE
    setTimeout(()=> {
      let msg = new UsersAckMessage(UsersAckMessage.SUCCESS, this.username, this.clientId);
      this.msgCallback(msg);
    }, 500);

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
    console.log('Let server know player ' + this.username + ', clientId ' + this.clientId + ' becomes inactive');
  }

  startGame() {
    console.log('Send message to request to start game');

    // REMOVE TESTING CODE
    // current team message only contain puzzle pieces, should also return teamId; otherwise don't know where to send swap message
    var size = 5;
    var pieces = [];
    for (var i = 0; i < size * size; ++i) {
      pieces.push({
        index: i
      });
    }
    console.log("pieces done");
    this.shuffle(pieces);
    console.log("shuffle");
    let msg = {
      puzzle: pieces
    };
    this.msgCallback(msg);
  }

  shuffle(array) {
    var currentIndex = array.length,
      temporaryValue,
      randomIndex;

    // While there remain elements to shuffle...
    while (0 !== currentIndex) {
      // Pick a remaining element...
      randomIndex = Math.floor(Math.random() * currentIndex);
      currentIndex -= 1;

      // And swap it with the current element.
      temporaryValue = array[currentIndex];
      array[currentIndex] = array[randomIndex];
      array[randomIndex] = temporaryValue;
    }

    return array;
  }

  pickAvatar(avatar) {
    console.log('Send message to request the selected avatar ' + avatar);

    // REMOVE TESTING CODE
    // fake team message to hardcode the rest of the properties
    let msg = {
      teamInfo: {
        teamId: '1',
        teamName: 'Team 1',
        puzzleName: 'puzzle3',
        timeAllowedForEachMove: 10,
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
            avatar: 'toad',
          },
          {
            clientId: '6',
            username: 'Bob',
            avatar: 'goomba',
          },
        ],
        rank: {
          personal: 1,
          team: 1,
          totalTeam: 5
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
