(ns status-im.extensions.capacities.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.screens.profile.photo-capture.styles :as styles]))

(defview screen-holder []
  (letsubs [{:keys [view]} [:get-screen-params]]
    (when view
      (let [[_ _ reactview] (view)]
        [react/view styles/container
         [status-bar/status-bar]
         reactview]))))