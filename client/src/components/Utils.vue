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
        this.updateData(oldArray[i], newArray[i]);
      }
      if (currLen < resultLen) {
        for (let i = currLen; i < resultLen; i++) {
          oldArray.push(newArray[i]);
        }
      } else if (currLen > resultLen) {
        oldArray.splice(resultLen, (currLen - resultLen));
      }
    }
  }
}
</script>
