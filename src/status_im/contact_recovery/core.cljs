(ns status-im.contact-recovery.core
  (:require
   [status-im.i18n :as i18n]
   [re-frame.core :as re-frame]
   [status-im.data-store.contact-recovery :as data-store.contact-recovery]
   [status-im.utils.fx :as fx]
   [status-im.accounts.db :as accounts.db]
   [status-im.contact.core :as models.contact]))

(defn prompt-dismissed! [public-key]
  (re-frame/dispatch [:contact-recovery.ui/prompt-dismissed public-key]))

(defn prompt-accepted! [public-key]
  (re-frame/dispatch [:contact-recovery.ui/prompt-accepted public-key]))

(defn show-contact-recovery-fx
  "Check that a pop up for that given user is not already shown, if not proceed fetching from the db whether we should be showing it"
  [{:keys [db] :as cofx} public-key]
  (let [my-public-key (accounts.db/current-public-key cofx)
        pfs? (get-in db [:account/account :settings :pfs?])]
    (when (and (not= public-key my-public-key)
               pfs?
               (not (get-in db [:contact-recovery/pop-up public-key])))
      {:db (update db :contact-recovery/pop-up conj public-key)
       :contact-recovery/show-contact-recovery-message public-key})))

(fx/defn prompt-dismissed [{:keys [db]} public-key]
  {:db (update db :contact-recovery/pop-up disj public-key)})

(defn show-contact-recovery-message? [public-key]
  (not (data-store.contact-recovery/get-contact-recovery-by-id public-key)))

(defn show-contact-recovery-message-fx [public-key]
  (when (show-contact-recovery-message? public-key)
    (re-frame/dispatch [:contact-recovery.callback/show-contact-recovery-message public-key])))

(re-frame/reg-fx
 :contact-recovery/show-contact-recovery-message
 show-contact-recovery-message-fx)

(fx/defn save-contact-recovery [{:keys [now]} public-key]
  {:data-store/tx [(data-store.contact-recovery/save-contact-recovery-tx {:timestamp now
                                                                          :id public-key})]})

(fx/defn prompt-accepted [cofx public-key]
  (fx/merge
   cofx
   (prompt-dismissed public-key)
   (save-contact-recovery public-key)))

(fx/defn show-contact-recovery-message [{:keys [db] :as cofx} public-key]
  (let [pfs?     (get-in db [:account/account :settings :pfs?])
        contact  (models.contact/build-contact cofx public-key)
        popup    {:ui/show-confirmation {:title   (i18n/label :t/contact-recovery-title {:name (:name contact)})
                                         :content (i18n/label :t/contact-recovery-content {:name (:name contact)})
                                         :confirm-button-text (i18n/label :t/add-to-contacts)
                                         :cancel-button-text (i18n/label :t/cancel)
                                         :on-cancel #(prompt-dismissed! public-key)
                                         :on-accept #(prompt-accepted! public-key)}}]

    (when pfs?
      (fx/merge cofx
                popup))))
