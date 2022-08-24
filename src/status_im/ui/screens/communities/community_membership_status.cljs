(ns status-im.ui.screens.communities.community-membership-status
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.ui.screens.communities.community-views-redesign :as community-views]
            [quo2.components.text :as quo2.text]
            [quo2.components.button :as quo2.button]
            [quo2.foundations.colors :as quo2.colors]
            [quo.components.safe-area :as safe-area]
            [status-im.qr-scanner.core :as qr-scanner]
            [quo2.components.tabs :as quo2.tabs]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.react-native.resources :as resources]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.topbar :as topbar]
            [status-im.utils.handlers :refer [<sub]]
            [status-im.ui.components.plus-button :as components.plus-button])
  (:require-macros [status-im.utils.views :as views]))

(def selected-tab (reagent/atom :all))

(defn qr-scanner []
  [quo2.button/button
   {:icon                true
    :size                32
    :type                :grey
    :style               {:margin-left 10}
    :accessibility-label :scan-qr-code-button
    :on-press #(re-frame/dispatch [::qr-scanner/scan-code
                                   {:title   (i18n/label :t/add-bootnode)
                                    :handler :bootnodes.callback/qr-code-scanned}])}
   :main-icons2/scanner])

(defn qr-code []
  [quo2.button/button
   {:icon                true
    :type                :grey
    :size                32
    :style               {:margin-left 10}
    :accessibility-label :contact-qr-code-button}
   :main-icons2/qr-code])

(views/defview notifications-button []
  (views/letsubs [notif-count [:activity.center/notifications-count]]
    [react/view
     [quo2.button/button {:icon                true
                          :type                :grey
                          :size                32
                          :style               {:margin-left 10}
                          :accessibility-label "notifications-button"
                          :on-press #(do
                                       (re-frame/dispatch [:mark-all-activity-center-notifications-as-read])
                                       (re-frame/dispatch [:navigate-to :notifications-center]))}
      :main-icons2/notifications]
     (when (pos? notif-count)
       [react/view {:style (merge (styles/counter-public-container) {:top 5 :right 5})
                    :pointer-events :none}
        [react/view {:style               styles/counter-public
                     :accessibility-label :notifications-unread-badge}]])]))

(views/defview plus-button []
  (views/letsubs [logging-in? [:multiaccounts/login]]
    [components.plus-button/plus-button
     {:on-press (when-not logging-in?
                  #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new {}]))
      :loading logging-in?
      :accessibility-label :new-chat-button}]))

(defn render-fn [community-item]
  [community-views/communities-membership-list-item community-item])

(defn community-list-key-fn [item]
  (:id item))

(defn get-item-layout [_ index]
  #js {:length 64 :offset (* 64 index) :index index})

(defn community-tabs []
  [react/view {:flex-direction     :row
               :align-items        :center
               :padding-bottom     8
               :padding-top        16
               :height             56
               :padding-horizontal 20}
   [react/view {:flex   1}
    [quo2.tabs/tabs {:size              32
                     :on-change         #(reset! selected-tab %)
                     :default-active    :all
                     :data [{:id :all   :label "Joined"}
                            {:id :open  :label "Pending"}
                            {:id :gated :label "Opened"}]}]]])

(views/defview popular-communities [communities]
  [list/flat-list
   {:key-fn                            community-list-key-fn
    :getItemLayout                     get-item-layout
    :keyboard-should-persist-taps      :always
    :shows-horizontal-scroll-indicator false
    :data                              communities
    :render-fn                         render-fn}])

(defn community-tabs-view [communities]
  (let [tab @selected-tab]
    [react/view
     (case tab
       (= tab :all)
       [popular-communities communities]

       :open
       [popular-communities communities]

       :gated
       [popular-communities communities])]))

(defn title-column []
  [react/view
   {:flex-direction     :row
    :align-items        :center
    :padding-vertical   12
    :padding-horizontal 20}
   [react/view
    {:flex           1}
    [quo2.text/text {:accessibility-label :communities-screen-title
                     :margin-right        6
                     :weight              :semi-bold
                     :size                :heading-1}
     "Communities"]]
   [plus-button]])
(defn discover-card []
  [react/view
   {:background-color   (quo2.colors/theme-colors
                         quo2.colors/white
                         quo2.colors/neutral-80)
    :align-items        :center
    :padding-horizontal 12
    :margin-vertical    8
    :border-radius      12
    :margin-horizontal  20
    :height             56
    :flex-direction     :row}
   [react/view
    {:flex           1}
    [quo2.text/text {:accessibility-label :community-name-text
                     :ellipsize-mode      :tail
                     :number-of-lines     1
                     :weight               :medium
                     :size                 :paragraph-1}
     (i18n/label :t/discover-communities)]
    [quo2.text/text {:accessibility-label :community-name-text
                     :ellipsize-mode      :tail
                     :number-of-lines     1
                     :color               (quo2.colors/theme-colors
                                           quo2.colors/neutral-50
                                           quo2.colors/neutral-40)
                     :weight               :medium
                     :size                 :paragraph-2}
     (i18n/label :t/whats-trending)]]
   [react/image {:source         (resources/get-image :discover)
                 :position       :absolute
                 :top            6
                 :right          24
                 :style {:width  56
                         :height 50}}]])

(defn views []
  (let [multiaccount @(re-frame/subscribe [:multiaccount])
        communities (<sub [:communities/communities])]
    (fn []
      [safe-area/consumer
       (fn []
         [react/view {:style {:flex             1
                              :background-color (quo2.colors/theme-colors
                                                 quo2.colors/neutral-5
                                                 quo2.colors/neutral-95)}}
          [topbar/topbar
           {:navigation      :none
            :left-component  [react/view {:margin-left 12}
                              [photos/photo (multiaccounts/displayed-photo multiaccount)
                               {:size 32}]]
            :right-component [react/view {:flex-direction :row
                                          :margin-right 12}
                              [qr-scanner]
                              [qr-code]
                              [notifications-button]]
            :new-ui?         true
            :border-bottom   false}]
          [title-column]
          [react/scroll-view
           [discover-card]
           [community-tabs]
           [community-tabs-view communities]]])])))