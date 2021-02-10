(ns status-im.ui.screens.home.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.communities.core :as communities]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.ui.screens.communities.views :as communities.views]
            [status-im.ui.screens.home.views.inner-item :as inner-item]
            [status-im.ui.screens.referrals.home-item :as referral-item]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.add-new.new-public-chat.view :as new-public-chat]
            [quo.core :as quo]
            [status-im.ui.screens.add-new.new-chat.events :as new-chat]
            [status-im.ui.components.search-input.view :as search-input]
            [status-im.ui.screens.add-new.new-public-chat.db :as db]
            [status-im.utils.debounce :as debounce]
            [status-im.utils.utils :as utils]
            [status-im.utils.config :as config]
            [cljs-bean.core :as bean]
            [status-im.multiaccounts.login.core :as multiaccounts.login]
            [status-im.ui.components.invite.views :as invite]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.plus-button :as components.plus-button])
  (:require-macros [status-im.utils.views :as views]))

(defn welcome-image-wrapper []
  (let [dimensions (reagent/atom {})]
    (fn []
      [react/view {:on-layout (fn [^js e]
                                (reset! dimensions (bean/->clj (-> e .-nativeEvent .-layout))))
                   :style     {:align-items     :center
                               :justify-content :center
                               :flex            1}}
       (let [padding    0
             image-size (- (min (:width @dimensions) (:height @dimensions)) padding)]
         [react/image {:source      (resources/get-theme-image :welcome)
                       :resize-mode :contain
                       :style       {:width image-size :height image-size}}])])))

(defn welcome []
  [react/view {:style styles/welcome-view}
   [welcome-image-wrapper]
   [react/i18n-text {:style styles/welcome-text :key :welcome-to-status}]
   [react/view
    [react/i18n-text {:style styles/welcome-text-description
                      :key   :welcome-to-status-description}]]
   [react/view {:align-items :center :margin-bottom 50}
    [quo/button {:on-press            #(re-frame/dispatch [::multiaccounts.login/welcome-lets-go])
                 :accessibility-label :lets-go-button}
     (i18n/label :t/lets-go)]]])

(defn home-tooltip-view []
  [react/view (styles/chat-tooltip)
   [react/view {:style {:flex-direction :row}}
    [react/view {:flex 1}
     [react/view {:style styles/empty-chats-header-container}
      [react/view {:style {:width       66 :position :absolute :top -6 :background-color colors/white
                           :align-items :center}}
       [react/image {:source (resources/get-image :empty-chats-header)
                     :style  {:width 50 :height 50}}]]]
     [react/touchable-highlight
      {:style               {:position :absolute :right  0  :top         0
                             :width    44        :height 44 :align-items :center :justify-content :center}
       :on-press            #(re-frame/dispatch [:multiaccounts.ui/hide-home-tooltip])
       :accessibility-label :hide-home-button}
      [icons/icon :main-icons/close-circle {:color colors/gray}]]]]
   [react/view
    [react/i18n-text {:style styles/no-chats-text :key :chat-and-transact}]]
   [invite/button]
   [react/view {:align-items :center}
    [react/view {:style (styles/hr-wrapper)}]
    [react/i18n-text {:style (styles/or-text) :key :or}]]
   [react/view {:margin-top 16}
    [react/i18n-text {:style {:margin-horizontal 16
                              :text-align        :center}
                      :key   :follow-your-interests}]
    [react/view {:style styles/tags-wrapper}
     [react/view {:flex-direction :row :flex-wrap :wrap :justify-content :center}
      (for [chat (new-public-chat/featured-public-chats)]
        (new-public-chat/render-topic chat))]]
    (when config/communities-enabled?
      [react/view
       [react/i18n-text {:style {:margin-horizontal 16
                                 :text-align        :center}
                         :key   :join-a-community}]
       [react/view {:style styles/tags-wrapper}
        [react/view {:flex-direction :row :flex-wrap :wrap :justify-content :center}
         (for [community communities/featured]
           (communities.views/render-featured-community community))]]])]])

(defn welcome-blank-page []
  [react/view {:style {:flex 1 :flex-direction :row :align-items :center :justify-content :center}}
   [react/i18n-text {:style styles/welcome-blank-text :key :welcome-blank-message}]])

(defonce search-active? (reagent/atom false))

