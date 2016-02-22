(ns messenger.ios.core
  (:require-macros [natal-shell.components :refer [view text image touchable-highlight]]
                   [natal-shell.alert :refer [alert]])
  (:require [om.next :as om :refer-macros [defui]]
            [re-natal.support :as sup]
            [messenger.state :as state]))

(set! js/React (js/require "react-native"))

(def app-registry (.-AppRegistry js/React))
(def logo-img (js/require "./images/cljs.png"))

(defui AppRoot
       static om/IQuery
       (query [this]
              '[:app/msg])
       Object
       (render [this]
               (let [{:keys [app/msg]} (om/props this)]
                    (view {:style {:flexDirection "column" :margin 40 :alignItems "center"}}
                          (text {:style {:fontSize 30 :fontWeight "100" :marginBottom 20 :textAlign "center"}} msg)
                          (image {:source logo-img
                                  :style  {:width 80 :height 80 :marginBottom 30}})
                          (touchable-highlight {:style   {:backgroundColor "#999" :padding 10 :borderRadius 5}
                                                :onPress #(alert "HELLO!")}
                                               (text {:style {:color "white" :textAlign "center" :fontWeight "bold"}} "press me"))))))

(defonce RootNode (sup/root-node! 1))
(defonce app-root (om/factory RootNode))

(defn init []
      (om/add-root! state/reconciler AppRoot 1)
      (.registerComponent app-registry "Messenger" (fn [] app-root)))