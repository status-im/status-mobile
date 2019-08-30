(ns status-im.notifications.core
  (:require [goog.object :as object]
            [re-frame.core :as re-frame]
            [status-im.react-native.js-dependencies :as rn]
            [taoensso.timbre :as log]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.contact.db :as contact.db]
            [status-im.chat.models :as chat-model]
            [status-im.contact.db :as contact.db]
            [status-im.ethereum.core :as ethereum]
            [status-im.i18n :as i18n]
            [status-im.react-native.js-dependencies :as rn]
            [status-im.utils.fx :as fx]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

;; Work in progress namespace responsible for push notifications and interacting
;; with Firebase Cloud Messaging.

(def ^:private pn-message-id-hash-length 10)
(def ^:private pn-pubkey-hash-length 10)
(def ^:private pn-pubkey-length 132)
(def ^:private pull-recent-messages-window (* 15 60))

(when-not platform/desktop?

  (def firebase (object/get rn/react-native-firebase "default")))

;; NOTE: Only need to explicitly request permissions on iOS.
(defn request-permissions []
  (when-not platform/desktop?
    (-> (.requestPermission (.messaging firebase))
        (.then
         (fn [_]
           (log/debug "notifications-granted"))
         (fn [_]
           (log/debug "notifications-denied"))))))

(defn valid-notification-payload?
  [{:keys [from to]}]
  (and from to
       (or
        ;; is it full pubkey?
        (and (= (.-length from) pn-pubkey-length)
             (= (.-length to) pn-pubkey-length))
        ;; partially deanonymized
        (and (= (.-length from) pn-pubkey-hash-length)
             (= (.-length to) pn-pubkey-length))
        ;; or is it an anonymized pubkey hash (v2 payload)?
        (and (= (.-length from) pn-pubkey-hash-length)
             (= (.-length to) pn-pubkey-hash-length)))))

(defn anonymize-pubkey
  [pubkey]
  "Anonymize a public key, if needed, by hashing it and taking the first 4 bytes"
  (if (= (count pubkey) pn-pubkey-hash-length)
    pubkey
    (apply str (take pn-pubkey-hash-length (ethereum/sha3 pubkey)))))

(defn encode-notification-payload
  [{:keys [from to id] :as payload}]
  (if (valid-notification-payload? payload)
    {:msg-v2 (js/JSON.stringify #js {:from (anonymize-pubkey from)
                                     :to   (anonymize-pubkey to)
                                     :id   (apply str (take pn-message-id-hash-length id))})}
    (throw (str "Invalid push notification payload" payload))))

(when platform/desktop?
  (defn handle-initial-push-notification [] ())) ;; no-op

