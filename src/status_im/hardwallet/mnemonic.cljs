(ns status-im.hardwallet.mnemonic
  (:require [status-im.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.hardwallet.common :as common]
            status-im.hardwallet.fx))

(fx/defn set-mnemonic
  [{:keys [db] :as cofx}]
  (log/debug "[hardwallet] set-mnemonic")
  (let [selected-id (get-in db [:intro-wizard :selected-id])
        mnemonic (reduce
                  (fn [_ {:keys [id mnemonic]}]
                    (when (= selected-id id)
                      (reduced mnemonic)))
                  nil
                  (get-in db [:intro-wizard :multiaccounts]))]
    (fx/merge
     cofx
     {:db (-> db
              (assoc-in [:hardwallet :setup-step] :recovery-phrase)
              (assoc-in [:hardwallet :secrets :mnemonic] mnemonic))}
     (common/clear-on-card-connected)
     (navigation/navigate-replace :keycard-onboarding-recovery-phrase nil))))

(fx/defn load-loading-keys-screen
  {:events [:hardwallet.ui/recovery-phrase-confirm-pressed
            :hardwallet/load-loading-keys-screen]}
  [{:keys [db] :as cofx}]
  (fx/merge
   cofx
   {:db (assoc-in db [:hardwallet :setup-step] :loading-keys)}
   (common/show-connection-sheet
    {:on-card-connected :hardwallet/load-loading-keys-screen
     :handler           (common/dispatch-event :hardwallet/generate-and-load-key)})))
