(ns status-im2.contexts.chat.home.chat-list-item.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  []
  {:margin-horizontal  8
   :padding-vertical   8
   :padding-horizontal 12
   :border-radius      12
   :flex-direction     :row
   :align-items        :center})

(def chat-data-container
  {:flex         1
   :margin-left  8
   :margin-right 16})

(def notification-container
  {:margin-left     :auto
   :height          20
   :width           20
   :justify-content :center
   :align-items     :center})

;; TODO: duplicate of `quo2.components.common.unread-grey-dot.style`
;; Replace it when this component is defined as part of `quo2.components`
(defn grey-dot
  []
  {:width            8
   :height           8
   :border-radius    4
   :background-color (colors/theme-colors colors/neutral-40 colors/neutral-60)})
