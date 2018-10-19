(ns status-im.ui.screens.chat.input.parameter-box
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.screens.chat.input.animations.expandable :as expandable]
            [status-im.ui.screens.chat.styles.input.parameter-box :as style]
            [status-im.ui.components.react :as react]))

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
