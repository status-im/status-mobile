(ns legacy.status-im.ui.screens.chat.photos
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.fast-image :as fast-image]
    [legacy.status-im.ui.components.react :as react]))

(defn radius [size] (/ size 2))

(defn photo-container
  [size]
  {:position      :relative
   :border-radius (radius size)})

(defn photo-border
  ([size] (photo-border size :absolute))
  ([size position]
   {:position      position
    :width         size
    :height        size
    :border-color  colors/black-transparent
    :border-width  1
    :border-radius (radius size)}))

(defn photo-style
  [size]
  {:border-radius    (radius size)
   :width            size
   :height           size
   :background-color colors/white})

(def memo-photo-rend
  (memoize
   (fn [photo-path size accessibility-label _]
     [react/view {:style (photo-container size)}
      [fast-image/fast-image
       {:source              photo-path
        :style               (photo-style size)
        :accessibility-label (or accessibility-label :chat-icon)}]
      [react/view {:style (photo-border size)}]])))

;; "(colors/dark?)" is passed to memoized function to avoid previous themes cache
;; TODO: it's only used for old code, `quo/user-avatar` should be used instead for all the new one
(defn photo
  ^:deprecated
  [photo-path {:keys [size accessibility-label]}]
  [memo-photo-rend photo-path size accessibility-label (colors/dark?)])
