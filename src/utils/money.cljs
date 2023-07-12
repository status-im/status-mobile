(ns utils.money
  (:require ["bignumber.js" :as BigNumber]
            [clojure.string :as string]
            [utils.i18n :as i18n]))

;; The BigNumber version included in web3 sometimes hangs when dividing large
;; numbers Hence we want to use these functions instead of fromWei etc, which
;; come bundled with web3. See
;; https://github.com/MikeMcl/bignumber.js/issues/120 for this regression being
;; introduced in some JS environments. It is fixed in the MikeMcl/bignumber.js
;; repo, but not in the web3 BigNumber fork:
;; https://github.com/ethereum/web3.js/issues/877
;;
;; Additionally, while it is possible to use the BigNumber constructor without
;; stringifying the number, this only works up to some 15 significant digits:
;; https://github.com/MikeMcl/bignumber.js/issues/120
;;
;; Lastly, notice the bad rounding for native Javascript numbers above 17 digits
;; that may result in errors earlier up the call chain. Ideally all money-related
;; sensitive functions should be moved into this namespace to check for such
;; matters:
;; (str 111122223333441239) => "111122223333441230"

(defn normalize
  "A normalized string representation of an amount"
  [s]
  {:pre [(or (nil? s) (string? s))]}
  (when s
    (string/replace (string/trim s) #"," ".")))

(defn bignumber
  [n]
  (when n
    (try
      (new BigNumber (normalize (str n)))
      (catch :default _ nil))))

(defn with-precision
  [n decimals]
  (when-let [^js bn (bignumber n)]
    (.round bn decimals)))

(defn format-amount
  "Format `amount` to thousands or millions. Return nil if `amount` is not truthy."
  [amount]
  (when amount
    (cond
      (> amount 999999)
      (str (with-precision (/ amount 1000000) 1) (i18n/label :t/M))

      (< 999 amount 1000000)
      (str (with-precision (/ amount 1000) 1) (i18n/label :t/K))

      :else
      (str amount))))
