(ns status-im.app.wallet.events
  (:require
    [utils.re-frame :as rf]))

;; TODO(volodymyr.kozieiev): Logic for starting swap was extracted from wallet home ui.
;; It needs to be rewritten to get the accounts data right from the rf-db
(rf/reg-event-fx :app.wallet/start-swap
 (fn [{:keys [_db]} [operable-accounts]]
   (let [multiple-accounts?    (> (count operable-accounts) 1)
         first-account-address (:address (first operable-accounts))
         modal-to-open         (if multiple-accounts?
                                 :screen/wallet.swap-select-account
                                 :screen/wallet.swap-select-asset-to-pay)]
     {:fx [[:dispatch [:wallet/clean-send-data]]
           [:dispatch [:wallet/clean-swap]]
           (when-not multiple-accounts?
             [:dispatch
              [:wallet/switch-current-viewing-account
               first-account-address]])
           [:dispatch [:open-modal modal-to-open]]]})))

