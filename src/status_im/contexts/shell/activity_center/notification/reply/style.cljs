(ns status-im.contexts.shell.activity-center.notification.reply.style
  (:require
    [quo.foundations.colors :as colors]))

(def tag
  {:background-color colors/white-opa-10})

(def tag-text
  {:color colors/white})

(def lowercase-text
  {:color          colors/white
   :text-transform :lowercase})
