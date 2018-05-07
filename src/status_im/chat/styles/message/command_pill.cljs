(ns status-im.chat.styles.message.command-pill
  (:require [status-im.utils.platform :as p]
            [status-im.ui.components.styles :refer [color-white]]))

(defn pill [command]
  {:background-color  (:color command)
   :height            24
   :border-radius     50
   :padding-top       (if p/ios? 4 3)
   :paddingHorizontal 12
   :text-align        :left})

(def pill-text
  {:font-size 12
   :color     color-white})
