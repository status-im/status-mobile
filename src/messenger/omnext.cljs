(ns messenger.omnext
  (:require [om.next :as om]
            [syng-im.utils.logging :as log]
            [re-natal.support :as sup]
            [messenger.models.messages :as msgs]
            [messenger.models.chat :as chat]
            [messenger.state :as state]))

(defmulti read om/dispatch)

(defmethod read :default [{:keys [state] :as env} key param]
  (log/debug "reading" "key=" key "param=" param)
  (let [st @state]
    (if-let [[_ v] (find st key)]
      {:value v}
      {:value :not-found})))

(defmethod read :chat [env key param]
  (log/debug "reading" "key=" key "param=" param)
  (let [chat-id (chat/current-chat-id)
        val     {:value {:chat/messages (msgs/get-messages chat-id)}}]
    val))

(defonce reconciler
  (om/reconciler
    {:state        state/app-state
     :parser       (om/parser {:read read})
     :root-render  sup/root-render
     :root-unmount sup/root-unmount}))
