(ns quo.components.messages.author.style
  (:require
    [quo.foundations.colors :as colors]))

(def container
  {:flex-wrap      :nowrap
   :flex-direction :row
   :align-items    :flex-end})

(def name-container
  {:margin-right   8
   :flex-direction :row
   :align-items    :flex-end
  })

(defn middle-dot
  [theme]
  {:color             (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)
   :margin-horizontal 2})

(defn chat-key-text
  [theme]
  {:color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)})

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
   :color       (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(defn icon-container
  [is-first?]
  {:margin-left   (if is-first? 4 2)
   :margin-bottom 2})

(defn time-text
  [theme]
  {:color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)})
