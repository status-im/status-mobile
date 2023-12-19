(ns legacy.status-im.keycard.unpair
  (:require
    [legacy.status-im.keycard.common :as common]
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [re-frame.core :as re-frame]
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(rf/defn unpair-card-pressed
  {:events [:keycard-settings.ui/unpair-card-pressed]}
  [_]
  {:ui/show-confirmation {:title               (i18n/label :t/unpair-card)
                          :content             (i18n/label :t/unpair-card-confirmation)
                          :confirm-button-text (i18n/label :t/yes)
                          :cancel-button-text  (i18n/label :t/no)
                          :on-accept           #(re-frame/dispatch
                                                 [:keycard-settings.ui/unpair-card-confirmed])
                          :on-cancel           #()}})

(rf/defn unpair-card-confirmed
  {:events [:keycard-settings.ui/unpair-card-confirmed]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (assoc-in db
                  [:keycard :pin]
                  {:enter-step  :current
                   :current     []
                   :puk         []
                   :status      nil
                   :error-label nil
                   :on-verified :keycard/unpair})}
            (common/navigate-to-enter-pin-screen)))

(rf/defn unpair
  {:events [:keycard/unpair]}
  [{:keys [db]}]
  (let [pin (common/vector->string (get-in db [:keycard :pin :current]))]
    {:keycard/unpair {:pin pin}}))

(rf/defn unpair-and-delete
  {:events [:keycard/unpair-and-delete]}
  [cofx]
  (common/show-connection-sheet
   cofx
   {:on-card-connected :keycard/unpair-and-delete
    :handler
    (fn [{:keys [db]}]
      (let [pin (common/vector->string (get-in db [:keycard :pin :current]))]
        {:keycard/unpair-and-delete
         {:pin pin}}))}))

(rf/defn remove-pairing-from-multiaccount
  [cofx {:keys [remove-instance-uid?]}]
  (rf/merge cofx
            (multiaccounts.update/multiaccount-update
             :keycard-pairing
             nil
             {})
            (multiaccounts.update/multiaccount-update
             :keycard-paired-on
             nil
             {})
            (when remove-instance-uid?
              (multiaccounts.update/multiaccount-update
               :keycard-instance-uid
               nil
               {}))))

(rf/defn on-unpair-success
  {:events [:keycard.callback/on-unpair-success]}
  [{:keys [db] :as cofx}]
  (let [instance-uid (get-in db [:keycard :application-info :instance-uid])
        pairings     (get-in db [:keycard :pairings])]
    (rf/merge
     cofx
     {:db                       (-> db
                                    (assoc-in [:keycard :secrets] nil)
                                    (update-in [:keycard :pairings] dissoc (keyword instance-uid))
                                    (assoc-in [:keycard :pin]
                                              {:status      nil
                                               :error-label nil
                                               :on-verified nil}))
      :keycard/persist-pairings (dissoc pairings (keyword instance-uid))
      :effects.utils/show-popup {:title   ""
                                 :content (i18n/label :t/card-unpaired)}}
     (common/clear-on-card-connected)
     (remove-pairing-from-multiaccount nil)
     (navigation/navigate-to :keycard-settings nil))))

(rf/defn on-unpair-error
  {:events [:keycard.callback/on-unpair-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[keycard] unpair error" error)
  (rf/merge cofx
            {:db                           (assoc-in db
                                            [:keycard :pin]
                                            {:status      nil
                                             :error-label nil
                                             :on-verified nil})
             :keycard/get-application-info nil
             :effects.utils/show-popup     {:title   ""
                                            :content (i18n/label :t/something-went-wrong)}}
            (common/clear-on-card-connected)
            (navigation/navigate-to :keycard-settings nil)))

(rf/defn remove-key-with-unpair
  {:events [:keycard/remove-key-with-unpair]}
  [cofx]
  (common/show-connection-sheet
   cofx
   {:on-card-connected :keycard/remove-key-with-unpair
    :handler
    (fn [{:keys [db]}]
      (let [pin (common/vector->string (get-in db [:keycard :pin :current]))]
        {:keycard/remove-key-with-unpair
         {:pin pin}}))}))

(defn handle-account-removal
  [{:keys [db] :as cofx} keys-removed-from-card?]
  (let [key-uid      (get-in db [:profile/profile :key-uid])
        instance-uid (get-in db [:keycard :application-info :instance-uid])
        pairings     (get-in db [:keycard :pairings])]
    (rf/merge
     cofx
     {:db                       (-> db
                                    (update :profile/profiles-overview dissoc key-uid)
                                    (assoc-in [:keycard :secrets] nil)
                                    (update-in [:keycard :pairings]
                                               dissoc
                                               (keyword instance-uid))
                                    (update-in [:keycard :pairings] dissoc instance-uid)
                                    (assoc-in [:keycard :whisper-public-key] nil)
                                    (assoc-in [:keycard :wallet-address] nil)
                                    (assoc-in [:keycard :application-info] nil)
                                    (assoc-in [:keycard :pin]
                                              {:status      nil
                                               :error-label nil
                                               :on-verified nil}))
      :keycard/persist-pairings (dissoc pairings (keyword instance-uid))
      :effects.utils/show-popup {:title      (i18n/label (if keys-removed-from-card?
                                                           :t/profile-deleted-title
                                                           :t/database-reset-title))
                                 :content    (i18n/label (if keys-removed-from-card?
                                                           :t/profile-deleted-keycard
                                                           :t/database-reset-content))
                                 :on-dismiss #(re-frame/dispatch [:logout])}}
     ;;should be reimplemented :key-storage/delete-profile {:key-uid    key-uid
     ;;:on-success #(log/debug "[keycard] remove account ok")
     ;;                                  :on-error   #(log/warn "[keycard] remove account: " %)}
     (common/clear-on-card-connected)
     (common/hide-connection-sheet))))

(rf/defn on-remove-key-success
  {:events [:keycard.callback/on-remove-key-success]}
  [cofx]
  (handle-account-removal cofx true))

(rf/defn on-remove-key-error
  {:events [:keycard.callback/on-remove-key-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[keycard] remove key error" error)
  (let [tag-was-lost? (common/tag-lost? (:error error))]
    (rf/merge cofx
              (if tag-was-lost?
                (rf/merge cofx
                          {:db (assoc-in db [:keycard :pin :status] nil)}
                          (common/set-on-card-connected :keycard/remove-key-with-unpair))
                (common/show-wrong-keycard-alert)))))

(rf/defn on-unpair-and-delete-success
  {:events [:keycard.callback/on-unpair-and-delete-success]}
  [cofx]
  (handle-account-removal cofx false))

(rf/defn on-unpair-and-delete-error
  {:events [:keycard.callback/on-unpair-and-delete-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[keycard] unpair and delete error" error)
  (let [tag-was-lost? (common/tag-lost? (:error error))]
    (rf/merge cofx
              (if tag-was-lost?
                (rf/merge cofx
                          {:db (assoc-in db [:keycard :pin :status] nil)}
                          (common/set-on-card-connected :keycard/unpair-and-delete))
                (common/show-wrong-keycard-alert)))))
