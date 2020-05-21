(ns status-im.ui.components.qr-code-viewer.views
  (:require [cljs-bean.core :as bean]
            [reagent.core :as reagent]
            [status-im.ui.components.qr-code-viewer.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.svg :as svg]
            ["qrcode" :as qr-code-js]))

(defn qr-logo [{:keys [size identicon]}]
  (let [background-size     (* size 0.25)
        logo-size           (* size 0.2)
        logo-position       (/ (- size logo-size) 2)
        background-position (/ (- size background-size) 2)]
    [svg/g
     [svg/defs
      [svg/clip-path {:id "logo-clip"}
       [svg/rect {:width         logo-size
                  :height        logo-size
                  :stroke        "blue"
                  "stroke-width" 2
                  :rx            (/ logo-size 2)
                  :ry            (/ logo-size 2)}]]]
     [svg/g {:x background-position :y background-position}
      [svg/rect {:width  background-size
                 :height background-size
                 :fill   "white"}]]
     [svg/g {:x logo-position :y logo-position}
      [svg/rect {:width        logo-size
                 :height       logo-size
                 :rx           (/ logo-size 2)
                 :ry           (/ logo-size 2)
                 :fill         "rgba(0,0,0,0)"
                 :stroke       "rgba(0,0,0,0.1)"
                 :stroke-width 1}]
      [svg/image {:width               logo-size
                  :height              logo-size
                  :href                identicon
                  :clip-path           "url(#logo-clip)"
                  :preserveAspectRatio "xMidYMid slice"}]]]))

(defn qr-code [{:keys [size value identicon]}]
  (let [path (reagent/atom nil)]
    (.toString qr-code-js
               value
               (bean/->js {:margin 0 :width size :errorCorrectionLevel "H"})
               (fn [_ uri]
                 (reset! path uri)))
    (fn []
      (when @path
        [svg/svg {:width   size
                  :height  size
                  :viewBox (str "0 0 " size " " size)}
         [svg/g
          [svg/svgxml {:xml @path :width size :height size}]]
         (when identicon
           [qr-logo {:size      size
                     :identicon identicon}])]))))

(defn qr-code-view
  "Qr Code view including the frame.
  Note: `size` includes frame with `styles/qr-code-padding.`"
  [size value & {icon :identicon}]
  (when (and size value)
    [react/view {:style               (styles/qr-code-container size)
                 :accessibility-label :qr-code-image}
     [qr-code {:value     value
               :identicon icon
               :size      (- size (* styles/qr-code-padding 2))}]]))
