(ns status-im2.contexts.chat.composer.link-preview.style)

(def padding-horizontal 20)
(def preview-list-padding-top 12)
(def preview-list-padding-bottom 8)
(def preview-height 56)

(def preview-list
  {:padding-top       preview-list-padding-top
   :padding-bottom    preview-list-padding-bottom
   :margin-horizontal (- padding-horizontal)
   ;; Keep a high index, otherwise the parent gesture detector used by the
   ;; composer grabs the initiating gesture event.
   :z-index           9999})
