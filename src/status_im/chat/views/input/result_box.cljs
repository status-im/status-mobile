(ns status-im.chat.views.input.result-box
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.ui.components.react :as react]
            [status-im.chat.views.input.animations.expandable :as expandable]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.styles :as common-styles]))

(defview result-box-view []
  [markup [:result-box-markup]]
  (when markup
    [expandable/expandable-view {:key :result-box}
     [react/view common-styles/flex
      markup]
     [connectivity/error-view]]))
