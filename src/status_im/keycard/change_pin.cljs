(ns status-im.keycard.change-pin
  (:require [status-im.i18n.i18n :as i18n]
            [status-im.navigation :as navigation]
            [status-im.keycard.onboarding :as onboarding]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.keycard.common :as common]
            [status-im.keycard.login :as keycard.login]))

(fx/defn change-pin-pressed
  {:events [:keycard-settings.ui/change-pin-pressed]}
  [{:keys [db] :as cofx}]
  (let [pin-retry-counter (get-in db [:keycard :application-info :pin-retry-counter])
        enter-step (if (zero? pin-retry-counter) :puk :current)]
    (if (= enter-step :puk)
      (keycard.login/reset-pin cofx)
      (fx/merge cofx
                {:db
                 (assoc-in db [:keycard :pin] {:enter-step   enter-step
                                               :current      []
                                               :puk          []
                                               :original     []
                                               :confirmation []
                                               :status       nil
                                               :error-label  nil
                                               :on-verified  :keycard/proceed-to-change-pin})}
                (common/navigate-to-enter-pin-screen)))))

(fx/defn proceed-to-change-pin
  {:events [:keycard/proceed-to-change-pin]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:keycard :pin :enter-step] :original)
                     (assoc-in [:keycard :pin :status] nil))}
            (navigation/navigate-to-cofx :enter-pin-settings nil)))

(fx/defn discard-pin-change
  {:events [::on-cancel]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            (common/clear-pin)
            (common/hide-connection-sheet)
            (if (get-in db [:keycard :pin :puk-restore?])
              (navigation/navigate-to-cofx :multiaccounts nil)
              (navigation/navigate-to-cofx :keycard-settings nil))))

(fx/defn change-pin
  {:events [:keycard/change-pin]}
  [{:keys [db] :as cofx}]
  (let [setup-step (get-in db [:keycard :setup-step])]
    (log/debug "[keycard] change-pin"
               "setup-step" setup-step)
    (if (= setup-step :pin)
      (onboarding/load-preparing-screen cofx)
      (common/show-connection-sheet
       cofx
       {:sheet-options     {:on-cancel [::on-cancel]}
        :on-card-connected :keycard/change-pin
        :handler
        (fn [{:keys [db] :as cofx}]
          (let [pairing     (common/get-pairing db)
                new-pin     (common/vector->string
                             (get-in db [:keycard :pin :original]))
                current-pin (common/vector->string
                             (get-in db [:keycard :pin :current]))]
            (fx/merge
             cofx
             {:db (assoc-in db [:keycard :pin :status] :verifying)

              :keycard/change-pin
              {:new-pin     new-pin
               :current-pin current-pin
               :pairing     pairing}})))}))))

(fx/defn on-change-pin-success
  {:events [:keycard.callback/on-change-pin-success]}
  [{:keys [db] :as cofx}]
  (let [pin          (get-in db [:keycard :pin :original])
        puk-restore? (get-in db [:keycard :pin :puk-restore?])]
    (fx/merge cofx
              {:db               (assoc-in db [:keycard :pin] {:status       nil
                                                               :login        pin
                                                               :confirmation []
                                                               :error-label  nil})
               :utils/show-popup {:title   ""
                                  :content (i18n/label :t/pin-changed)}}
              (common/hide-connection-sheet)
              (if puk-restore?
                (navigation/navigate-to-cofx :multiaccounts nil)
                (navigation/navigate-to-cofx :keycard-settings nil))
              (when (:multiaccounts/login db)
                (common/get-keys-from-keycard)))))

(fx/defn on-change-pin-error
  {:events [:keycard.callback/on-change-pin-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[keycard] change pin error" error)
  (let [tag-was-lost? (= "Tag was lost." (:error error))
        pairing       (common/get-pairing db)]
    (fx/merge cofx
              (if tag-was-lost?
                (fx/merge cofx
                          {:db (assoc-in db [:keycard :pin :status] nil)}
                          (common/set-on-card-connected :keycard/change-pin))
                (if (re-matches common/pin-mismatch-error (:error error))
                  (fx/merge cofx
                            {:db (update-in db [:keycard :pin] merge {:status       :error
                                                                      :enter-step   :current
                                                                      :puk          []
                                                                      :current      []
                                                                      :original     []
                                                                      :confirmation []
                                                                      :sign         []
                                                                      :error-label  :t/pin-mismatch})}
                            (navigation/navigate-to-cofx :enter-pin-settings nil)
                            (common/get-application-info pairing nil))
                  (common/show-wrong-keycard-alert true))))))
