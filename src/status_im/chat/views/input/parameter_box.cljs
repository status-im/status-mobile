(ns status-im.chat.views.input.parameter-box
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.chat.views.input.animations.expandable :as expandable]
            [status-im.chat.views.input.box-header :as box-header]
            [status-im.chat.styles.input.parameter-box :as style]
            [status-im.commands.utils :as command-utils]
            [status-im.ui.components.react :as react]
            [taoensso.timbre :as log]))

(defview parameter-box-container []
  [{:keys [markup]} [:chat-parameter-box]
   bot-id-bot-db [:current-bot-db]]
  (when markup
    [react/view style/root
     (command-utils/generate-hiccup markup (first bot-id-bot-db) (second bot-id-bot-db))]))

(defview parameter-box-view []
  (letsubs [show-parameter-box? [:show-parameter-box?]
            parameter-box [:chat-parameter-box]]
    (let [{:keys [title]} parameter-box]
      (when show-parameter-box?
        [expandable/expandable-view
         {:key           :parameter-box
          :custom-header (when title
                           (box-header/get-header :parameter-box))}
         [parameter-box-container]]))))
