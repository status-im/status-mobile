(ns status-im2.contexts.shell.activity-center.notification.reply.style
  (:require [quo2.foundations.colors :as colors]))

(def tag
  {:background-color colors/white-opa-10})

(def tag-text
  {:color colors/white})

(def lowercase-text
  {:color          colors/white
   :text-transform :lowercase})
