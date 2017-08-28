(ns status-im.components.checkbox.view
  (:require [cljs.spec.alpha :as s]
            [status-im.components.checkbox.styles :as cst]
            [status-im.components.react :as rn]
            [status-im.utils.platform :as p]))

(defn checkbox [{:keys [on-press checked?]}]
  [rn/touchable-highlight {:on-press on-press}
   [rn/view (cst/icon-check-container checked?)
    (when checked?
      [rn/icon :check_on cst/check-icon])]])