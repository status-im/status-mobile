(ns quo2.components.icon
  (:require [status-im.ui.components.icons.icons :as icons]))

(defn icon
  ([icon-name] (icon icon-name nil))
  ([icon-name {:keys [size] :as props}]
   (let [size (or size 20)]
     [icons/icon (str (name icon-name) size) (merge props
                                                    {:width size
                                                     :height size})])))