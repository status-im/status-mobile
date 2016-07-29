(ns status-im.components.geth
  (:require [status-im.components.react :as r]))

(def geth
  (when (exists? (.-NativeModules r/react-native))
    (.-Geth (.-NativeModules r/react-native))))

(defn start-node [on-result on-already-running]
  (when geth
    (.startNode geth on-result on-already-running)))

(defn create-account [password on-result]
  (when geth
    (.createAccount geth password on-result)))

(defn login [address password on-result]
  (when geth
    (.login geth address password on-result)))