(ns status-im2.contexts.communities.home.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [reagent.core :as reagent]
            [react-native.core :as rn]
            [status-im2.common.home.view :as common.home]
            [status-im2.contexts.communities.menus.community-options.view :as options]
            [utils.re-frame :as rf]
            [react-native.reanimated :as reanimated]
            [status-im2.common.sticky-scroll-view.view :as sticky-scroll-view]
            [react-native.safe-area :as safe-area]))

(defn tab-segments
  [selected-tab]
  (let [{:keys [joined pending opened]} (rf/sub [:communities/community-ids-by-user-involvement])
        selected-items                  (case selected-tab
                                          :joined  joined
                                          :pending pending
                                          :opened  opened)]
    [rn/view {:style {:flex 1 :padding-horizontal 20 :padding-vertical 12}}
     (doall
      (map-indexed
       (fn [index id]
         (let [community (rf/sub [:communities/home-item id])]
           ^{:key index}
           [quo/communities-membership-list-item
            {:on-press      #(rf/dispatch [:navigate-to-nav2 :community-overview id])
             :on-long-press #(rf/dispatch
                              [:bottom-sheet/show-sheet
                               {:content       (fn []
                                                 [options/community-options-bottom-sheet id])
                                :selected-item (fn []
                                                 [quo/communities-membership-list-item {} community])}])}
            community]))
       selected-items))]))

(def tabs-data
  [{:id :joined :label (i18n/label :chats/joined) :accessibility-label :joined-tab}
   {:id :pending :label (i18n/label :t/pending) :accessibility-label :pending-tab}
   {:id :opened :label (i18n/label :t/opened) :accessibility-label :opened-tab}])

(defn home
  []
  (let [selected-tab (reagent/atom :joined)]
    (fn []
      [:f>
       (fn []
         (let [header-height      112
               sticky-item-height 60
               card-height        72
               scroll-ref         (rn/create-ref)
               scroll-y           (reanimated/use-shared-value 0)
               tabs-translation-y (reanimated/interpolate scroll-y
                                                          [card-height
                                                           (+ card-height sticky-item-height)]
                                                          [0 sticky-item-height]
                                                          {:extrapolateLeft  "clamp"
                                                           :extrapolateRight "extend"})]
           [safe-area/consumer
            (fn [{:keys [top]}]
              [:<>
               [rn/view {:position :absolute :top top :left 0 :right 0 :height header-height :z-index 1}
                [common.home/top-nav]
                [common.home/title-column
                 {:label               (i18n/label :t/communities)
                  :handler             #(rf/dispatch [:bottom-sheet/show-sheet :add-new {}])
                  :accessibility-label :new-chat-button}]]
               [sticky-scroll-view/scroll-view
                {:ref      scroll-ref
                 :scroll-y scroll-y
                 :blur     {:height (+ top header-height sticky-item-height)
                            :delta  sticky-item-height}}
                [rn/view {:height (+ header-height top)}]
                [quo/discover-card
                 {:on-press            #(rf/dispatch [:navigate-to :discover-communities])
                  :title               (i18n/label :t/discover)
                  :description         (i18n/label :t/whats-trending)
                  :accessibility-label :communities-home-discover-card}]
                [sticky-scroll-view/sticky-item
                 {:translation-y tabs-translation-y
                  :height        sticky-item-height}
                 [quo/tabs
                  {:size 32
                   :style {:padding-horizontal 20
                           :padding-top        16
                           :padding-bottom     12}
                   :on-change (fn [val]
                                (js/setTimeout #(reanimated/set-shared-value scroll-y 0) 300)
                                (some-> ^js (rn/current-ref scroll-ref)
                                        (.scrollTo #js {:x 0 :animated true}))
                                (reset! selected-tab val))
                   :default-active
                   @selected-tab
                   :data
                   tabs-data}]]
                [tab-segments @selected-tab]]])]))])))
