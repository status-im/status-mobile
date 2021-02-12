(ns status-im.contact.chat
  (:require [re-frame.core :as re-frame]
            [status-im.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.chat.models :as chat]
            [status-im.contact.core :as contact]))

(fx/defn send-message-pressed
  {:events       [:contact.ui/send-message-pressed]
   :interceptors [(re-frame/inject-cofx :random-id-generator)]}
  [cofx {:keys [public-key]}]
  (chat/start-chat cofx public-key))

(fx/defn contact-code-submitted
  {:events       [:contact.ui/contact-code-submitted]
   :interceptors [(re-frame/inject-cofx :random-id-generator)]}
  [{{:contacts/keys [new-identity]} :db :as cofx} new-contact? nickname]
  (let [{:keys [public-key ens-name]} new-identity]
    (fx/merge cofx
              #(if new-contact?
                 (contact/add-contact % public-key nickname)
                 (chat/start-chat % public-key))
              #(when new-contact?
                 (navigation/navigate-back %))
              #(when ens-name
                 (contact/name-verified % public-key ens-name)))))