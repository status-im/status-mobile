(ns messenger.models.user-data
  (:require-macros
   [natal-shell.async-storage :refer [get-item set-item]])
  (:require [cljs.core.async :as async :refer [chan put! <! >!]]
            [syng-im.protocol.web3 :as web3]
            [messenger.state :as state]
            [messenger.utils.utils :refer [log on-error toast]]))

(def ethereum-rpc-url "http://localhost:8545")

(defn set-phone-number [phone-number]
  (swap! state/app-state assoc :user-phone-number phone-number))

(defn save-phone-number [phone-number]
  (set-item "user-phone-number" phone-number)
  (swap! state/app-state assoc :user-phone-number phone-number))

(defn load-phone-number []
  (get-item "user-phone-number"
            (fn [error value]
              (if error
                (on-error error)
                (swap! state/app-state assoc :user-phone-number (when value
                                                                  (str value)))))))

(defn save-whisper-identity [identity]
  (set-item "user-whisper-identity" identity)
  (swap! state/app-state assoc :user-whisper-identity identity))

(defn load-whisper-identity []
  (let [ch (chan)]
    (get-item "user-whisper-identity"
              (fn [error value]
                (log (str "load whisper identity: " value))
                (put! ch
                      {:error error
                       :value (let [whisper-identity (when value (str value))]
                                (swap! state/app-state assoc :user-whisper-identity
                                       whisper-identity)
                                whisper-identity)})))
    ch))

(defn new-whisper-identity []
  (let [ch (chan)]
    (let [web3 (web3/make-web3 ethereum-rpc-url)]
      (.newIdentity (web3/whisper web3)
                    (fn [error value]
                      (log (str "new whisper identity: " value))
                      (swap! state/app-state assoc :user-whisper-identity value)
                      (put! ch {:error error
                                :value value}))))
    ch))

(defn set-confirmation-code [code]
  (swap! state/app-state assoc :confirmation-code code))
