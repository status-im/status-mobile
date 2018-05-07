(ns status-im.chat.views.message.datemark
  (:require [status-im.ui.components.react :as react]
            [clojure.string :as str]
            [status-im.chat.styles.message.datemark :as st]))

(defn chat-datemark [value]
  [react/view st/datemark-wrapper
   [react/view st/datemark
    [react/text {:style st/datemark-text}
     (str/capitalize value)]]])
