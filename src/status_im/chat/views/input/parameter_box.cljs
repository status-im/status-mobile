(ns status-im.chat.views.input.parameter-box
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.chat.views.input.animations.expandable :as expandable]
            [status-im.chat.styles.input.parameter-box :as style]
            [status-im.ui.components.react :as react]
            [taoensso.timbre :as log]))

(defview parameter-box-container []
  (letsubs [parameter-box [:chat-parameter-box]]
    (when parameter-box
      [react/view style/root
       [parameter-box]])))

(defview parameter-box-view []
  (letsubs [show-box? [:show-parameter-box?]]
    (when show-box?
      [react/view]
      [expandable/expandable-view {:key :parameter-box}
       [parameter-box-container]])))
