(ns status-im.ui.screens.home.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.ui.screens.communities.views :as communities.views]
            [status-im.ui.screens.home.views.inner-item :as inner-item]
            [quo.design-system.colors :as colors]
            [quo.core :as quo]
            [quo.platform :as platform]
            [status-im.add-new.core :as new-chat]
            [status-im.ui.components.search-input.view :as search-input]
            [status-im.add-new.db :as db]
            [status-im.utils.debounce :as debounce]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.plus-button :as components.plus-button]
            [status-im.ui.screens.chat.sheets :as sheets]
            [status-im.ui.components.tabbar.core :as tabbar]
            [status-im.ui.components.invite.views :as invite]
            [status-im.utils.config :as config]
            [quo2.components.text :as quo2.text]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.chat-icon.styles :as chat-icon.styles]
            [quo2.foundations.colors :as quo2.colors]
            [quo2.components.button :as quo2.button])
  (:require-macros [status-im.utils.views :as views]))

(defn home-tooltip-view []
  [react/view (styles/chat-tooltip)
   [react/view {:style {:width       66 :position :absolute :top -6 :background-color colors/white
                        :align-items :center}}
    [react/image {:source (resources/get-image :empty-chats-header)
                  :style  {:width 50 :height 50}}]]
   [react/touchable-highlight
    {:style               {:position :absolute :right  0  :top         0
                           :width    44        :height 44 :align-items :center :justify-content :center}
     :on-press            #(re-frame/dispatch [:multiaccounts.ui/hide-home-tooltip])
     :accessibility-label :hide-home-button}
    [icons/icon :main-icons/close-circle {:color colors/gray}]]
   [react/i18n-text {:style styles/no-chats-text :key :chat-and-transact}]
   [react/view {:align-items   :center
                :margin-top    8
                :margin-bottom 12}
    [invite/button]]])

(defn welcome-blank-page []
  [react/view {:style {:flex 1 :flex-direction :row :align-items :center :justify-content :center}}
   [react/i18n-text {:style styles/welcome-blank-text :key :welcome-blank-message}]])

(defonce search-active? (reagent/atom false))

