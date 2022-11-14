(ns status-im.keycard.change-pin
  (:require [status-im.i18n.i18n :as i18n]
            [status-im2.navigation.events :as navigation]
            [status-im.keycard.onboarding :as onboarding]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.keycard.common :as common]
            [status-im.utils.security :as security]))

(fx/defn change-credentials-pressed
  {:events [:keycard-settings.ui/change-credentials-pressed]}
  [{:keys [db] :as cofx} changing]
  (fx/merge cofx
            {:db
             (assoc-in db [:keycard :pin] {:enter-step       :current
                                           :current          []
                                           :puk              []
                                           :original         []
                                           :confirmation     []
                                           :puk-original     []
                                           :puk-confirmation []
                                           :status           nil
                                           :error-label      nil
                                           :on-verified      (case changing
                                                               :pin     :keycard/proceed-to-change-pin
                                                               :puk     :keycard/proceed-to-change-puk
                                                               :pairing :keycard/proceed-to-change-pairing)})}
            (common/navigate-to-enter-pin-screen)))

(fx/defn proceed-to-change-pin
  {:events [:keycard/proceed-to-change-pin]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:keycard :pin :enter-step] :original)
                     (assoc-in [:keycard :pin :status] nil))}
            (navigation/navigate-replace :enter-pin-settings nil)))

(fx/defn proceed-to-change-puk
  {:events [:keycard/proceed-to-change-puk]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:keycard :pin :enter-step] :puk-original)
                     (assoc-in [:keycard :pin :status] nil))}
            (navigation/navigate-replace :enter-pin-settings nil)))

(fx/defn proceed-to-change-pairing
  {:events [:keycard/proceed-to-change-pairing]}
  [{:keys [db] :as cofx}]
  (navigation/navigate-replace cofx :change-pairing-code nil))

(fx/defn discard-pin-change
  {:events [::on-cancel]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            (common/clear-pin)
            (common/hide-connection-sheet)
            (if (get-in db [:keycard :pin :puk-restore?])
              (navigation/navigate-to-cofx :multiaccounts nil)
              (navigation/set-stack-root :profile-stack [:my-profile :keycard-settings]))))

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
          (let [new-pin     (common/vector->string
                             (get-in db [:keycard :pin :original]))
                current-pin (common/vector->string
                             (get-in db [:keycard :pin :current]))]
            (fx/merge
             cofx
             {:db (assoc-in db [:keycard :pin :status] :verifying)

              :keycard/change-pin
              {:new-pin     new-pin
               :current-pin current-pin}})))}))))

(fx/defn change-puk
  {:events [:keycard/change-puk]}
  [{:keys [db] :as cofx}]
  (common/show-connection-sheet
   cofx
   {:sheet-options     {:on-cancel [::on-cancel]}
    :on-card-connected :keycard/change-puk
    :handler
    (fn [{:keys [db] :as cofx}]
      (let [puk (common/vector->string
                 (get-in db [:keycard :pin :puk-original]))
            pin (common/vector->string
                 (get-in db [:keycard :pin :current]))]
        (fx/merge
         cofx
         {:db                 (assoc-in db [:keycard :pin :status] :verifying)
          :keycard/change-puk {:puk puk
                               :pin pin}})))}))

(fx/defn change-pairing
  {:events [:keycard/change-pairing]}
  [{:keys [db] :as cofx}]
  (common/show-connection-sheet
   cofx
   {:sheet-options     {:on-cancel [::on-cancel]}
    :on-card-connected :keycard/change-pairing
    :handler
    (fn [{:keys [db] :as cofx}]
      (let [pairing (get-in db [:keycard :pin :pairing-code])
            pin     (common/vector->string
                     (get-in db [:keycard :pin :current]))]
        (fx/merge
         cofx
         {:db                     (assoc-in db [:keycard :pin :status] :verifying)
          :keycard/change-pairing {:pairing pairing
                                   :pin     pin}})))}))

(fx/defn change-pairing-code
  {:events [:keycard/change-pairing-code]}
  [{:keys [db] :as cofx} pairing]
  (fx/merge
   cofx
   {:db (assoc-in db [:keycard :pin :pairing-code] (security/unmask pairing))}
   (change-pairing)))

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
                (navigation/set-stack-root :profile-stack [:my-profile :keycard-settings]))
              (when (:multiaccounts/login db)
                (common/get-keys-from-keycard)))))

(fx/defn on-change-puk-success
  {:events [:keycard.callback/on-change-puk-success]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db               (assoc-in db [:keycard :pin] {:status           nil
                                                             :puk-original     []
                                                             :puk-confirmation []
                                                             :error-label      nil})
             :utils/show-popup {:title   ""
                                :content (i18n/label :t/puk-changed)}}
            (common/hide-connection-sheet)
            (navigation/set-stack-root :profile-stack [:my-profile :keycard-settings])))

(fx/defn on-change-pairing-success
  {:events [:keycard.callback/on-change-pairing-success]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db               (assoc-in db [:keycard :pin] {:status           nil
                                                             :pairing-code     nil
                                                             :error-label      nil})
             :utils/show-popup {:title   ""
                                :content (i18n/label :t/pairing-changed)}}
            (common/hide-connection-sheet)
            (navigation/set-stack-root :profile-stack [:my-profile :keycard-settings])))

(fx/defn on-change-pin-error
  {:events [:keycard.callback/on-change-pin-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[keycard] change pin error" error)
  (let [tag-was-lost? (common/tag-lost? (:error error))
        pin-retries (common/pin-retries (:error error))]
    (fx/merge cofx
              (if tag-was-lost?
                (fx/merge cofx
                          {:db (assoc-in db [:keycard :pin :status] nil)}
                          (common/set-on-card-connected :keycard/change-pin))
                (if-not (nil? pin-retries)
                  (fx/merge cofx
                            {:db (-> db
                                     (assoc-in [:keycard :application-info :pin-retry-counter] pin-retries)
                                     (update-in [:keycard :pin] assoc
                                                :status       :error
                                                :enter-step   :current
                                                :puk          []
                                                :current      []
                                                :original     []
                                                :confirmation []
                                                :sign         []
                                                :error-label  :t/pin-mismatch))}
                            (when (zero? pin-retries) (common/frozen-keycard-popup))
                            (navigation/navigate-to-cofx :enter-pin-settings nil))
                  (common/show-wrong-keycard-alert))))))
