(ns messenger.android.core
  (:require-macros
   [natal-shell.components :refer [view text image touchable-highlight list-view
                                   toolbar-android]]
   [natal-shell.data-source :refer [data-source clone-with-rows]]
   [natal-shell.core :refer [with-error-view]]
   [natal-shell.alert :refer [alert]])
  (:require [om.next :as om :refer-macros [defui]]
            [re-natal.support :as sup]
            [messenger.state :as state]
            [messenger.android.contacts-list :as contacts-list]))

(set! js/React (js/require "react-native"))
(def app-registry (.-AppRegistry js/React))

(defui AppRoot
  static om/IQuery
  (query [this]
         '[:contacts-ds])
  Object
  (render [this]
          (let [{:keys [contacts-ds]} (om/props this)]
            (contacts-list/contacts-list (om/props this)))))

(swap! state/app-state assoc :contacts-ds
       (data-source {:rowHasChanged (fn [row1 row2]
                                      (not= row1 row2))}))

(defonce RootNode (sup/root-node! 1))
(defonce app-root (om/factory RootNode))

(defn init []
  (om/add-root! state/reconciler AppRoot 1)
  (.registerComponent app-registry "Messenger" (fn [] app-root)))
