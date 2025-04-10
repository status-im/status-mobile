(ns utils.worklets.header-animations)

(def worklets (js/require "../src/js/worklets/header_animations.js"))

(def use-blur-amount (.-useBlurAmount worklets))

(def use-layer-opacity (.-useLayerOpacity worklets))
