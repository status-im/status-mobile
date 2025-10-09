(ns status-im.contexts.chat.home.chat-list-item.style
  (:require
    [quo.foundations.colors :as colors]))

(def container
  {:margin-horizontal  8
   :padding-vertical   8
   :padding-horizontal 12
   :border-radius      12
   :flex-direction     :row
   :align-items        :center})

(def chat-data-container
  {:flex        1
   :margin-left 8})

(def notification-container
  {:margin-left     :auto
   :height          20
   :width           20
   :justify-content :center
   :align-items     :center})

(def notification-container-layout
  {:flex-grow       1
   :justify-content :center
   :margin-left     8})

;; TODO: duplicate of `quo.components.common.unread-grey-dot.style`
;; Replace it when this component is defined as part of `quo.components`
(defn grey-dot
  [theme]
  {:width            8
   :height           8
   :border-radius    4
   :background-color (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)})
