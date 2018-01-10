(ns status-im.ui.screens.wallet.assets.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.ui.screens.main-tabs.styles :as tabs.styles]
            [status-im.utils.platform :as platform]))

;; TODO(goranjovic) - the following styles will be removed once reusable components
;; from other Wallet screens have been generalized and extracted
;; The main difference is in colors - Wallet main screen uses blue background and
;; white or very light text colors, while on assets screens its usually black text on white background

(defnstyle tab [active?]
           {:flex                1
            :height              tabs.styles/tab-height
            :justify-content     :center
            :align-items         :center
            :border-bottom-width (if active? 2 1)
            :border-bottom-color (if active?
                                   styles/color-blue4
                                   styles/color-gray10-transparent)})

(def tabs-container
  {:flex-direction :row})

(defnstyle tab-title [active?]
  {:ios        {:font-size 15}
   :android    {:font-size 14}
   :text-align :center
   :color      (if active?
                 styles/color-blue4
                 styles/color-black)})

(def total-balance-container
  {:padding-top     20
   :padding-bottom  24
   :align-items     :center
   :justify-content :center})

(def total-balance
  {:flex-direction :row})

(def total-balance-value
  {:font-size 37
   :color     styles/color-black})

(defstyle total-balance-currency
  {:font-size   37
   :margin-left 9
   :color       styles/color-gray4
   :android     {:letter-spacing 1.5}
   :ios         {:letter-spacing 1.16}})

(def value-variation
  {:flex-direction :row
   :align-items    :center})

(defstyle value-variation-title
  {:font-size 14
   :color     styles/color-gray4
   :android   {:letter-spacing -0.18}
   :ios       {:letter-spacing -0.2}})

(defstyle main-button-text
  {:color              styles/color-blue4
   :padding-horizontal nil
   :android            {:font-size      13
                        :letter-spacing 0.46}
   :ios                {:font-size      15
                        :letter-spacing -0.2}})

(defstyle transactions-title
  {:margin-left 16
   :color       styles/color-gray4})

;; TODO(goranjovic): Generalize this and the set of buttons from main Wallet screen
;; into a single component e.g. button-set that would receive an ordered collection
;; of button descriptions (text, handler, disabled?, etc) and render them properly
;; while managing the position dependent formatting under the hood.
;; https://github.com/status-im/status-react/issues/2492
(defn- border [position]
  (let [radius (if platform/ios? 8 4)]
    (case position
      :first {:border-bottom-left-radius radius
              :border-top-left-radius    radius
              :ios                       {:border-width 1}
              :border-right-color        styles/color-blue4}
      :last {:border-bottom-right-radius radius
             :border-top-right-radius    radius
             :ios                        {:border-top-width    1
                                          :border-right-width  1
                                          :border-bottom-width 1}}
      {:android            {:border-left-width  1
                            :border-right-width 1}
       :ios                {:border-top-width    1
                            :border-right-width  1
                            :border-bottom-width 1}
       :border-right-color styles/color-blue4})))

(defnstyle button-bar [position]
  (merge {:border-color     styles/color-white-transparent-3
          :background-color styles/color-blue4-transparent}
         (border position)))

(def token-toolbar-container
  {:height      130
   :align-items :flex-start})

(def token-toolbar
  {:flex-direction  :column
   :align-items     :center
   :justify-content :center
   :height          130})

(defstyle token-name-title
  {:margin-top    8
   :margin-bottom 4
   :android       {:font-size 16}
   :ios           {:font-size 17}})

(defstyle token-symbol-title
  {:color   styles/color-gray4
   :android {:font-size 14}
   :ios     {:font-size 15}})
