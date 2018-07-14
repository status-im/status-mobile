(ns status-im.ui.screens.usage-data.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]))

(def window-width (:width (react/get-dimensions "window")))
(def window-height (:height (react/get-dimensions "window")))
(defn scaled-x [n] (* (/ window-width 375) n))
(defn scaled-y [n] (* (/ window-height 667) n))

(def usage-data-view
  {:flex               1
   :background-color   colors/white
   :align-items        :center
   :justify-content    :center})

(def image-container
  {;; on screens less tall than iPhone 5, let's not show the image at all
   :display         (if (>= window-height 568) "flex" "none")
   :align-items     :center
   :justify-content :center
   :margin-bottom   (scaled-y 30)})

(def usage-data-image
  {:width  (* (/ 390 432) (scaled-y 138))
   :height (scaled-y 138)})

(defstyle help-improve-text
  {:text-align    :center
   :color         colors/black
   :margin-bottom (scaled-y 8)
   :margin-left   46
   :margin-right  46
   :ios           {:line-height    28
                   :font-size      22
                   :font-weight    :bold
                   :letter-spacing -0.3}
   :android       {:font-size   24
                   :line-height 30}})

(def help-improve-text-description
  {:line-height    21
   :margin-bottom  (scaled-y 26)
   :margin-left    34
   :margin-right   34
   :text-align     :center
   :color          colors/gray})

(def learn-what-we-collect-link
  {:text-align    :center
   :color         colors/blue
   :margin-left   61
   :margin-right  63})

(def bottom-button-container
  {:flex-direction :row
   ;; we need to make a margin smaller on iPhone 5(s)
   :margin-top     (scaled-y (if (and platform/ios?
                                      (> window-height 568))
                               96 48))
   :margin-left    41
   :margin-right   42})

(def share-button
  {:padding-horizontal  18
   :width               (scaled-x 138)
   :margin-right        16})

(def dont-share-button
  {:padding-horizontal  18
   ;; don't do text wrap on super small devices
   :min-width           130
   :width               (scaled-x 138)})
