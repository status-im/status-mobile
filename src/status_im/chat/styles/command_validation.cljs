(ns status-im.chat.styles.command-validation
  (:require [status-im.chat.constants :as constants]))

(def messages-container
  {:background-color :#d50000
   :height           constants/request-info-height
   :padding-left     16
   :padding-top      12})

(def title
  {:color     :white
   :font-size 14})

(def description
  (assoc title :opacity 0.9
               :font-size 12))
