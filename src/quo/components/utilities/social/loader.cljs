(ns quo.components.utilities.social.loader
  (:require-macros [quo.components.utilities.social.loader :as loader])
  (:require [taoensso.timbre :as log]))

(def socials (loader/resolve-socials))

(defn get-social-image
  [social]
  (if-let [res (get socials social)]
    res
    (do
      (log/error "could not find source for " social " social icon")
      nil)))

