(ns status-im.chat.views.message.datemark
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.ui.components.react :refer [view
                                                text]]
            [clojure.string :as str]
            [status-im.i18n :refer [label]]
            [status-im.chat.styles.message.datemark :as st]))

(defn chat-datemark [value]
  [view st/datemark-wrapper
   [view st/datemark
    [text {:style st/datemark-text}
     (str/capitalize (or value (label :t/datetime-today)))]]])