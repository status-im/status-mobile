(ns status-im.contexts.chat.messenger.composer.link-preview.style
  (:require [quo.foundations.colors :as colors]
            [status-im.contexts.chat.messenger.composer.constants :as constants]))

(def padding-horizontal 20)
(def preview-height 56)

(def preview-list
  {:padding-top       constants/links-padding-top
   :padding-bottom    constants/links-padding-bottom
   :margin-horizontal (- padding-horizontal)
   ;; Keep a high index, otherwise the parent gesture detector used by the
   ;; composer grabs the initiating gesture event.
   :z-index           9999})

(defn unfurl-link-options
  [theme]
  {:flex-direction   :row
   :justify-content  :space-between
   :align-items      :center
   :background-color (colors/theme-colors colors/white colors/neutral-80 theme)
   :border-radius    15
   :height           48
   :padding          12})
