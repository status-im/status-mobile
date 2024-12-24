(ns status-im.contexts.shell.activity-center.notification.mentions.style
  (:require
    [quo.foundations.colors :as colors]))

(def tag-text
  {:color colors/white})

(def mention-text
  {:color              colors/white
   :border-radius      6
   :padding-horizontal 3
   :background-color   colors/white-opa-10})
