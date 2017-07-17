(ns status-im.protocol.web3.keys)

(defonce status-sym-key-id (atom nil))
(def status-key-password "status-key-password")
(def status-group-key-password "status-public-group-key-password")

(defonce password->keys (atom {}))

(defn- add-sym-key-from-password
  [web3 password callback]
  (.. web3
      -shh
      (addSymmetricKeyFromPassword password callback)))

(defn get-sym-key [web3 password callback]
  (if-let [key-id (get @password->keys password)]
    (callback key-id)
    (add-sym-key-from-password
      web3 password
      (fn [err res]
        (swap! password->keys assoc password res)
        (callback res)))))


(defn get-status-sym-key
  [web3 callback]
  (get-sym-key web3 status-key-password callback))
