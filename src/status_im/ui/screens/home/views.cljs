(ns status-im.ui.screens.home.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.colors :as colors]
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
            [status-im.ui.components.list-selection :as list-selection])
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
   [react/i18n-text {:style styles/no-chats-text :key :no-recent-chats}]
   [react/view {:align-items :center :margin-top 20}
    [components.common/button {:on-press #(list-selection/open-share {:message (i18n/label :t/get-status-at)})
                               :label    (i18n/label :t/invite-friends)}]]])

(defn home-items-view [_ _ _]
  (let [previous-touch      (reagent/atom nil)
        scrolling-from-top? (reagent/atom true)]
    (fn [search-filter chats all-home-items]
      (if (not-empty search-filter)
        [filter.views/home-filtered-items-list chats]
        [react/view
         (merge {:style {:flex 1}}
                (when (and @scrolling-from-top?
                           (not (:show? @filter.views/search-input-state)))
                  {:on-start-should-set-responder-capture
                   (fn [^js event]
                     (let [current-position  (.-pageY (.-nativeEvent event))
                           current-timestamp (.-timestamp (.-nativeEvent event))]
                       (reset! previous-touch
                               [current-position current-timestamp]))

                     false)
                   :on-move-should-set-responder
                   (fn [^js event]
                     (let [current-position  (.-pageY (.-nativeEvent event))
                           current-timestamp (.-timestamp (.-nativeEvent event))
                           [previous-position previous-timestamp] @previous-touch]
                       (when (and previous-position
                                  (> 100 (- current-timestamp previous-timestamp))
                                  (< 10 (- current-position
                                           previous-position)))
                         (filter.views/show-search!)))
                     false)}))
         [list/flat-list {:data           all-home-items
                          :key-fn         first
                          :footer         [react/view
                                           {:style {:height     tabs.styles/tabs-diff
                                                    :align-self :stretch}}]
                          :on-scroll-begin-drag
                          (fn [^js e]
                            (reset! scrolling-from-top?
                                    ;; check if scrolling up from top of list
                                    (zero? (.-y (.-contentOffset (.-nativeEvent e))))))
                          :render-fn
                          (fn [home-item]
                            [inner-item/home-list-item home-item])}]]))))

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
  (views/letsubs [{:keys [search-filter chats all-home-items]} [:home-items]]
    {:component-did-mount (fn [^js this]
                            (let [[_ loading?] (.. this -props -argv)]
                              (when loading? (utils/set-timeout #(re-frame/dispatch [:init-rest-of-chats]) 100))))}
    [react/view {:flex 1}
     [status-bar/status-bar {:type :main}]
     [react/keyboard-avoiding-view {:style     {:flex 1
                                                :align-items :center}
                                    :on-layout (fn [^js e]
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
              [connectivity/connectivity-view]
              [react/activity-indicator {:flex      1
                                         :animating true}]]

             :else
             [react/view {:style {:flex 1}}
              [connectivity/connectivity-view]
              [filter.views/animated-search-input search-filter]
              (if (and (not search-filter)
                       (empty? all-home-items))
                [home-empty-view]
                [home-items-view search-filter chats all-home-items])])]
      [home-action-button]]]))

(views/defview home-wrapper []
  (views/letsubs [loading? [:chats/loading?]]
    [home loading?]))
