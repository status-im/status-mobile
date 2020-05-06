(ns status-im.ui.components.icons.icons
  (:require-macros [status-im.ui.components.icons.icons :as icons]))

(def icons (icons/resolve-icons))

(defn icon-source [icon]
  (get icons (name icon)))
