(ns messenger.android.core
  (:require-macros
    [natal-shell.components :refer [navigator view text image touchable-highlight
                                    list-view toolbar-android]]
    [natal-shell.data-source :refer [data-source clone-with-rows]]
    [natal-shell.back-android :refer [add-event-listener remove-event-listener]]
    [natal-shell.core :refer [with-error-view]]
    [natal-shell.alert :refer [alert]])
  (:require [om.next :as om :refer-macros [defui]]
            [re-natal.support :as sup]
            [messenger.omnext :as omnext]
            [messenger.state :as state]
            [messenger.utils.utils :refer [log toast]]
            [messenger.android.login :refer [login Login]]
            [messenger.comm.pubsub :as pubsub]
            [messenger.comm.intercom :as intercom :refer [load-user-phone-number]]
            [messenger.protocol.protocol-handler :refer [make-handler]]
            [messenger.init :refer [init-simple-store]]
            [messenger.components.chat.chat :as chat]
            [syng-im.protocol.api :refer [init-protocol]]
            [syng-im.utils.logging :as log]
            [messenger.components.iname :as in]
            [messenger.models.navigation :as n]
            [messenger.components.contact-list.contact-list :refer [ContactList contact-list]]))


(def app-registry (.-AppRegistry js/React))

(def back-button-handler-atom (atom {:nav     nil
                                     :handler nil}))

(defn init-back-button-handler! [nav]
  (let [back-button-handler @back-button-handler-atom]
    (when (not= (:nav back-button-handler) nav)
      (remove-event-listener "hardwareBackPress" (:handler back-button-handler))
      (let [new-listener (fn []
                           (binding [state/*nav-render* false]
                             (when (< 1 (.-length (.getCurrentRoutes nav)))
                               (.pop nav)
                               true)))]
        (reset! back-button-handler-atom {:nav     nav
                                          :handler new-listener})
        (add-event-listener "hardwareBackPress" new-listener)))))

(defui AppRoot
  static om/IQuery
  (query [this]
    (let [component (n/current-screen-class)]
      [{(in/get-name component) (om/get-query component)}]))
  Object
  (render [this]
    (log/debug "APPROOT Rendering")
    (navigator
      {:initialRoute {:component contact-list}
       :renderScene  (fn [route nav]
                       (log/debug "RENDER SCENE")
                       (when state/*nav-render*
                         (init-back-button-handler! nav)
                         (let [{:keys [component]} (js->clj route :keywordize-keys true)
                               props (om/props this)
                               props (om/computed props {:nav nav})]
                           (component props))))})))

;; TODO to service?
(swap! state/app-state assoc :contacts-ds
       (data-source {:rowHasChanged (fn [row1 row2]
                                      (not= row1 row2))}))

(defonce RootNode (sup/root-node! 1))
(defonce app-root (om/factory RootNode))

(defn init []
  (n/set-current-screen-class ContactList)
  (init-simple-store)
  (pubsub/setup-pub-sub)
  (init-protocol (make-handler))
  (load-user-phone-number)
  (om/add-root! omnext/reconciler AppRoot 1)
  (.registerComponent app-registry "Messenger" (fn [] app-root)))
