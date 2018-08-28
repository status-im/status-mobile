(ns status-im.ui.screens.progress.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.screens.progress.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.react :as components]))

;; a simple view with animated progress indicator in its center
(defview progress [_]
  [react/keyboard-avoiding-view {:style styles/container}
   [components/activity-indicator {:animating true}]])
