(ns status-im2.contexts.communities.home.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.safe-area :as safe-area]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im2.common.home.view :as common.home]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.communities.actions.community-options.view :as options]
            [status-im2.contexts.communities.actions.home-plus.view :as actions.home-plus]
            [status-im2.contexts.communities.home.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

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

(defn empty-state-content
  [selected-tab]
  (case selected-tab
    :joined
    {:title       (i18n/label :t/no-communities)
     :description [:<>
                   [rn/text {:style {:text-decoration-line :line-through}}
                    (i18n/label :t/no-communities-description-strikethrough)]
                   " "
                   (i18n/label :t/no-communities-description)]
     :image       (resources/get-image (theme/theme-value :no-communities-light
                                                          :no-communities-dark))}
    :pending
    {:title       (i18n/label :t/no-pending-communities)
     :description (i18n/label :t/no-pending-communities-description)
     :image       (resources/get-image (theme/theme-value :no-pending-communities-light
                                                          :no-pending-communities-dark))}
    :opened
    {:title       (i18n/label :t/no-opened-communities)
     :description (i18n/label :t/no-opened-communities-description)
     :image       (resources/get-image (theme/theme-value :no-opened-communities-light
                                                          :no-opened-communities-dark))}
    nil))

(defn empty-state
  [{:keys [style selected-tab customization-color]}]
  (let [{:keys [image title description]} (empty-state-content selected-tab)]
    [rn/view {:style style}
     [quo/empty-state
      {:customization-color customization-color
       :image               image
       :title               title
       :description         description}]]))

(defn home
  []
  (let [selected-tab                    (or (rf/sub [:communities/selected-tab]) :joined)
        {:keys [joined pending opened]} (rf/sub [:communities/grouped-by-status])
        {:keys [key-uid]}               (rf/sub [:multiaccount])
        account                         (rf/sub [:profile/multiaccount])
        customization-color             (or (:color (rf/sub [:onboarding-2/profile]))
                                            (rf/sub [:profile/customization-color key-uid]))
        selected-items                  (case selected-tab
                                          :joined joined
                                          :pending pending
                                          :opened opened)
        top                             (safe-area/get-top)]
    [:<>
     (if (empty? selected-items)
       [empty-state
        {:style               (style/empty-state-container top)
         :selected-tab        selected-tab
         :customization-color customization-color}]
       [rn/flat-list
        {:key-fn                            :id
         :content-inset-adjustment-behavior :never
         :header                            [rn/view {:style (style/header-spacing top)}]
         :render-fn                         item-render
         :data                              selected-items}])

     [rn/view {:style (style/blur-container top)}
      [blur/view
       {:blur-amount (if platform/ios? 20 10)
        :blur-type   (if (colors/dark?) :dark (if platform/ios? :light :xlight))
        :style       style/blur}]
      [common.home/top-nav
       {:type   :grey
        :avatar {:customization-color customization-color
                 :full-name           (multiaccounts/displayed-name account)
                 :profile-picture     (multiaccounts/displayed-photo account)}}]
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
        :data           tabs-data}]]]))
