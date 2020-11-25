(ns status-im.keycard.export-key
  (:require [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.keycard.wallet :as wallet]
            [status-im.keycard.common :as common]))

(fx/defn on-export-key-error
  {:events [:keycard.callback/on-export-key-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[keycard] export key error" error)
  (let [tag-was-lost? (common/tag-lost? (:error error))
        pin-retries (common/pin-retries (:error error))]
    (cond tag-was-lost?
          (fx/merge cofx
                    {:db (assoc-in db [:keycard :pin :status] nil)}
                    (common/set-on-card-connected :wallet.accounts/generate-new-keycard-account))

          (not (nil? pin-retries))
          (fx/merge cofx
                    {:db (-> db
                             (assoc-in [:keycard :application-info :pin-retry-counter] pin-retries)
                             (update-in [:keycard :pin] assoc
                                        :status       :error
                                        :enter-step   :export-key
                                        :puk          []
                                        :current      []
                                        :original     []
                                        :confirmation []
                                        :sign         []
                                        :export-key   []
                                        :error-label  :t/pin-mismatch))}
                    (common/hide-connection-sheet)
                    (when (zero? pin-retries) (common/frozen-keycard-popup)))
          :else
          (fx/merge cofx
                    (common/show-wrong-keycard-alert true)
                    (common/clear-pin)
                    (common/hide-connection-sheet)))))

(fx/defn on-export-key-success
  {:events [:keycard.callback/on-export-key-success]}
  [{:keys [db] :as cofx} pubkey]
  (let [callback-fn (get-in db [:keycard :on-export-success])]
    (fx/merge cofx
              {:dispatch (callback-fn pubkey)}
              (wallet/hide-pin-sheet)
              (common/clear-pin)
              (common/hide-connection-sheet))))
