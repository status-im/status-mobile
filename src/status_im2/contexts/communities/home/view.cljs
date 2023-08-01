(ns status-im2.contexts.communities.home.view
  (:require [oops.core :as oops]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [status-im2.common.home.view :as common.home]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.communities.actions.community-options.view :as options]
            [status-im2.contexts.communities.actions.home-plus.view :as actions.home-plus]
            [status-im2.contexts.communities.home.style :as style]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.number]
            [utils.re-frame :as rf]))

(defn item-render
  [{:keys [id] :as item}]
  (let [unviewed-counts     (rf/sub [:communities/unviewed-counts id])
        customization-color (rf/sub [:profile/customization-color])
        item                (merge item unviewed-counts)]
    [quo/communities-membership-list-item
     {:customization-color customization-color
      :style               {:padding-horizontal 18}
      :on-press            #(debounce/dispatch-and-chill [:navigate-to :community-overview id] 500)
      :on-long-press       #(rf/dispatch
                             [:show-bottom-sheet
                              {:content       (fn []
                                                [options/community-options-bottom-sheet id])
                               :selected-item (fn []
                                                [quo/communities-membership-list-item {} true item])}])}
     false
     item]))

(def tabs-data
  [{:id :joined :label (i18n/label :chats/joined) :accessibility-label :joined-tab}
   {:id :pending :label (i18n/label :t/pending) :accessibility-label :pending-tab}
   {:id :opened :label (i18n/label :t/opened) :accessibility-label :opened-tab}])

(def empty-state-content
  {:joined
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
                                                         :no-opened-communities-dark))}})

(defn home
  []
  (let [flat-list-ref (atom nil)]
    (fn []
      (let [selected-tab                    (or (rf/sub [:communities/selected-tab]) :joined)
            {:keys [joined pending opened]} (rf/sub [:communities/grouped-by-status])
            selected-items                  (case selected-tab
                                              :joined  joined
                                              :pending pending
                                              :opened  opened)
            animated-opacity                (reanimated/use-shared-value 1)
            animated-translation-y          (reanimated/use-shared-value 0)]
        [:<>
         (if (empty? selected-items)
           [common.home/empty-state-image
            {:selected-tab selected-tab
             :tab->content empty-state-content}]
           [rn/flat-list
            {:ref                               #(reset! flat-list-ref %)
             :key-fn                            :id
             :content-inset-adjustment-behavior :never
             :header                            [common.home/header-spacing]
             :render-fn                         item-render
             :data                              selected-items
             :on-scroll                         #(set-animated-banner-values
                                                  {:scroll-offset (oops/oget
                                                                   %
                                                                   "nativeEvent.contentOffset.y")
                                                   :translation-y animated-translation-y
                                                   :opacity       animated-opacity})}])
         [:f> animated-banner
          {:selected-tab           selected-tab
           :animated-translation-y animated-translation-y
           :animated-opacity       animated-opacity
           :flat-list-ref          flat-list-ref}]]))))
