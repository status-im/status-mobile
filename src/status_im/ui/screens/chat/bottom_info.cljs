(ns status-im.ui.screens.chat.bottom-info
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.main :as styles]))

(defn overlay
  [{:keys [on-click-outside]} items]
  [react/view styles/bottom-info-overlay
   [react/touchable-highlight {:on-press on-click-outside
                               :style    styles/overlay-highlight}
    [react/view nil]]
   items])

(defn container
  [height & children]
  [react/view {:style (styles/bottom-info-container height)}
   (into [react/view] children)])
