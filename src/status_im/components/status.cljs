(ns status-im.components.status
  (:require-macros [status-im.utils.slurp :refer [slurp]])
  (:require [status-im.components.react :as r]
            [status-im.utils.types :as t]
            [re-frame.core :refer [dispatch]]
            [taoensso.timbre :as log]))

(def status-js (slurp "resources/status.js"))

(def status
  (when (exists? (.-NativeModules r/react-native))
    (.-Status (.-NativeModules r/react-native))))

(when status
  (.initJail status status-js #(log/debug "jail initialized")))

(.addListener r/device-event-emitter "gethEvent"
              #(dispatch [:signal-event (.-jsonEvent %)]))

(defn start-node [on-result]
  (when status
    (.startNode status on-result)))

(defn create-account [password on-result]
  (when status
    (.createAccount status password on-result)))

(defn recover-account [passphrase password on-result]
  (when status
    (.recoverAccount status passphrase password on-result)))

(defn login [address password on-result]
  (when status
    (.login status address password on-result)))

(defn complete-transaction
  [hash password callback]
  (when status
    (.completeTransaction status hash password callback)))

(defn parse-jail [chat-id file callback]
  (when status
    (.parseJail status chat-id file callback)))

(defn cljs->json [data]
  (.stringify js/JSON (clj->js data)))

(defn call-jail [chat-id path params callback]
  (when status
    (println :call chat-id (cljs->json path) (cljs->json params))
    (let [cb (fn [r]
               (let [r' (t/json->clj r)]
                 (println r')
                 (callback r')))]
      (.callJail status chat-id (cljs->json path) (cljs->json params) cb))))

(defn set-soft-input-mode [mode]
  (when status
    (.setSoftInputMode status mode)))

(def adjust-resize 16)
(def adjust-pan 32)
