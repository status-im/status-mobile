(ns status-im.ui.screens.profile.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame :refer [reg-fx trim-v]]
            [status-im.components.react :refer [show-image-picker]]
            [status-im.constants :refer [console-chat-id]]
            [status-im.ui.screens.profile.navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.image-processing :refer [img->base64]]
            [taoensso.timbre :as log]))

(def account-profile-keys [:name :photo-path :status])

(reg-fx
  :open-image-picker
  ;; the image picker is only used here for now, this effect can be use in other scenarios as well
  (fn [callback-event]
    (show-image-picker
     (fn [image]
       (let [path (get (js->clj image) "path")
             _ (log/debug path)
             on-success (fn [base64]
                          (re-frame/dispatch [callback-event base64]))
             on-error   (fn [type error]
                          (.log js/console type error))]
         (img->base64 path on-success on-error))))))

(handlers/register-handler-fx
  :profile/send-transaction
  [trim-v]
  (fn [{:keys [db]} [chat-id contact-id]]
    {:dispatch-n [[:clear-seq-arguments]
                  [:navigate-to :chat chat-id]
                  [:select-chat-input-command {:name "send"}]
                  [:set-contact-as-command-argument {:arg-index 0
                                                     :bot-db-key "recipient"
                                                     :contact (get-in db [:contacts/contacts contact-id])}]]}))

(handlers/register-handler-fx
  :profile/send-message
  (fn [_ [_ identity]]
    (when identity
      {:dispatch [:navigation-replace :chat identity]})))

(handlers/register-handler-fx
  :my-profile/edit
  (fn [{:keys [db]} [_ edit-type edit-value]]
    (let [current-account-id (:accounts/current-account-id db)
          current-account (-> db
                              (get-in [:accounts/accounts current-account-id])
                              (select-keys account-profile-keys))
          new-db (-> db
                     (update-in [:my-profile/edit] merge current-account)
                     (assoc-in [:my-profile/edit :edit-status?] (= edit-type :status true)))]
      {:db new-db
       :dispatch [:navigate-to :edit-my-profile]})))

(handlers/register-handler-fx
  :my-profile/update-status
  (fn [_ [_ new-status]]
    (when-not (string/blank? new-status)
      {:dispatch-n [[:check-status-change new-status]
                    [:account-update {:status new-status}]]})))

(handlers/register-handler-fx
  :my-profile/update-phone-number
  ;; Switch user to the console issuing the !phone command automatically to let him change his phone number.
  ;; We allow to change phone number only from console because this requires entering SMS verification code.
  (fn [_ _]
    {:dispatch-n [[:navigate-to :chat console-chat-id]
                  [:select-chat-input-command {:name "phone"}]]}))

(handlers/register-handler-fx
  :my-profile/update-picture
  (fn [{:keys [db]} [this-event base64-image]]
    (if base64-image
      {:db (assoc-in db [:my-profile/edit :photo-path] (str "data:image/jpeg;base64," base64-image))}
      {:open-image-picker this-event})))

(handlers/register-handler-fx
  :my-profile/save-changes
  (fn [{:keys [db]} _]
    (let [{:keys [:my-profile/edit]} db]
      {:dispatch-n [[:check-status-change (:status edit)]
                    [:account-update (select-keys edit account-profile-keys)]]})))
