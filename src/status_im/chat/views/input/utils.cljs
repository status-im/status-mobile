(ns status-im.chat.views.input.utils
  (:require [taoensso.timbre :as log]
            [status-im.utils.platform :refer [platform-specific]]))

(def default-area-height 300)

(defn max-area-height [bottom screen-height]
  (let [status-bar-height (get-in platform-specific [:component-styles :status-bar :default :height])]
    (if (> (+ bottom default-area-height status-bar-height) screen-height)
      (- screen-height bottom status-bar-height)
      default-area-height)))