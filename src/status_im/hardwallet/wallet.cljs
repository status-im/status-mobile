(ns status-im.hardwallet.wallet
  (:require [status-im.ethereum.core :as ethereum]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.fx :as fx]
            [status-im.hardwallet.common :as common]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.constants :as constants]
            [status-im.ethereum.eip55 :as eip55]))

(fx/defn generate-new-keycard-account
  {:events [:wallet.accounts/generate-new-keycard-account]}
  [{:keys [db] :as cofx}]
  (let [path-num (inc (get-in db [:multiaccount :latest-derived-path]))
        path (str constants/path-wallet-root "/" path-num)
        card-connected? (get-in db [:hardwallet :card-connected?])
        pin (common/vector->string (get-in db [:hardwallet :pin :export-key]))
        pairing (common/get-pairing db)]
    (if card-connected?
      (fx/merge cofx
                {:db (assoc-in db [:hardwallet :on-export-success]
                               #(vector :wallet.accounts/account-generated
                                        {:name (str "Account " path-num)
                                         ;; Strip leading 04 prefix denoting uncompressed key format
                                         :address (eip55/address->checksum (str "0x" (ethereum/public-key->address (subs % 2))))
                                         :public-key (str "0x" %)
                                         :path path
                                         :color (rand-nth colors/account-colors)}))
                 :hardwallet/export-key {:pin pin :pairing pairing :path path}}
                (navigation/navigate-to-cofx :keycard-processing nil)
                (common/set-on-card-connected :wallet.accounts/generate-new-keycard-account))
      (fx/merge cofx
                (common/set-on-card-connected :wallet.accounts/generate-new-keycard-account)
                (navigation/navigate-to-cofx :keycard-processing nil)))))
