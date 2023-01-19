(ns status-im2.contexts.communities.home.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [status-im.ui.components.react :as react]
            [status-im2.common.home.view :as common.home]
            [status-im2.common.scroll-page.view :as scroll-page]
            [status-im2.contexts.communities.menus.community-options.view :as options]
            [status-im2.contexts.communities.home.style :as style]
            [utils.re-frame :as rf]))

(defn home-community-segments
  [selected-tab]
  [rn/view
   {:style style/community-segments}
   [quo/tabs
    {:size           32
     :on-change      #(reset! selected-tab %)
     :default-active :joined
     :data           [{:id :joined :label (i18n/label :chats/joined) :accessibility-label :joined-tab}
                      {:id :pending :label (i18n/label :t/pending) :accessibility-label :pending-tab}
                      {:id :opened :label (i18n/label :t/opened) :accessibility-label :opened-tab}]}]])

(defn communities-list
  [communities-ids]
  [rn/view
   (map-indexed
    (fn [index id]
      (let [community (rf/sub [:communities/home-item id])]
        ^{:key index}
        [quo/communities-membership-list-item
         {:on-press      (fn []
                           (rf/dispatch [:communities/load-category-states id])
                           (rf/dispatch [:dismiss-keyboard])
                           (rf/dispatch [:navigate-to-nav2 :community {:community-id id}]))
          :on-long-press #(rf/dispatch
                           [:bottom-sheet/show-sheet
                            {:content       (fn []
                                              [options/community-options-bottom-sheet id])
                             :selected-item (fn []
                                              [quo/communities-membership-list-item {} community])}])}
         community]))
    communities-ids)])


(defn communities-list-component-fn
  [selected-tab]
  (let [ids-by-user-involvement (rf/sub [:communities/community-ids-by-user-involvement])
        tab                     @selected-tab]
    [rn/view
     {:style {:flex               1
              :padding-horizontal 20
              :padding-vertical   12}}
     (case tab
       :joined
       [communities-list (:joined ids-by-user-involvement)]

       :pending
       [communities-list (:pending ids-by-user-involvement)]

       :opened
       [communities-list (:opened ids-by-user-involvement)]

       [quo/information-box
        {:type :error
         :icon :i/info}
        (i18n/label :t/error)])]))

(def home-communities-lists (memoize communities-list-component-fn))

(defn communities-home-header
  [{:keys [selected-tab]}]
  [react/animated-view
   [common.home/top-nav {:type :default :hide-search true}]
   [common.home/title-column
    {:label               (i18n/label :t/communities)
     :handler             #(rf/dispatch [:bottom-sheet/show-sheet :add-new {}])
     :accessibility-label :new-chat-button}]
   [quo/discover-card
    {:on-press            #(rf/dispatch [:navigate-to :discover-communities])
     :title               (i18n/label :t/discover)
     :description         (i18n/label :t/whats-trending)
     :accessibility-label :communities-home-discover-card}]
   [home-community-segments selected-tab]])

(defn render-page-content
  [selected-tab]
  (fn []
    [home-communities-lists selected-tab]))

(defn render-sticky-header
  [selected-tab]
  (fn [scroll-height]
    (when (> scroll-height 90)
      [react/blur-view
       {:blur-amount   32
        :blur-type     :xlight
        :overlay-color (if platform/ios? colors/white-opa-70 :transparent)
        :style         style/blur-tabs-header}
       [home-community-segments selected-tab]])))

(defn communities-screen-content
  [selected-tab]
  (let [scroll-component (scroll-page/scroll-page
                          {:navigate-back?    false
                           :background-color  (colors/theme-colors
                                               colors/white
                                               colors/neutral-95)
                           :content-container (fn []
                                                [communities-home-header
                                                 {:selected-tab selected-tab}])
                           :name              (i18n/label :t/communities)})]
    (fn []
      (let [sticky-header  (memoize (render-sticky-header selected-tab))
            page-component (memoize (render-page-content selected-tab))]
        (fn []
          (scroll-component
           sticky-header
           page-component))))))

(defn home
  []
  (fn []
    (let [selected-tab (reagent/atom :joined)]
      [rn/view
       {:style (style/home-communities-container (colors/theme-colors
                                                  colors/white
                                                  colors/neutral-95))}
       [communities-screen-content selected-tab]])))
