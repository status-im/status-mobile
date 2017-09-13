(ns status-im.utils.money
  (:require [status-im.js-dependencies :as dependencies]))

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

(defn bignumber [n]
  (dependencies/Web3.prototype.toBigNumber (str n)))

(def ether-unit-value (bignumber "1000000000000000000"))

(defn wei->ether [n]
  (.dividedBy (bignumber n) ether-unit-value))

(defn fee-value [gas gas-price]
  (.times (bignumber gas) (bignumber gas-price)))

(defn eth->usd [eth usd-price]
  (.times (bignumber eth) (bignumber usd-price)))

(defn percent-change [from to]
  (-> (.dividedBy (bignumber from) (bignumber to))
      (.minus 1)
      (.times 100)))

(defn with-precision [n decimals]
  (.round (bignumber n) decimals))
