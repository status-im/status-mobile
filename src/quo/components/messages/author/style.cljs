(ns quo.components.messages.author.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(defn- primary-name-top-offset
  [size]
  (when (= size 15)
    (cond platform/ios?     1
          platform/android? -0.5
          :else             0)))

(defn- primary-name-margin-bottom-offset
  [size]
  (when (and (= size 15)
             (or platform/ios? platform/android?))
    -0.25))

(defn- primary-name-layout-offsets
  [size]
  ;; NOTE(seanstrom): We need to sometimes offset the primary-name to align the baseline of the text
  ;; while avoiding shifting elements downward.
  {:top           (primary-name-top-offset size)
   :margin-bottom (primary-name-margin-bottom-offset size)})

(defn container
  [size]
  {:flex-shrink    1
   :flex-wrap      :nowrap
   :flex-direction :row
   :align-items    :baseline
   ;; NOTE(seanstrom): Because we're offseting the primary-name we need to inverse the offset on the
   ;; container to avoid shifting elements downward
   :top            (* -1 (primary-name-top-offset size))})

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
  (merge (primary-name-layout-offsets size)
         {:color       (if muted?
                         colors/neutral-50
                         (colors/theme-colors colors/neutral-100 colors/white theme))
          :flex-shrink 1}))

(defn secondary-name
  [theme]
  {:flex-shrink 999999
   :color       (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(defn icon-container
  [is-first?]
  {:margin-left (if is-first? 4 2)
   ;; NOTE(seanstrom): Because we're using flex baseline to align elements
   ;; we need to offset the icon container to match the designs.
   :top         (cond platform/ios?     1
                      platform/android? 2
                      :else             0)})

(defn time-text
  [theme]
  {:color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)})
