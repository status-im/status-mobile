(ns messenger.omnext
  (:require [om.next :as om]
            [syng-im.utils.logging :as log]
            [re-natal.support :as sup]
            [messenger.models.messages :as msgs]
            [messenger.models.chat :as chat]
            [messenger.state :as state]
            [messenger.components.iname :as in]))

(defmulti read om/dispatch)

(defmethod read :default [{:keys [state] :as env} key param]
  (log/debug "reading" "key=" key "param=" param)
  (let [st @state]
    (if-let [[_ v] (find st key)]
      {:value v}
      {:value :not-found})))

(defmethod read :chat/chat [env key param]
  (log/debug "reading" "key=" key "param=" param)
  (let [chat-id (chat/current-chat-id)
        val     {:value {:chat/messages (msgs/get-messages chat-id)
                         :chat/chat-id  chat-id}}
        _       (log/debug "returning" val)]
    val))

(defmethod read :chat/messages [env key param]
  (log/debug "reading" "key=" key "param=" param)
  (let [chat-id (chat/current-chat-id)
        val     {:value {:chat/messages (msgs/get-messages chat-id)
                         :chat/chat-id  chat-id}}
        _       (log/debug "returning" val)]
    val))

(defmethod read :contacts/contacts [env key param]
  (log/debug "reading" "key=" key "param=" param)
  (let [val {:value {:contacts-ds (get-in @state/app-state [:contacts-ds])}}
        _   (log/debug "returning" val)]
    val))

(defmethod read :login/login [env key param]
  (log/debug "reading" "key=" key "param=" param)
  (let [val {:value (select-keys @state/app-state [:user-phone-number :user-identity :loading])}
        _   (log/debug "returning" val)]
    val))

(defmethod read :signup/confirm [env key param]
  (log/debug "reading" "key=" key "param=" param)
  (let [val {:value (select-keys @state/app-state [:confirmation-code :loading])}
        _   (log/debug "returning" val)]
    val))

(defmulti mutate om/dispatch)

(defmethod mutate 'chat/add-msg-to-chat [{:keys [state] :as env} key {:keys [chat-id msg] :as param}]
  (log/debug "writing" "key=" key "param=" param)
  {:action #(do
             (log/debug "Writing msg to db")
             (msgs/save-message chat-id msg)
             (swap! state/app-state assoc-in [:chat :messages] msg))})

(defonce reconciler
  (om/reconciler
    {:state        state/app-state
     :parser       (om/parser {:read   read
                               :mutate mutate})
     :root-render  sup/root-render
     :root-unmount sup/root-unmount}))


(defn set-root-query [component]
  (let [app-root (om/class->any reconciler (om/app-root reconciler))]
    (om/set-query! app-root {:query [{(in/get-name component) (om/get-query component)}]})
    (om.next.protocols/reindex! reconciler)))