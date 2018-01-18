(ns status-im.ui.components.chat-preview
  (:require [status-im.ui.components.react :as components]
            [status-im.ui.screens.home.styles :as home.styles]
            [status-im.utils.core :as utils]))

(def default-attributes
  {:style           home.styles/last-message-text
   :number-of-lines 1})

(defn text [attributes s]
  (-> default-attributes
      (utils/deep-merge attributes)
      (components/text s)))
