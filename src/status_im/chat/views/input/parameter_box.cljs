(ns status-im.chat.views.input.parameter-box
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.chat.views.input.animations.expandable :refer [expandable-view]]
            [status-im.chat.views.input.box-header :as box-header]
            [status-im.commands.utils :as command-utils]
            [taoensso.timbre :as log]))

(defview parameter-box-container []
  [{:keys [markup]} [:chat-parameter-box]
   bot-id-bot-db [:current-bot-db]]
  (when markup
    (command-utils/generate-hiccup markup (first bot-id-bot-db) (second bot-id-bot-db))))

(defview parameter-box-view []
  [show-parameter-box? [:show-parameter-box?]
   {:keys [title]} [:chat-parameter-box]]
  (when show-parameter-box?
    [expandable-view {:key           :parameter-box
                      :draggable?    true
                      :custom-header (when title
                                       (box-header/get-header :parameter-box))}
     [parameter-box-container]]))
