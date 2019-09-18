(ns status-im.ui.screens.chat.image.styles
  (:require [status-im.utils.platform :as platform]))

(defn image-panel-height []
  (cond
    platform/iphone-x? 199
    platform/ios? 158
    :else 172))