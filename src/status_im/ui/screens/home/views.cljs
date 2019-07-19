(ns status-im.ui.screens.home.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.ui.screens.home.filter.views :as filter.views]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.bottom-bar.styles :as tabs.styles]
            [status-im.ui.screens.home.views.inner-item :as inner-item]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.animation :as animation]
            [status-im.constants :as constants]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.announcements.views :as announcements])
  (:require-macros [status-im.utils.views :as views]))

(views/defview les-debug-info []
  (views/letsubs [sync-state           [:chain-sync-state]
                  latest-block-number  [:latest-block-number]
                  rpc-network?         [:current-network-uses-rpc?]
                  network-initialized? [:current-network-initialized?]]
    (when (and network-initialized? (not rpc-network?))
      [react/view {:style styles/sync-wrapper}
       [react/touchable-highlight {:on-press #(re-frame/dispatch [:home.ui/sync-info-pressed])}
        [react/text {:style styles/sync-info}
         (str "LES: 'latest' #" latest-block-number "\n"
              (if sync-state
                (str "syncing " (:currentBlock sync-state) " of " (:highestBlock sync-state) " blocks...")
                (str "not syncing")))]]])))

(defn welcome []
  [react/view {:style styles/welcome-view}
   [react/view {:flex 1}]
   [status-bar/status-bar {:type :main}]
   [react/view {:style styles/welcome-image-container}
    [components.common/image-contain
     {:container-style {}}
     {:image (resources/get-image :welcome-image) :width 750 :height 556}]]
   [react/i18n-text {:style styles/welcome-text :key :welcome-to-status}]
   [react/view
    [react/i18n-text {:style styles/welcome-text-description
                      :key   :welcome-to-status-description}]]
   [react/view {:flex 1}]
   [react/view {:align-items :center :margin-bottom 52}
    [components.common/button {:on-press #(re-frame/dispatch [:navigate-back])
                               :label    (i18n/label :t/get-started)}]]])

(defn home-empty-view []
  [react/view styles/no-chats
   [announcements/public-launch-banner {}]
   [react/view {:flex               1
                :align-items        :center
                :justify-content    :center
                :padding-horizontal 34
                :align-self         :stretch}
    [react/i18n-text {:style styles/no-chats-text :key :no-recent-chats}]
    [react/view {:align-items :center :margin-top 20}
     [components.common/button {:on-press #(list-selection/open-share {:message (i18n/label :t/get-status-at)})
                                :label    (i18n/label :t/invite-friends)}]]]])

(defn home-items-view [_ _ _ search-input-state]
  (let [previous-touch      (reagent/atom nil)
        scrolling-from-top? (reagent/atom true)]
    (filter.views/reset-height)
    (fn [search-filter chats all-home-items]
      (if (not-empty search-filter)
        [filter.views/home-filtered-items-list chats]
        [react/animated-view
         (merge {:style {:flex             1
                         :margin-bottom    -35
                         :background-color :white
                         :transform        [{:translateY (:height @search-input-state)}]}}
                (when @scrolling-from-top?
                  {:on-start-should-set-responder-capture
                   (fn [event]
                     (let [current-position  (.-pageY (.-nativeEvent event))
                           current-timestamp (.-timestamp (.-nativeEvent event))]
                       (reset! previous-touch
                               [current-position current-timestamp]))

                     false)
                   :on-move-should-set-responder
                   (fn [event]
                     (let [current-position  (.-pageY (.-nativeEvent event))
                           current-timestamp (.-timestamp (.-nativeEvent event))
                           [previous-position previous-timestamp] @previous-touch]
                       (when (and previous-position
                                  (> 100 (- current-timestamp previous-timestamp))
                                  (< 10 (- current-position
                                           previous-position)))
                         (filter.views/show-search!)))
                     false)}))
         [announcements/public-launch-banner {}]
         [list/flat-list {:style          {:margin-bottom (- styles/search-input-height)}
                          :data           all-home-items
                          :key-fn         first
                          :footer         [react/view
                                           {:style {:height     tabs.styles/tabs-diff
                                                    :align-self :stretch}}]
                          :on-scroll-begin-drag
                          (fn [e]
                            (reset! scrolling-from-top?
                                    ;; check if scrolling up from top of list
                                    (zero? (.-y (.-contentOffset (.-nativeEvent e))))))
                          :render-fn
                          (fn [home-item]
                            [inner-item/home-list-item home-item])}]
         (when (:show? @search-input-state)
           [react/view {:width  1
                        :height styles/search-input-height}])]))))

(views/defview home-action-button []
  (views/letsubs [logging-in? [:accounts/login]]
    [react/view styles/action-button-container
     [react/touchable-highlight {:accessibility-label :new-chat-button
                                 :on-press            (when-not logging-in? #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new {}]))}
      [react/view styles/action-button
       (if logging-in?
         [react/activity-indicator {:color     :white
                                    :animating true}]
         [icons/icon :main-icons/add {:color :white}])]]]))

(views/defview home [loading?]
  (views/letsubs
    [anim-translate-y (animation/create-value -35)
     {:keys [search-filter chats all-home-items]} [:home-items]
     window-width [:dimensions/window-width]
     two-pane-ui-enabled? [:two-pane-ui-enabled?]]
    {:component-did-mount (fn [this]
                            (let [[_ loading?] (.. this -props -argv)]
                              (when loading? (utils/set-timeout #(re-frame/dispatch [:init-rest-of-chats]) 100))))}
    (let [home-width (if (> window-width constants/two-pane-min-width)
                       (max constants/left-pane-min-width (/ window-width 3))
                       window-width)]
      [react/view (merge {:flex 1 :width home-width}
                         (when two-pane-ui-enabled?
                           {:border-right-width 1 :border-right-color colors/gray-light}))
       [status-bar/status-bar {:type :main}]
       [react/keyboard-avoiding-view {:style     {:flex        1
                                                  :align-items :center}
                                      :on-layout (fn [e]
                                                   (re-frame/dispatch
                                                    [:set-once :content-layout-height
                                                     (-> e .-nativeEvent .-layout .-height)]))}
        [react/view {:style {:flex       1
                             :align-self :stretch}}
         [toolbar/toolbar nil nil [toolbar/content-title (i18n/label :t/chat)]]
         [les-debug-info]
         (cond loading?
               [react/view {:style {:flex            1
                                    :justify-content :center
                                    :align-items     :center}}
                [connectivity/connectivity-view anim-translate-y]
                [connectivity/connectivity-animation-wrapper
                 {}
                 anim-translate-y
                 [react/activity-indicator {:flex      1
                                            :animating true}]]] :else
               [react/view {:style {:flex 1}}
                [connectivity/connectivity-view anim-translate-y]
                [connectivity/connectivity-animation-wrapper
                 {}
                 anim-translate-y
                 [filter.views/search-input-wrapper search-filter]
                 (if (and (not search-filter)
                          (empty? all-home-items))
                   [home-empty-view]
                   [home-items-view
                    search-filter
                    chats
                    all-home-items
                    filter.views/search-input-state])]])]
        [home-action-button]]])))

(views/defview home-wrapper []
  (views/letsubs [loading? [:chats/loading?]]
    [home loading?]))
