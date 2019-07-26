(ns status-im.ui.screens.chat.input.parameter-box
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.screens.chat.input.animations.expandable :as expandable]
            [status-im.ui.screens.chat.styles.input.parameter-box :as style]
            [status-im.ui.components.react :as react]))

(defview parameter-box-container []
  (letsubs [parameter-box [:chats/parameter-box]]
    (when parameter-box
      [parameter-box])))

(defview parameter-box-view []
  (letsubs [show-box? [:chats/show-parameter-box?]]
    (when show-box?
      [expandable/expandable-view {:key :parameter-box}
       ;; TODO need to add the whole payload (and details about previous parameters?)
       [parameter-box-container]])))
