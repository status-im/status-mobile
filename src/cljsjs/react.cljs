(ns cljsjs.react)

(when (exists? js/window)
  (set! js/ReactNative (js/require "react-native")))
