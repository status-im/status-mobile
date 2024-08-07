(ns status-im.common.floating-button-page.style)

(def page-container
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(def keyboard-avoiding-view
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(defn content-keyboard-avoiding-view
  [{:keys [top bottom]}]
  {:position :absolute
   :top      top
   :left     0
   :right    0
   :bottom   bottom})

(def scroll-view-container {:flex 1})
