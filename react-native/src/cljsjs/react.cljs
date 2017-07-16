(ns cljsjs.react
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]))

(when (exists? js/window)
  (set! js/ReactNative rn-dependencies/react-native))
