(ns status-im.hardwallet.delete-key
  (:require [status-im.multiaccounts.logout.core :as multiaccounts.logout]
            [status-im.i18n :as i18n]
            [status-im.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.hardwallet.common :as common]))

(fx/defn on-delete-success
  {:events [:hardwallet.callback/on-delete-success]}
  [{:keys [db] :as cofx}]
  (let [key-uid (get-in db [:multiaccount :key-uid])]
    (fx/merge cofx
              {:db                 (-> db
                                       (update :multiaccounts/multiaccounts dissoc key-uid)
                                       (assoc-in [:hardwallet :secrets] nil)
                                       (assoc-in [:hardwallet :application-info] nil)
                                       (assoc-in [:hardwallet :pin] {:status      nil
                                                                     :error-label nil
                                                                     :on-verified nil}))
               ;;FIXME delete multiaccount
               :utils/show-popup   {:title   ""
                                    :content (i18n/label :t/card-reseted)}}
              (common/clear-on-card-connected)
              (multiaccounts.logout/logout))))

(fx/defn on-delete-error
  {:events [:hardwallet.callback/on-delete-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[hardwallet] delete error" error)
  (fx/merge cofx
            {:db                              (assoc-in db [:hardwallet :pin] {:status      nil
                                                                               :error-label nil
                                                                               :on-verified nil})
             :hardwallet/get-application-info nil
             :utils/show-popup                {:title   ""
                                               :content (i18n/label :t/something-went-wrong)}}
            (common/clear-on-card-connected)
            (navigation/navigate-to-cofx :keycard-settings nil)))

(fx/defn reset-card-pressed
  {:events [:keycard-settings.ui/reset-card-pressed]}
  [cofx]
  (navigation/navigate-to-cofx cofx :reset-card nil))

(fx/defn delete-card
  [{:keys [db] :as cofx}]
  (let [key-uid (get-in db [:hardwallet :application-info :key-uid])
        multiaccount-key-uid (get-in db [:multiaccount :key-uid])]
    (if (and key-uid
             (= key-uid multiaccount-key-uid))
      {:hardwallet/delete nil}
      (common/unauthorized-operation cofx))))

(fx/defn navigate-to-reset-card-screen
  {:events [:hardwallet/navigate-to-reset-card-screen]}
  [cofx]
  (navigation/navigate-to-cofx cofx :reset-card nil))

(fx/defn reset-card-next-button-pressed
  {:events [:keycard-settings.ui/reset-card-next-button-pressed]}
  [{:keys [db]}]
  {:db       (assoc-in db [:hardwallet :reset-card :disabled?] true)
   :dispatch [:hardwallet/proceed-to-reset-card]})

(fx/defn proceed-to-reset-card
  {:events [:hardwallet/proceed-to-reset-card]}
  [{:keys [db] :as cofx}]
  (let [pin-retry-counter (get-in db [:hardwallet :application-info :pin-retry-counter])
        enter-step (if (zero? pin-retry-counter) :puk :current)]
    (fx/merge cofx
              {:db (assoc-in db [:hardwallet :pin] {:enter-step  enter-step
                                                    :current     []
                                                    :puk         []
                                                    :status      nil
                                                    :error-label nil
                                                    :on-verified :hardwallet/remove-key-with-unpair})}
              (common/set-on-card-connected :hardwallet/navigate-to-enter-pin-screen)
              (common/navigate-to-enter-pin-screen))))
