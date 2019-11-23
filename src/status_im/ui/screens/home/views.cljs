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
            [status-im.utils.platform :as platform]
            [status-im.ui.components.tabbar.styles :as tabs.styles]
            [status-im.ui.screens.home.views.inner-item :as inner-item]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.animation :as animation]
            [status-im.constants :as constants]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.fx :as fx]
            [status-im.ui.screens.add-new.new-public-chat.view :as new-public-chat])
  (:require-macros [status-im.utils.views :as views]))

(defn welcome-blank-page [blank?]
  (when blank?
    [react/view {:style {:flex 1 :flex-direction :row :align-items :center :justify-content :center}}
     [react/i18n-text {:style styles/welcome-blank-text :key :welcome-blank-message}]]))

(defn welcome-image-wrapper []
  (let [dimensions (reagent/atom {})]
    (fn []
      [react/view {:on-layout  (fn [e]
                                 (reset! dimensions (js->clj (-> e .-nativeEvent .-layout) :keywordize-keys true)))
                   :style {:align-items :center
                           :justify-content :center
                           :flex 1}}
       (let [padding    0
             image-size (- (min (:width @dimensions) (:height @dimensions)) padding)]
         [react/image {:source (resources/get-image :welcome)
                       :resize-mode :contain
                       :style {:width image-size :height image-size}}])])))

(defn welcome []
  [react/view {:style {:flex 1}}
   [react/view {:style styles/welcome-view}
    [welcome-image-wrapper]
    [react/i18n-text {:style styles/welcome-text :key :welcome-to-status}]
    [react/view
     [react/i18n-text {:style styles/welcome-text-description
                       :key   :welcome-to-status-description}]]
    [react/view {:align-items :center :margin-bottom 50}
     [components.common/button {:on-press #(re-frame/dispatch [:navigate-back])
                                :accessibility-label :lets-go-button
                                :label    (i18n/label :t/lets-go)}]]]])

(defn chat-tags-view []
  [react/view {:align-items :center :margin-top 16}
   [react/i18n-text {:style styles/no-chats-text :key :follow-your-interests}]
   [react/view {:style styles/tags-wrapper}
    [react/view {:flex-direction :row :flex-wrap :wrap :justify-content :center}
     (for [chat new-public-chat/default-public-chats]
       (new-public-chat/render-topic chat))]]])

(defn home-tooltip-view [blank? hide-home-tooltip?]
  (filter.views/reset-height)
  [react/view styles/no-chats
   (if-not hide-home-tooltip?
     [react/view styles/no-chats-wrapper
      [react/view {:style {:flex-direction :row}}
       [react/view {:flex 1}
        [react/view {:style styles/empty-chats-header-container}
         [components.common/image-contain
          {:container-style {:width 60 :height 60 :margin-top -20}}
          {:image (resources/get-image :empty-chats-header) :width 60 :height 60}]]
        [react/view {:style {:position :absolute :right 0 :top 5}}
         [react/touchable-highlight {:on-press (when-not hide-home-tooltip?
                                                 #(re-frame/dispatch [:multiaccounts.ui/hide-home-tooltip]))
                                     :accessibility-label :hide-home-button}
          [react/view {:style styles/close-icon-container}
           [icons/icon :main-icons/close {:color colors/white :width 19 :height 19}]]]]]]
      [react/i18n-text {:style styles/no-chats-text :key :chat-and-transact}]
      [react/view {:align-items :center :margin-top 16}
       [components.common/button {:on-press #(list-selection/open-share {:message (i18n/label :t/get-status-at)})
                                  :accessibility-label :invite-friends-button
                                  :label    (i18n/label :t/invite-friends)}]]
      [react/view {:align-items :center :margin-top 16}
       [react/view {:style styles/hr-wrapper}]
       [react/i18n-text {:style styles/or-text :key :or}]]
      [chat-tags-view]]
     [welcome-blank-page blank?])])

(defn home-items-view [_ _ _ _ search-input-state]
  (let [previous-touch      (reagent/atom nil)
        scrolling-from-top? (reagent/atom true)]
    (fn [search-filter chats all-home-items hide-home-tooltip?]
      (if search-filter
        [filter.views/home-filtered-items-list chats]
        [react/animated-view
         (merge {:style {:flex             1
                         :background-color :white
                         :margin-bottom    (- styles/search-input-height)
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
                                  (not (:show? @search-input-state))
                                  (> 100 (- current-timestamp previous-timestamp))
                                  (< 10 (- current-position
                                           previous-position)))
                         (filter.views/show-search!)))
                     false)}))
         [list/flat-list {:data           all-home-items
                          :key-fn         first
                          :header         [react/view {:height 4 :flex 1}]
                          :footer         [react/view {:height 380 :margin-top 70} [home-tooltip-view false hide-home-tooltip?]]
                          :on-scroll-begin-drag
                          (fn [e]
                            (reset! scrolling-from-top?
                                    ;; check if scrolling up from top of list
                                    (zero? (.-y (.-contentOffset (.-nativeEvent e))))))
                          :render-fn
                          (fn [home-item _]
                            [inner-item/home-list-item home-item])}]
         (when (:to-hide? @search-input-state)
           [react/view {:width  1
                        :height styles/search-input-height}])]))))

