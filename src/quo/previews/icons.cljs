(ns quo.previews.icons
  (:require [quo.design-system.colors :as colors]
            [quo.react-native :as rn]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.icons.vector-icons :as vector-icons]))

(defn preview []
  [rn/scroll-view {:background-color (:ui-background @colors/theme)
                   :flex             1}
   (for [i (keys icons/icons)]
     [rn/view {:flex-direction :row}
      [vector-icons/icon (keyword i)]
      [rn/text i]])])