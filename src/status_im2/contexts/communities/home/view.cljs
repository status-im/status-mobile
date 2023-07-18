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
  (let [unviewed-counts (rf/sub [:communities/unviewed-counts id])
        item            (merge item unviewed-counts)]
    [quo/communities-membership-list-item
     {:style         {:padding-horizontal 18}
      :on-press      #(debounce/dispatch-and-chill [:navigate-to :community-overview id] 500)
      :on-long-press #(rf/dispatch
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

(defn- empty-state
  [{:keys [style selected-tab]}]
  (let [{:keys [image title description]} (empty-state-content selected-tab)
        customization-color               (rf/sub [:profile/customization-color])]
    [rn/view {:style style}
     [quo/empty-state
      {:customization-color customization-color
       :image               image
       :title               title
       :description         description}]]))

(defn- blur-banner-layer
  [animated-translation-y]
  (let [open-sheet? (-> (rf/sub [:bottom-sheet]) :sheets seq)]
    [reanimated/view {:style (style/blur-banner-layer animated-translation-y)}
     [blur/view
      {:blur-amount   (if platform/ios? 20 10)
       :blur-type     (theme/theme-value (if platform/ios? :light :xlight) :dark)
       :style         style/blur
       :overlay-color (if open-sheet?
                        (colors/theme-colors colors/white colors/neutral-95-opa-70)
                        (theme/theme-value nil colors/neutral-95-opa-70))}]]))

(defn- hiding-banner-layer
  [animated-translation-y animated-opacity]
  (let [customization-color (rf/sub [:profile/customization-color])]
    [rn/view {:style (style/hiding-banner-layer)}
     [common.home/top-nav {:type :grey}]
     [common.home/title-column
      {:label               (i18n/label :t/communities)
       :handler             #(rf/dispatch
                              [:show-bottom-sheet {:content actions.home-plus/view}])
       :accessibility-label :new-communities-button
       :customization-color customization-color}]
     [rn/view {:style style/animated-card-container}
      [reanimated/view {:style (style/animated-card animated-opacity animated-translation-y)}
       [quo/discover-card
        {:on-press            #(rf/dispatch [:navigate-to :discover-communities])
         :title               (i18n/label :t/discover)
         :description         (i18n/label :t/favorite-communities)
         :banner              (resources/get-image :discover)
         :accessibility-label :communities-home-discover-card}]]]]))

(defn- tabs-banner-layer
  [animated-translation-y animated-opacity selected-tab flat-list-ref]
  (let [on-tab-change (fn [tab]
                        (if (empty? (get (rf/sub [:communities/grouped-by-status]) tab))
                          (do
                            (reanimated/animate-shared-value-with-timing animated-opacity 1 200 :easing3)
                            (reanimated/animate-shared-value-with-timing animated-translation-y
                                                                         0
                                                                         200
                                                                         :easing3))
                          (some-> @flat-list-ref
                                  (.scrollToOffset #js {:offset 0 :animated? true})))
                        (rf/dispatch [:communities/select-tab tab]))]
    [reanimated/view {:style (style/tabs-banner-layer animated-translation-y)}
     ^{:key (str "tabs-" selected-tab)}
     [quo/tabs
      {:size           32
       :style          style/tabs
       :on-change      on-tab-change
       :default-active selected-tab
       :data           tabs-data}]]))

(defn- animated-banner
  [{:keys [selected-tab animated-translation-y animated-opacity flat-list-ref]}]
  [:<>
   [:f> blur-banner-layer animated-translation-y]
   [:f> hiding-banner-layer animated-translation-y animated-opacity]
   [:f> tabs-banner-layer animated-translation-y animated-opacity selected-tab flat-list-ref]])

(def ^:private card-height (+ 56 16)) ; Card height + its vertical margins
(def ^:private card-opacity-factor (/ 100 card-height 100))
(def ^:private max-scroll (- (+ card-height 8))) ; added 8 from tabs top padding

(defn- set-animated-banner-values
  [{:keys [scroll-offset translation-y opacity]}]
  (let [new-opacity       (-> (* (- card-height scroll-offset) card-opacity-factor)
                              (utils.number/value-in-range 0 1))
        new-translation-y (-> (- scroll-offset)
                              (utils.number/value-in-range max-scroll 0))]
    (reanimated/set-shared-value opacity new-opacity)
    (reanimated/set-shared-value translation-y new-translation-y)))

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
           [empty-state
            {:style        (style/empty-state-container)
             :selected-tab selected-tab}]
           [rn/flat-list
            {:ref                               #(reset! flat-list-ref %)
             :key-fn                            :id
             :content-inset-adjustment-behavior :never
             :header                            [rn/view {:style (style/header-spacing)}]
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
