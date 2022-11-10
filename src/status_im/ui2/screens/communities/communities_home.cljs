(ns status-im.ui2.screens.communities.communities-home
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [quo2.components.community.community-list-view :as community-list-view]
            [quo.components.safe-area :as safe-area]
            [quo2.components.markdown.text :as text]
            [quo2.components.tabs.tabs :as tabs]
            [quo2.foundations.colors :as colors]
            [quo2.components.community.discover-card :as discover-card]
            [quo2.components.navigation.top-nav :as topnav]
            [status-im.utils.handlers :refer [<sub >evt]]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as rn]
            [status-im.ui.components.plus-button :as components.plus-button]
            [status-im.ui.screens.communities.community :as community]))

(def selected-tab (reagent/atom :joined))

(defn plus-button []
  (let [logging-in? (<sub [:multiaccounts/login])]
    [components.plus-button/plus-button
     {:on-press (when-not logging-in?
                  #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new {}]))
      :loading logging-in?
      :accessibility-label :new-chat-button}]))

(defn render-fn [community-item]
  [community-list-view/communities-membership-list-item
   {:on-press      (fn []
                     (>evt [:communities/load-category-states (:id community-item)])
                     (>evt [:dismiss-keyboard])
                     (>evt [:navigate-to :community {:community-id (:id community-item)}]))
    :on-long-press #(>evt [:bottom-sheet/show-sheet
                           {:content (fn []
                                       [community/community-actions community-item])}])}
   community-item])

(defn community-list-key-fn [item]
  (:id item))

(defn get-item-layout-js [_ index]
  #js {:length 64 :offset (* 64 index) :index index})

(defn home-community-segments []
  [rn/view {:style {:padding-bottom     12
                    :padding-top        16
                    :margin-top         8
                    :height             60
                    :padding-horizontal 20}}
   [tabs/tabs {:size              32
               :on-change         #(reset! selected-tab %)
               :default-active    :joined
               :data [{:id :joined   :label (i18n/label :chats/joined) :accessibility-label :joined-tab}
                      {:id :pending  :label (i18n/label :t/pending)    :accessibility-label :pending-tab}
                      {:id :opened   :label (i18n/label :t/opened)     :accessibility-label :opened-tab}]}]])

(defn communities-list [communities]
  [list/flat-list
   {:key-fn                            community-list-key-fn
    :getItemLayout                     get-item-layout-js
    :keyboard-should-persist-taps      :always
    :shows-horizontal-scroll-indicator false
    :data                              communities
    :render-fn                         render-fn}])

(defn segments-community-lists [communities]
  (let [tab @selected-tab]
    [rn/view {:style {:padding-left      20
                      :padding-vertical  12}}
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
    {:flex           1}
    [text/text {:accessibility-label :communities-screen-title
                :margin-right        6
                :weight              :semi-bold
                :size                :heading-1}
     (i18n/label :t/communities)]]
   [plus-button]])

(defn discover-card []
  [discover-card/discover-card {:on-press            #(>evt [:navigate-to  :discover-communities])
                                :title               (i18n/label :t/discover)
                                :description         (i18n/label :t/whats-trending)
                                :accessibility-label :communities-home-discover-card}])

(defn communities-home []
  (let [communities  (<sub [:communities/communities])]
    [rn/view  {:flex    1}
     [topnav/top-nav {:type    :default}]
     [title-column]
     [discover-card]
     [home-community-segments]
     [segments-community-lists communities]]))

(defn views []
  [safe-area/consumer
   (fn [insets]
     [rn/view {:style {:flex             1
                       :padding-top      (:top insets)
                       :background-color (colors/theme-colors
                                          colors/neutral-5
                                          colors/neutral-95)}}
      [communities-home]])])


