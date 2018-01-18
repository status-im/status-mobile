(ns status-im.ui.components.button.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.utils.platform :as platform]))

(def border-color styles/color-white-transparent-3)
(def border-color-high styles/color-white-transparent-4)

(def buttons-container {:flex-direction :row})

(def button-container styles/flex)

(def button
  {:flex-direction     :row
   :justify-content    :center
   :align-items        :center
   :padding-horizontal 12})


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

(defstyle button-text
  {:font-weight        :normal
   :color              styles/color-white
   :padding-horizontal 16
   :android            {:font-size        14
                        :padding-vertical 10
                        :letter-spacing   0.5}
   :ios                {:font-size        15
                        :padding-vertical 9
                        :letter-spacing   -0.2}})

(defstyle button-text-disabled
  {:android {:opacity 0.4}
   :ios     {:opacity 0.6}})


(defstyle button-borders
  {:border-radius 8
   :ios           {;; Border radius is ignored with transparent background unless overflow "hidden" is used
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
