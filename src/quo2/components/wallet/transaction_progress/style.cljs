(ns quo2.components.wallet.transaction-progress.style
  (:require [quo2.foundations.colors :as colors]))

(def title-container
  {:flex 1})


(defn title
  [override-theme]
  {:color (colors/theme-colors
           colors/neutral-100
           colors/white
           override-theme)})

(def icon
  {:margin-right 6
   :align-self   :flex-start})

(def main-container
  {
    ;; :flex 1
  ;; :margin-horizontal 20
  })   

(def box-style
  {:border-radius 16
  :border-width 1
  :border-color colors/neutral-10
  ;; :flex 1
  })     

(def item-container
  {
    ;; :justify-content    :space-between
   :align-items        :center
   :padding-horizontal 12
   :flex-direction :row
  ;;  :align-items    :center
   :padding-vertical   13
   
   })

(def top-border
  {
    :border-top-width 1
    :border-color colors/neutral-5
    :padding-top 12
   })
(def inner-container
  {
    :flex-direction :row
    
  ;;  :align-items    :center
   })

(def padding-row
  {
    :padding-horizontal 12
    :flex-direction :row
  ;;  :align-items    :center
   })

(def inner-containers
  {
    :flex-direction :row
    :background-color colors/neutral-10
  ;;  :align-items    :center
   })
(def progress-box
  {
    :width 12
    :height 20
    :border-width 1
    :border-radius 3
    :border-color colors/neutral-5
    :background-color "#F5F6F8"
    :margin-horizontal 2
  ;;  :align-items    :center
   })

(defn dot
  [override-theme]
  {:width            15
   :height           15
   :border-radius    8
   :margin-right     14.5
   :background-color (colors/theme-colors (colors/custom-color :blue 50)
                                          (colors/custom-color :blue 60)
                                          override-theme)})

(defn community-icon
  [index override-theme]
  {:width         24
   :height        24
   :border-width  1
   :border-color  (colors/theme-colors colors/white colors/black override-theme)
   :border-radius 12
   :position      :absolute
   :right         (* 20 index)})

(def communities-container
  {:flex            1
   :justify-content :center
   :align-content   :center
   :margin-right    12})

(def tag-container
  {:margin-top 8})
