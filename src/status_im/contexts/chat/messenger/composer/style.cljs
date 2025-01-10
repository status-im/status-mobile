(ns status-im.contexts.chat.messenger.composer.style
  (:require
    [quo.foundations.colors :as colors]
    [quo.foundations.typography :as typography]))

(defn input-text
  [theme]
  (assoc typography/paragraph-1
         :color               (colors/theme-colors :black :white theme)
         :text-align-vertical :top
         :top                 0
         :left                0
         :max-height          150))
