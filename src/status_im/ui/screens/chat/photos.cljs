(ns status-im.ui.screens.chat.photos
  (:require [quo.design-system.colors :as colors]
            [status-im.ui.components.fast-image :as fast-image]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.photos :as style]))

(def memo-photo-rend
  (memoize
   (fn [photo-path size accessibility-label _]
     [react/view {:style (style/photo-container size)}
      [fast-image/fast-image
       {:source              photo-path
        :style               (style/photo size)
        :accessibility-label (or accessibility-label :chat-icon)}]
      [react/view {:style (style/photo-border size)}]])))

;; "(colors/dark?)" is passed to memoized function to avoid previous themes cache
;; TODO: it's only used for old code, `quo/user-avatar` should be used instead for all the new one
(defn photo
  ^:deprecated
  [photo-path {:keys [size accessibility-label]}]
  [memo-photo-rend photo-path size accessibility-label (colors/dark?)])
