(ns quo.components.messages.author.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(defn container
  [size]
  (cond->
    {:flex-shrink    1
     :flex-wrap      :nowrap
     :flex-direction :row
     :align-items    :center
     :height         (if (= size 15) 22 18)}))

(defn middle-dot
  [theme]
  {:color             (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)
   :margin-horizontal 2})

(defn chat-key-text
  [theme]
  {:color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)})

(defn primary-name
  [muted? theme size]
  {:color           (if muted?
                      colors/neutral-50
                      (colors/theme-colors colors/neutral-100 colors/white theme))
   ;; iOS: primary-name height is 22.3 / 18.7
   ;; Android: primary-name height is 21.8 / 18.5
   :margin-vertical (if (= size 15)
                      (if platform/ios? -0.15 0)
                      (if platform/ios? -0.35 -0.25))
   :flex-shrink     1})

(defn secondary-name
  [theme]
  {:flex-shrink 999999
   :color       (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(defn icon-container
  [is-first? size]
  {:margin-left (if is-first? 4 2)
   :padding-top (if (= size 15) 6 4)})

(defn time-text
  [theme]
  {:color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)})
