(ns status-im.ui.screens.profile.events
  (:require [re-frame.core :as re-frame :refer [reg-fx]]
            [status-im.components.react :refer [show-image-picker]]
            [status-im.constants :refer [console-chat-id]]
            [status-im.ui.screens.profile.navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.image-processing :refer [img->base64]]
            [taoensso.timbre :as log]))

(defn message-user [identity]
  (when identity
    (re-frame/dispatch [:navigation-replace :chat identity])))

(handlers/register-handler
  :open-image-picker
  (handlers/side-effect!
   (fn [_ _]
     (show-image-picker
      (fn [image]
        (let [path (get (js->clj image) "path")
              _ (log/debug path)
              on-success (fn [base64]
                           (re-frame/dispatch [:set-in [:profile-edit :photo-path] (str "data:image/jpeg;base64," base64)]))
              on-error   (fn [type error]
                           (.log js/console type error))]
          (img->base64 path on-success on-error)))))))

(handlers/register-handler
  :phone-number-change-requested
  ;; Switch user to the console issuing the !phone command automatically to let him change his phone number.
  ;; We allow to change phone number only from console because this requires entering SMS verification code.
  (handlers/side-effect!
   (fn [db _]
     (re-frame/dispatch [:navigate-to :chat console-chat-id])
     (js/setTimeout #(re-frame/dispatch [:select-chat-input-command {:name "phone"}]) 500))))

(handlers/register-handler
  :open-chat-with-the-send-transaction
  (handlers/side-effect!
   (fn [db [_ chat-id]]
     (re-frame/dispatch [:clear-seq-arguments])
     (re-frame/dispatch [:navigate-to :chat chat-id])
     (js/setTimeout #(re-frame/dispatch [:select-chat-input-command {:name "send"}]) 500))))

(defn prepare-edit-profile
  [{:keys [current-account-id] :as db} _]
  (let [current-account (select-keys (get-in db [:accounts current-account-id])
                                     [:name :photo-path :status])]
    (update-in db [:profile-edit] merge current-account)))

(defn open-edit-profile [_ _]
  (re-frame/dispatch [:navigate-to :edit-my-profile]))

(handlers/register-handler
  :open-edit-my-profile
  (handlers/handlers->
   prepare-edit-profile
   open-edit-profile))
