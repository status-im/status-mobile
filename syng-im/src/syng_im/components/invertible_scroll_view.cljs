(ns syng-im.components.invertible-scroll-view)

(set! js/InvertibleScrollView (js/require "react-native-invertible-scroll-view"))

(defn invertible-scroll-view [props]
  (js/React.createElement js/InvertibleScrollView
                          (clj->js (merge {:inverted true} props))))

