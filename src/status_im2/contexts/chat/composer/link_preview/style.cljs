(ns status-im2.contexts.chat.composer.link-preview.style
  (:require [status-im2.contexts.chat.composer.constants :as constants]))

(def padding-horizontal 20)
(def preview-height 56)

(def preview-list
  {:padding-top       constants/links-padding-top
   :padding-bottom    constants/links-padding-bottom
   :margin-horizontal (- padding-horizontal)
   ;; Keep a high index, otherwise the parent gesture detector used by the
   ;; composer grabs the initiating gesture event.
   :z-index           9999})
