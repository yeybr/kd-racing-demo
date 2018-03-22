export class Player {
  constructor(solace, appProps, msgCallback, stateChangeCallback) {
    this.solace = solace;
    this.appProps = appProps;
    // upon receiving a message, call this callback with json content, so that UI can get updated
    this.msgCallback = msgCallback;
    // only call this callback to update UI if this class is up or down, handling real solace session event should be done
    // inside this class
    this.stateChangeCallback = stateChangeCallback;
    this.session = null;
  }

  join() {
    // create session, publisher, subscriber
    try {
      if (!this.session) {
        console.log("Creating the connection", this.solace, this.appProps);
        var factoryProps = new this.solace.SolclientFactoryProperties();
        factoryProps.profile = this.solace.SolclientFactoryProfiles.version10;
        this.solace.SolclientFactory.init(factoryProps);
        // enable logging to JavaScript console at WARN level
        // NOTICE: works only with "solclientjs-debug.js"
        this.solace.SolclientFactory.setLogLevel(solace.LogLevel.WARN);
        this.session = this.solace.SolclientFactory.createSession({
          url: this.appProps.url,
          vpnName: this.appProps.vpn,
          userName: this.appProps.username,
          password: this.appProps.password
        });
        this.session.connect();
      }
    } catch (e) {
      console.log('Join fails', e);
      if (this.session) {
        this.leave();
      }
    }
  }

  leave() {
    // session disconnect
    console.log("Disconect");
    try {
      if (this.session) {
        this.session.disconnect();
        this.session = null;
      }
    } catch (e) {
      console.log('Leave fails', e);
    }
  }
}
