(ns status-im.chat.styles.command-validation
  (:require [status-im.components.styles :as st]
            [status-im.chat.constants :as constants]))

(def messages-container
  {:background-color :red
   :height           constants/request-info-height
   :padding-left     16
   :padding-top      14})

(def title
  {:color       :white
   :font-size   12
   :font-family st/font})

(def description
  (assoc title :opacity 0.69))
