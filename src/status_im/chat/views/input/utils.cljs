(ns status-im.chat.views.input.utils
  (:require [taoensso.timbre :as log]
            [status-im.ui.components.toolbar.styles :as toolbar-st]
            [status-im.utils.platform :as p]))

(def min-height     19)
(def default-height 300)

(defn default-container-area-height [bottom screen-height]
  (let [status-bar-height (get-in p/platform-specific [:component-styles :status-bar :main :height])]
    (if (> (+ bottom default-height status-bar-height) screen-height)
      (- screen-height bottom status-bar-height)
      default-height)))

(defn max-container-area-height [bottom screen-height]
  (let [status-bar-height (get-in p/platform-specific [:component-styles :status-bar :main :height])
        toolbar-height (:height toolbar-st/toolbar)
        margin-top (+ status-bar-height (/ toolbar-height 2))]
    (- screen-height bottom margin-top)))
