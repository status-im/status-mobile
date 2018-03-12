(ns status-im.chat.views.input.parameter-box
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.chat.views.input.animations.expandable :as expandable]
            [status-im.chat.styles.input.parameter-box :as style]
            [status-im.commands.utils :as command-utils]
            [status-im.ui.components.react :as react]))

(defview parameter-box-container []
  [{:keys [markup]} [:chat-parameter-box]
   bot-id-bot-db [:current-bot-db]]
  (when markup
    [react/view style/root
     (command-utils/generate-hiccup markup (first bot-id-bot-db) (second bot-id-bot-db))]))

(defview parameter-box-view []
  (letsubs [show-parameter-box? [:show-parameter-box?]]
    (when show-parameter-box?
      [expandable/expandable-view {:key :parameter-box}
       [parameter-box-container]])))
