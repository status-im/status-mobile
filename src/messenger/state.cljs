(ns messenger.state
  (:require [cljs.core.async :as async :refer [chan pub sub]]
            [om.next :as om]
            [re-natal.support :as sup]))

(def ^{:dynamic true :private true} *nav-render*
  "Flag to suppress navigator re-renders from outside om when pushing/popping."
  true)

(set! js/React (js/require "react-native"))

(defonce app-state (atom {:component             nil
                          :loading               false
                          :user-phone-number     nil
                          :user-identity         nil
                          :confirmation-code     nil
                          :identity-password     "replace-me-with-user-entered-password"
                          :channels              {:pub-sub-publisher   (chan)
                                                  :pub-sub-publication nil}}))

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
(def protocol-initialized-path [:protocol-initialized])
(def simple-store-path [:simple-store])
(def identity-password-path [:identity-password])

(defn pub-sub-publisher [app] (get-in app pub-sub-bus-path))
(defn kv-store []
  (get-in @app-state simple-store-path))
