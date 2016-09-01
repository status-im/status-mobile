(ns status-im.components.geth
  (:require [status-im.components.react :as r]
            [re-frame.core :refer [dispatch]]))

(def geth
  (when (exists? (.-NativeModules r/react-native))
    (.-Geth (.-NativeModules r/react-native))))

(.addListener r/device-event-emitter "gethEvent"
              #(dispatch [:signal-event (.-jsonEvent %)]))

(defn start-node [on-result on-already-running]
  (when geth
    (.startNode geth on-result on-already-running)))

(defn create-account [password on-result]
  (when geth
    (.createAccount geth password on-result)))

(defn recover-account [passphrase password on-result]
  (when geth
    (.recoverAccount geth passphrase password on-result)))

(defn login [address password on-result]
  (when geth
    (.login geth address password on-result)))

(defn complete-transaction
  [hash password callback]
  (when geth
    (.completeTransaction geth hash password callback)))
