(ns status-im.ui.screens.wallet.db
  (:require [cljs.spec.alpha :as spec]
            status-im.ui.screens.wallet.request.db
            status-im.ui.screens.wallet.send.db
            [status-im.i18n :as i18n]
            [clojure.string :as string]))

;; (angusiguess) If we add more error types we can treat them as 'one-of' the following
(spec/def :wallet/error #{:error})

(spec/def :wallet.send/recipient string?)

(spec/def :wallet/send (spec/keys :req-un [:wallet.send/recipient]))

(spec/def :wallet/wallet (spec/keys :opt [:wallet/error]))

;; Placeholder namespace for wallet specs, which are a WIP depending on data
;; model we decide on for balances, prices, etc.

;; TODO(oskarth): spec for balance as BigNumber
;; TODO(oskarth): Spec for prices as as: {:from ETH, :to USD, :price 290.11, :last-day 304.17}

(defn get-amount-validation-error [amount web3]
  (let [amount' (string/replace amount #"," ".")
        amount-splited (string/split amount' #"[.]")]
    (cond
      (or (nil? amount) (= amount "") (re-matches #"0[,.]0*$" amount))
      nil

      (= amount "0")
      (i18n/label :t/validation-amount-invalid)

      (or (js/isNaN (js/parseFloat amount'))
          (try (when (<= (.toWei web3 amount' "ether") 0) true)
               (catch :default err true)))
      (i18n/label :t/validation-amount-invalid-number)

      (and (= (count amount-splited) 2) (> (count (last amount-splited)) 18))
      (i18n/label :t/validation-amount-is-too-precise)

      :else nil)))