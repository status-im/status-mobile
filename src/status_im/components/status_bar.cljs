(ns status-im.components.status-bar
  (:require [status-im.components.react :as ui]
            [status-im.utils.platform :refer [platform-specific]]))

(defn status-bar [{type :type
                   :or  {type :default}}]
  (let [{:keys [height
                bar-style
                elevation
                translucent?
                color]} (get-in platform-specific [:component-styles :status-bar type])]
    [ui/view
     [ui/view {:style {:height           height
                       :elevation        elevation
                       :background-color color}}]]))
