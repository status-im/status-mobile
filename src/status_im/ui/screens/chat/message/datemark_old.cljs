(ns status-im.ui.screens.chat.message.datemark-old
  (:require
   [clojure.string :as string]
   [status-im.ui.components.react :as react]
   [status-im.ui.screens.chat.styles.message.datemark-old :as style]))

(defn chat-datemark
  [value]
  [react/touchable-without-feedback
   {:on-press (fn [_]
                (react/dismiss-keyboard!))}
   [react/view style/datemark-mobile
    [react/text {:style (style/datemark-text)}
     (string/capitalize value)]]])