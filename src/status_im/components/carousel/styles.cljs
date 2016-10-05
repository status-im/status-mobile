(ns status-im.components.carousel.styles)

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
