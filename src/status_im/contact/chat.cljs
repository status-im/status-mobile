(ns status-im.contact.chat
  (:require [re-frame.core :as re-frame]
            [status-im.contact.core :as contact]
            [utils.re-frame :as rf]
            [status-im2.navigation.events :as navigation]))

(rf/defn contact-code-submitted
  {:events       [:contact.ui/contact-code-submitted]
   :interceptors [(re-frame/inject-cofx :random-id-generator)]}
  [{{:contacts/keys [new-identity]} :db :as cofx} new-contact? nickname]
  (let [{:keys [public-key ens-name]} new-identity]
    (rf/merge cofx
              #(if new-contact?
                 (contact/add-contact % public-key nickname ens-name)
                 {:dispatch [:chat.ui/start-chat public-key ens-name]})
              #(when new-contact?
                 (navigation/navigate-back %)))))
