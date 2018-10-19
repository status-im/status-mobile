(ns status-im.ui.screens.chat.message.datemark
  (:require [status-im.ui.components.react :as react]
            [clojure.string :as string]
            [status-im.ui.screens.chat.styles.message.datemark :as style]))

(defn chat-datemark [value]
  [react/view style/datemark-wrapper
   [react/view style/datemark
    [react/text {:style style/datemark-text}
     (string/capitalize value)]]])
