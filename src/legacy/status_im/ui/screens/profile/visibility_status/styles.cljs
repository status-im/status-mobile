(ns legacy.status-im.ui.screens.profile.visibility-status.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [quo.foundations.colors :as quo.colors]))

(defn visibility-status-button-container
  []
  {:background-color       (:interactive-02 @colors/theme)
   :margin-left            16
   :border-radius          16
   :border-top-left-radius 4
   :align-self             :flex-start
   :flex-direction         :row
   :align-items            :center
   :justify-content        :center
   :padding                6
   :padding-right          12})

(defn visibility-status-dot
  [{:keys [color size new-ui?]}]
  (if new-ui?
    {:background-color color
     :width            size
     :height           size
     :border-radius    (/ size 2)
     :border-width     3.5
     :border-color     (quo.colors/theme-colors quo.colors/white quo.colors/neutral-90)}
    {:background-color color
     :width            size
     :height           size
     :border-radius    (/ size 2)
     :border-width     1
     :border-color     colors/white}))

(defn visibility-status-profile-dot
  [{:keys [color size border-width margin-left new-ui?]}]
  (merge (visibility-status-dot {:color   color
                                 :size    size
                                 :new-ui? new-ui?})
         {:margin-right 6
          :margin-left  margin-left
          :border-width border-width}))

(defn visibility-status-text
  []
  {:color      (:text-01 @colors/theme)
   :font-size  16
   :text-align :center})

(defn visibility-status-subtitle
  []
  {:color        (:text-02 @colors/theme)
   :font-size    16
   :margin-left  22
   :margin-right 6})

(defn visibility-status-option
  []
  {:flex-direction :row
   :align-items    :center})

(defn visibility-status-options
  [scale position]
  {:background-color       (if (colors/dark?)
                             (:interactive-02 @colors/theme)
                             colors/white)
   :border-radius          16
   :border-top-left-radius 4
   :justify-content        :center
   :align-self             :flex-start
   :left                   16
   :top                    -76
   :padding-bottom         6
   :padding-top            8
   :transform              [{:scaleY scale}
                            {:translateY position}]})

(defn visibility-status-popover-container
  []
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(defn visibility-status-popover-ios-backdrop
  [alpha-value]
  {:flex             1
   :background-color colors/black-persist
   :opacity          alpha-value})

(defn visibility-status-popover-child-container
  [window-height]
  {:position :absolute
   :height   window-height
   :left     0
   :right    0})
