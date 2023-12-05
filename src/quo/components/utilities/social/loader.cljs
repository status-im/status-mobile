(ns quo.components.utilities.social.loader
  (:require-macros [quo.components.utilities.social.loader :as loader])
  (:require [clojure.string :as string]))

(def ^:private socials (loader/resolve-socials))

(defn- get-social-image*
  [social]
  (let [social-symbol (cond-> social
                        (keyword? social) name
                        :always           string/lower-case)]
    (get socials social-symbol)))

(def get-social-image (memoize get-social-image*))
