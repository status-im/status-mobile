(ns status-im2.contexts.communities.home.view
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [quo2.components.markdown.text :as text]
            [quo2.components.tabs.tabs :as tabs]
            [quo2.foundations.colors :as colors]
            [quo2.components.community.discover-card :as discover-card]
            [quo2.components.navigation.top-nav :as topnav]
            [status-im2.common.plus-button.view :as components.plus-button]
            [utils.re-frame :as rf]
            [i18n.i18n :as i18n]
            [quo2.core :as quo]
            [status-im.ui.screens.communities.community-options-bottom-sheet :as home-actions]))

(defn plus-button []
  (let [logging-in? (rf/sub [:multiaccounts/login])]
    [components.plus-button/plus-button
     {:on-press            (when-not logging-in?
                             #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new {}]))
      :loading             logging-in?
      :accessibility-label :new-chat-button}]))

(defn render-fn [id]
  (let [community-item (rf/sub [:communities/home-item id])]
    [quo/communities-membership-list-item
     {:on-press      (fn []
                       (rf/dispatch [:communities/load-category-states id])
                       (rf/dispatch [:dismiss-keyboard])
                       (rf/dispatch [:navigate-to :community {:community-id id}]))
      :on-long-press #(rf/dispatch [:bottom-sheet/show-sheet
                                    {:content (fn []
                                                [home-actions/options-menu community-item])}])}
     community-item]))

(defn get-item-layout-js [_ index]
  #js {:length 64 :offset (* 64 index) :index index})

(def selected-tab (reagent/atom :joined))

(defn home-community-segments []
  [rn/view {:style {:padding-bottom     12
                    :padding-top        16
                    :margin-top         8
                    :height             60
                    :padding-horizontal 20}}
   [tabs/tabs {:size           32
               :on-change      #(reset! selected-tab %)
               :default-active :joined
               :data           [{:id :joined :label (i18n/label :chats/joined) :accessibility-label :joined-tab}
                                {:id :pending :label (i18n/label :t/pending) :accessibility-label :pending-tab}
                                {:id :opened :label (i18n/label :t/opened) :accessibility-label :opened-tab}]}]])

(defn communities-list [community-ids]
  [rn/flat-list
   {:key-fn                            identity
    :get-item-layout                   get-item-layout-js
    :keyboard-should-persist-taps      :always
    :shows-horizontal-scroll-indicator false
    :data                              community-ids
    :render-fn                         render-fn}])

(defn segments-community-lists []
  (let [communities (rf/sub [:communities/community-ids])
        tab @selected-tab]
    [rn/view {:style {:padding-left     20
                      :padding-right    8
                      :padding-vertical 12}}
     (case tab
       :joined
       [communities-list communities]

       :pending
       [communities-list communities]

       :opened
       [communities-list communities])]))

(defn title-column []
  [rn/view
   {:style {:flex-direction     :row
            :align-items        :center
            :height             56
            :padding-vertical   12
            :margin-bottom      8
            :padding-horizontal 20}}
   [rn/view
    {:flex 1}
    [text/text {:accessibility-label :communities-screen-title
                :margin-right        6
                :weight              :semi-bold
                :size                :heading-1}
     (i18n/label :t/communities)]]
   [plus-button]])

(defn discover-card []
  [discover-card/discover-card {:on-press            #(rf/dispatch [:navigate-to :discover-communities])
                                :title               (i18n/label :t/discover)
                                :description         (i18n/label :t/whats-trending)
                                :accessibility-label :communities-home-discover-card}])

(defn home []
  [safe-area/consumer
   (fn [insets]
     [rn/view {:style {:flex             1
                       :padding-top      (:top insets)
                       :background-color (colors/theme-colors
                                          colors/neutral-5
                                          colors/neutral-95)}}
      [rn/view {:flex 1}
       [topnav/top-nav {:type :default}]
       [title-column]
       [discover-card]
       [home-community-segments]
       [segments-community-lists]]])])
