(ns status-im.ui.screens.profile.events
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as string]
            [re-frame.core :as re-frame :refer [reg-fx trim-v]]
            [status-im.components.react :refer [show-image-picker]]
            [status-im.constants :refer [console-chat-id]]
            [status-im.ui.screens.profile.db :as db]
            [status-im.ui.screens.profile.navigation]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.image-processing :refer [img->base64]]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

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
    {:dispatch-n [[:navigate-to :chat chat-id]
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
  :my-profile/update-phone-number
  ;; Switch user to the console issuing the !phone command automatically to let him change his phone number.
  ;; We allow to change phone number only from console because this requires entering SMS verification code.
  (fn [_ _]
    {:dispatch-n [[:navigate-to :chat console-chat-id]
                  [:select-chat-input-command {:name "phone"}]]}))

(defn get-current-account [{:keys [:accounts/current-account-id] :as db}]
  (get-in db [:accounts/accounts current-account-id]))

(handlers/register-handler-fx
  :my-profile.drawer/edit-name
  (fn [{:keys [db]} _]
    (let [{:my-profile/keys [default-name edit auto-save]} db
          {:keys [name public-key]}                        (get-current-account db)]
      {:db (cond-> db
             (not default-name) (assoc :my-profile/default-name (gfycat/generate-gfy public-key))
             :always            (assoc-in [:my-profile/drawer :name] name))})))

(handlers/register-handler-fx
  :my-profile.drawer/edit-status
  (fn [{:keys [db]} _]
    (let [{:keys [status]} (get-current-account db)]
      {:db (-> db
               (assoc-in [:my-profile/drawer :status] status)
               (assoc-in [:my-profile/drawer :edit-status?] true))})))

(handlers/register-handler-fx
  :my-profile/edit-profile
  (fn [{:keys [db]} [_ edit-status?]]
    (let [new-db (-> db
                     (assoc-in [:my-profile/profile :edit-status?] edit-status?)
                     (update-in [:my-profile/profile]
                                #(merge (select-keys (get-current-account db) db/account-profile-keys) %)))]
      {:db        new-db
       :dispatch [:navigate-to :edit-my-profile]})))

(defn valid-name? [name]
  (spec/valid? :profile/name name))

(handlers/register-handler-fx
  :my-profile.drawer/update-name
  (fn [{:keys [db]} [_ name]]
    {:db (-> db
             (assoc-in [:my-profile/drawer :valid-name?] (valid-name? name))
             (assoc-in [:my-profile/drawer :name] name))}))

(handlers/register-handler-fx
  :my-profile.drawer/update-status
  (fn [{:keys [db]} [_ status]]
    (let [linebreak?  (string/includes? status "\n")
          new-db      (if linebreak?
                        (-> db
                            (assoc-in [:my-profile/drawer :edit-status?] nil)
                            (assoc-in [:my-profile/drawer :status] (utils/clean-text status)))
                        (assoc-in db [:my-profile/drawer :status] status))]
      (if linebreak?
        {:db new-db
         :dispatch [:my-profile.drawer/save-status]}
        {:db new-db}))))

(handlers/register-handler-fx
  :my-profile/update-name
  (fn [{:keys [db]} [_ name]]
    {:db (-> db
             (assoc-in [:my-profile/profile :valid-name?] (valid-name? name))
             (assoc-in [:my-profile/profile :name] name))}))

(handlers/register-handler-fx
  :my-profile/update-status
  (fn [{:keys [db]} [_ status]]
    {:db (if (string/includes? status "\n")
           (-> db
               (assoc-in [:my-profile/profile :edit-status?] nil)
               (assoc-in [:my-profile/profile :status] (utils/clean-text status)))
           (assoc-in db [:my-profile/profile :status] status))}))

(handlers/register-handler-fx
  :my-profile/update-picture
  (fn [{:keys [db]} [this-event base64-image]]
    (if base64-image
      {:db (assoc-in db [:my-profile/profile :photo-path] (str "data:image/jpeg;base64," base64-image))}
      {:open-image-picker this-event})))

(defn clean-name [{:accounts/keys [current-account-id] :as db} edit-view]
  (let [name (get-in db [edit-view :name])]
    (if (valid-name? name)
      name
      (get-in db [:accounts/accounts current-account-id :name]))))

(defn clear-profile [db]
  (dissoc db :my-profile/profile :my-profile/drawer :my-profile/default-name))

(handlers/register-handler-fx
  :my-profile.drawer/save-name
  (fn [{:keys [db]} _]
    (let [cleaned-name (clean-name db :my-profile/drawer)]
      {:db (clear-profile db)
       :dispatch [:account-update {:name cleaned-name}]})))

(handlers/register-handler-fx
  :my-profile.drawer/save-status
  (fn [{:keys [db]} _]
    (let [status (get-in db [:my-profile/drawer :status])
          new-db (clear-profile db)]
      (if (string/blank? status)
        {:db new-db}
        {:db new-db
         :dispatch-n [[:check-status-change status]
                      [:account-update {:status status}]]}))))

(handlers/register-handler-fx
  :my-profile/save-profile
  (fn [{:keys [db]} _]
    (let [{:keys [status photo-path]} (:my-profile/profile db)
          cleaned-name                (clean-name db :my-profile/profile)
          cleaned-edit                {:name cleaned-name
                                       :status status
                                       :photo-path photo-path}]
      {:db (clear-profile db)
       :dispatch-n [[:check-status-change status]
                    [:account-update cleaned-edit]
                    [:navigate-back]]})))
