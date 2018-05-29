(ns status-im.ui.screens.wallet.db
  (:require [clojure.string :as string]
            [cljs.spec.alpha :as spec]
            [status-im.i18n :as i18n]
            status-im.ui.screens.wallet.request.db
            status-im.ui.screens.wallet.send.db
            [status-im.utils.money :as money]))

(spec/def :wallet.send/recipient string?)

(spec/def :wallet/send (spec/keys :req-un [:wallet.send/recipient]))

(spec/def :wallet/wallet (spec/keys :opt-un [:wallet/send-transaction :wallet/request-transaction
                                             :wallet/transactions-queue]))

;; Placeholder namespace for wallet specs, which are a WIP depending on data
;; model we decide on for balances, prices, etc.

(defn- too-precise-amount? [amount decimals]
  (let [amount-splited (string/split amount #"[.]")]
    (and (= (count amount-splited) 2) (> (count (last amount-splited)) decimals))))

(defn parse-amount [amount decimals]
  (when-not (empty? amount)
    (let [normalized-amount (money/normalize amount)
          value (money/bignumber normalized-amount)]
      (cond
        (not (money/valid? value))
        {:error (i18n/label :t/validation-amount-invalid-number) :value value}

        (too-precise-amount? normalized-amount decimals)
        {:error (i18n/label :t/validation-amount-is-too-precise) :value value}

        :else
        {:value value}))))
