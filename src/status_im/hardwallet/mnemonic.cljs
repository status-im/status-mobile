(ns status-im.hardwallet.mnemonic
  (:require [clojure.string :as string]
            [status-im.ethereum.mnemonic :as mnemonic]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.hardwallet.common :as common]
            status-im.hardwallet.fx))

(fx/defn on-generate-mnemonic-success
  {:events [:hardwallet.callback/on-generate-mnemonic-success]}
  [{:keys [db] :as cofx} mnemonic]
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:hardwallet :setup-step] :recovery-phrase)
                     (assoc-in [:hardwallet :secrets :mnemonic] mnemonic))}
            (common/clear-on-card-connected)
            (navigation/navigate-replace-cofx :keycard-onboarding-recovery-phrase nil)))

(fx/defn set-mnemonic
  {:events [:test-mnemonic]}
  [{:keys [db] :as cofx}]
  (let [selected-id (get-in db [:intro-wizard :selected-id])
        accounts    (get-in db [:intro-wizard :multiaccounts])
        mnemonic    (->> accounts
                         (filter (fn [{:keys [id]}]
                                   (= id selected-id)))
                         first
                         :mnemonic)]
    (fx/merge cofx
              {:db (assoc-in db [:hardwallet :secrets :mnemonic] mnemonic)}
              (on-generate-mnemonic-success mnemonic))))

(fx/defn generate-mnemonic
  {:events [:hardwallet/generate-mnemonic]}
  [cofx]
  (let [{:keys [pairing]} (get-in cofx [:db :hardwallet :secrets])]
    {:hardwallet/generate-mnemonic {:pairing pairing
                                    :words   (string/join "\n" mnemonic/dictionary)}}))

(fx/defn proceed-with-generating-mnemonic
  [{:keys [db] :as cofx}]
  (let [pin (or (get-in db [:hardwallet :secrets :pin])
                (common/vector->string (get-in db [:hardwallet :pin :current])))]
    (if (empty? pin)
      (fx/merge cofx
                {:db (assoc-in db [:hardwallet :pin] {:enter-step  :current
                                                      :on-verified :hardwallet/generate-mnemonic
                                                      :current     []})}
                (navigation/navigate-to-cofx :keycard-onboarding-pin nil))
      (generate-mnemonic cofx))))

(fx/defn load-generating-mnemonic-screen
  {:events [:hardwallet/load-generating-mnemonic-screen]}
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:hardwallet :card-connected?])]
    (fx/merge cofx
              {:db (assoc-in db [:hardwallet :setup-step] :generating-mnemonic)}
              (common/set-on-card-connected :hardwallet/load-generating-mnemonic-screen)
              (if card-connected?
                (common/dispatch-event :hardwallet/generate-mnemonic)
                (common/show-pair-sheet {})))))

(fx/defn on-generate-mnemonic-error
  {:events [:hardwallet.callback/on-generate-mnemonic-error]}
  [{:keys [db] :as cofx} {:keys [error code]}]
  (log/debug "[hardwallet] generate mnemonic error: " error)
  (fx/merge cofx
            {:db (assoc-in db [:hardwallet :setup-error] error)}
            (common/set-on-card-connected :hardwallet/load-generating-mnemonic-screen)
            (common/process-error code error)))

(fx/defn proceed-to-generate-mnemonic
  {:events [:hardwallet/proceed-to-generate-mnemonic]}
  [{:keys [db] :as cofx}]
  (if (= (get-in db [:hardwallet :flow]) :create)
    (load-generating-mnemonic-screen cofx)
    {:db (assoc-in db [:hardwallet :setup-step] :recovery-phrase)}))

(fx/defn load-loading-keys-screen
  {:events [:hardwallet.ui/recovery-phrase-confirm-pressed
            :hardwallet/load-loading-keys-screen]}
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:hardwallet :card-connected?])]
    (fx/merge cofx
              {:db (assoc-in db [:hardwallet :setup-step] :loading-keys)}
              (common/set-on-card-connected :hardwallet/load-loading-keys-screen)
              (if card-connected?
                (common/dispatch-event :hardwallet/generate-and-load-key)
                (common/show-pair-sheet {})))))
