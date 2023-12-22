(ns quo.components.utilities.token.loader
  (:require-macros [quo.components.utilities.token.loader :as loader])
  (:require [clojure.string :as string]))

(def ^:private tokens (loader/resolve-tokens))
(def ^:private safe-lower-case (comp string/lower-case str))

(defn- get-token-image*
  [token]
  (let [token-symbol (cond-> token
                       (keyword? token) name
<<<<<<< HEAD
                       :always          safe-lower-case)]
=======
                       :always          string/lower-case)]
>>>>>>> dca438344 (wallet: loading routes)
    (get tokens token-symbol)))

(def get-token-image (memoize get-token-image*))
