(ns quo.components.messages.author.style
  (:require
    [quo.foundations.colors :as colors]))

(def container
  {:flex-wrap      :nowrap
   :flex-direction :row
   :align-items    :flex-end})

(defn middle-dot-nickname
  [theme]
  {:color             (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)
   :margin-horizontal 4})

(defn chat-key-text
  [theme]
  {:color       (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)
   :margin-left 8
   :padding-top 2})

(defn middle-dot-chat-key
  [theme]
  {:color             (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)
   :margin-horizontal 2})

(defn primary-name
  [muted? theme]
  {:color       (if muted?
                  colors/neutral-50
                  (colors/theme-colors colors/neutral-100 colors/white theme))
   :flex-shrink 1})

(defn secondary-name
  [theme]
  {:padding-top 1
   :flex-shrink 999999
   :min-width   40
   :color       (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(def icon-container
  {:margin-left   4
   :margin-bottom 2})

(defn time-text
  [theme]
  {:color       (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)
   :padding-top 2})
