(ns status-im.keycard.delete-key
  (:require [status-im.multiaccounts.logout.core :as multiaccounts.logout]
            [status-im.i18n.i18n :as i18n]
            [status-im.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.keycard.common :as common]))

(fx/defn on-delete-success
  {:events [:keycard.callback/on-delete-success]}
  [{:keys [db] :as cofx}]
  (let [key-uid (get-in db [:multiaccount :key-uid])]
    (fx/merge cofx
              {:db                 (-> db
                                       (update :multiaccounts/multiaccounts dissoc key-uid)
                                       (assoc-in [:keycard :secrets] nil)
                                       (assoc-in [:keycard :application-info] nil)
                                       (assoc-in [:keycard :pin] {:status      nil
                                                                  :error-label nil
                                                                  :on-verified nil}))
               ;;FIXME delete multiaccount
               :utils/show-popup   {:title   ""
                                    :content (i18n/label :t/card-reseted)}}
              (common/clear-on-card-connected)
              (multiaccounts.logout/logout))))

(fx/defn on-delete-error
  {:events [:keycard.callback/on-delete-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[keycard] delete error" error)
  (fx/merge cofx
            {:db                              (assoc-in db [:keycard :pin] {:status      nil
                                                                            :error-label nil
                                                                            :on-verified nil})
             :keycard/get-application-info nil
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
  (let [key-uid (get-in db [:keycard :application-info :key-uid])
        multiaccount-key-uid (get-in db [:multiaccount :key-uid])]
    (if (and key-uid
             (= key-uid multiaccount-key-uid))
      {:keycard/delete nil}
      (common/unauthorized-operation cofx))))

(fx/defn navigate-to-reset-card-screen
  {:events [:keycard/navigate-to-reset-card-screen]}
  [cofx]
  (navigation/navigate-to-cofx cofx :reset-card nil))

(fx/defn reset-card-next-button-pressed
  {:events [:keycard-settings.ui/reset-card-next-button-pressed]}
  [{:keys [db]}]
  {:db       (assoc-in db [:keycard :reset-card :disabled?] true)
   :dispatch [:keycard/proceed-to-reset-card]})

(fx/defn proceed-to-reset-card
  {:events [:keycard/proceed-to-reset-card]}
  [{:keys [db] :as cofx}]
  (let [pin-retry-counter (get-in db [:keycard :application-info :pin-retry-counter])
        enter-step (if (zero? pin-retry-counter) :puk :current)]
    (fx/merge cofx
              {:db (assoc-in db [:keycard :pin] {:enter-step  enter-step
                                                 :current     []
                                                 :puk         []
                                                 :status      nil
                                                 :error-label nil
                                                 :on-verified :keycard/remove-key-with-unpair})}
              (common/set-on-card-connected :keycard/navigate-to-enter-pin-screen)
              (common/navigate-to-enter-pin-screen))))
