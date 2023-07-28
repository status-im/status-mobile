(ns status-im2.contexts.shell.activity-center.notification.mentions.style
  (:require [quo2.foundations.colors :as colors]))

(def tag
  {:background-color colors/white-opa-10})

(def tag-text
  {:color colors/white})

(def mention-text
  {:color              colors/white
   :border-radius      6
   :padding-horizontal 3
   :background-color   colors/white-opa-10})
