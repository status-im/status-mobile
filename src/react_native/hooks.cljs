(ns react-native.hooks
  (:require ["@react-native-community/hooks" :as hooks]
            [react-native.core :as rn]
            [oops.core :as oops]))

(defn use-keyboard
  []
  (let [kb (.useKeyboard hooks)]
    {:keyboard-shown  (.-keyboardShown ^js kb)
     :keyboard-height (.-keyboardHeight ^js kb)}))

(defn use-back-handler
  [handler]
  (.useBackHandler hooks handler))

(defn use-interval
  [cb cleanup-cb delay]
  (let [saved-callback (rn/use-ref)]
    (rn/use-effect
     (fn []
       (oops/oset! saved-callback "current" cb))
     [cb])

    (rn/use-effect
     (fn []
       (let [tick (oops/oget saved-callback "current")]
         (when delay
           (let [id (js/setInterval tick delay)]
             (fn []
               (cleanup-cb)
               (js/clearInterval id))))))
     [delay])))
