(ns quo.components.utilities.social.loader
  (:require-macros [quo.components.utilities.social.loader :as loader])
  (:require [clojure.string :as string]))

(defn socials
  [size type]
  (loader/resolve-socials size "default"))

(defn- get-social-image*
  [social size type]
  (let [social-symbol (cond-> social
                        (keyword? social) name
                        :always           string/lower-case)
        symbols  (socials "bigger" type)]
    (println "symbols" symbols)
    (get symbols social-symbol)))

(def get-social-image (memoize get-social-image*))
