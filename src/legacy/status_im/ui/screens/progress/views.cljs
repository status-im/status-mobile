(ns legacy.status-im.ui.screens.progress.views
  (:require-macros [legacy.status-im.utils.views :refer [defview]])
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.react :as react]))

;; a simple view with animated progress indicator in its center
(defview progress
  [_]
  [react/view
   {:flex             1
    :align-items      :center
    :justify-content  :center
    :background-color colors/white}
   [react/activity-indicator {:animating true}]])
