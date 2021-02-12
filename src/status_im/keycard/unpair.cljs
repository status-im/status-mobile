(ns status-im.keycard.unpair
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.multiaccounts.logout.core :as multiaccounts.logout]
            [status-im.i18n.i18n :as i18n]
            [status-im.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.keycard.common :as common]))

(fx/defn unpair-card-pressed
  {:events [:keycard-settings.ui/unpair-card-pressed]}
  [_]
  {:ui/show-confirmation {:title               (i18n/label :t/unpair-card)
                          :content             (i18n/label :t/unpair-card-confirmation)
                          :confirm-button-text (i18n/label :t/yes)
                          :cancel-button-text  (i18n/label :t/no)
                          :on-accept           #(re-frame/dispatch [:keycard-settings.ui/unpair-card-confirmed])
                          :on-cancel           #()}})

(fx/defn unpair-card-confirmed
  {:events [:keycard-settings.ui/unpair-card-confirmed]}
  [{:keys [db] :as cofx}]
  (let [pin-retry-counter (get-in db [:keycard :application-info :pin-retry-counter])
        enter-step (if (zero? pin-retry-counter) :puk :current)]
    (fx/merge cofx
              {:db (assoc-in db [:keycard :pin] {:enter-step  enter-step
                                                 :current     []
                                                 :puk         []
                                                 :status      nil
                                                 :error-label nil
                                                 :on-verified :keycard/unpair})}
              (common/navigate-to-enter-pin-screen))))

(fx/defn unpair
  {:events [:keycard/unpair]}
  [{:keys [db]}]
  (let [pin     (common/vector->string (get-in db [:keycard :pin :current]))
        pairing (common/get-pairing db)]
    {:keycard/unpair {:pin     pin
                      :pairing pairing}}))

(fx/defn unpair-and-delete
  {:events [:keycard/unpair-and-delete]}
  [{:keys [db]}]
  (let [pin     (common/vector->string (get-in db [:keycard :pin :current]))
        pairing (common/get-pairing db)]
    {:keycard/unpair-and-delete {:pin     pin
                                 :pairing pairing}}))

(fx/defn remove-pairing-from-multiaccount
  [cofx {:keys [remove-instance-uid?]}]
  (fx/merge cofx
            (multiaccounts.update/multiaccount-update
             :keycard-pairing nil {})
            (multiaccounts.update/multiaccount-update
             :keycard-paired-on nil {})
            (when remove-instance-uid?
              (multiaccounts.update/multiaccount-update
               :keycard-instance-uid nil {}))))

(fx/defn on-unpair-success
  {:events [:keycard.callback/on-unpair-success]}
  [{:keys [db] :as cofx}]
  (let [instance-uid (get-in db [:keycard :application-info :instance-uid])
        pairings     (get-in db [:keycard :pairings])]
    (fx/merge cofx
              {:db                          (-> db
                                                (assoc-in [:keycard :secrets] nil)
                                                (update-in [:keycard :pairings] dissoc (keyword instance-uid))
                                                (assoc-in [:keycard :pin] {:status      nil
                                                                           :error-label nil
                                                                           :on-verified nil}))
               :keycard/persist-pairings (dissoc pairings (keyword instance-uid))
               :utils/show-popup            {:title   ""
                                             :content (i18n/label :t/card-unpaired)}}
              (common/clear-on-card-connected)
              (remove-pairing-from-multiaccount nil)
              (navigation/navigate-to-cofx :keycard-settings nil))))

(fx/defn on-unpair-error
  {:events [:keycard.callback/on-unpair-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[keycard] unpair error" error)
  (fx/merge cofx
            {:db                              (assoc-in db [:keycard :pin] {:status      nil
                                                                            :error-label nil
                                                                            :on-verified nil})
             :keycard/get-application-info nil
             :utils/show-popup                {:title   ""
                                               :content (i18n/label :t/something-went-wrong)}}
            (common/clear-on-card-connected)
            (navigation/navigate-to-cofx :keycard-settings nil)))

(fx/defn remove-key-with-unpair
  {:events [:keycard/remove-key-with-unpair]}
  [cofx]
  (common/show-connection-sheet
   cofx
   {:on-card-connected :keycard/remove-key-with-unpair
    :handler
    (fn [{:keys [db]}]
      (let [pin     (common/vector->string (get-in db [:keycard :pin :current]))
            pairing (common/get-pairing db)]
        {:keycard/remove-key-with-unpair
         {:pin     pin
          :pairing pairing}}))}))

(fx/defn on-remove-key-success
  {:events [:keycard.callback/on-remove-key-success]}
  [{:keys [db] :as cofx}]
  (let [key-uid (get-in db [:multiaccount :key-uid])
        instance-uid (get-in db [:keycard :application-info :instance-uid])
        pairings (get-in db [:keycard :pairings])]
    (fx/merge cofx
              {:db                 (-> db
                                       (update :multiaccounts/multiaccounts dissoc key-uid)
                                       (assoc-in [:keycard :secrets] nil)
                                       (update-in [:keycard :pairings] dissoc (keyword instance-uid))
                                       (assoc-in [:keycard :whisper-public-key] nil)
                                       (assoc-in [:keycard :wallet-address] nil)
                                       (assoc-in [:keycard :application-info] nil)
                                       (assoc-in [:keycard :pin] {:status      nil
                                                                  :error-label nil
                                                                  :on-verified nil}))
               :keycard/persist-pairings (dissoc pairings (keyword instance-uid))
               ;;FIXME delete multiaccount
               :utils/show-popup   {:title   ""
                                    :content (i18n/label :t/card-reseted)}}
              (common/clear-on-card-connected)
              (multiaccounts.logout/logout))))

(fx/defn on-remove-key-error
  {:events [:keycard.callback/on-remove-key-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[keycard] remove key error" error)
  (let [tag-was-lost? (common/tag-lost? (:error error))]
    (fx/merge cofx
              (if tag-was-lost?
                (fx/merge cofx
                          {:db (assoc-in db [:keycard :pin :status] nil)}
                          (common/set-on-card-connected :keycard/remove-key-with-unpair))
                (common/show-wrong-keycard-alert true)))))
