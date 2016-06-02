(ns cljsjs.react)

(when (exists? js/window)
  (set! js/window.React (js/require "react-native")))
