(ns messenger.state
  (:require [cljs.core.async :as async :refer [chan pub sub]]
            [om.next :as om]
            [re-natal.support :as sup]))

(set! js/React (js/require "react-native"))

(defonce app-state (atom {:component             nil
                          :user-phone-number     nil
                          :user-whisper-identity nil
                          :confirmation-code     nil
                          :channels              {:pub-sub-publisher   (chan)
                                                  :pub-sub-publication nil}}))
(def ^{:dynamic true :private true} *nav-render*
  "Flag to suppress navigator re-renders from outside om when pushing/popping."
  true)

(defmulti read om/dispatch)
(defmethod read :default
  [{:keys [state]} k _]
  (let [st @state]
    (if-let [[_ v] (find st k)]
      {:value v}
      {:value :not-found})))

(defonce reconciler
         (om/reconciler
           {:state        app-state
            :parser       (om/parser {:read read})
            :root-render  sup/root-render
            :root-unmount sup/root-unmount}))


(defn state [] @app-state)

(def pub-sub-bus-path [:channels :pub-sub-publisher])
(def pub-sub-path [:channels :pub-sub-publication])
(def user-notification-path [:user-notification])

(defn pub-sub-publisher [app] (get-in app pub-sub-bus-path))