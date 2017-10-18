(ns status-im.ui.components.button.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.utils.platform :as platform]))

(def border-color styles/color-white-transparent-3)
(def border-color-high styles/color-white-transparent-4)

(def buttons-container {:flex-direction :row})

(def button-container styles/flex)

(defnstyle button [disabled?]
  {:flex-direction  :row
   :justify-content :center
   :align-items     :center
   :android         {:background-color border-color}
   :ios             (when-not disabled?
                      {:background-color border-color})})

(defn- border [position]
  (let [radius (if platform/ios? 8 4)]
    (case position
      :first {:border-bottom-left-radius radius
              :border-top-left-radius    radius
              :ios                       {:border-top-width    1
                                          :border-left-width   1
                                          :border-bottom-width 1}}
      :last {:border-bottom-right-radius radius
             :border-top-right-radius    radius
             :ios                        {:border-top-width    1
                                          :border-right-width  1
                                          :border-bottom-width 1}}
      {:android {:border-left-width  1
                 :border-right-width 1}
       :ios     {:border-width 1}})))

(defnstyle button-bar [position]
  (merge {:border-color border-color}
         (border position)))

(defnstyle button-text [disabled?]
  {:font-weight        :normal
   :color              styles/color-white
   :padding-horizontal 16
   :android            (merge
                         {:font-size        14
                          :padding-vertical 10
                          :letter-spacing   0.5}
                         (when disabled? {:opacity 0.4}))
   :ios                (merge
                         {:font-size        15
                          :padding-vertical 9
                          :letter-spacing   -0.2}
                         (when disabled? {:opacity 0.6}))})

(defstyle button-borders
  {:android {:border-radius 4}
   :ios     {:border-radius 8
             ;; Border radius is ignored with transparent background unless overflow "hidden" is used
             ;; See https://github.com/facebook/react-native/issues/13760
             :overflow      :hidden}})

(def primary-button
  (merge
    button-borders
    {:background-color styles/color-blue4}))

(def primary-button-text {:color styles/color-white})

(def secondary-button
  (merge
    button-borders
    {:background-color styles/color-blue4-transparent}))

(def secondary-button-text {:color styles/color-blue4})