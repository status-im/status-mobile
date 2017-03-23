(ns status-im.chat.views.input.parameter-box
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                scroll-view
                                                touchable-highlight
                                                text
                                                icon]]
            [status-im.chat.views.input.animations.expandable :refer [expandable-view]]
            [status-im.chat.views.input.utils :as input-utils]
            [status-im.commands.utils :as command-utils]
            [status-im.i18n :refer [label]]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

(defview parameter-box-container []
  [parameter-box [:chat-parameter-box]
   bot-db [:current-bot-db]]
  (when (:hiccup parameter-box)
    (command-utils/generate-hiccup (:hiccup parameter-box) bot-db)))

(defview parameter-box-view []
  [show-parameter-box? [:show-parameter-box?]]
  (when show-parameter-box?
    [expandable-view {:key        :parameter-box
                      :draggable? true}
     [parameter-box-container]]))
