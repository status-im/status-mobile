(ns status-im.contexts.onboarding.create-password.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(def heading {:margin-bottom 20})
(def heading-subtitle {:color colors/white})
(def heading-title (assoc heading-subtitle :margin-bottom 8))

(def space-between-inputs {:height 16})

(def password-tips
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-horizontal 20})

(def form-container
  {:justify-content    :space-between
   :padding-top        12
   :padding-horizontal 20})

(def footer-container {:padding-bottom 12})
(def footer-button-container {:margin-top 20 :padding-horizontal 20})

(def blur-options
  {:blur-amount        34
   :blur-radius        20
   :blur-type          :transparent
   :overlay-color      :transparent
   :background-color   (if platform/android?
                         colors/neutral-100
                         colors/neutral-80-opa-1-blur)
   :padding-vertical   0
   :padding-horizontal 0})

(def page-nav-height 56)
