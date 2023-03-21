(ns react-native.hooks
  (:require ["@react-native-community/hooks" :as hooks]))

(defn use-keyboard
  []
  (let [kb (.useKeyboard hooks)]
    {:keyboard-shown  (.-keyboardShown ^js kb)
     :keyboard-height (.-keyboardHeight ^js kb)}))

(defn use-back-handler
  [handler]
  (.useBackHandler hooks handler))
