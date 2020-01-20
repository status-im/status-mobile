(ns status-im.ui.screens.home.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.video :as video]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.tabbar.styles :as tabs.styles]
            [status-im.ui.screens.home.views.inner-item :as inner-item]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.animation :as animation]
            [status-im.constants :as constants]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.add-new.new-public-chat.view :as new-public-chat]
            [status-im.ui.components.button :as button])
  (:require-macros [status-im.utils.views :as views]))

(defonce search-active? (reagent/atom false))

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

(defn welcome-video-wrapper []
  [react/view {:style {:align-items     :center
                       :justify-content :center
                       :flex            1}}
   [video/video {:source           (resources/get-video :welcome-video)
                 :repeat           true
                 :pause            false
                 :muted            true
                 :playWhenInactive true
                 :resize-mode      :contain
                 :style            {:position "absolute"
                                    :top      0
                                    :bottom   0
                                    :left     0
                                    :right    0}}]])

(defn welcome []
  [react/view {:style styles/welcome-view}
   (if platform/android?
     [welcome-image-wrapper]
     [welcome-video-wrapper])
   [react/i18n-text {:style styles/welcome-text :key :welcome-to-status}]
   [react/view
    [react/i18n-text {:style styles/welcome-text-description
                      :key   :welcome-to-status-description}]]
   [react/view {:align-items :center :margin-bottom 50}
    [components.common/button {:on-press #(re-frame/dispatch [:navigate-back])
                               :accessibility-label :lets-go-button
                               :label    (i18n/label :t/lets-go)}]]])

(defn home-tooltip-view []
  [react/view styles/chat-tooltip
   [react/view {:style {:flex-direction :row}}
    [react/view {:flex 1}
     [react/view {:style styles/empty-chats-header-container}
      [react/image {:source (resources/get-image :empty-chats-header)
                    :style  {:width 60 :height 50 :position :absolute :top -6}}]]
     [react/touchable-highlight
      {:style               {:position :absolute :right 0 :top 0
                             :width    44 :height 44 :align-items :center :justify-content :center}
       :on-press            #(re-frame/dispatch [:multiaccounts.ui/hide-home-tooltip])
       :accessibility-label :hide-home-button}
      [react/view {:style styles/close-icon-container}
       [icons/icon :main-icons/close {:color colors/white}]]]]]
   [react/i18n-text {:style styles/no-chats-text :key :chat-and-transact}]
   [react/view {:align-items :center :margin-top 16}
    [button/button {:label               :t/invite-friends
                    :on-press            #(list-selection/open-share {:message (i18n/label :t/get-status-at)})
                    :accessibility-label :invite-friends-button}]]
   [react/view {:align-items :center :margin-top 16}
    [react/view {:style styles/hr-wrapper}]
    [react/i18n-text {:style styles/or-text :key :or}]]
   [react/view {:margin-top 16}
    [react/i18n-text {:style {:margin-horizontal 16
                              :text-align        :center}
                      :key   :follow-your-interests}]
    [react/view {:style styles/tags-wrapper}
     [react/view {:flex-direction :row :flex-wrap :wrap :justify-content :center}
      (for [chat new-public-chat/default-public-chats]
        (new-public-chat/render-topic chat))]]]])

(defn welcome-blank-page []
  [react/view {:style {:flex 1 :flex-direction :row :align-items :center :justify-content :center}}
   [react/i18n-text {:style styles/welcome-blank-text :key :welcome-blank-message}]])

(defn chat-list-footer [hide-home-tooltip?]
  (let [show-tooltip? (and (not hide-home-tooltip?) (not @search-active?))]
    [react/view
     (when show-tooltip?
       [home-tooltip-view])
     [react/view {:height 68 :flex 1}]]))

(views/defview search-input [{:keys [on-cancel on-focus on-change]}]
  (views/letsubs
    [{:keys [search-filter]} [:home-items]
     input-ref (reagent/atom nil)]
    [react/view {:style styles/search-container}
     [react/view {:style styles/search-input-container}
      [icons/icon :main-icons/search {:color           colors/gray
                                      :container-style {:margin-left  6
                                                        :margin-right 2}}]
      [react/text-input {:placeholder     (i18n/label :t/search)
                         :blur-on-submit  true
                         :multiline       false
                         :ref             #(reset! input-ref %)
                         :style           styles/search-input
                         :default-value   search-filter
                         :on-focus        #(do
                                             (when on-focus
                                               (on-focus search-filter))
                                             (reset! search-active? true))
                         :on-change       (fn [e]
                                            (let [native-event (.-nativeEvent e)
                                                  text         (.-text native-event)]
                                              (when on-change
                                                (on-change text))))}]]
     (when @search-active?
       [react/touchable-highlight
        {:on-press (fn []
                     (.clear @input-ref)
                     (.blur @input-ref)
                     (when on-cancel
                       (on-cancel))
                     (reset! search-active? false))
         :style    {:margin-left 16}}
        [react/text {:style {:color colors/blue}}
         (i18n/label :t/cancel)]])]))

(defn search-input-wrapper []
  [search-input
   {:on-cancel #(re-frame/dispatch [:search/filter-changed nil])
    :on-focus  (fn [search-filter]
                 (when-not search-filter
                   (re-frame/dispatch [:search/filter-changed ""])))
    :on-change (fn [text]
                 (re-frame/dispatch [:search/filter-changed text]))}])

(defn section-footer [{:keys [title data]}]
  (when (and @search-active? (empty? data))
    [list/big-list-item
     {:text          (i18n/label :t/no-result)
      :text-color    colors/gray
      :hide-chevron? true
      :action-fn     #()
      :icon          (case title
                       "messages" :main-icons/one-on-one-chat
                       "browser" :main-icons/browser
                       "chats" :main-icons/message)
      :icon-color    colors/gray}]))

(views/defview home-filtered-items-list []
  (views/letsubs
    [{:keys [chats all-home-items]} [:home-items]
     {:keys [hide-home-tooltip?]} [:multiaccount]]
    (let [list-ref (reagent/atom nil)]
      [list/section-list
       (merge
        {:sections                     [{:title :t/chats
                                         :data (if @search-active? chats all-home-items)}]
         :key-fn                        first
          ;; true by default on iOS
         :stickySectionHeadersEnabled   false
         :keyboard-should-persist-taps  :always
         :ref                           #(reset! list-ref %)
         :footer                        [chat-list-footer hide-home-tooltip?]
         :contentInset                  {:top styles/search-input-height}
         :render-section-header-fn      (fn [data] [react/view])
         :render-section-footer-fn      section-footer
         :render-fn                     (fn [home-item]
                                          [inner-item/home-list-item home-item])
         :header                        (when (or @search-active? (not-empty all-home-items))
                                          [search-input-wrapper])
         :on-scroll-end-drag
         (fn [e]
           (let [y (-> e .-nativeEvent .-contentOffset .-y)
                 hide-searchbar? (cond
                                   platform/ios?     (and (neg? y) (> y (- (/ styles/search-input-height 2))))
                                   platform/android? (and (< y styles/search-input-height) (> y (/ styles/search-input-height 2))))]
             (if hide-searchbar?
               (.scrollToLocation @list-ref #js {:sectionIndex 0 :itemIndex 0}))))})])))

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
     {:keys [all-home-items]} [:home-items]
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
            (if (and (empty? all-home-items) hide-home-tooltip? (not @search-active?))
              [welcome-blank-page]
              [home-filtered-items-list])])]
        [home-action-button home-width]]])))

(views/defview home-wrapper []
  (views/letsubs [loading? [:chats/loading?]]
    [home loading?]))
