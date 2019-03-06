(ns status-im.ui.screens.routing.core
  (:require
   [status-im.ui.components.react :as react]
   [status-im.ui.components.styles :as common-styles]
   [status-im.utils.navigation :as navigation]
   [cljs-react-navigation.reagent :as nav-reagent]
   [re-frame.core :as re-frame]
   [taoensso.timbre :as log]
   [status-im.utils.platform :as platform]
   [status-im.utils.core :as utils]
   [status-im.ui.screens.routing.screens :as screens]
   [status-im.ui.screens.routing.intro-login-stack :as intro-login-stack]
   [status-im.ui.screens.routing.chat-stack :as chat-stack]
   [status-im.ui.screens.routing.wallet-stack :as wallet-stack]
   [status-im.ui.screens.routing.profile-stack :as profile-stack]
   [status-im.ui.components.bottom-bar.core :as bottom-bar]
   [status-im.ui.components.status-bar.view :as status-bar]))

(defn navigation-events [view-id modal?]
  [:> navigation/navigation-events
   {:on-will-focus
    (fn []
      (log/debug :on-will-focus view-id)
      (when modal?
        (status-bar/set-status-bar view-id))
      (re-frame/dispatch [:screens/on-will-focus view-id]))
    :on-did-focus
    (fn []
      (log/debug :on-did-focus view-id)
      (when-not modal?
        (status-bar/set-status-bar view-id)))}])

(defn wrap [view-id component]
  "Wraps screen with main view and adds navigation-events component"
  (fn []
    (let [main-view (react/create-main-screen-view view-id)]
      [main-view common-styles/flex
       [component]
       [navigation-events view-id false]])))

(defn wrap-modal [modal-view component]
  "Wraps modal screen with necessary styling and adds :on-request-close handler
  on Android"
  (fn []
    (if platform/android?
      [react/view common-styles/modal
       [react/modal
        {:transparent      true
         :animation-type   :slide
         :on-request-close (fn []
                             (cond
                               (#{:wallet-send-transaction-modal
                                  :wallet-sign-message-modal}
                                modal-view)
                               (re-frame/dispatch
                                [:wallet/discard-transaction-navigate-back])

                               :else
                               (re-frame/dispatch [:navigate-back])))}
        [react/main-screen-modal-view modal-view
         [component]]
        [navigation-events modal-view true]]]
      [react/main-screen-modal-view modal-view
       [component]
       [navigation-events modal-view true]])))

(defn prepare-config [config]
  (-> config
      (utils/update-if-present :initialRouteName name)
      (utils/update-if-present :mode name)))

(defn stack-navigator [routes config]
  (nav-reagent/stack-navigator
   routes
   (cond->
    (merge {:headerMode "none"
            :cardStyle  {:backgroundColor (when platform/ios? :white)}}
           (prepare-config config)))))

(defn switch-navigator [routes config]
  (nav-reagent/switch-navigator
   routes
   (prepare-config config)))

(defn tab-navigator [routes config]
  (nav-reagent/tab-navigator
   routes
   (prepare-config config)))

(declare stack-screens)

(defn build-screen [screen]
  "Builds screen from specified configuration. Currently screen can be
  - keyword, which points to some specific route
  - vector of [:modal :screen-key] type when screen should be wrapped as modal
  - map with `name`, `screens`, `config` keys, where `screens` is a vector
    of children and `config` is `stack-navigator` configuration"
  (let [[screen-name screen-config]
        (cond (keyword? screen)
              [screen (screens/get-screen screen)]
              (map? screen)
              [(:name screen) screen]
              :else screen)]
    (let [res (cond
                (map? screen-config)
                (let [{:keys [screens config]} screen-config]
                  (stack-navigator
                   (stack-screens screens)
                   config))

                (vector? screen-config)
                (let [[_ screen] screen-config]
                  (nav-reagent/stack-screen
                   (wrap-modal screen-name screen)))

                :else
                (nav-reagent/stack-screen (wrap screen-name screen-config)))]
      [screen-name (cond-> {:screen res}
                     (:navigation screen-config)
                     (assoc :navigationOptions
                            (:navigation screen-config)))])))

(defn stack-screens [screens-map]
  (->> screens-map
       (map build-screen)
       (into {})))

(defn get-main-component [view-id]
  (log/debug :component view-id)
  (switch-navigator
   (into {}
         [(build-screen (intro-login-stack/intro-login-stack view-id))
          [:tabs
           {:screen (tab-navigator
                     (->> [(build-screen chat-stack/chat-stack)
                           (build-screen wallet-stack/wallet-stack)
                           (build-screen profile-stack/profile-stack)]
                          (into {}))
                     {:initialRouteName :chat-stack
                      :tabBarComponent  (reagent.core/reactify-component
                                         bottom-bar/bottom-bar)})}]])
   {:initialRouteName :intro-login-stack}))
