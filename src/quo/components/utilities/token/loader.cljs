(ns quo.components.utilities.token.loader
  (:require-macros [quo.components.utilities.token.loader :as loader])
  (:require [clojure.string :as string]))

(def ^:private tokens (loader/resolve-tokens))
(def ^:private safe-lower-case (comp string/lower-case str))

;; NOTE: temporarily workaround added to show USDC and USDT on different chains
;; with different decimals. This should be removed when the we move to CoinGecko API
(def ^:private dt-tokens #{"usdc(6)" "usdc(18)" "usdt(6)" "usdt(18)"})

(defn- get-token-image*
  [token]
  (let [lower-case-token-symbol (cond-> token
                                  (keyword? token) name
                                  :always          safe-lower-case)
        token-symbol            (if (dt-tokens lower-case-token-symbol)
                                  (string/replace lower-case-token-symbol #"\(\d+\)" "")
                                  lower-case-token-symbol)]
    (get tokens token-symbol)))

(def get-token-image (memoize get-token-image*))
