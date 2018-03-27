export class Player {
  constructor(solaceApi, appProps, username, msgCallback, stateChangeCallback) {
    this.solaceApi = solaceApi;
    this.appProps = appProps;
    this.username = username;
    // upon receiving a message, call this callback with json content, so that UI can get updated
    this.msgCallback = msgCallback;
    // only call this callback to update UI if this class is up or down, handling real solace session event should be done
    // inside this class
    this.stateChangeCallback = stateChangeCallback;
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
          console.log('=== Successfully connected and ready to publish messages. ===');
          this.register();
        });
        this.session.on(solace.SessionEventCode.CONNECT_FAILED_ERROR, (sessionEvent) => {
          console.log('Connection failed to the message router: ' + sessionEvent.infoStr +
            ' - check correct parameter values and connectivity!');
          this.stateChangeCallback({state: 'connecting'});
        });
        this.session.on(solace.SessionEventCode.DISCONNECTED, (sessionEvent) => {
          console.log('Disconnected: ' + sessionEvent.infoStr);
          this.stateChangeCallback({state: 'connecting'});
          if (this.session !== null) {
            this.session.dispose();
            this.session = null;
          }

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

  register() {
    console.log('register player ' + this.username + ' to a game team');
    /*
    publish request to players
	    username: test
    reply:
      {
        userId: 1,
        username: test
      }
    subscribe to
    players/<userId>
    players/<teamid>
    */
   // TESTING CODE
   this.stateChangeCallback({state: 'waiting'});
   setTimeout(()=> {
    let msg = {
      state: 'playing',
      userId: '1',
      userName: this.username,
      gameInfo: {
        gameId: '1',
        gameName: 'Mario & Yoshi',
        teamId: '1',
        teamName: 'Team 1',
        win: false,
        players: [
          {
            id: 1,
            name: 'Kevin'
          },
          {
            id: 2,
            name: 'Rob'
          }
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
   }, 2000);
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
      this.stateChangeCallback({state: 'connecting'});
    }
  }
}
