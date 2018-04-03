<script>
export default {

  // any actions
  methods: {
    updateData: function(oldData, newData) {
      // console.log(oldData, newData);
      // set any attribute that's not in newData to undefined
      Object.keys(oldData).forEach(key => {
        if (Array.isArray(oldData[key])) {
          if (Array.isArray(newData[key])) {
            this.updateArray(oldData[key], newData[key]);
          } else {
            oldData[key] = newData[key];
          }
        } else if (oldData[key] && typeof oldData[key] === 'object') {
          if (newData[key] && typeof newData[key] === 'object') {
            this.updateData(oldData[key], newData[key]);
          } else {
            oldData[key] = newData[key];
          }
        } else {
          oldData[key] = newData[key];
        }
      });
      // merge new attributes in newData into oldData using shallow copy
      Object.keys(newData).forEach(key => {
        if (oldData[key] === undefined) {
          oldData[key] = newData[key];
        }
      });
      // console.log('data after merge', oldData);
    },
    updateArray: function(oldArray, newArray) {
      let resultLen = newArray ? newArray.length : 0;
      let currLen = oldArray ? oldArray.length : 0;
      for (let i = 0; i < Math.min(currLen, resultLen); i++) {
        if (typeof oldArray[i] !== 'object') {
          // primitive type
          // Vue reactivity will lose if using oldArray[i] = newArray[i], workaround
          oldArray.splice(i, 1, newArray[i]);
        } else {
          this.updateData(oldArray[i], newArray[i]);
        }
      }
      if (currLen < resultLen) {
        for (let i = currLen; i < resultLen; i++) {
          oldArray.push(newArray[i]);
        }
      } else if (currLen > resultLen) {
        oldArray.splice(resultLen, (currLen - resultLen));
      }
    },
    getStorage(type) {
      if (type === 'localStorage') {
        if ('localStorage' in window && window.localStorage !== null) return localStorage;
        throw new Error('Local Storage is disabled or unavailable.');
      } else if (type === 'sessionStorage') {
        if ('sessionStorage' in window && window.sessionStorage !== null) return sessionStorage;
        throw new Error('Session Storage is disabled or unavailable.');
      }

      throw new Error('Invalid storage type specified: ' + type);
    },
    saveIntoStorage: function(storageType, key, value) {
      let storage = null;
      try {
        storage = this.getStorage(storageType);
        if (key && value) {
          console.log('saveIntoStorage', key, value);
          storage.setItem(key, JSON.stringify(value));
        }
      } catch (e) {
        console.log('Error occurred while saving to ' + storageType, e);
      }
    },
    retrieveFromStorage: function(storageType, key) {
      let storage = null;
      try {
        storage = this.getStorage(storageType);
        let itemValue = storage.getItem(key);
        if (itemValue !== undefined) {
          console.log('retrieveFromStorage', key, itemValue);
          return JSON.parse(itemValue);
        }
      } catch (e) {
        console.log('Error occurred while loading from ' + storageType, e);
      }
      return null;
    },
  }
}
</script>
