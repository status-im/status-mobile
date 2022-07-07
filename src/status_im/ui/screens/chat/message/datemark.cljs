(ns status-im.ui.screens.chat.message.datemark
  (:require [status-im.ui.components.react :as react]
            [clojure.string :as string]
            [status-im.ui.screens.chat.styles.message.datemark :as style]))

(defn chat-datemark [value]
  [react/touchable-without-feedback
   {:on-press (fn [_]
                (react/dismiss-keyboard!))}
   [react/view style/datemark-mobile
    [react/text {:style (style/datemark-text)}
     (string/capitalize value)]]])
