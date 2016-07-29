(ns cljsjs.react)

(when (exists? js/window)
  ;; cause there is no window.document object in the newest version
  ;; of React Native, but chance.js requires it
  (set! js/window.document #js {})

  (set! js/window.React (js/require "react"))
  (set! js/ReactNative (js/require "react-native")))
