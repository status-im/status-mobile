(ns messenger.state
  (:require [om.next :as om]
            [re-natal.support :as sup]))

(set! js/React (js/require "react-native"))

(defonce app-state (atom {:component nil
                          :user-phone-number nil
                          :user-whisper-identity nil}))
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
