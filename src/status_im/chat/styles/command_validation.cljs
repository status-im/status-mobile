(ns status-im.chat.styles.command-validation
  (:require [status-im.components.styles :as st]))

(def messages-container
  {:background-color :red
   :height           61
   :padding-left     16
   :padding-top      14})

(def title
  {:color       :white
   :font-size   12
   :font-family st/font})

(def description
  (assoc title :opacity 0.69))
