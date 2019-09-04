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
   [status-im.ui.screens.routing.browser-stack :as browser-stack]
   [status-im.ui.screens.routing.modals :as modals]
   [status-im.ui.components.tabbar.core :as tabbar]
   [status-im.ui.components.status-bar.view :as status-bar]
   [status-im.ui.components.tabbar.styles :as tabs.styles]
   [status-im.react-native.js-dependencies :as js-dependencies]))

(defonce view-id (reagent.core/atom nil))

(defn navigation-events [current-view-id modal? screen-focused?]
  [:> navigation/navigation-events
   {:on-will-focus
    (fn []
      (reset! screen-focused? true)
      (when (not= @view-id current-view-id)
        (reset! view-id current-view-id))
      (log/debug :on-will-focus current-view-id)
      (when modal?
        (status-bar/set-status-bar current-view-id))
      (re-frame/dispatch [:screens/on-will-focus current-view-id]))
    :on-did-focus
    (fn []
      (when-not modal?
        (status-bar/set-status-bar current-view-id)))
    :on-will-blur
    (fn []
      (reset! screen-focused? false)
      ;; Reset currently mounted text inputs to their default values
      ;; on navigating away; this is a privacy measure
      (doseq [[text-input default-value] @react/text-input-refs]
        (.setNativeProps text-input (clj->js {:text default-value}))))}])

(defn wrap
  "Wraps screen with main view and adds navigation-events component"
  [view-id component]
  (fn [args]
    (let [main-view       (react/create-main-screen-view view-id)
          ;; params passed to :navigate-to
          params          (get-in args [:navigation :state :params])
          screen-focused? (reagent.core/atom true)]
      (if platform/ios?
        [main-view (assoc common-styles/flex
                          :margin-bottom
                          (cond
                            ;; there is no need to show bottom nav bar on
                            ;; `intro-login-stack` screens
                            (contains?
                             intro-login-stack/all-screens
                             view-id)
                            0

                            ;; :wallet-onboarding-setup is the only screen
                            ;; except main tabs which requires maximised
                            ;; bottom nav bar, that's why it requires an extra
                            ;; bottom margin, otherwise bottom nav bar will
                            ;; partially cover the screen
                            (contains?
                             #{:wallet-onboarding-setup}
                             view-id)
                            tabs.styles/tabs-height

                            :else
                            tabs.styles/minimized-tabs-height))
         [component params screen-focused?]
         [navigation-events view-id false screen-focused?]]

        [main-view common-styles/flex
         [component params screen-focused?]
         [navigation-events view-id false screen-focused?]]))))

(defn wrap-modal [modal-view component]
  "Wraps modal screen with necessary styling and adds :on-request-close handler
  on Android"
  (fn [args]
    (let [params  (get-in args [:navigation :state :params])
          active? (reagent.core/atom true)]
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
           [component params active?]]
          [navigation-events modal-view true active?]]]
        [react/main-screen-modal-view modal-view
         [component params active?]
         [navigation-events modal-view true active?]]))))

(defn prepare-config [config]
  (-> config
      (utils/update-if-present :initialRouteName name)
      (utils/update-if-present :mode name)))

(defn stack-navigator [routes config]
  (nav-reagent/stack-navigator
   routes
   (merge {:headerMode        "none"
           :cardStyle         {:backgroundColor :white}
           #_:transitionConfig
           #_(fn []
               #js {:transitionSpec #js{:duration 10}})
           :onTransitionStart (fn [n]
                                (let [idx    (.. n
                                                 -navigation
                                                 -state
                                                 -index)
                                      routes (.. n
                                                 -navigation
                                                 -state
                                                 -routes)]
                                  (when (and (array? routes) (int? idx))
                                    (let [route      (aget routes idx)
                                          route-name (keyword (.-routeName route))]
                                      (tabbar/minimize-bar route-name)))))}
          (prepare-config config))))

(defn twopane-navigator [routes config]
  (navigation/twopane-navigator
   routes
   (merge {:headerMode        "none"
           :cardStyle         {:backgroundColor :white}
           :onTransitionStart (fn [n]
                                (let [idx    (.. n
                                                 -navigation
                                                 -state
                                                 -index)
                                      routes (.. n
                                                 -navigation
                                                 -state
                                                 -routes)]
                                  (when (and (array? routes) (int? idx))
                                    (let [route      (aget routes idx)
                                          route-name (keyword (.-routeName route))]
                                      (tabbar/minimize-bar route-name)))))}
          (prepare-config config))))

(defn switch-navigator [routes config]
  (nav-reagent/switch-navigator
   routes
   (prepare-config config)))

(defn tab-navigator [routes config]
  (nav-reagent/tab-navigator
   routes
   (assoc (prepare-config config) :lazy false)))

(declare stack-screens)

(defn build-screen [navigator screen]
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
                  (navigator
                   (stack-screens navigator screens)
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

(defn stack-screens [navigator screens-map]
  (->> screens-map
       (map (partial build-screen navigator))
       (into {})))

(defn wrap-tabbar
  [nav]
  [tabbar/tabbar nav view-id])

(defn app-container [navigator]
  (.createAppContainer js-dependencies/react-navigation navigator))

(defn get-main-component [view-id two-pane?]
  (log/debug :component view-id)
  (app-container
   (switch-navigator
    (into {}
          [(build-screen stack-navigator (intro-login-stack/login-stack view-id))
           (build-screen stack-navigator (intro-login-stack/intro-stack))
           [:tabs-and-modals
            {:screen
             (stack-navigator
              (merge
               {:tabs
                {:screen (tab-navigator
                          (->> [(build-screen (if two-pane? twopane-navigator stack-navigator) chat-stack/chat-stack)
                                (build-screen stack-navigator browser-stack/browser-stack)
                                (build-screen stack-navigator wallet-stack/wallet-stack)
                                (build-screen stack-navigator profile-stack/profile-stack)]
                               (into {}))
                          {:initialRouteName :chat-stack
                           :tabBarComponent  (reagent.core/reactify-component
                                              wrap-tabbar)})}}
               (stack-screens stack-navigator modals/modal-screens))
              {:mode              :modal
               :initialRouteName  :tabs
               :onTransitionStart (fn [])})}]])
    {:initialRouteName (if (= view-id :intro)
                         :intro-stack
                         :login-stack)})))
