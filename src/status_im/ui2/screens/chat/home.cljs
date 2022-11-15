(ns status-im.ui2.screens.chat.home
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.home.styles :as styles]
            [quo.design-system.colors :as quo.colors]
            [quo.core :as quo]
            [status-im.add-new.core :as new-chat]
            [status-im.ui.components.search-input.view :as search-input]
            [status-im.add-new.db :as db]
            [status-im.utils.debounce :as debounce]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.plus-button :as components.plus-button]
            [status-im.ui.components.invite.views :as invite]
            [status-im.utils.handlers :refer [<sub >evt]]
            [status-im.utils.config :as config]
            [quo2.components.markdown.text :as quo2.text]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.ui.components.chat-icon.styles :as chat-icon.styles]
            [quo.react-native :as rn]
            [quo.react]
            [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]
            [quo2.components.buttons.button :as quo2.button]
            [quo2.components.tabs.tabs :as quo2.tabs]
            [quo2.components.community.discover-card :as discover-card]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [quo2.components.icon :as quo2.icons]
            [quo.components.safe-area :as safe-area]
            [quo2.components.notifications.info-count :refer [info-count]]
            [status-im.ui2.screens.chat.components.received-cr-item :as received-cr-item]
            [status-im.ui2.screens.chat.components.message-home-item.view :refer [messages-home-item]]
            [status-im.ui2.screens.chat.components.contact-item.view :refer [contact-item]]
            [clojure.string :as str])
  (:require-macros [status-im.utils.views :as views]))

(defn home-tooltip-view []
  [rn/view (styles/chat-tooltip)
   [rn/view {:style {:width       66 :position :absolute :top -6 :background-color quo.colors/white
                     :align-items :center}}
    [rn/image {:source (resources/get-image :empty-chats-header)
               :style  {:width 50 :height 50}}]]
   [rn/touchable-highlight
    {:style               {:position :absolute :right 0 :top 0
                           :width    44 :height 44 :align-items :center :justify-content :center}
     :on-press            #(re-frame/dispatch [:multiaccounts.ui/hide-home-tooltip])
     :accessibility-label :hide-home-button}
    [icons/icon :main-icons/close-circle {:color quo.colors/gray}]]
   [react/i18n-text {:style styles/no-chats-text :key :chat-and-transact}]
   [rn/view {:align-items   :center
             :margin-top    8
             :margin-bottom 12}
    [invite/button]]])

(defn welcome-blank-chats []
  [rn/view {:style {:flex 1 :align-items :center :justify-content :center}}
   [icons/icon :main-icons/placeholder20 {:width 120 :height 120}]
   [rn/text {:style (merge typography/font-semi-bold typography/paragraph-1)} (i18n/label :t/no-messages)]
   [rn/text {:style (merge typography/font-regular typography/paragraph-2)} (i18n/label :t/blank-messages-text)]])

(defn welcome-blank-page []
  [rn/view {:style {:flex            1
                    :flex-direction  :row
                    :align-items     :center
                    :justify-content :center}}
   [react/i18n-text {:style styles/welcome-blank-text :key :welcome-blank-message}]])

(defonce search-active? (reagent/atom false))

