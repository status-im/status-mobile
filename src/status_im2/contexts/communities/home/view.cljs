(ns status-im2.contexts.communities.home.view
  (:require [reagent.core :as reagent]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [i18n.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.components.community.discover-card :as discover-card]
            [status-im2.contexts.communities.home.actions.view :as home.actions]
            [status-im2.common.home.view :as common.home]))

(defn render-fn [id]
  (let [community-item (rf/sub [:communities/home-item id])]
    [quo/communities-membership-list-item
     {:on-press      (fn []
                       (rf/dispatch [:communities/load-category-states id])
                       (rf/dispatch [:dismiss-keyboard])
                       (rf/dispatch [:navigate-to :community {:community-id id}]))
      :on-long-press #(rf/dispatch [:bottom-sheet/show-sheet
                                    {:content (fn []
                                                [home.actions/actions community-item])}])}
     community-item]))

(defn get-item-layout-js [_ index]
  #js {:length 64 :offset (* 64 index) :index index})

(defn home-community-segments [selected-tab]
  [rn/view {:style {:padding-bottom     12
                    :padding-top        16
                    :margin-top         8
                    :height             60
                    :padding-horizontal 20}}
   [quo/tabs {:size           32
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

(defn segments-community-lists [selected-tab]
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

(defn home []
  (let [selected-tab (reagent/atom :joined)]
    (fn []
      [:<>
       [common.home/top-nav {:type :default :hide-search true}]
       [common.home/title-column {:label               (i18n/label :t/communities)
                                  :handler             #(rf/dispatch [:bottom-sheet/show-sheet :add-new {}])
                                  :accessibility-label :new-chat-button}]
       [discover-card/discover-card {:on-press            #(rf/dispatch [:navigate-to :discover-communities])
                                     :title               (i18n/label :t/discover)
                                     :description         (i18n/label :t/whats-trending)
                                     :accessibility-label :communities-home-discover-card}]
       [home-community-segments selected-tab]
       [segments-community-lists selected-tab]])))
