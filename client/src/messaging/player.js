import { UsersMessage, UsersAckMessage, publishMessageToTopic, parseReceivedMessage } from '@/messaging/messages.js';

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
          //this.register();
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
        console.log('Subscribe failed.');
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
          console.log('here');
          this.msgCallback(messageInstance);
        }
      } catch (e) {
        console.log('Error: ' + e);
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
      console.log("Publish failed. error = " + error);
    }
  }

  // called by Game.vue destroy method
  unregister() {
    console.log('Let server know player ' + this.username + ', clientId ' + this.clientId + ' becomes inactive');
  }

  startGame() {
    console.log('Send message to request to start game');

    // REMOVE TESTING CODE
    setTimeout(()=> {
      this.msgCallback(this.simulateStartGameResponse());
    }, 0);
  }

  // TODO REMOVE TESTING CODE
  simulateStartGameResponse() {
    var size = 5;
    var pieces = [];
    for (var i = 0; i < size * size; ++i) {
      pieces.push({
        index: i
      });
    }
    this.shuffle(pieces);
    let msg = {
      // pieces are from server message
      puzzle: pieces,
      // hardcode teamInfo without avatar so that the GUI can transition to pick avatar view
      teamInfo: {
        // teamId can be from message destination string or server message
        teamId: '1',
        teamName: 'Team 1',
        puzzleName: 'puzzle3',
        timeAllowedForEachMove: 0,
        players: [
          {
            clientId: this.clientId,
            username: this.username
          },
          {
            clientId: '1',
            username: 'Kevin'
          },
          {
            clientId: '2',
            username: 'Rob'
          },
          {
            clientId: '5',
            username: 'Roland'
          },
          {
            clientId: '6',
            username: 'Bob'
          },
        ],
        rank: {
          personal: 1,
          team: 1,
          totalTeam: 5
        }
      }
    };
    return msg;
  }

  // TODO REMOVE TESTING CODE
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
    setTimeout(() => {
      this.msgCallback(this.simulatePickAvatarResponse(avatar));
    }, 0);
  }

  // when integrating with the server, make sure the attributes that are not
  // available in the server message are provided with hardcoded value.
  simulatePickAvatarResponse(avatar) {
    let msg = {
      // hardcode teamInfo with avatar
      teamInfo: {
        // teamId can be from message destination string or server message
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
    return msg;
  }

  swap(piece1, piece2, puzzle) {
    console.log('publish swap message to server', piece1, piece2);

    // TESTING CODE
    setTimeout(() => {
      this.msgCallback(this.simulateSwapResponse(piece1, piece2, puzzle));
    }, 0);
  }

  simulateSwapResponse(piece1, piece2, puzzle) {
    // console.log(piece1, piece2, puzzle);
    let piece1Index = puzzle.findIndex(p => {
      return p.index == piece1.index;
    });
    let piece2Index = puzzle.findIndex(p => {
      return p.index == piece2.index;
    });
    var temp = puzzle[piece1Index];
    puzzle[piece1Index] = puzzle[piece2Index];
    puzzle[piece2Index] = temp;
    return {
      puzzle: puzzle
    };
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
