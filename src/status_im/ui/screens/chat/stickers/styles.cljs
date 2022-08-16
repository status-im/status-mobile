(ns status-im.ui.screens.chat.stickers.styles)

(def stickers-panel {:flex 1 :margin 5 :flex-direction :row :justify-content :space-between :flex-wrap :wrap})

(defn pack-icon [background-color icon-size icon-horizontal-margin]
  {:background-color  background-color
   :margin-vertical   5
   :margin-horizontal icon-horizontal-margin
   :height            icon-size
   :width             icon-size
   :border-radius     (/ icon-size 2)
   :align-items       :center
   :justify-content   :center})
