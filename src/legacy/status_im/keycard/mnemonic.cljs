(ns legacy.status-im.keycard.mnemonic
  (:require
    [legacy.status-im.keycard.common :as common]
    legacy.status-im.keycard.fx
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/defn set-mnemonic
  [{:keys [db] :as cofx}]
  (log/debug "[keycard] set-mnemonic")
  (let [selected-id (get-in db [:intro-wizard :selected-id])
        mnemonic    (reduce
                     (fn [_ {:keys [id mnemonic]}]
                       (when (= selected-id id)
                         (reduced mnemonic)))
                     nil
                     (get-in db [:intro-wizard :multiaccounts]))]
    (rf/merge
     cofx
     {:db (-> db
              (assoc-in [:keycard :setup-step] :recovery-phrase)
              (assoc-in [:keycard :secrets :mnemonic] mnemonic))}
     (common/clear-on-card-connected)
     (common/hide-connection-sheet)
     (navigation/navigate-replace :keycard-onboarding-recovery-phrase nil))))

(rf/defn load-loading-keys-screen
  {:events [:keycard.ui/recovery-phrase-confirm-pressed
            :keycard/load-loading-keys-screen]}
  [{:keys [db] :as cofx}]
  (rf/merge
   cofx
   {:db (assoc-in db [:keycard :setup-step] :loading-keys)}
   (common/show-connection-sheet
    {:on-card-connected :keycard/load-loading-keys-screen
     :handler           (common/dispatch-event :keycard/generate-and-load-key)})))
