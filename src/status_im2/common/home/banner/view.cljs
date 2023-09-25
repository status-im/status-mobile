(ns status-im2.common.home.banner.view
  (:require [oops.core :as oops]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [reagent.core :as reagent]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [status-im2.common.home.banner.style :as style]
            [status-im2.common.home.title-column.view :as title-column]
            [status-im2.common.home.top-nav.view :as top-nav]
            [utils.re-frame :as rf]))

(def card-banner-overflow-threshold 3)
(def card-banner-overflow (reagent/atom :visible))

(defn- reset-banner-animation
  [scroll-shared-value]
  (reanimated/animate-shared-value-with-timing scroll-shared-value 0 200 :easing3))

(defn- reset-scroll
  [scroll-ref]
  (cond
    (.-scrollToLocation scroll-ref)
    (oops/ocall! scroll-ref "scrollToLocation" #js {:itemIndex 0 :sectionIndex 0 :viewOffset 0})
    (.-scrollToOffset scroll-ref)
    (oops/ocall! scroll-ref "scrollToOffset" #js {:offset 0})))

(defn- banner-card-blur-layer
  [scroll-shared-value child]
  (let [open-sheet? (-> (rf/sub [:bottom-sheet]) :sheets seq)]
    [reanimated/view {:style (style/banner-card-blur-layer scroll-shared-value)}
     [blur/view
      {:style         style/fill-space
       :blur-amount   (if platform/ios? 20 10)
       :blur-type     (theme/theme-value (if platform/ios? :light :xlight) :dark)
       :overlay-color (if open-sheet?
                        (colors/theme-colors colors/white colors/neutral-95-opa-70)
                        (theme/theme-value nil colors/neutral-95-opa-70))}
      child]]))

(defn- banner-card-hiding-layer
  [{:keys [title-props card-props scroll-shared-value]}]
  (let [customization-color (rf/sub [:profile/customization-color])]
    [reanimated/view {:style (style/banner-card-hiding-layer scroll-shared-value)}
     [top-nav/view]
     [title-column/view (assoc title-props :customization-color customization-color)]
     [rn/view {:style {:overflow @card-banner-overflow}}
      [reanimated/view {:style (style/animated-banner-card scroll-shared-value)}
       [quo/discover-card card-props]]]]))

(defn- banner-card-tabs-layer
  [{:keys [selected-tab tabs on-tab-change scroll-ref scroll-shared-value customization-color]}]
  [reanimated/view {:style (style/banner-card-tabs-layer scroll-shared-value)}
   ^{:key (str "tabs-" selected-tab)}
   [quo/tabs
    {:style               style/banner-card-tabs
     :customization-color customization-color
     :size                32
     :default-active      selected-tab
     :data                tabs
     :on-change           (fn [tab]
                            (reset-banner-animation scroll-shared-value)
                            (some-> scroll-ref
                                    deref
                                    reset-scroll)
                            (on-tab-change tab))}]])

(defn animated-banner
  [{:keys [scroll-ref tabs selected-tab on-tab-change scroll-shared-value content customization-color]}]
  [:<>
   [:f> banner-card-blur-layer scroll-shared-value
    [:f> banner-card-hiding-layer (assoc content :scroll-shared-value scroll-shared-value)]]
   [:f> banner-card-tabs-layer
    {:scroll-shared-value scroll-shared-value
     :selected-tab        selected-tab
     :tabs                tabs
     :on-tab-change       on-tab-change
     :scroll-ref          scroll-ref
     :customization-color customization-color}]])

(defn set-scroll-shared-value
  [{:keys [shared-value scroll-input]}]
  (reanimated/set-shared-value shared-value scroll-input)
  (let [new-overflow (if (<= scroll-input card-banner-overflow-threshold) :visible :hidden)]
    (when-not (= new-overflow @card-banner-overflow)
      (reset! card-banner-overflow new-overflow))))
