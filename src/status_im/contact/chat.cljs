(ns status-im.contact.chat
  (:require [re-frame.core :as re-frame]
            [status-im.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.chat.models :as chat]
            [status-im.contact.core :as contact]
            [status-im.notifications-center.core :as notification-center]))

(fx/defn send-message-pressed
  {:events       [:contact.ui/send-message-pressed]
   :interceptors [(re-frame/inject-cofx :random-id-generator)]}
  [cofx {:keys [public-key ens-name]}]
  (fx/merge cofx
            (chat/start-chat public-key ens-name)
            (notification-center/accept-all-activity-center-notifications-from-chat public-key)))

(fx/defn contact-code-submitted
  {:events       [:contact.ui/contact-code-submitted]
   :interceptors [(re-frame/inject-cofx :random-id-generator)]}
  [{{:contacts/keys [new-identity]} :db :as cofx} new-contact? nickname]
  (let [{:keys [public-key ens-name]} new-identity]
    (fx/merge cofx
              #(if new-contact?
                 (contact/add-contact % public-key nickname ens-name)
                 (chat/start-chat % public-key ens-name))
              #(when new-contact?
                 (navigation/navigate-back %)))))

(fx/defn pinned-messages-pressed
  {:events       [:contact.ui/pinned-messages-pressed]}
  [cofx public-key]
  (chat/navigate-to-user-pinned-messages cofx public-key))
