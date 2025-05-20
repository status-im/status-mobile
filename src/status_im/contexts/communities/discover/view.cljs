(ns status-im.contexts.communities.discover.view
  (:require
    [quo.context :as quo.context]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.reanimated :as reanimated]
    [status-im.common.events-helper :as events-helper]
    [status-im.common.resources :as resources]
    [status-im.contexts.communities.discover.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.worklets.communities :as worklets]))

(defn- page-nav-blur
  [{:keys [scroll-offset theme]}]
  (let [blur-start       (reanimated/use-shared-value 8)
        blur-end         (reanimated/use-shared-value 24)
        blur-nav-opacity (worklets/interpolate-value
                          {:shared-value scroll-offset
                           :input-range  [blur-start blur-end]
                           :output-range [0 1]})]
    [reanimated/view {:style (style/page-nav-blur-container blur-nav-opacity)}
     [quo/blur
      {:style         rn/stylesheet-absolute-fill
       :blur-amount   20
       :blur-type     :transparent
       :overlay-color (style/page-nav-blur-color theme)
       :blur-radius   20}]
     [rn/view {:style style/page-nav-blur}
      [quo/page-nav
       {:type       :no-title
        :background :blur
        :on-press   events-helper/navigate-back
        :icon-name  :i/close}]]]))

(defn- vote-button
  []
  [rn/view {:style style/vote-button}
   [quo/button
    {:icon-right :i/external
     :type       :primary
     :on-press   (fn external-link-to-vote [])
     :size       32}
    (i18n/label :t/vote-for-communities-action)]])

(defn- base-page-nav
  [{:keys [scroll-offset theme]}]
  (let [base-start       (reanimated/use-shared-value 24)
        base-end         (reanimated/use-shared-value 48)
        base-nav-opacity (worklets/interpolate-value
                          {:shared-value scroll-offset
                           :input-range  [base-start base-end]
                           :output-range [1 0]})]
    [reanimated/view {:style [style/page-nav {:opacity base-nav-opacity}]}
     [quo/page-nav
      {:type       :no-title
       :background (if (= theme :light) :white :neutral-95)
       :on-press   events-helper/navigate-back
       :icon-name  :i/close}]]))

(defn- dynamic-page-nav
  [{:keys [theme scroll-offset]}]
  [rn/view {:style style/dynamic-page-nav}
   [page-nav-blur
    {:theme         theme
     :scroll-offset scroll-offset}]
   [vote-button]
   [base-page-nav
    {:theme         theme
     :scroll-offset scroll-offset}]])

(defn- list-header
  []
  (let [theme                      (quo.context/use-theme)
        [dismissed? set-dismissed] (rn/use-state false)]
    [:<>
     (when-not dismissed?
       [rn/view {:style style/header-info-box}
        [quo/information-box
         {:type     :informative
          :closed?  false
          :on-close #(set-dismissed true)
          :theme    theme}
         (i18n/label :t/vote-for-communities)]])
     [quo/page-top {:title (i18n/label :t/discover-communities)}]]))

(defn- community-card
  [community]
  (let [cover-uri      {:uri (-> community :images :banner :uri)}
        members-count  (count (:members community))
        open-community (rn/use-callback
                        #(rf/dispatch [:communities/navigate-to-community-overview (:id community)]))]
    [rn/view {:style style/community-card}
     [quo/community-card-view-item
      {:community (assoc community
                         :cover         cover-uri
                         :members-count members-count)
       :on-press  open-community}]]))

(defn- fetching-message
  [{:keys [theme]}]
  [rn/view {:style style/fetching-message}
   [rn/view {:style style/fetching-top-container}
    [rn/image
     {:style  style/fetching-image
      :source (resources/get-themed-image :no-communities theme)}]
    [rn/view {:style style/fetching-text-container}
     [quo/text
      {:size   :paragraph-1
       :weight :semi-bold
       :style  style/fetching-text}
      (i18n/label :t/fetching-communities-title)]
     [quo/text {:style style/fetching-text}
      (i18n/label :t/fetching-communities-description)]]]
   [quo/status-tag
    {:status          {:type :pending}
     :container-style style/fetching-tag
     :icon            :i/loading
     :icon-color      colors/neutral-40
     :label           (i18n/label :t/fetching)}]])

(defn- loading-cards
  []
  (let [theme (quo.context/use-theme)]
    [rn/view {:style style/loading-cards}
     [quo/community-card-view-item {:loading? true}]
     [quo/community-card-view-item {:loading? true}]
     [quo/community-card-view-item {:loading? true}]
     [linear-gradient/linear-gradient
      {:style  style/loading-cards-gradient
       :colors [(colors/theme-colors colors/white-opa-50 colors/neutral-95-opa-50 theme)
                (colors/theme-colors colors/white colors/neutral-95 theme)]}]
     [fetching-message {:theme theme}]]))

(defn view
  []
  (let [theme                   (quo.context/use-theme)
        animated-ref            (reanimated/use-animated-ref)
        scroll-offset           (reanimated/use-scroll-view-offset animated-ref)
        featured-communities    (rf/sub [:communities/featured-contract-communities])
        other-communities       (rf/sub [:communities/other-contract-communities])
        communities-render-data (concat featured-communities other-communities)]
    (rn/use-mount #(rf/dispatch [:fetch-contract-communities]))
    [rn/view {:style (style/container theme)}
     [dynamic-page-nav
      {:theme         theme
       :scroll-offset scroll-offset}]
     [reanimated/flat-list
      {:ref                             animated-ref
       :header                          [list-header]
       :content-container-style         style/community-listing-content
       :data                            communities-render-data
       :render-fn                       community-card
       :scroll-enabled                  (boolean (seq communities-render-data))
       :empty-component                 [loading-cards]
       :shows-vertical-scroll-indicator false}]]))