(when-not platform/desktop?

  (def channel-id "status-im")
  (def channel-name "Status")
  (def sound-name "message.wav")
  (def group-id "im.status.ethereum.MESSAGE")
  (def icon "ic_stat_status_notification")

  (defn- hash->contact [hash-or-pubkey multiaccounts]
    (let [hash (anonymize-pubkey hash-or-pubkey)]
      (->> multiaccounts
           (filter #(= (anonymize-pubkey (:public-key %)) hash))
           first)))

  (defn- hash->pubkey [hash multiaccounts]
    (:public-key (hash->contact hash multiaccounts)))

  (defn lookup-contact-pubkey-from-hash
    [{:keys [db] :as cofx} contact-pubkey-or-hash]
    "Tries to deanonymize a given contact pubkey hash by looking up the
    full pubkey (if db is unlocked) in :multiaccount and, if not found,
    in :contacts/contacts. 
    Returns original value if not a hash (e.g. already a public key)."
    (if (and contact-pubkey-or-hash
             (= (count contact-pubkey-or-hash) pn-pubkey-hash-length))
      (if-let [multiaccount-pubkey (hash->pubkey contact-pubkey-or-hash
                                                 [(:multiaccount db)])]
        multiaccount-pubkey
        (if (multiaccounts.model/logged-in? cofx)
          ;; TODO: for simplicity we're doing a linear lookup of the contacts,
          ;; but we might want to build a map of hashed pubkeys to pubkeys
          ;; for this purpose
          (hash->pubkey contact-pubkey-or-hash
                        (contact.db/get-active-contacts (:contacts/contacts db)))
          (do
            (log/warn "failed to lookup contact from hash, not logged in")
            contact-pubkey-or-hash)))
      contact-pubkey-or-hash))

  (defn parse-notification-v1-payload [msg-json]
    (let [msg (js/JSON.parse msg-json)]
      {:from    (object/get msg "from")
       :to      (object/get msg "to")}))

  (defn parse-notification-v2-payload [msg-v2-json]
    (let [msg (js/JSON.parse msg-v2-json)]
      {:from    (object/get msg "from")
       :to      (object/get msg "to")
       :id      (object/get msg "id")}))

  (defn decode-notification-payload [message-js]
    ;; message-js.-data is Notification.data():
    ;; https://github.com/invertase/react-native-firebase/blob/adcbeac3d11585dd63922ef178ff6fd886d5aa9b/src/modules/notifications/Notification.js#L79
    (let [data-js     (.. message-js -data)
          msg-v2-json (object/get data-js "msg-v2")]
      (try
        (let [payload (if msg-v2-json
                        (parse-notification-v2-payload msg-v2-json)
                        (parse-notification-v1-payload (object/get data-js "msg")))]
          (if (valid-notification-payload? payload)
            payload
            (log/warn "failed to retrieve notification payload from"
                      (js/JSON.stringify data-js))))
        (catch :default e
          (log/debug "failed to parse" (js/JSON.stringify data-js)
                     "exception:" e)))))

  (defn rehydrate-payload
    [cofx {:keys [from to id] :as decoded-payload}]
    "Takes a payload with hashed pubkeys and returns a payload with the real
    (matched) pubkeys"
    {:from (lookup-contact-pubkey-from-hash cofx from)
     :to   (lookup-contact-pubkey-from-hash cofx to)
     ;; TODO: Rehydrate message id
     :id   id})

  (defn- get-contact-name [{:keys [db] :as cofx} from]
    (if (multiaccounts.model/logged-in? cofx)
      (:name (hash->contact from (-> db :contacts/contacts vals)))
      (anonymize-pubkey from)))

  (defn- build-notification [{:keys [title body decoded-payload]}]
    (let [native-notification
          (clj->js
           (merge
            {:title title
             :body  body
             :data  (clj->js (encode-notification-payload decoded-payload))
             :sound sound-name}
            (when-let [msg-id (:id decoded-payload)]
              ;; We must prefix the notification ID, otherwise it will
              ;; cause a crash in iOS
              {:notificationId (str "hash:" msg-id)})))]
      (firebase.notifications.Notification.
       native-notification (.notifications firebase))))

  (defn display-notification [{:keys [title body] :as params}]
    (let [notification (build-notification params)]
      (when platform/android?
        (.. notification
            (-android.setChannelId channel-id)
            (-android.setAutoCancel true)
            (-android.setPriority firebase.notifications.Android.Priority.High)
            (-android.setCategory firebase.notifications.Android.Category.Message)
            (-android.setGroup group-id)
            (-android.setSmallIcon icon)))
      (.. firebase
          notifications
          (displayNotification notification)
          (then #(log/debug "Display Notification" title body))
          (catch (fn [error]
                   (log/debug "Display Notification error" title body error))))))

  (defn get-fcm-token []
    (-> (.getToken (.messaging firebase))
        (.then (fn [x]
                 (log/debug "get-fcm-token:" x)
                 (re-frame/dispatch
                  [:notifications.callback/get-fcm-token-success x])))))

  (defn create-notification-channel []
    (let [channel (firebase.notifications.Android.Channel.
                   channel-id
                   channel-name
                   firebase.notifications.Android.Importance.High)]
      (.setSound channel sound-name)
      (.setShowBadge channel true)
      (.enableVibration channel true)
      (.. firebase
          notifications
          -android
          (createChannel channel)
          (then #(log/debug "Notification channel created:" channel-id)
                #(log/error "Notification channel creation error:" channel-id %)))))

  (defn- show-notification?
    "Ignore push notifications from unknown contacts or removed chats"
    [{:keys [db] :as cofx} {:keys [from] :as rehydrated-payload}]
    (and (valid-notification-payload? rehydrated-payload)
         (multiaccounts.model/logged-in? cofx)
         (some #(= (:public-key %) from)
               (contact.db/get-active-contacts (:contacts/contacts db)))
         (some #(= (:chat-id %) from)
               (vals (:chats db)))))

  (fx/defn handle-on-message
    [{:keys [db now] :as cofx} decoded-payload {:keys [force]}]
    (let [view-id            (:view-id db)
          current-chat-id    (:current-chat-id db)
          app-state          (:app-state db)
          rehydrated-payload (rehydrate-payload cofx decoded-payload)
          from               (:from rehydrated-payload)]
      (log/debug "handle-on-message" "app-state:" app-state
                 "view-id:" view-id "current-chat-id:" current-chat-id
                 "from:" from "force:" force)
      (merge
       (when (and (= (count from) pn-pubkey-length)
                  (show-notification? cofx rehydrated-payload))
         {:dispatch [:mailserver/fetch-history from (- (quot now 1000) pull-recent-messages-window)]})
       (when (or force
                 (and
                  (not= app-state "active")
                  (show-notification? cofx rehydrated-payload)))
         {:db
          (assoc-in db [:push-notifications/stored (:to rehydrated-payload)]
                    (js/JSON.stringify (clj->js rehydrated-payload)))
          :notifications/display-notification
          {:title           (get-contact-name cofx from)
           :body            (i18n/label :notifications-new-message-body)
           :decoded-payload rehydrated-payload}}))))

  (fx/defn handle-push-notification-open
    [{:keys [db] :as cofx} decoded-payload {:keys [stored?] :as ctx}]
    (let [current-public-key (multiaccounts.model/current-public-key cofx)
          nav-opts           (when stored? {:navigation-reset? true})
          rehydrated-payload (rehydrate-payload cofx decoded-payload)
          from               (:from rehydrated-payload)
          to                 (:to   rehydrated-payload)]
      (log/debug "handle-push-notification-open"
                 "current-public-key:" current-public-key
                 "rehydrated-payload:" rehydrated-payload "stored?:" stored?)
      (if (= to current-public-key)
        (fx/merge cofx
                  {:db (update db :push-notifications/stored dissoc to)}
                  (chat-model/navigate-to-chat from nav-opts))
        {:db (assoc-in db [:push-notifications/stored to]
                       (js/JSON.stringify (clj->js rehydrated-payload)))})))

  ;; https://github.com/invertase/react-native-firebase/blob/adcbeac3d11585dd63922ef178ff6fd886d5aa9b/src/modules/notifications/Notification.js#L13
  (defn handle-notification-open-event [event]
    (log/debug "handle-notification-open-event" event)
    (let [decoded-payload (decode-notification-payload (.. event -notification))]
      (when decoded-payload
        (re-frame/dispatch
         [:notifications/notification-open-event-received decoded-payload nil]))))

  (defn handle-initial-push-notification []
    "This method handles pending push notifications.
    It is only needed to handle PNs from legacy clients
    (which use firebase.notifications API)"
    (log/debug "Handle initial push notifications")
    (.. firebase
        notifications
        getInitialNotification
        (then (fn [event]
                (log/debug "getInitialNotification" event)
                (when event
                  (handle-notification-open-event event))))))

  (defn setup-token-refresh-callback []
    (.onTokenRefresh
     (.messaging firebase)
     (fn [x]
       (log/debug "onTokenRefresh:" x)
       (re-frame/dispatch [:notifications.callback/get-fcm-token-success x]))))

  (defn setup-on-notification-callback []
    "Calling onNotification is only needed so that we're able to receive PNs"
    "while in foreground from older clients who are still relying"
    "on the notifications API. Once that is no longer a consideration"
    "we can remove this method"
    (log/debug "calling onNotification")
    (.onNotification
     (.notifications firebase)
     (fn [message-js]
       (log/debug "handle-on-notification-callback called")
       (let [decoded-payload (decode-notification-payload message-js)]
         (log/debug "handle-on-notification-callback payload:" decoded-payload)
         (when decoded-payload
           (re-frame/dispatch
            [:notifications.callback/on-message decoded-payload]))))))

  (defn setup-on-message-callback []
    (log/debug "calling onMessage")
    (.onMessage
     (.messaging firebase)
     (fn [message-js]
       (log/debug "handle-on-message-callback called")
       (let [decoded-payload (decode-notification-payload message-js)]
         (log/debug "handle-on-message-callback decoded-payload:"
                    decoded-payload)
         (when decoded-payload
           (re-frame/dispatch
            [:notifications.callback/on-message decoded-payload]))))))

  (defn setup-on-notification-opened-callback []
    (log/debug "setup-on-notification-opened-callback")
    (.. firebase
        notifications
        (onNotificationOpened handle-notification-open-event)))

  (defn init []
    (log/debug "Init notifications")
    (setup-token-refresh-callback)
    (setup-on-message-callback)
    (setup-on-notification-callback)
    (setup-on-notification-opened-callback)
    (when platform/android?
      (create-notification-channel))
    (handle-initial-push-notification)))

(fx/defn process-stored-event
  [{:keys [db] :as cofx} address stored-pns]
  (when-not platform/desktop?
    (if (multiaccounts.model/logged-in? cofx)
      (let [current-multiaccount        (:multiaccount db)
            current-address        (:address current-multiaccount)
            current-multiaccount-pubkey (:public-key current-multiaccount)
            stored-pn-val-json     (or (get stored-pns current-multiaccount-pubkey)
                                       (get stored-pns (anonymize-pubkey current-multiaccount-pubkey)))
            stored-pn-payload      (if (= (first stored-pn-val-json) \{)
                                     (js->clj (js/JSON.parse stored-pn-val-json) :keywordize-keys true)
                                     {:from stored-pn-val-json
                                      :to   current-multiaccount-pubkey})
            from                   (lookup-contact-pubkey-from-hash cofx (:from stored-pn-payload))
            to                     (lookup-contact-pubkey-from-hash cofx (:to stored-pn-payload))]
        (when (and from
                   (not (contact.db/blocked? db from))
                   (= address current-address))
          (log/debug "process-stored-event" "address" address "from" from "to" to)
          (handle-push-notification-open cofx
                                         stored-pn-payload
                                         {:stored? true})))
      (log/error "process-stored-event called without user being logged in!"))))

(re-frame/reg-fx
 :notifications/display-notification
 display-notification)

(re-frame/reg-fx
 :notifications/init
 (fn []
   (cond
     platform/android?
     (init)

     platform/ios?
     (utils/set-timeout init 100))))

(re-frame/reg-fx
 :notifications/get-fcm-token
 (fn [_]
   (when platform/mobile?
     (get-fcm-token))))

(re-frame/reg-fx
 :notifications/request-notifications-permissions
 (fn [_]
   (request-permissions)))
