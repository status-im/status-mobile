(ns messenger.android.core
  (:require-macros
   [natal-shell.components :refer [navigator view text image touchable-highlight list-view
                                   toolbar-android]]
   [natal-shell.data-source :refer [data-source clone-with-rows]]
   [natal-shell.back-android :refer [add-event-listener]]
   [natal-shell.core :refer [with-error-view]]
   [natal-shell.alert :refer [alert]])
  (:require [om.next :as om :refer-macros [defui]]
            [re-natal.support :as sup]
            [messenger.state :as state]
            [messenger.android.login :refer [login]]
            [messenger.android.contacts-list :refer [contacts-list]]
            [messenger.android.chat :refer [chat]]
            [messenger.comm.pubsub :as pubsub]
            [messenger.comm.intercom :refer [load-user-phone-number]]))

(def app-registry (.-AppRegistry js/React))

(def initialized-atom (atom false))
(defn init-back-button-handler! [nav]
  (if (not @initialized-atom)
    (swap! initialized-atom
           (fn [initialized]
             (when (not initialized)
               (add-event-listener "hardwareBackPress"
                                   (fn []
                                     (binding [state/*nav-render* false]
                                       (when (< 1 (.-length (.getCurrentRoutes nav)))
                                         (.pop nav)
                                         true)))))
             true))))

(defui AppRoot
  static om/IQuery
  (query [this]
         '[:page :contacts-ds :user-phone-number :confirmation-code])
  Object
  (render [this]
          (let [{:keys [page]} (om/props this)]
            (navigator
             {:initialRoute {:component login}
              :renderScene (fn [route nav]
                             (when state/*nav-render*
                               (init-back-button-handler! nav)
                               (let [{:keys [component]}
                                     (js->clj route :keywordize-keys true)]
                                 (component (om/computed (om/props this)
                                                         {:nav nav})))))}))))

(swap! state/app-state assoc :contacts-ds
       (data-source {:rowHasChanged (fn [row1 row2]
                                      (not= row1 row2))}))
(swap! state/app-state assoc :component contacts-list)

(defonce RootNode (sup/root-node! 1))
(defonce app-root (om/factory RootNode))

(defn init []
  (pubsub/setup-pub-sub)
  (load-user-phone-number)
  (om/add-root! state/reconciler AppRoot 1)
  (.registerComponent app-registry "Messenger" (fn [] app-root)))
