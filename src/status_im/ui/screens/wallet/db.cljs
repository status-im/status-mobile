(ns status-im.ui.screens.wallet.db
  (:require [clojure.string :as string]
            [cljs.spec.alpha :as spec]
            [status-im.i18n :as i18n]
            status-im.ui.screens.wallet.request.db
            status-im.ui.screens.wallet.send.db
            [status-im.utils.money :as money]))

;; (angusiguess) If we add more error types we can treat them as 'one-of' the following
(spec/def :wallet/error #{:error})

(spec/def :wallet.send/recipient string?)

(spec/def :wallet/send (spec/keys :req-un [:wallet.send/recipient]))

(spec/def :wallet/wallet (spec/keys :opt    [:wallet/error]
                                    :opt-un [ :wallet/send-transaction]))

;; Placeholder namespace for wallet specs, which are a WIP depending on data
;; model we decide on for balances, prices, etc.

;; TODO(oskarth): spec for balance as BigNumber
;; TODO(oskarth): Spec for prices as as: {:from ETH, :to USD, :price 290.11, :last-day 304.17}

(defn- empty-amount? [amount]
  (or (nil? amount) (= amount "") (= amount "0") (re-matches #"0[,.]0*$" amount)))

(defn- too-precise-amount? [amount]
  (let [amount-splited (string/split amount #"[.]")]
    (and (= (count amount-splited) 2) (> (count (last amount-splited)) 18))))

(defn get-amount-validation-error [amount]
  (when-not (empty-amount? amount)
    (let [normalized-amount (money/normalize amount)]
      (cond
        (not (money/valid? normalized-amount))
        (i18n/label :t/validation-amount-invalid-number)

        (too-precise-amount? normalized-amount)
        (i18n/label :t/validation-amount-is-too-precise)))))
