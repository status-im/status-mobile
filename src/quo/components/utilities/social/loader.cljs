(ns quo.components.utilities.social.loader
  (:require-macros [quo.components.utilities.social.loader :as loader])
  (:require [taoensso.timbre :as log]))

(def ^:private socials (loader/resolve-socials))

(defn get-social-image
  [social]
  (if-let [res (get socials social)]
    res
    (log/error "could not find source for " social " social icon")))

