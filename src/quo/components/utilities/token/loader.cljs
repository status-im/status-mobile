(ns quo.components.utilities.token.loader
  (:require-macros [quo.components.utilities.token.loader :as loader])
  (:require [clojure.string :as string]))

(def ^:private tokens (loader/resolve-tokens))
(def ^:private safe-lower-case (comp string/lower-case str))

;; NOTE: temporarily workaround added to show USDC and USDT on different chains
;; with different decimals. This should be removed when the we move to CoinGecko API
(def ^:private tokens-with-different-decimals #{"usdc (evm)" "usdc (bsc)" "usdt (evm)" "usdt (bsc)"})

(defn- get-token-image*
  [token]
  (let [lower-case-token-symbol (cond-> token
                                  (keyword? token) name
                                  :always          safe-lower-case)
        token-symbol            (if (tokens-with-different-decimals lower-case-token-symbol)
                                  (-> (string/split lower-case-token-symbol #" ") first)
                                  lower-case-token-symbol)]
    (get tokens token-symbol)))

(def get-token-image (memoize get-token-image*))
