(ns quo2.components.browser.browser-input.style
  (:require [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]))

(def clear-icon-container
  {:align-items     :center
   :height          20
   :justify-content :center
   :margin-left     8
   :top             4
   :width           20})

(def favicon-icon-container
  {:margin-right 2})

(defn input
  [disabled?]
  (assoc (text/text-style {:size   :paragraph-1
                           :weight :regular})
         :flex       1
         :min-height 36
         :min-width  120
         :opacity    (if disabled? 0.3 1)))

(def lock-icon-container
  {:margin-left 2})

(defn active-container
  [display?]
  {:align-items     :center
   :flex-direction  :row
   :justify-content :center
   :opacity         (if display? 1 0)})

(def default-container
  {:align-items        :center
   :bottom             0
   :flex-direction     :row
   :left               0
   :padding-horizontal 20
   :position           :absolute
   :right              0
   :top                0
   :z-index            10})

(defn text
  []
  (assoc (text/text-style {:size   :paragraph-1
                           :weight :medium})
         :color
         (colors/theme-colors colors/neutral-100 colors/white)))

(def root-container
  {:height             60
   :padding-horizontal 20
   :padding-vertical   8})
