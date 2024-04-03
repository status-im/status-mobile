(ns quo.components.messages.author.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(defn- primary-name-offset
  [size]
  (when (= size 15)
    (cond platform/ios?     1
          platform/android? -0.5
          :else             0)))

(defn container
  [size]
  {:flex-shrink    1
   :flex-wrap      :nowrap
   :flex-direction :row
   :align-items    :baseline
   :top            (* -1 (primary-name-offset size))})

(def details-container
  {:flex-direction :row
   :margin-left    8})

(defn middle-dot
  [theme]
  {:color             (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)
   :margin-horizontal 2})

(defn chat-key-text
  [theme]
  {:color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)})

(defn primary-name
  [muted? theme size]
  {:color       (if muted?
                  colors/neutral-50
                  (colors/theme-colors colors/neutral-100 colors/white theme))
   :flex-shrink 1
   :top         (primary-name-offset size)})

(defn secondary-name
  [theme]
  {:flex-shrink 999999
   :color       (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(defn icon-container
  [is-first?]
  {:margin-left   (if is-first? 4 2)
   :margin-bottom 0
   :top           (cond platform/ios?     1
                        platform/android? 2
                        :else             0)})

(defn time-text
  [theme]
  {:color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)})
