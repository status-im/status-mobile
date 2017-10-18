(ns status-im.chat.styles.message.command-pill
  (:require  [status-im.utils.platform :as p]
             [status-im.ui.components.styles :refer [color-white]]))

(defn pill [command]
  {:backgroundColor   (:color command)
   :height            24
   :borderRadius      50
   :padding-top       (if p/ios? 4 3)
   :paddingHorizontal 12
   :text-align        :left})

(def pill-text
  {:fontSize    12
   :color       color-white})
