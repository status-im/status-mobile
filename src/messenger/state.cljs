(ns messenger.state
  (:require [cljs.core.async :as async :refer [chan pub sub]]
            [re-natal.support :as sup]
            [syng-im.utils.logging :as log]))

(def ^{:dynamic true :private true} *nav-render*
  "Flag to suppress navigator re-renders from outside om when pushing/popping."
  true)

(set! js/React (js/require "react-native"))

(defonce app-state (atom {:component         nil
                          :loading           false
                          :user-phone-number nil
                          :user-identity     nil
                          :confirmation-code nil
                          :chat              {:chat-id nil}
                          :identity-password "replace-me-with-user-entered-password"
                          :channels          {:pub-sub-publisher   (chan)
                                              :pub-sub-publication nil}}))
(defn state [] @app-state)

(def pub-sub-bus-path [:channels :pub-sub-publisher])
(def pub-sub-path [:channels :pub-sub-publication])
(def user-notification-path [:user-notification])
(def protocol-initialized-path [:protocol-initialized])
(def simple-store-path [:simple-store])
(def identity-password-path [:identity-password])
(def current-chat-id-path [:chat :current-chat-id])

(defn pub-sub-publisher [app] (get-in app pub-sub-bus-path))
(defn kv-store []
  (get-in @app-state simple-store-path))


(comment

  (use 'figwheel-sidecar.repl-api)
  (cljs-repl)

  (defn read
    [{:keys [state] :as env} key params]
    (let [st @state]
      (if-let [[_ v] (find st key)]
        {:value v}
        {:value :not-found})))

  (def my-parser (om/parser {:read read}))

  (def my-state (atom {:count 0 :title "what"}))
  (my-parser {:state my-state} [:count :title])

  (defn mutate
    [{:keys [state] :as env} key params]
    (if (= 'increment key)
      {:value  {:keys [:count]}
       :action #(swap! state update-in [:count] inc)}
      {:value :not-found}))

  (def my-parser (om/parser {:read read :mutate mutate}))
  (my-parser {:state my-state} '[(increment)])
  @my-state

  )