(defn search-input-wrapper [search-filter chats-empty]
  [rn/view {:padding-horizontal 16
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

(defn start-suggestion [search-value]
  (let [{:keys [state ens-name public-key]}
        @(re-frame/subscribe [:contacts/new-identity])
        valid-private? (= state :valid)
        valid-public?  (db/valid-topic? search-value)]
    (when (or valid-public? valid-private?)
      [rn/view
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

(defn chat-list-key-fn [item]
  (or (:chat-id item) (:public-key item) (:id item)))

(defn get-item-layout [_ index]
  #js {:length 64 :offset (* 64 index) :index index})

(def selected-tab (reagent/atom :recent))

(defn prepare-items [current-active-tab items]
  (if (= current-active-tab :groups)
    (filter #(-> % :group-chat (= true)) items)
    (filter #(-> % (contains? :chat-id)) items)))

(defn prepare-contacts [contacts]
  (let [data (atom {})]
    (doseq [i (range (count contacts))]
      (let [first-char (get (:alias (nth contacts i)) 0)]
        (if-not (contains? @data first-char)
          (swap! data #(assoc % first-char {:title first-char :data [(nth contacts i)]}))
          (swap! data #(assoc-in % [first-char :data] (conj (:data (get @data first-char)) (nth contacts i)))))))
    (swap! data #(sort @data))
    (vals @data)))

(defn contacts-section-header [{:keys [title]}]
  [rn/view {:style {:border-top-width 1 :border-top-color colors/neutral-20 :padding-vertical 8 :padding-horizontal 20 :margin-top 8}}
   [rn/text {:style (merge typography/font-medium typography/paragraph-2 {:color colors/neutral-50})} title]])

(defn find-contact-requests [notifications]
  (let [received-requests (atom [])
        has-unread?       (atom false)]
    (doseq [i (range (count notifications))]
      (doseq [j (range (count (:data (nth notifications i))))]
        (when (= 1 (get-in (nth (:data (nth notifications i)) j) [:message :contact-request-state]))
          (swap! received-requests conj (nth (:data (nth notifications i)) j)))
        (when (= false (get-in (nth (:data (nth notifications i)) j) [:read]))
          (reset! has-unread? true))))
    {:received-requests @received-requests :has-unread? @has-unread?}))

(def selected-requests-tab (reagent/atom :received))

(defn contact-requests-sheet []
  [:f>
   (fn []
     (let [{window-height :height} (rn/use-window-dimensions)
           safe-area     (safe-area/use-safe-area)
           notifications (<sub [:activity.center/notifications-grouped-by-date])
           {received-requests :received-requests} (find-contact-requests notifications)
           sent-requests []]
       [rn/view {:style {:margin-left 20
                         :height      (- window-height (:top safe-area))}}
        [rn/touchable-opacity
         {:on-press #(>evt [:bottom-sheet/hide])
          :style
          {:background-color (colors/theme-colors colors/neutral-10 colors/neutral-80)
           :width            32
           :height           32
           :border-radius    10
           :justify-content  :center
           :align-items      :center
           :margin-bottom    24}}
         [quo2.icons/icon :i/close {:color (colors/theme-colors "#000000" "#ffffff")}]]
        [rn/text {:style (merge
                          typography/heading-1
                          typography/font-semi-bold
                          {:color (colors/theme-colors "#000000" "#ffffff")})}
         (i18n/label :t/pending-requests)]
        [quo2.tabs/tabs
         {:style          {:margin-top 12 :margin-bottom 20}
          :size           32
          :on-change      #(reset! selected-requests-tab %)
          :default-active :received
          :data           [{:id    :received
                            :label (i18n/label :t/received)}
                           {:id    :sent
                            :label (i18n/label :t/sent)}]}]
        [list/flat-list
         {:key-fn    :first
          :data      (if (= @selected-requests-tab :received) received-requests sent-requests)
          :render-fn received-cr-item/received-cr-item}]]))])

(defn get-display-name [{:keys [chat-id message]}]
  (let [name        (first (<sub [:contacts/contact-two-names-by-identity chat-id]))
        no-ens-name (str/blank? (get-in message [:content :ens-name]))]
    (if no-ens-name
      (first (str/split name " "))
      name)))

(defn requests-summary [requests]
  (case (count requests)
    1
    (get-display-name (first requests))
    2
    (str (get-display-name (first requests)) " " (i18n/label :t/and) " " (get-display-name (second requests)))
    (str (get-display-name (first requests)) ", " (get-display-name (second requests)) " " (i18n/label :t/and) " " (- (count requests) 2) " " (i18n/label :t/more))))

(defn contact-requests [requests]
  [rn/touchable-opacity
   {:active-opacity 1
    :on-press       #(do
                       (>evt
                        [:bottom-sheet/show-sheet
                         {:content (fn [] [contact-requests-sheet])}])
                       (>evt [:mark-all-activity-center-notifications-as-read]))
    :style          {:flex-direction     :row
                     :margin             8
                     :padding-horizontal 12
                     :padding-vertical   8
                     :align-items        :center}}
   [rn/view {:style {:justify-content :center
                     :align-items     :center
                     :width           32
                     :height          32
                     :border-radius   16
                     :border-width    1
                     :border-color    (colors/theme-colors colors/neutral-20 colors/neutral-80)}}
    [quo2.icons/icon :i/pending-user {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]]
   [rn/view {:style {:margin-left 8}}
    [rn/text {:style
              (merge typography/paragraph-1 typography/font-semi-bold {:color (colors/theme-colors "#000000" "#ffffff")})} (i18n/label :t/pending-requests)]
    [rn/text {:style (merge typography/paragraph-2 typography/font-regular {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)})} (requests-summary requests)]]
   [info-count (count requests)]])

(defn chats []
  (let [{:keys [items search-filter]} (<sub [:home-items])
        current-active-tab @selected-tab
        items              (prepare-items current-active-tab items)
        contacts           (<sub [:contacts/active])
        contacts           (prepare-contacts contacts)
        notifications      (<sub [:activity.center/notifications-grouped-by-date])
        {requests :received-requests new-info :has-unread?} (find-contact-requests notifications)]
    (println "wtfff" items contacts)
    [rn/view {:style {:flex 1}}
     [discover-card/discover-card {:title       (i18n/label :t/invite-friends-to-status)
                                   :description (i18n/label :t/share-invite-link)}]
     [quo2.tabs/tabs {:style          {:margin-left   20
                                       :margin-bottom 20
                                       :margin-top    24}
                      :size           32
                      :on-change      #(reset! selected-tab %)
                      :default-active @selected-tab
                      :data           [{:id    :recent
                                        :label (i18n/label :t/recent)}
                                       {:id    :groups
                                        :label (i18n/label :t/groups)}
                                       {:id       :contacts
                                        :label    (i18n/label :t/contacts)
                                        :new-info new-info}]}]
     (if (and (empty? items)
              (empty? search-filter)
              (not @search-active?))
       [welcome-blank-chats]
       (if (not= current-active-tab :contacts)
         [list/flat-list
          {:key-fn                       chat-list-key-fn
           :getItemLayout                get-item-layout
           :on-end-reached               #(re-frame/dispatch [:chat.ui/show-more-chats])
           :keyboard-should-persist-taps :always
           :data                         items
           :render-fn                    messages-home-item}]
         [rn/view {:style {:flex 1}} (when (> (count requests) 0)
                                       [contact-requests requests])
          [list/section-list
           {:key-fn                         :title
            :sticky-section-headers-enabled false
            :sections                       contacts
            :render-section-header-fn       contacts-section-header
            :render-fn                      contact-item}]]))]))

(views/defview chats-list []
  (views/letsubs [loading? [:chats/loading?]]
    [:<>
     [connectivity/loading-indicator]
     (if loading?
       [rn/view {:flex 1 :align-items :center :justify-content :center}
        [rn/activity-indicator {:animating true}]]
       [chats])]))

(views/defview plus-button []
  (views/letsubs [logging-in? [:multiaccounts/login]]
    [components.plus-button/plus-button
     {:on-press            (when-not logging-in?
                             #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new {}]))
      :loading             logging-in?
      :accessibility-label :new-chat-button}]))

(views/defview plus-button-new-messages []
  (let [logging-in? (<sub [:multiaccounts/login])]
    [components.plus-button/plus-button
     {:on-press            (when-not logging-in?
                             #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new-sheet-view {}]))
      :loading             logging-in?
      :accessibility-label :new-chat-button}]))

(views/defview notifications-button []
  (views/letsubs [notif-count [:activity.center/notifications-count]]
    [rn/view
     [quo2.button/button {:type                :grey
                          :size                32
                          :width               32
                          :style               {:margin-left 12}
                          :accessibility-label :notifications-button
                          :on-press            #(do
                                                  (re-frame/dispatch [:mark-all-activity-center-notifications-as-read])
                                                  (if config/new-activity-center-enabled?
                                                    (re-frame/dispatch [:navigate-to :activity-center])
                                                    (re-frame/dispatch [:navigate-to :notifications-center])))}
      [icons/icon :main-icons/notification2 {:color (colors/theme-colors colors/neutral-100 colors/white)}]]
     (when (pos? notif-count)
       [rn/view {:style          (merge (styles/counter-public-container) {:top 5 :right 5})
                 :pointer-events :none}
        [rn/view {:style               styles/counter-public
                  :accessibility-label :notifications-unread-badge}]])]))

(defn qr-button []
  [quo2.button/button {:type                :grey
                       :accessibility-label "qr-button"
                       :size                32
                       :width               32
                       :style               {:margin-left 12}
                       :on-press            #(do
                                               (re-frame/dispatch [::qr-scanner/scan-code
                                                                   {:handler ::qr-scanner/on-scan-success}]))}
   [icons/icon :main-icons/qr2 {:color (colors/theme-colors colors/neutral-100 colors/white)}]])

(defn scan-button []
  [quo2.button/button {:type                :grey
                       :size                32
                       :width               32
                       :accessibility-label "scan-button"
                       :on-press            #(do
                                               (re-frame/dispatch [::qr-scanner/scan-code
                                                                   {:handler ::qr-scanner/on-scan-success}]))}
   [icons/icon :main-icons/scan2 {:color (colors/theme-colors colors/neutral-100 colors/white)}]])

(views/defview profile-button []
  (views/letsubs [{:keys [public-key preferred-name emoji]} [:multiaccount]]
    [rn/view
     [chat-icon/emoji-chat-icon-view public-key false preferred-name emoji
      {:size      28
       :chat-icon chat-icon.styles/chat-icon-chat-list}]]))

(defn home []
  [:f>
   (fn []
     (quo.react/effect! #(re-frame/dispatch [:get-activity-center-notifications]))
     [rn/keyboard-avoiding-view {:style         {:flex             1
                                                 :background-color (colors/theme-colors colors/neutral-5 colors/neutral-95)}
                                 :ignore-offset true}
      [topbar/topbar {:navigation      :none
                      :use-insets      true
                      :background      (colors/theme-colors colors/neutral-5 colors/neutral-95)
                      :left-component  [rn/view {:flex-direction :row :margin-left 20}
                                        [profile-button]]
                      :right-component [rn/view {:flex-direction :row :margin-right 20}
                                        [scan-button]
                                        [qr-button]
                                        [notifications-button]]
                      :border-bottom   false}]
      [rn/view {:flex-direction    :row
                :justify-content   :space-between
                :align-items       :center
                :margin-horizontal 20
                :margin-top        15
                :margin-bottom     20}
       [quo2.text/text {:size :heading-1 :weight :semi-bold} (i18n/label :t/messages)]
       [plus-button-new-messages]]
      [chats-list]])])