(views/defview home-action-button [home-width]
  (views/letsubs [logging-in? [:multiaccounts/login]]
    [react/view (styles/action-button-container home-width)
     [react/touchable-highlight {:accessibility-label :new-chat-button
                                 :on-press            (when-not logging-in? #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new {}]))}
      [react/view styles/action-button
       (if logging-in?
         [react/activity-indicator {:color     :white
                                    :animating true}]
         [icons/icon :main-icons/add {:color :white}])]]]))

(views/defview home [loading?]
  (views/letsubs
    [anim-translate-y (animation/create-value connectivity/neg-connectivity-bar-height)
     {:keys [search-filter chats all-home-items]} [:home-items]
     {:keys [hide-home-tooltip?]} [:multiaccount]
     window-width [:dimensions/window-width]
     two-pane-ui-enabled? [:two-pane-ui-enabled?]]
    (let [home-width (if (> window-width constants/two-pane-min-width)
                       (max constants/left-pane-min-width (/ window-width 3))
                       window-width)]
      [react/view (merge {:flex 1
                          :width home-width}
                         (when platform/ios?
                           {:margin-bottom tabs.styles/tabs-diff})
                         (when two-pane-ui-enabled?
                           {:border-right-width 1 :border-right-color colors/gray-lighter}))
       [react/keyboard-avoiding-view {:style     {:flex 1}
                                      :on-layout (fn [e]
                                                   (re-frame/dispatch
                                                    [:set-once :content-layout-height
                                                     (-> e .-nativeEvent .-layout .-height)]))}
        [toolbar/toolbar {:style {:z-index 2}} nil [toolbar/content-title (i18n/label :t/chat)]]
        ;; toolbar, connectivity-view, cannectivity-animation-wrapper are expected
        ;; to be next to each other as siblings for them to work effctively.
        ;; les-debug-info being here could disrupt that. Assuming its purpose is
        ;; debug only, commenting it out for now.
        ;; [les-debug-info]
        [connectivity/connectivity-view anim-translate-y]
        [connectivity/connectivity-animation-wrapper
         {}
         anim-translate-y
         true
         (if loading?
           [react/activity-indicator {:flex      1
                                      :animating true}]
           [react/view {:flex 1}
            [filter.views/search-input-wrapper search-filter]
            (if (and (not search-filter)
                     (empty? all-home-items))
              [home-tooltip-view true hide-home-tooltip?]
              [home-items-view
               search-filter
               chats
               all-home-items
               hide-home-tooltip?
               filter.views/search-input-state])])]
        [home-action-button home-width]]])))

(views/defview home-wrapper []
  (views/letsubs [loading? [:chats/loading?]]
    [home loading?]))
