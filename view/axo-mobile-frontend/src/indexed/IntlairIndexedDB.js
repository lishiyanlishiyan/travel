export default {
  // indexedDB兼容
  indexedDB: window.indexedDB || window.webkitindexedDB || window.msIndexedDB || window.mozIndexedDB,
  // 打开数据库
  // 新对象储存空间newStore参数：newStore.name、newStore.key
  // 新增对象存储空间要更改数据库版本
  openDB: function (dbname, version, db, newStore, callback) {
    version = version || 1
    const request = this.indexedDB.open(dbname, version)
    console.log(request)
    request.onerror = function (event) {
      console.log('IndexedDB数据库打开错误')
    }
    request.onsuccess = function (event) {
      console.log('IndexedDB数据库打开成功')
      db = event.target.result
      if (callback && (typeof callback === 'function')) {
        callback(db)
      }
    }
    // onupgradeneeded，调用创建新的储存空间
    request.onupgradeneeded = function (event) {
      const db = event.target.result
      if (newStore) {
        if (!db.objectStoreNames.contains(newStore.name)) {
          db.createObjectStore(newStore.name, { autoIncrement: true })
          // objectStore.createIndex('counter_index', 'counter', { unique: false }) 创建索引
        }
      }
    }
  },
  // 删除数据库
  deleteDB: function (db, callback) {
    const deleteQuest = this.indexedDB.deleteDatabase(db)
    deleteQuest.onerror = function () {
      console.log('删除数据库出错')
    }
    deleteQuest.onsuccess = function () {
      if (callback && (typeof callback === 'function')) {
        callback()
      }
    }
  },
  // 关闭数据库
  closeDB: function (db) {
    db.close()
    console.log('数据库已关闭')
  },
  // 添加Data
  addData: function (db, storeName, data) {
    const transaction = db.transaction(storeName, 'readwrite')
    const store = transaction.objectStore(storeName)
    const request = store.add(data)
    request.onsuccess = function () {
      console.log('缓存机票查询结果成功')
    }
  },
  // 清空store
  clearStore: function (db, storeName) {
    const transaction = db.transaction(storeName, 'readwrite')
    const store = transaction.objectStore(storeName)
    const request = store.clear()
    request.onsuccess = function () {
      console.log('清空机票查询结果成功')
    }
  },
  // 获取Data
  getData: function (db, storeName, callback) {
    const transaction = db.transaction(storeName, 'readwrite')
    const store = transaction.objectStore(storeName)
    const request = store.getAll()
    request.onsuccess = (e) => {
      if (e.target.result) {
        if (callback && (typeof callback === 'function')) {
          callback(e.target.result[0])
        }
      }
    }
  }
}
