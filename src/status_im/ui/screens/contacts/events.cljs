(ns status-im.ui.screens.contacts.events
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.add-new.new-chat.db :as new-chat.db]
            [status-im.ui.screens.contacts.default-dapps :as default-dapps]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.js-resources :as js-res]
            [status-im.chat.events :as chat.events]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.utils :as utils]
            [status-im.models.contact :as models.contact]))

(defn add-contact-and-open-chat [whisper-id cofx]
  (handlers-macro/merge-fx cofx
                           (navigation/navigate-to-clean :home)
                           (models.contact/add-contact whisper-id)
                           (chat.events/start-chat whisper-id {})))

(re-frame/reg-cofx
 :get-default-contacts
 (fn [coeffects _]
   (assoc coeffects :default-contacts js-res/default-contacts)))

(re-frame/reg-cofx
 :get-default-dapps
 (fn [coeffects _]
   (assoc coeffects :default-dapps default-dapps/all)))

;;;; Handlers

(handlers/register-handler-fx
 :load-contacts
 [(re-frame/inject-cofx :data-store/get-all-contacts)]
 (fn [{:keys [db all-contacts]} _]
   (let [contacts-list (map #(vector (:whisper-identity %) %) all-contacts)
         contacts (into {} contacts-list)]
     {:db (update db :contacts/contacts #(merge contacts %))})))

(handlers/register-handler-fx
 :add-contact
 [(re-frame/inject-cofx :random-id)]
 (fn [cofx [_ whisper-id]]
   (models.contact/add-contact whisper-id cofx)))

(handlers/register-handler-fx
 :set-contact-identity-from-qr
 [(re-frame/inject-cofx :random-id)]
 (fn [{:keys [db] :as cofx} [_ _ contact-identity]]
   (let [current-account (:account/account db)
         fx              {:db (assoc db :contacts/new-identity contact-identity)}
         validation-result (new-chat.db/validate-pub-key contact-identity current-account)]
     (if (some? validation-result)
       (utils/show-popup (i18n/label :t/unable-to-read-this-code) validation-result #(re-frame/dispatch [:navigate-to-clean :home]))
       (handlers-macro/merge-fx cofx
                                fx
                                (add-contact-and-open-chat contact-identity))))))

(handlers/register-handler-db
 :open-contact-toggle-list
 (fn [db _]
   (-> (assoc db
              :group/selected-contacts #{}
              :new-chat-name "")
       (navigation/navigate-to :contact-toggle-list))))

(handlers/register-handler-fx
 :open-chat-with-contact
 [(re-frame/inject-cofx :random-id)]
 (fn [cofx [_ {:keys [whisper-identity]}]]
   (add-contact-and-open-chat whisper-identity cofx)))

(handlers/register-handler-fx
 :add-contact-handler
 [(re-frame/inject-cofx :random-id)]
 (fn [{{:contacts/keys [new-identity]} :db :as cofx} _]
   (when (seq new-identity)
     (add-contact-and-open-chat new-identity cofx))))
