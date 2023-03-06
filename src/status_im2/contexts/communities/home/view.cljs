(ns status-im2.contexts.communities.home.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [react-native.core :as rn]
            [status-im2.common.home.view :as common.home]
            [status-im2.common.scroll-page.view :as scroll-page]
            [status-im2.contexts.communities.menus.community-options.view :as options]
            [status-im2.contexts.communities.home.style :as style]
            [utils.re-frame :as rf]))

(defn community-segments
  [selected-tab padding-top]
  [rn/view
   {:style (style/community-segments padding-top)}
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
         {:on-press      #(rf/dispatch [:navigate-to-nav2 :community-overview id])
          :on-long-press #(rf/dispatch
                           [:bottom-sheet/show-sheet
                            {:content       (fn []
                                              [options/community-options-bottom-sheet id])
                             :selected-item (fn []
                                              [quo/communities-membership-list-item {} community])}])}
         community]))
    communities-ids)])


(defn render-communities-segments
  [selected-tab]
  (let [ids-by-user-involvement (rf/sub [:communities/community-ids-by-user-involvement])
        tab                     @selected-tab]
    [rn/view
     {:style style/render-segments-container}
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

(defn communities-header
  [selected-tab padding-top]
  [:<>
   [rn/view
    {:style style/communities-header-style}
    [quo/discover-card
     {:on-press            #(rf/dispatch [:navigate-to :discover-communities])
      :title               (i18n/label :t/discover)
      :description         (i18n/label :t/whats-trending)
      :accessibility-label :communities-home-discover-card}]]
   [community-segments selected-tab padding-top]])

(defn home-page-comunity-lists
  [{:keys [selected-tab padding-top]}]
  [rn/view {:style {:flex 1}}
   [communities-header selected-tab padding-top]
   [render-communities-segments selected-tab]])

(defn home-sticky-header
  [{:keys [selected-tab scroll-height padding-top]}]
  (when (> @scroll-height 80)
    [rn/view
     {:style style/blur-tabs-header}
     [community-segments selected-tab padding-top]]))

(defn home-nav
  []
  [common.home/top-nav
   {:type        :default
    :hide-search true
    :style       {:background-color :transparent}}])

(defn title-column
  []
  [common.home/title-column
   {:label               (i18n/label :t/communities)
    :handler             #(rf/dispatch [:bottom-sheet/show-sheet :add-new {}])
    :accessibility-label :new-chat-button}])

(defn communities-screen-content
  []
  (let [scroll-height (reagent/atom 0)
        selected-tab  (reagent/atom :joined)]
    (fn []
      [scroll-page/scroll-page
       {:name             (i18n/label :t/communities)
        :on-scroll        #(reset! scroll-height %)
        :top-nav          [home-nav]
        :title-colum      [title-column]
        :background-color (colors/theme-colors
                           colors/white
                           colors/neutral-95)
        :navigate-back?   :false
        :height           (if (> @scroll-height 80)
                            208
                            156)}
       [home-sticky-header
        {:selected-tab  selected-tab
         :scroll-height scroll-height
         :padding-top   8}]
       [home-page-comunity-lists
        {:selected-tab selected-tab
         :padding-top  16
         :height       60}]])))

(defn home
  []
  [rn/view
   {:style (style/home-communities-container (colors/theme-colors
                                              colors/white
                                              colors/neutral-95))}
   [communities-screen-content]])
