(ns quo.components.messages.message.style
  (:require [quo.foundations.colors :as colors]))

(defn container
  [theme header? reacted? state pinned-by customization-color]
  {:padding-horizontal 12
   :padding-vertical   (if header? 8 4)
   :padding-bottom     (when reacted? (if header? 12 8))
   :border-radius      16
   :background-color   (cond
                         pinned-by          (colors/resolve-color customization-color theme 5)
                         (= state :default) (colors/theme-colors colors/white colors/neutral-5 theme)
                         :else              (colors/theme-colors colors/neutral-5
                                                                 colors/neutral-90
                                                                 theme))
  }
)

(def avatar-container
  {:padding-vertical 4
   :margin-right     8
  })

(def pin-indicator-container
  {:flex-direction :row
   :align-items    :center
   :padding-left   40
   :margin-bottom  2
  })

(def message-content {})
