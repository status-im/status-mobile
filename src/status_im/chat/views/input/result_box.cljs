(ns status-im.chat.views.input.result-box
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                scroll-view
                                                touchable-highlight
                                                text
                                                icon]]
            [status-im.chat.views.input.animations.expandable :refer [expandable-view]]
            [status-im.chat.views.input.box-header :as box-header]
            [status-im.components.sync-state.offline :refer [offline-view]]))

(defview result-box-container [markup]
  [view {:flex 1}
   markup])

(defview result-box-view []
  [{:keys [markup] :as result-box} [:chat-ui-props :result-box]]
  (when result-box
    [expandable-view {:key           :result-box
                      :draggable?    true
                      :custom-header (box-header/get-header :result-box)}
     [result-box-container markup]
     [offline-view]]))
