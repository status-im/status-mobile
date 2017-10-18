(ns status-im.ui.components.status-bar
  (:require [status-im.ui.components.react :as ui]
            [status-im.utils.platform :refer [platform-specific]]))

(defn status-bar [{type :type
                   :or  {type :default}}]
  (let [{:keys [height
                bar-style
                elevation
                translucent?
                color]} (get-in platform-specific [:component-styles :status-bar type])]
    [ui/view
     [ui/status-bar {:background-color (if translucent? "transparent" color)
                     :translucent      translucent?
                     :bar-style        bar-style}]
     [ui/view {:style {:height           height
                       :elevation        elevation
                       :background-color color}}]]))