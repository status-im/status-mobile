(ns status-im.ui.screens.contacts.events
  (:require [re-frame.core :as re-frame]
            [status-im.chat.models :as chat.models]
            [status-im.i18n :as i18n]
            [status-im.models.contact :as models.contact]
            [status-im.ui.screens.add-new.new-chat.db :as new-chat.db]
            [status-im.ui.screens.browser.default-dapps :as default-dapps]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.js-resources :as js-res]
            [status-im.utils.utils :as utils]
            [status-im.utils.fx :as fx]))

(fx/defn add-contact-and-open-chat
  [cofx whisper-id]
  (fx/merge cofx
            (models.contact/add-contact whisper-id)
            (chat.models/start-chat whisper-id {:navigation-reset? true})))

(re-frame/reg-cofx
 :get-default-contacts
 (fn [coeffects _]
   (assoc coeffects :default-contacts js-res/default-contacts)))

(re-frame/reg-cofx
 :get-default-dapps
 (fn [coeffects _]
   (assoc coeffects :default-dapps default-dapps/all)))

(handlers/register-handler-fx
 :add-contact
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [cofx [_ whisper-id]]
   (models.contact/add-contact cofx whisper-id)))

(handlers/register-handler-fx
 :hide-contact
 (fn [{:keys [db]} [_ whisper-id]]
   (when (get-in db [:contacts/contacts whisper-id])
     {:db (assoc-in db [:contacts/contacts whisper-id :hide-contact?] true)})))

(handlers/register-handler-fx
 :set-contact-identity-from-qr
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [{:keys [db] :as cofx} [_ _ contact-identity]]
   (let [current-account (:account/account db)
         fx              {:db (assoc db :contacts/new-identity contact-identity)}
         validation-result (new-chat.db/validate-pub-key db contact-identity)]
     (if (some? validation-result)
       (utils/show-popup (i18n/label :t/unable-to-read-this-code) validation-result #(re-frame/dispatch [:navigate-to-clean :home]))
       (fx/merge cofx
                 fx
                 (add-contact-and-open-chat contact-identity))))))

(handlers/register-handler-fx
 :open-contact-toggle-list
 (fn [{:keys [db] :as cofx} _]
   (fx/merge cofx
             {:db (assoc db
                         :group/selected-contacts #{}
                         :new-chat-name "")}
             (navigation/navigate-to-cofx :contact-toggle-list nil))))

(handlers/register-handler-fx
 :open-chat-with-contact
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [cofx [_ {:keys [whisper-identity]}]]
   (add-contact-and-open-chat cofx whisper-identity)))

(handlers/register-handler-fx
 :add-contact-handler
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [{{:contacts/keys [new-identity]} :db :as cofx} _]
   (when (seq new-identity)
     (add-contact-and-open-chat cofx new-identity))))