(defn search-input-wrapper [search-filter chats-empty]
  [react/view {:padding-horizontal 16
               :padding-vertical   10}
   [search-input/search-input
    {:search-active? search-active?
     :search-filter  search-filter
     :on-cancel      #(re-frame/dispatch [:search/home-filter-changed nil])
     :on-blur        (fn []
                       (when chats-empty
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

(defn search-input-wrapper-old [search-filter chats-empty]
  [react/view {:padding-horizontal 16
               :padding-vertical   10}
   [search-input/search-input-old
    {:search-active? search-active?
     :search-filter  search-filter
     :on-cancel      #(re-frame/dispatch [:search/home-filter-changed nil])
     :on-blur        (fn []
                       (when chats-empty
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

(defn render-fn [{:keys [chat-id] :as home-item}]
  ;; We use `chat-id` to distinguish communities from chats
  (if chat-id
    [inner-item/home-list-item
     home-item
     {:on-press      (fn []
                       (re-frame/dispatch [:dismiss-keyboard])
                       (if (and @config/new-ui-enabled? platform/android?)
                         (re-frame/dispatch [:chat.ui/navigate-to-chat-nav2 chat-id])
                         (re-frame/dispatch [:chat.ui/navigate-to-chat chat-id]))
                       (re-frame/dispatch [:search/home-filter-changed nil])
                       (re-frame/dispatch [:accept-all-activity-center-notifications-from-chat chat-id]))
      :on-long-press #(re-frame/dispatch [:bottom-sheet/show-sheet
                                          {:content (fn []
                                                      [sheets/actions home-item])}])}]
    [communities.views/community-home-list-item home-item]))

(defn render-fn-old [{:keys [chat-id] :as home-item}]
  ;; We use `chat-id` to distinguish communities from chats
  (if chat-id
    [inner-item/home-list-item-old
     home-item
     {:on-press      (fn []
                       (re-frame/dispatch [:dismiss-keyboard])
                       (if (and @config/new-ui-enabled? platform/android?)
                         (re-frame/dispatch [:chat.ui/navigate-to-chat-nav2 chat-id])
                         (re-frame/dispatch [:chat.ui/navigate-to-chat chat-id]))
                       (re-frame/dispatch [:search/home-filter-changed nil])
                       (re-frame/dispatch [:accept-all-activity-center-notifications-from-chat chat-id]))
      :on-long-press #(re-frame/dispatch [:bottom-sheet/show-sheet
                                          {:content (fn []
                                                      [sheets/actions home-item])}])}]
    [communities.views/community-home-list-item home-item]))

(defn chat-list-key-fn [item]
  (or (:chat-id item) (:id item)))

(defn get-item-layout [_ index]
  #js {:length 64 :offset (* 64 index) :index index})

(views/defview communities-and-chats []
  (views/letsubs [{:keys [items search-filter]} [:home-items]
                  hide-home-tooltip? [:hide-home-tooltip?]]
    (if (and (empty? items)
             (empty? search-filter)
             hide-home-tooltip?
             (not @search-active?))
      [welcome-blank-page]
      [list/flat-list
       {:key-fn                       chat-list-key-fn
        :getItemLayout                get-item-layout
        :on-end-reached               #(re-frame/dispatch [:chat.ui/show-more-chats])
        :keyboard-should-persist-taps :always
        :data                         items
        :render-fn                    render-fn
        :header                       [:<>
                                       (when (or (seq items) @search-active? (seq search-filter))
                                         [search-input-wrapper search-filter (empty? items)])
                                       (when (and (empty? items)
                                                  (or @search-active? (seq search-filter)))
                                         [start-suggestion search-filter])]
        :footer                       (if (and (not hide-home-tooltip?) (not @search-active?))
                                        [home-tooltip-view]
                                        [react/view {:height 68}])}])))

(views/defview communities-and-chats-old []
  (views/letsubs [{:keys [items search-filter]} [:home-items]
                  hide-home-tooltip? [:hide-home-tooltip?]]
    (if (and (empty? items)
             (empty? search-filter)
             hide-home-tooltip?
             (not @search-active?))
      [welcome-blank-page]
      [list/flat-list
       {:key-fn                       chat-list-key-fn
        :getItemLayout                get-item-layout
        :on-end-reached               #(re-frame/dispatch [:chat.ui/show-more-chats])
        :keyboard-should-persist-taps :always
        :data                         items
        :render-fn                    render-fn-old
        :header                       [:<>
                                       (when (or (seq items) @search-active? (seq search-filter))
                                         [search-input-wrapper-old search-filter (empty? items)])
                                       (when (and (empty? items)
                                                  (or @search-active? (seq search-filter)))
                                         [start-suggestion search-filter])]
        :footer                       (if (and (not hide-home-tooltip?) (not @search-active?))
                                        [home-tooltip-view]
                                        [react/view {:height 68}])}])))

(views/defview chats-list []
  (views/letsubs [loading? [:chats/loading?]]
    [:<>
     [connectivity/loading-indicator]
     (if loading?
       [react/view {:flex 1 :align-items :center :justify-content :center}
        [react/activity-indicator {:animating true}]]
       [communities-and-chats])]))

(views/defview chats-list-old []
  (views/letsubs [loading? [:chats/loading?]]
    [:<>
     [connectivity/loading-indicator]
     (if loading?
       [react/view {:flex 1 :align-items :center :justify-content :center}
        [react/activity-indicator {:animating true}]]
       [communities-and-chats-old])]))

(views/defview plus-button []
  (views/letsubs [logging-in? [:multiaccounts/login]]
    [components.plus-button/plus-button
     {:on-press (when-not logging-in?
                  #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new {}]))
      :loading logging-in?
      :accessibility-label :new-chat-button}]))

(views/defview plus-button-old []
  (views/letsubs [logging-in? [:multiaccounts/login]]
    [components.plus-button/plus-button-old
     {:on-press (when-not logging-in?
                  #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new {}]))
      :loading logging-in?
      :accessibility-label :new-chat-button}]))

(views/defview notifications-button []
  (views/letsubs [notif-count [:activity.center/notifications-count]]
    [react/view
     [quo2.button/button {:type :grey
                          :size 32
                          :width 32
                          :style {:margin-left 12}
                          :accessibility-label :notifications-button
                          :on-press #(do
                                       (re-frame/dispatch [:mark-all-activity-center-notifications-as-read])
                                       (re-frame/dispatch [:navigate-to :notifications-center]))}
      [icons/icon :main-icons/notification2 {:color (quo2.colors/theme-colors quo2.colors/black quo2.colors/white)}]]
     (when (pos? notif-count)
       [react/view {:style (merge (styles/counter-public-container) {:top 5 :right 5})
                    :pointer-events :none}
        [react/view {:style               styles/counter-public
                     :accessibility-label :notifications-unread-badge}]])]))

(views/defview notifications-button-old []
  (views/letsubs [notif-count [:activity.center/notifications-count]]
    [react/view
     [quo/button {:type     :icon
                  :style {:margin-left 10}
                  :accessibility-label "notifications-button"
                  :on-press #(do
                               (re-frame/dispatch [:mark-all-activity-center-notifications-as-read])
                               (re-frame/dispatch [:navigate-to :notifications-center]))
                  :theme    :icon}
      :main-icons/notification]
     (when (pos? notif-count)
       [react/view {:style (merge (styles/counter-public-container) {:top 5 :right 5})
                    :pointer-events :none}
        [react/view {:style               styles/counter-public
                     :accessibility-label :notifications-unread-badge}]])]))

(defn qr-button []
  [quo2.button/button {:type :grey
                       :accessibility-label "qr-button"
                       :size 32
                       :width 32
                       :style {:margin-left 12}
                       :on-press #(do
                                    (re-frame/dispatch [::qr-scanner/scan-code
                                                        {:handler ::qr-scanner/on-scan-success}]))}
   [icons/icon :main-icons/qr2 {:color (quo2.colors/theme-colors quo2.colors/black quo2.colors/white)}]])

(defn scan-button []
  [quo2.button/button {:type :grey
                       :size 32
                       :width 32
                       :accessibility-label "scan-button"
                       :on-press #(do
                                    (re-frame/dispatch [::qr-scanner/scan-code
                                                        {:handler ::qr-scanner/on-scan-success}]))}
   [icons/icon :main-icons/scan2 {:color (quo2.colors/theme-colors quo2.colors/black quo2.colors/white)}]])

(views/defview profile-button []
  (views/letsubs [{:keys [public-key preferred-name emoji]} [:multiaccount]]
    [react/view
     [chat-icon.screen/emoji-chat-icon-view public-key false preferred-name emoji
      {:size                   28
       :chat-icon              chat-icon.styles/chat-icon-chat-list}]]))

(defn home []
  [react/keyboard-avoiding-view {:style {:flex 1 :background-color (quo2.colors/theme-colors quo2.colors/neutral-5 quo2.colors/neutral-95)}
                                 :ignore-offset true}
   [topbar/topbar {:navigation      :none
                   :use-insets true
                   :background (quo2.colors/theme-colors quo2.colors/neutral-5 quo2.colors/neutral-95)
                   :left-component [react/view {:flex-direction :row :margin-left 16}
                                    [profile-button]]
                   :right-component [react/view {:flex-direction :row :margin-right 16}
                                     [scan-button]
                                     [qr-button]
                                     [notifications-button]]
                   :border-bottom false}]
   [react/view {:flex-direction :row
                :justify-content :space-between
                :align-items :center
                :margin-horizontal 16
                :margin-top 15
                :margin-bottom 8}
    [quo2.text/text {:size :heading-1 :weight :semi-bold} (i18n/label :t/messages)]
    [plus-button]]
   [chats-list]
   [tabbar/tabs-counts-subscriptions]])

(defn home-old []
  [react/keyboard-avoiding-view {:style {:flex 1}
                                 :ignore-offset true}
   [topbar/topbar {:title           (i18n/label :t/chat)
                   :navigation      :none
                   :right-component [react/view {:flex-direction :row :margin-right 16}
                                     [connectivity/connectivity-button]
                                     [notifications-button-old]]}]
   [chats-list-old]
   [plus-button-old]
   [tabbar/tabs-counts-subscriptions]])
