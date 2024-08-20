(ns react-native.hooks
  (:require
    ["@react-native-community/hooks" :as hooks]
    [oops.core :as oops]
    [react-native.core :as rn]))

(defn- add-keyboard-listener
  [listener callback]
  (oops/ocall rn/keyboard "addListener" listener callback))

(defn use-keyboard
  []
  (let [[keyboard-height set-keyboard-height] (rn/use-state 0)
        [did-show? set-did-show]              (rn/use-state false)
        [will-show? set-will-show]            (rn/use-state false)]
    (rn/use-mount
     (fn []
       (let [will-show-listener (add-keyboard-listener "keyboardWillShow" #(set-will-show true))
             did-show-listener  (add-keyboard-listener "keyboardDidShow"
                                                       (fn [e]
                                                         (set-did-show true)
                                                         (set-keyboard-height
                                                          (oops/oget e "endCoordinates.height"))))
             will-hide-listener (add-keyboard-listener "keyboardWillHide" #(set-will-show false))
             did-hide-listener  (add-keyboard-listener "keyboardDidHide"
                                                       (fn [e]
                                                         (set-did-show false)
                                                         (when e
                                                           (oops/oget e "endCoordinates.height"))))]
         (fn []
           (oops/ocall will-show-listener "remove")
           (oops/ocall did-show-listener "remove")
           (oops/ocall will-hide-listener "remove")
           (oops/ocall did-hide-listener "remove")))))
    {:keyboard-shown      did-show?
     :keyboard-will-show? will-show?
     :keyboard-height     keyboard-height}))

(defn use-back-handler
  [handler]
  (.useBackHandler hooks handler))

(defn use-interval
  [cb cleanup-cb delay-ms]
  (let [saved-callback (rn/use-ref)]
    (rn/use-effect
     (fn []
       (oops/oset! saved-callback "current" cb))
     [cb])

    (rn/use-effect
     (fn []
       (let [tick (oops/oget saved-callback "current")]
         (when delay-ms
           (let [id (js/setInterval tick delay-ms)]
             (fn []
               (cleanup-cb)
               (js/clearInterval id))))))
     [delay-ms])))
