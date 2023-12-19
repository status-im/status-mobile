(ns legacy.status-im.keycard.wallet
  (:require
    [legacy.status-im.bottom-sheet.events :as bottom-sheet]
    [legacy.status-im.keycard.common :as common]
    [legacy.status-im.ui.screens.wallet.add-new.views :as add-new.views]
    [legacy.status-im.utils.hex :as utils.hex]
    [native-module.core :as native-module]
    [status-im2.constants :as constants]
    [utils.ethereum.eip.eip55 :as eip55]
    [utils.re-frame :as rf]))

(rf/defn show-pin-sheet
  {:events [:keycard/new-account-pin-sheet]}
  [{:keys [db] :as cofx}]
  (rf/merge
   cofx
   {:db               (-> db
                          (assoc-in [:keycard :pin :enter-step] :export-key)
                          (update-in [:keycard :pin] dissoc :export-key))
    :dismiss-keyboard nil}
   (bottom-sheet/show-bottom-sheet-old {:view {:content add-new.views/pin}})))

(rf/defn verify-pin-with-delay
  [cofx]
  {:utils/dispatch-later
   ;; We need to give previous sheet some time to be fully hidden
   [{:ms       200
     :dispatch [:wallet-legacy.accounts/verify-pin]}]})

(rf/defn hide-pin-sheet
  {:events [:keycard/new-account-pin-sheet-hide]}
  [cofx]
  (bottom-sheet/hide-bottom-sheet-old cofx))

(defn public-key->address
  [public-key]
  (let [length         (count public-key)
        normalized-key (case length
                         132 (str "0x" (subs public-key 4))
                         130 public-key
                         128 (str "0x" public-key)
                         nil)]
    (when normalized-key
      (subs (native-module/sha3 normalized-key) 26))))

(rf/defn generate-new-keycard-account
  {:events [:wallet-legacy.accounts/generate-new-keycard-account]}
  [{:keys [db]}]
  (let [path-num (inc (get-in db [:profile/profile :latest-derived-path]))
        path     (str constants/path-wallet-root "/" path-num)
        pin      (common/vector->string (get-in db [:keycard :pin :export-key]))]
    {:db
     (assoc-in
      db
      [:keycard :on-export-success]
      #(vector :wallet-legacy.accounts/account-stored
               (let [public-key (utils.hex/normalize-hex %)]
                 {;; Strip leading 04 prefix denoting uncompressed key format
                  :address    (eip55/address->checksum
                               (str "0x"
                                    (public-key->address
                                     (subs public-key 2))))
                  :public-key (str "0x" public-key)
                  :path       path})))

     :keycard/export-key {:pin pin :path path}}))

(rf/defn verify-pin
  {:events [:wallet-legacy.accounts/verify-pin]}
  [cofx]
  (common/verify-pin
   cofx
   {:pin-step          :export-key
    :on-card-connected :wallet-legacy.accounts/verify-pin
    :on-success        :wallet-legacy.accounts/generate-new-keycard-account
    :on-failure        :keycard/new-account-pin-sheet}))
