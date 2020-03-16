(ns status-im.ui.screens.add-new.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as components.styles]
            [status-im.utils.styles :as styles]))

(def new-chat-container
  {:flex-direction :row
   :padding-top    16
   :padding-left   16})

(defn input-container []
  {:flex-direction   :row
   :align-items      :center
   :border-radius    components.styles/border-radius
   :height           52
   :background-color colors/gray-lighter})

(defn new-chat-input-container []
  (merge
   (input-container)
   {:flex 1}))

(def button-container
  {:justify-content    :center
   :border-radius      components.styles/border-radius
   :padding-horizontal 16
   :height             52})

(styles/def input
  {:flex               1
   :padding-horizontal 14
   :desktop            {:height 30
                        :width  "100%"}
   :android            {:padding 0
                        :width   "100%"}})
