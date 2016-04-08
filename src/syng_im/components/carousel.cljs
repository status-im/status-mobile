(ns syng-im.components.carousel)

(set! js/Carousel (.-default (js/require "react-native-carousel-control")))


(defn carousel [opts & children]
  (apply js/React.createElement js/Carousel (clj->js opts) children))



(comment



  )