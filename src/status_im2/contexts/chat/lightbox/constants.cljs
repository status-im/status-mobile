(ns status-im2.contexts.chat.lightbox.constants)

(def ^:const small-image-size 40)
(def ^:const focused-extra-size 16)
(def ^:const focused-image-size (+ small-image-size focused-extra-size))
(def ^:const small-list-height 80)
(def ^:const small-list-padding-vertical 12)
(def ^:const top-view-height 56)
(def ^:const separator-width 16)
(def ^:const drag-threshold 100)

;;; TEXT SHEET
(def ^:const text-margin 12)
(def ^:const bar-container-height 30)
(def ^:const line-height 22)
(def ^:const text-min-height (+ bar-container-height (* line-height 2) 4))