(defn search-input-wrapper [search-filter chats]
  [react/view {:padding-horizontal 16
               :padding-vertical   10}
   [search-input/search-input
    {:search-active? search-active?
     :search-filter  search-filter
     :on-cancel      #(re-frame/dispatch [:search/home-filter-changed nil])
     :on-blur        (fn []
                       (when-not (seq chats)
                         (re-frame/dispatch [:search/home-filter-changed nil]))
                       (re-frame/dispatch [::new-chat/clear-new-identity]))
     :on-focus       (fn [search-filter]
                       (when-not search-filter
                         (re-frame/dispatch [:search/home-filter-changed ""])
                         (re-frame/dispatch [::new-chat/clear-new-identity])))
     :on-change      (fn [text]
                       (re-frame/dispatch [:search/home-filter-changed text])
                       (re-frame/dispatch [:set-in [:contacts/new-identity :state] :searching])
                       (debounce/debounce-and-dispatch [:new-chat/set-new-identity text] 300))}]])

(defn start-suggestion [search-value]
  (let [{:keys [state ens-name public-key]}
        @(re-frame/subscribe [:contacts/new-identity])
        valid-private? (= state :valid)
        valid-public?  (db/valid-topic? search-value)]
    (when (or valid-public? valid-private?)
      [react/view
       [quo/list-header (i18n/label :t/search-no-chat-found)]
       (when valid-private?
         [quo/list-item {:theme    :accent
                         :icon     :main-icons/private-chat
                         :title    (or ens-name (utils/get-shortened-address public-key))
                         :subtitle (i18n/label :t/join-new-private-chat)
                         :on-press (fn []
                                     (debounce/dispatch-and-chill [:contact.ui/contact-code-submitted false] 3000)
                                     (re-frame/dispatch [:search/home-filter-changed nil]))}])
       (when valid-public?
         [quo/list-item {:theme    :accent
                         :icon     :main-icons/public-chat
                         :title    (str "#" search-value)
                         :subtitle (i18n/label :t/join-new-public-chat)
                         :on-press (fn []
                                     (re-frame/dispatch [:chat.ui/start-public-chat search-value])
                                     (re-frame/dispatch [:set :public-group-topic nil])
                                     (re-frame/dispatch [:search/home-filter-changed nil]))}])])))

(defn render-fn [home-item]
  [inner-item/home-list-item home-item])

(defn communities-and-chats [chats status-community loading? search-filter hide-home-tooltip?]
  (if loading?
    [react/view {:flex 1 :align-items :center :justify-content :center}
     [react/activity-indicator {:animating true}]]
    (if (and (empty? chats)
             (not status-community)
             (empty? search-filter)
             hide-home-tooltip?
             (not @search-active?))
      [welcome-blank-page]
      [react/view
       [:<>
        (when (or (seq chats) @search-active? (seq search-filter))
          [search-input-wrapper search-filter chats])
        [referral-item/list-item]]
       (when (and (empty? chats)
                  (not status-community)
                  (or @search-active? (seq search-filter)))
         [start-suggestion search-filter])
       (when status-community
         ;; We only support one community now, Status
         [communities.views/status-community status-community])
       (when (and status-community
                  (seq chats))
         [quo/separator])
       [list/flat-list
        {:key-fn                       :chat-id
         :keyboard-should-persist-taps :always
         :data                         chats
         :render-fn                    render-fn
         :footer                       (if (and (not hide-home-tooltip?) (not @search-active?))
                                         [home-tooltip-view]
                                         [react/view {:height 68}])}]])))

(views/defview chats-list []
  (views/letsubs [status-community [:communities/status-community]
                  loading? [:chats/loading?]
                  {:keys [chats search-filter]} [:home-items]
                  {:keys [hide-home-tooltip?]} [:multiaccount]]
    [react/scroll-view
     [communities-and-chats chats status-community loading? search-filter hide-home-tooltip?]]))

(views/defview plus-button []
  (views/letsubs [logging-in? [:multiaccounts/login]]
    [components.plus-button/plus-button
     {:on-press (when-not logging-in?
                  #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new {}]))
      :loading logging-in?
      :accessibility-label :new-chat-button}]))

(defn home []
  [react/keyboard-avoiding-view {:style styles/home-container}
   [topbar/topbar {:title             (i18n/label :t/chat)
                   :navigation        :none
                   :right-component   [connectivity/connectivity-button]}]
   [connectivity/loading-indicator]
   [chats-list]
   [plus-button]])
