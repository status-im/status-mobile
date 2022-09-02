(ns status-im.ui.screens.communities.community-membership-status
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [quo2.components.community-card-view :as community-card]
            [quo2.components.markdown.text :as quo2.text]
            [quo2.foundations.colors :as quo2.colors]
            [quo.components.safe-area :as safe-area]
            [quo2.components.tabs.tabs :as quo2.tabs]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.react-native.resources :as resources]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.topnav :as topnav]
            [status-im.utils.handlers :refer [<sub]]
            [status-im.ui.components.plus-button :as components.plus-button]))

(def selected-tab (reagent/atom :all))

(defn plus-button []
  (let [logging-in? (<sub [:multiaccounts/login])]
    [components.plus-button/plus-button
     {:on-press (when-not logging-in?
                  #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new {}]))
      :loading logging-in?
      :accessibility-label :new-chat-button}]))

(defn render-fn [community-item]
  [community-card/communities-membership-list-item community-item])

(defn community-list-key-fn [item]
  (:id item))

(defn get-item-layout-js [_ index]
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
                     :data [{:id :all   :label (i18n/label :chats/joined)}
                            {:id :open  :label (i18n/label :t/pending)}
                            {:id :gated :label (i18n/label :t/opened)}]}]]])

(defn popular-communities [communities]
  [list/flat-list
   {:key-fn                            community-list-key-fn
    :getItemLayout                     get-item-layout-js
    :keyboard-should-persist-taps      :always
    :shows-horizontal-scroll-indicator false
    :data                              communities
    :render-fn                         render-fn}])

(defn community-tabs-view [communities]
  (let [tab @selected-tab]
    [react/view {:padding-left 20}
     (case tab
       :all
       [popular-communities communities]

       :open
       [popular-communities communities]

       :gated
       [popular-communities communities])]))

(defn title-column []
  [react/view {:flex-direction     :row
               :align-items        :center
               :padding-vertical   12
               :padding-horizontal 20}
   [react/view  {:flex           1}
    [quo2.text/text {:accessibility-label :communities-screen-title
                     :margin-right        6
                     :weight              :semi-bold
                     :size                :heading-1}
     (i18n/label :t/communities)]]
   [plus-button]])

(defn discover-card []
  [react/view {:background-color   (quo2.colors/theme-colors
                                    quo2.colors/white
                                    quo2.colors/neutral-80)
               :align-items        :center
               :padding-horizontal 12
               :margin-vertical    8
               :border-radius      12
               :margin-horizontal  20
               :height             56
               :flex-direction     :row}
   [react/view {:flex           1}
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
  (let [multiaccount (<sub [:multiaccount])
        communities  (<sub [:communities/communities])]
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
                              [topnav/qr-scanner]
                              [topnav/qr-code]
                              [topnav/notifications-button]]
            :new-ui?         true
            :border-bottom   false}]
          [title-column]
          [react/scroll-view
           [discover-card]
           [community-tabs]
           [community-tabs-view communities]]])])))