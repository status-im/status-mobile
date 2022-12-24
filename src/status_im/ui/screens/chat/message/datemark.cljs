(ns status-im.ui.screens.chat.message.datemark
  (:require
   [clojure.string :as string]
   [quo2.components.markdown.text :as quo2.text]
   [quo2.foundations.colors :as quo2.colors]
   [status-im.ui.components.react :as react]
   [status-im.ui.screens.chat.styles.message.datemark :as style]))

(defn chat-datemark
  [value]
  [react/touchable-without-feedback
   {:on-press (fn [_]
                (react/dismiss-keyboard!))}
   [react/view style/datemark-mobile
    [quo2.text/text
     {:weight              :medium
      :accessibility-label :message-datemark-text
      :style               {:color quo2.colors/neutral-50}}
     (string/capitalize value)]
    [react/view (style/divider)]]])
