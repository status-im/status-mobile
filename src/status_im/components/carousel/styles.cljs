(ns status-im.components.carousel.styles
  (:require [status-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               chat-background
                                               online-color
                                               selected-message-color
                                               separator-color
                                               text1-color
                                               text2-color
                                               toolbar-background1]]))

(def scroll-view-container
  {:flex 1})

(defn content-container [sneak gap]
  {:paddingLeft (+ 0 (quot gap 2))
   :paddingRight (+ sneak (quot gap 2))})

(defn page [page-width margin]
  {:width          page-width
   :justifyContent :center
   :marginLeft     margin
   :marginRight    margin})
