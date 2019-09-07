(ns status-im.ui.screens.progress.views
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.ui.screens.progress.styles :as styles]
            [status-im.ui.components.react :as react]))

;; a simple view with animated progress indicator in its center
(defview progress [_]
  [react/keyboard-avoiding-view {:style (styles/container)}
   [react/activity-indicator {:animating true}]])
