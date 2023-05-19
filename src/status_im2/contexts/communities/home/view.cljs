(ns status-im2.contexts.communities.home.view
  (:require [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [react-native.blur :as blur]
            [react-native.platform :as platform]
            [status-im2.common.home.view :as common.home]
            [status-im2.contexts.communities.actions.community-options.view :as options]
            [status-im2.contexts.communities.home.style :as style]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.communities.actions.home-plus.view :as actions.home-plus]
            [status-im.ui.components.webview :as webview]))

(defn item-render
  [{:keys [id] :as item}]
  (let [unviewed-counts (rf/sub [:communities/unviewed-counts id])
        item            (merge item unviewed-counts)]
    [quo/communities-membership-list-item
     {:style         {:padding-horizontal 18}
      :on-press      #(rf/dispatch [:navigate-to :community-overview id])
      :on-long-press #(rf/dispatch
                       [:show-bottom-sheet
                        {:content       (fn []
                                          [options/community-options-bottom-sheet id])
                         :selected-item (fn []
                                          [quo/communities-membership-list-item {} item])}])}
     item]))

(def tabs-data
  [{:id :joined :label (i18n/label :chats/joined) :accessibility-label :joined-tab}
   {:id :pending :label (i18n/label :t/pending) :accessibility-label :pending-tab}
   {:id :opened :label (i18n/label :t/opened) :accessibility-label :opened-tab}])

(defn empty-state
  []
  [rn/view {:style style/empty-state-container}
   [rn/view {:style style/empty-state-placeholder}]
   [quo/text
    {:accessibility-label :communities-rule-index
     :weight              :semi-bold
     :size                :paragraph-1}
    (i18n/label :t/no-communities)]
   [quo/text
    {:accessibility-label :communities-rule-index
     :weight              :regular
     :size                :paragraph-2}
    (i18n/label :t/no-communities-sub-title)]])

(defn home
  []
  (fn []
    (let [selected-tab                    (or (rf/sub [:communities/selected-tab]) :joined)
          {:keys [joined pending opened]} (rf/sub [:communities/grouped-by-status])
          {:keys [key-uid]}               (rf/sub [:multiaccount])
          profile-color                   (:color (rf/sub [:onboarding-2/profile]))
          customization-color             (if profile-color
                                            profile-color
                                            (rf/sub [:profile/customization-color key-uid]))
          selected-items                  (case selected-tab
                                            :joined  joined
                                            :pending pending
                                            :opened  opened)
          top                             (safe-area/get-top)]
      [:<>
       (if (empty? selected-items)
         [empty-state]
         [rn/flat-list
          {:key-fn                            :id
           :content-inset-adjustment-behavior :never
           :header                            [rn/view (style/header-height top)]
           :render-fn                         item-render
           :data                              selected-items}])
       [rn/view
        {:style (style/blur-container top)}
        [blur/webview-blur
         {:style style/blur
          :blur-radius (if platform/ios? 20 10)}]
        ;[blur/view
        ; {:blur-amount (if platform/ios? 20 10)
        ;  :blur-type   (if (colors/dark?) :dark (if platform/ios? :light :xlight))
        ;  :style       style/blur}]
        [common.home/top-nav
         {:type   :grey
          :avatar {:customization-color customization-color}}]
        [common.home/title-column
         {:label               (i18n/label :t/communities)
          :handler             #(rf/dispatch [:show-bottom-sheet {:content actions.home-plus/view}])
          :accessibility-label :new-communities-button
          :customization-color customization-color}]
        [quo/discover-card
         {:on-press            #(rf/dispatch [:navigate-to :discover-communities])
          :title               (i18n/label :t/discover)
          :description         (i18n/label :t/favorite-communities)
          :banner              (resources/get-image :discover)
          :accessibility-label :communities-home-discover-card}]
        ^{:key (str "tabs-" selected-tab)}
        [quo/tabs
         {:size           32
          :style          style/tabs
          :on-change      (fn [tab]
                            (rf/dispatch [:communities/select-tab tab]))
          :default-active selected-tab
          :data           tabs-data}]]])))
