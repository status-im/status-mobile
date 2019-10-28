(ns fiddle.views.screens
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.intro.views :as intro]
            [status-im.ui.screens.wallet.add-new.views :as add-new]
            [fiddle.frame :as frame]))

;;:generate-key
;;:choose-key
;;:select-key-storage
;;:create-code
;;:confirm-code
;;:enable-fingerprint
(re-frame/reg-sub :intro-wizard (fn [_] {:step :generate-key  :generating-keys? false}))

(defn screens []
  [react/view {:flex-direction :row}
   [frame/frame
    [intro/intro]]
   [frame/frame
    [intro/wizard]]
   [frame/frame
    [add-new/add-account]]])
