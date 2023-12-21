(ns quo.components.utilities.token.loader
  (:require-macros [quo.components.utilities.token.loader :as loader])
  (:require [clojure.string :as string]))

(def ^:private tokens (loader/resolve-tokens))

(defn- get-token-image*
  [token]
  (let [token-symbol (cond-> token
                       (keyword? token) name
                       :always          (comp string/lower-case str))]
    (get tokens token-symbol)))

(def get-token-image (memoize get-token-image*))
