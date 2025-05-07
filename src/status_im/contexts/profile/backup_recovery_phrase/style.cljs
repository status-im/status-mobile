(ns status-im.contexts.profile.backup-recovery-phrase.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(defn seed-phrase-container
  [theme shell?]
  {:padding-horizontal 12
   :border-width       (if shell? 0 1)
   :border-color       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)
   :border-radius      16
   :background-color   (if shell?
                         colors/white-opa-10
                         (colors/theme-colors colors/white colors/neutral-80-opa-40 theme))
   :flex-direction     :row})

(def word-item
  {:align-items    :center
   :flex-direction :row
   :column-gap     4})

(defn separator
  [theme shell?]
  {:margin-vertical    12
   :margin-horizontal  12.5
   :border-width       (when platform/ios? 1)
   :border-right-width (when platform/android? 1)
   :border-color       (if shell?
                         colors/white-opa-10
                         (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))
   :border-style       :dashed})

(def step-item
  {:flex-direction   :row
   :padding-vertical 8
   :align-items      :center
   :column-gap       12})

(def blur-container
  {:border-radius 16
   :overflow      :hidden
   :margin        1})

(defn blur
  [theme]
  {:style       {:flex 1}
   :blur-radius 25
   :blur-type   theme
   :blur-amount 20})

(def slide-button
  {:position :absolute
   :bottom   12
   :left     0
   :right    0})

(def container {:flex 1})

(def seed-phrase {:margin-horizontal 20})

(def words-colum
  {:flex             1
   :row-gap          12
   :padding-vertical 8})

(defn seed-phrase-blur-overlay
  [shell? theme]
  (cond-> {:flex 1}
    ;; Variant for android + shell because we have no blur, so we provide an opaque color
    (and platform/android? shell?)   (assoc :background-color colors/neutral-60)
    (and platform/ios? shell?)       (assoc :background-color colors/white-opa-5)
    (and platform/ios? (not shell?)) (assoc :background-color
                                            (colors/theme-colors colors/white-70-blur
                                                                 colors/neutral-95-opa-70
                                                                 theme))))

(def instructions
  {:padding-horizontal 20
   :padding-top        20})

(def instructions-header
  {:margin-bottom 8})
