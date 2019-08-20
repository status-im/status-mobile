(ns status-im.contact-recovery.core
  "This namespace handles the case where a user has just recovered their account
  and is not able to decrypt messages, as the encryption is device-to-device.
  Upon receiving this message, an empty message is sent back carrying device information
  which will tell the other peer to target this device as well"
  (:require
   [status-im.i18n :as i18n]
   [re-frame.core :as re-frame]
   [status-im.data-store.contact-recovery :as data-store.contact-recovery]
   [status-im.utils.config :as config]
   [status-im.utils.fx :as fx]
   [status-im.multiaccounts.model :as multiaccounts.model]
   [status-im.contact.core :as models.contact]))

;; How long do we wait until we process a contact-recovery again?
(def contact-recovery-interval-ms (* 60 60 1000))

(defn prompt-dismissed! [public-key]
  (re-frame/dispatch [:contact-recovery.ui/prompt-dismissed public-key]))

(defn prompt-accepted! [public-key]
  (re-frame/dispatch [:contact-recovery.ui/prompt-accepted public-key]))

(defn handle-contact-recovery-fx
  "Check that a contact-recovery for the given user is not already in process, if not
  fetch from db and check"
  [{:keys [db now] :as cofx} public-key]
  (let [my-public-key (multiaccounts.model/current-public-key cofx)]
    (when (and (not= public-key my-public-key)
               (not (get-in db [:contact-recovery/pop-up public-key])))
      {:db (update db :contact-recovery/pop-up conj public-key)
       :contact-recovery/handle-recovery [now public-key]})))

(fx/defn prompt-dismissed [{:keys [db]} public-key]
  {:db (update db :contact-recovery/pop-up disj public-key)})

(defn notified-recently?
  "We don't want to notify the user each time, so we wait an interval before
  sending a message again"
  [now public-key]
  (let [{:keys [timestamp]} (data-store.contact-recovery/get-contact-recovery-by-id public-key)]
    (and timestamp
         (> contact-recovery-interval-ms (- now timestamp)))))

(defn handle-recovery-fx [now public-key]
  (if (notified-recently? now public-key)
    (prompt-dismissed! public-key)
    (re-frame/dispatch [:contact-recovery.callback/handle-recovery public-key])))

(fx/defn notify-user
  "Send an empty message to the user, which will carry device information"
  [cofx public-key]
  (let [current-public-key (multiaccounts.model/current-public-key cofx)]
    {:shh/send-direct-message
     [{:src     current-public-key
       :dst     public-key
       :payload ""}]}))

(re-frame/reg-fx
 :contact-recovery/handle-recovery
 (fn [[now public-key]]
   (handle-recovery-fx now public-key)))

(fx/defn save-contact-recovery [{:keys [now]} public-key])

(fx/defn prompt-accepted [cofx public-key]
  (fx/merge
   cofx
   (prompt-dismissed public-key)
   (save-contact-recovery public-key)
   (notify-user public-key)))

(fx/defn handle-recovery [{:keys [db] :as cofx} public-key]
  (let [contact  (models.contact/build-contact cofx public-key)
        popup    {:ui/show-confirmation {:title   (i18n/label :t/contact-recovery-title {:name (:name contact)})
                                         :content (i18n/label :t/contact-recovery-content {:name (:name contact)})
                                         :confirm-button-text (i18n/label :t/notify)
                                         :cancel-button-text (i18n/label :t/cancel)
                                         :on-cancel #(prompt-dismissed! public-key)
                                         :on-accept #(prompt-accepted! public-key)}}]

    (if config/show-contact-recovery-pop-up?
      (fx/merge cofx popup)
      (prompt-accepted cofx public-key))))
