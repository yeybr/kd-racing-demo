import Vue from 'vue';

export default {

  // any actions
  methods: {
    updateData: function(oldData, newData) {
      // console.log('updateData', oldData, newData, this);
      // set any attribute that's not in newData to undefined
      Object.keys(oldData).forEach(key => {
        if (Array.isArray(oldData[key])) {
          if (newData && Array.isArray(newData[key])) {
            this.updateArray(oldData[key], newData[key]);
          } else {
            // oldData[key] is array, newData[key] is not. Should avoid doing this to confuse Vue reactive system.
            // Delete all the elements in the old array
            console.log('updateData set array attribute ' + key + ' to empty array');
            oldData[key].splice(0, oldData[key].length);
          }
        } else if (oldData[key] && typeof oldData[key] === 'object') {
          if (newData && newData[key] && typeof newData[key] === 'object') {
            this.updateData(oldData[key], newData[key]);
          } else {
            // oldData[key] is object, newData[key] is not object, could be primitive, null, undefined.
            // Should avoid doing this to confuse Vue reactive system.
            // Set all oldData's attributes to undefined
            console.log('updateData set attribute ' + key + ' to undefined');
            this.updateData(oldData[key], undefined);
          }
        } else {
          // primitive type, if key is not in newData, oldData[key] will become undefined
          oldData[key] = newData ? newData[key] : undefined;
        }
      });
      // merge new attributes in newData into oldData using shallow copy
      if (newData) {
        Object.keys(newData).forEach(key => {
          if (oldData[key] === undefined) {
            console.log('updateData add new attribute ' + key + ', value', newData[key]);
            // Vue specific way to add new reactive property to an existing object
            Vue.set(oldData, key, newData[key]);
          }
        });
      }
      // console.log('data after merge', oldData);
    },
    updateArray: function(oldArray, newArray) {
      // console.log('updateArray', this);
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
