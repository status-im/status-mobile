(ns react-native.svg
  (:require
    ["react-native-svg" :as Svg]
    [react-native.pure :as rn.pure]))

(def svg (rn.pure/get-create-element-fn Svg/default))
(def path (rn.pure/get-create-element-fn Svg/Path))
(def rect (rn.pure/get-create-element-fn Svg/Rect))
(def clippath (rn.pure/get-create-element-fn Svg/ClipPath))
(def defs (rn.pure/get-create-element-fn Svg/Defs))
(def circle (rn.pure/get-create-element-fn Svg/Circle))
(def svg-xml (rn.pure/get-create-element-fn Svg/SvgXml))
(def svg-uri (rn.pure/get-create-element-fn Svg/SvgUri))
(def g (rn.pure/get-create-element-fn Svg/G))
(def linear-gradient (rn.pure/get-create-element-fn Svg/LinearGradient))
(def stop (rn.pure/get-create-element-fn Svg/Stop))
