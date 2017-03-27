(ns status-im.chat.views.input.utils
  (:require [taoensso.timbre :as log]
            [status-im.utils.platform :refer [platform-specific]]))

(def min-height     20)
(def default-height 300)

(defn default-container-area-height [bottom screen-height]
  (let [status-bar-height (get-in platform-specific [:component-styles :status-bar :default :height])]
    (if (> (+ bottom default-height status-bar-height) screen-height)
      (- screen-height bottom status-bar-height)
      default-height)))

(defn max-container-area-height [bottom screen-height]
  (let [status-bar-height (get-in platform-specific [:component-styles :status-bar :default :height])]
    (- screen-height bottom status-bar-height)))