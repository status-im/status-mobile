(ns status-im.ui.screens.wallet.db
  (:require [cljs.spec.alpha :as spec]
            [status-im.i18n :as i18n]
            status-im.ui.screens.wallet.request.db
            status-im.ui.screens.wallet.send.db
            [status-im.utils.money :as money]))

(spec/def :wallet.send/recipient string?)

(spec/def :wallet/send (spec/keys :req-un [:wallet.send/recipient]))

(spec/def :wallet/balance-loading? (spec/nilable boolean?))

;; TODO these key specs are not needed, they don't do anything
(spec/def :wallet/errors any?)
(spec/def :wallet/transactions any?)
(spec/def :wallet/transactions-queue any?)
(spec/def :wallet/edit any?)
(spec/def :wallet/current-tab any?)
(spec/def :wallet/current-transaction any?)
(spec/def :wallet/modal-history? any?)
(spec/def :wallet/visible-tokens any?)
(spec/def :wallet/currency any?)
(spec/def :wallet/balance any?)

(spec/def :wallet/wallet (spec/keys :opt-un [:wallet/send-transaction
                                             :wallet/request-transaction
                                             :wallet/transactions-queue
                                             :wallet/balance-loading?
                                             :wallet/errors
                                             :wallet/transactions
                                             :wallet/edit
                                             :wallet/current-tab
                                             :wallet/current-transaction
                                             :wallet/modal-history?
                                             :wallet/visible-tokens
                                             :wallet/currency
                                             :wallet/balance]))

(defn- too-precise-amount?
  "Checks if number has any extra digit beyond the allowed number of decimals.
  It does so by checking the number against its rounded value."
  [amount decimals]
  (let [bn (money/bignumber amount)]
    (not (.eq bn
              (.round bn decimals)))))

(defn parse-amount [amount decimals]
  (when-not (empty? amount)
    (let [normalized-amount (money/normalize amount)
          value (money/bignumber normalized-amount)]
      (cond
        (not (money/valid? value))
        {:error (i18n/label :t/validation-amount-invalid-number) :value value}

        (too-precise-amount? normalized-amount decimals)
        {:error (i18n/label :t/validation-amount-is-too-precise {:decimals decimals}) :value value}

        :else
        {:value value}))))
