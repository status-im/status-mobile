(ns quo.components.icons.icons
  (:require-macros [quo.components.icons.icons :as icons])
  (:require
    [taoensso.timbre :as log]))

(def ^:private icons (icons/resolve-icons))

(defn icon-source
  [icon]
  (if-let [icon (get icons (name icon))]
    icon
    (do
      (log/error "could not find source for " icon " icon")
      nil)))
