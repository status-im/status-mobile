(ns fiddle.views.screens
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.add-new.views :as add-new]
            [fiddle.frame :as frame]))


(defn screens []
  [react/view {:flex-direction :row}
   [frame/frame
    [add-new/add-account]]])
