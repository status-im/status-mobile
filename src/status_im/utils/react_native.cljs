(ns status-im.utils.react-native
  (:require ["react-native" :as react-native]))

; Moved from status-im.ui.components.react(let's short for R), so status-im.native-module.core(short for N) don't need to have a dependency to R.
; We should keep N's dependencies as simple as possible, otherwise we may see issue like:
; Circular dependency detected: N -> R -> status-im.utils.utils -> status-im.ethereum.eip55 -> N
(def device-event-emitter (.-DeviceEventEmitter react-native))