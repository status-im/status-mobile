(ns status-im.ui.screens.progress.views
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]))

;; a simple view with animated progress indicator in its center
(defview progress [_]
  [react/view {:flex             1
               :align-items      :center
               :justify-content  :center
               :background-color colors/white}
   [react/activity-indicator {:animating true}]])
