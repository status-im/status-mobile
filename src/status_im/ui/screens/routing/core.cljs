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
   [status-im.ui.components.bottom-bar.core :as bottom-bar]
   [status-im.ui.components.status-bar.view :as status-bar]
   [status-im.ui.components.bottom-bar.styles :as tabs.styles]))

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
    (fn [] (reset! screen-focused? false))}])

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
   (cond->
    (merge {:headerMode        "none"
            :cardStyle         {:backgroundColor (when (or platform/ios? platform/desktop?) :white)}
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
                                       (bottom-bar/minimize-bar route-name)))))}
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

(defn wrap-bottom-bar
  [nav]
  [bottom-bar/bottom-bar nav view-id])

(defn get-main-component [view-id]
  (log/debug :component view-id)
  (switch-navigator
   (into {}
         [(build-screen (intro-login-stack/login-stack view-id))
          (build-screen (intro-login-stack/intro-stack))
          [:tabs-and-modals
           {:screen
            (stack-navigator
             (merge
              {:tabs
               {:screen (tab-navigator
                         (->> [(build-screen chat-stack/chat-stack)
                               (build-screen browser-stack/browser-stack)
                               (build-screen wallet-stack/wallet-stack)
                               (build-screen profile-stack/profile-stack)]
                              (into {}))
                         {:initialRouteName :chat-stack
                          :tabBarComponent  (reagent.core/reactify-component
                                             wrap-bottom-bar)})}}
              (stack-screens modals/modal-screens))
             {:mode              :modal
              :initialRouteName  :tabs
              :onTransitionStart (fn [])})}]])
   {:initialRouteName (if (= view-id :intro)
                        :intro-stack
                        :login-stack)}))
