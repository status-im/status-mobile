(ns status-im2.common.home.banner.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [status-im2.common.home.banner.style :as style]
            [status-im2.common.home.view :as common.home]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.chat.actions.view :as home.sheet]
            [status-im2.contexts.communities.actions.home-plus.view :as actions.home-plus]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(def ^:private banner-content
  {:communities
   {:title-props
    {:label               (i18n/label :t/communities)
     :handler             #(rf/dispatch [:show-bottom-sheet {:content actions.home-plus/view}])
     :accessibility-label :new-communities-button}
    :card-props
    {:on-press            #(rf/dispatch [:navigate-to :discover-communities])
     :title               (i18n/label :t/discover)
     :description         (i18n/label :t/favorite-communities)
     :banner              (resources/get-image :discover)
     :accessibility-label :communities-home-discover-card}}
   :chats
   {:title-props
    {:label               (i18n/label :t/messages)
     :handler             #(rf/dispatch [:show-bottom-sheet {:content home.sheet/new-chat}])
     :accessibility-label :new-chat-button}
    :card-props
    {:banner      (resources/get-image :invite-friends)
     :title       (i18n/label :t/invite-friends-to-status)
     :description (i18n/label :t/share-invite-link)}}})

(defn- reset-banner-animation
  [scroll-shared-value]
  (reanimated/animate-shared-value-with-timing scroll-shared-value 0 200 :easing3))

(defn- reset-scroll
  [flat-list-ref]
  (some-> flat-list-ref
          (.scrollToOffset #js {:offset 0 :animated? true})))

(defn- banner-card-blur-layer
  [scroll-shared-value]
  (let [open-sheet? (-> (rf/sub [:bottom-sheet]) :sheets seq)]
    [reanimated/view {:style (style/banner-card-blur-layer scroll-shared-value)}
     [blur/view
      {:style         style/fill-space
       :blur-amount   (if platform/ios? 20 10)
       :blur-type     (theme/theme-value (if platform/ios? :light :xlight) :dark)
       :overlay-color (if open-sheet?
                        (colors/theme-colors colors/white colors/neutral-95-opa-70)
                        (theme/theme-value nil colors/neutral-95-opa-70))}]]))

(defn- banner-card-hiding-layer
  [{:keys [title-props card-props scroll-shared-value]}]
  (let [customization-color (rf/sub [:profile/customization-color])]
    [rn/view {:style (style/banner-card-hiding-layer)}
     [common.home/top-nav {:type :grey}]
     [common.home/title-column (assoc title-props :customization-color customization-color)]
     [rn/view {:style style/animated-banner-card-container}
      [reanimated/view {:style (style/animated-banner-card scroll-shared-value)}
       [quo/discover-card card-props]]]]))

(defn- banner-card-tabs-layer
  [{:keys [selected-tab tabs on-tab-change-event flat-list-ref scroll-shared-value]}]
  [reanimated/view {:style (style/banner-card-tabs-layer scroll-shared-value)}
   ^{:key (str "tabs-" selected-tab)}
   [quo/tabs
    {:style          style/banner-card-tabs
     :size           32
     :default-active selected-tab
     :data           tabs
     :on-change      (fn [tab]
                       (if (empty? (get (rf/sub [:communities/grouped-by-status]) tab))
                         (reset-banner-animation scroll-shared-value)
                         (reset-scroll @flat-list-ref))
                       (on-tab-change-event tab))}]])

(defn animated-banner
  [{:keys [flat-list-ref tabs selected-tab on-tab-change-event scroll-shared-value content]}]
  [:<>
   [:f> banner-card-blur-layer scroll-shared-value]
   [:f> banner-card-hiding-layer
    (assoc (banner-content content) :scroll-shared-value scroll-shared-value)]
   [:f> banner-card-tabs-layer
    {:scroll-shared-value scroll-shared-value
     :selected-tab        selected-tab
     :tabs                tabs
     :on-tab-change-event on-tab-change-event
     :flat-list-ref       flat-list-ref}]])

(defn set-scroll-shared-value
  [{:keys [shared-value scroll-input]}]
  (reanimated/set-shared-value shared-value scroll-input))
