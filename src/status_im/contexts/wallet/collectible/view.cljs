(ns status-im.contexts.wallet.collectible.view
  (:require
    [oops.core :as oops]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.collectible.options.view :as options-drawer]
    [status-im.contexts.wallet.collectible.style :as style]
    [status-im.contexts.wallet.collectible.tabs.view :as tabs]
    [status-im.contexts.wallet.collectible.utils :as utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.worklets.header-animations :as header-animations]))

(defn header
  [collectible-name collection-name collection-image-url set-title-ref]
  [rn/view
   {:style       style/header
    :ref         set-title-ref
    :collapsable false}
   [quo/text
    {:weight :semi-bold
     :size   :heading-1}
    collectible-name]
   [rn/view {:style style/collection-container}
    [rn/view {:style style/collection-avatar-container}
     [quo/collection-avatar {:image collection-image-url}]]
    [quo/text
     {:weight :semi-bold
      :size   :paragraph-1}
     collection-name]]])

(defn cta-buttons
  [{:keys [chain-id token-id contract-address collectible watch-only?]}]
  (let [theme         (quo.theme/use-theme)
        on-press-send (rn/use-callback
                       (fn []
                         (rf/dispatch [:wallet/clean-send-data])
                         (rf/dispatch
                          [:wallet/set-collectible-to-send
                           {:collectible    collectible
                            :start-flow?    true
                            :current-screen :screen/wallet.collectible}])))]
    [rn/view {:style style/buttons-container}
     (when-not watch-only?
       [quo/button
        {:container-style style/send-button
         :type            :outline
         :size            40
         :icon-left       :i/send
         :on-press        on-press-send}
        (i18n/label :t/send)])
     [quo/button
      {:container-style  style/opensea-button
       :type             :outline
       :size             40
       :on-press         (fn []
                           (rf/dispatch [:wallet/navigate-to-opensea chain-id token-id
                                         contract-address]))
       :icon-left        :i/opensea
       :icon-left-color  (colors/theme-colors colors/neutral-100 colors/neutral-40 theme)
       :icon-right       :i/external
       :icon-right-color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}
      (i18n/label :t/opensea)]]))

(def tabs-data
  [{:id                  :overview
    :label               (i18n/label :t/overview)
    :accessibility-label :overview-tab}
   {:id                  :about
    :label               (i18n/label :t/about)
    :accessibility-label :about-tab}])

(defn navigate-back-and-clear-collectible
  []
  (rf/dispatch [:navigate-back])
  (rf/dispatch [:wallet/clear-collectible-details]))

(defn animated-header
  [{:keys [scroll-amount title-opacity page-nav-type theme]}]
  (let [blur-amount               (header-animations/use-blur-amount scroll-amount)
        layer-opacity             (header-animations/use-layer-opacity
                                   scroll-amount
                                   (colors/theme-colors colors/white-opa-0
                                                        colors/neutral-95-opa-0
                                                        theme)
                                   (colors/theme-colors colors/white-opa-50
                                                        colors/neutral-95-opa-70-blur
                                                        theme))
        {{preview-uri :uri}  :preview-url
         {title :name}       :collectible-data
         {description :name} :collection-data
         id                  :id} (rf/sub [:wallet/collectible-details])]
    [rn/view {:style (style/animated-header)}
     [reanimated/blur-view
      {:style         {:flex             1
                       :background-color (when platform/android?
                                           (colors/theme-colors colors/white colors/neutral-80 theme))}
       :blur-type     :transparent
       :overlay-color :transparent
       :blur-amount   blur-amount
       :blur-radius   blur-amount}
      [reanimated/view {:style layer-opacity}
       [quo/page-nav
        {:type                page-nav-type
         :picture             preview-uri
         :title               title
         :description         description
         :background          :blur
         :icon-name           :i/close
         :accessibility-label :back-button
         :on-press            navigate-back-and-clear-collectible
         :right-side          [{:icon-name :i/options
                                :on-press  #(rf/dispatch
                                             [:show-bottom-sheet
                                              {:content (fn []
                                                          [options-drawer/view
                                                           {:name  title
                                                            :image preview-uri
                                                            :id    id}])
                                               :theme   theme}])}]
         :center-opacity      title-opacity}]]]]))

(defn on-scroll
  [e scroll-amount title-opacity title-bottom-coord]
  (let [scroll-y    (oops/oget e "nativeEvent.contentOffset.y")
        new-opacity (if (>= scroll-y @title-bottom-coord) 1 0)]
    (reanimated/set-shared-value scroll-amount scroll-y)
    (reanimated/set-shared-value title-opacity
                                 (reanimated/with-timing new-opacity #js {:duration 300}))))

(defn- gradient-layer
  [image-uri]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style style/gradient-layer}
     [rn/image
      {:style       style/image-background
       :source      {:uri image-uri}
       :blur-radius 14}]
     [rn/view {:style style/gradient}
      [linear-gradient/linear-gradient
       {:style     {:flex 1}
        :colors    (colors/theme-colors
                    [colors/white-opa-70 colors/white colors/white]
                    [colors/neutral-95-opa-70 colors/neutral-95 colors/neutral-95]
                    theme)
        :locations [0 0.9 1]}]]]))

(defn collectible-details
  [_]
  (let [selected-tab  (reagent/atom :overview)
        on-tab-change #(reset! selected-tab %)]
    (fn [{:keys [set-title-bottom theme]}]
      (let [title-ref                      (rn/use-ref-atom nil)
            set-title-ref                  (rn/use-callback #(reset! title-ref %))
            animation-shared-element-id    (rf/sub [:animation-shared-element-id])
            collectible                    (rf/sub [:wallet/collectible-details])
            collectible-owner              (rf/sub [:wallet/current-viewing-account])
            aspect-ratio                   (rf/sub [:wallet/collectible-aspect-ratio])
            gradient-color                 (rf/sub [:wallet/collectible-gradient-color])
            total-owned                    (:total-owned collectible)
            {:keys [id
                    preview-url
                    collection-data
                    collectible-data]}     collectible
            {svg?        :svg?
             preview-uri :uri
             :or         {preview-uri ""}} preview-url
            token-id                       (:token-id id)
            chain-id                       (get-in id [:contract-id :chain-id])
            contract-address               (get-in id [:contract-id :address])
            {collection-image :image-url
             collection-name  :name}       collection-data
            {collectible-name :name}       collectible-data
            collectible-image              {:image        preview-uri
                                            :image-width  300
                                            ; collectibles don't have width/height
                                            ; but we need to pass something
                                            ; without it animation doesn't work smoothly
                                            ; and :border-radius not  applied
                                            :image-height 300
                                            :id           token-id
                                            :header       collectible-name
                                            :description  collection-name}]
        [rn/view {:style style/container}
         [rn/view
          [gradient-layer preview-uri]
          [quo/expanded-collectible
           {:aspect-ratio         aspect-ratio
            :image-src            preview-uri
            :container-style      (style/preview-container)
            :gradient-color-index gradient-color
            :counter              (utils/collectible-owned-counter total-owned)
            :native-ID            (when (= animation-shared-element-id token-id) :shared-element)
            :supported-file?      (utils/supported-file? (:animation-media-type collectible-data))
            :on-press             (fn []
                                    (if svg?
                                      (js/alert "Can't visualize SVG images in lightbox")
                                      (rf/dispatch
                                       [:lightbox/navigate-to-lightbox
                                        token-id
                                        {:images           [collectible-image]
                                         :index            0
                                         :on-options-press #(rf/dispatch
                                                             [:show-bottom-sheet
                                                              {:content
                                                               (fn []
                                                                 [options-drawer/view
                                                                  {:name  collectible-name
                                                                   :image preview-uri
                                                                   :id    id}])}])}])))
            :on-collectible-load  (fn []
                                    ;; We need to delay the measurement because the
                                    ;; navigation has an animation
                                    (js/setTimeout
                                     #(some-> @title-ref
                                              (oops/ocall "measureInWindow" set-title-bottom))
                                     300))}]]
         [rn/view {:style (style/background-color theme)}
          [header collectible-name collection-name collection-image set-title-ref]
          [cta-buttons
           {:chain-id         chain-id
            :token-id         token-id
            :contract-address contract-address
            :watch-only?      (:watch-only? collectible-owner)
            :collectible      collectible}]
          [quo/tabs
           {:size           32
            :style          style/tabs
            :scrollable?    true
            :default-active @selected-tab
            :on-change      on-tab-change
            :data           tabs-data}]
          [tabs/view
           {:selected-tab @selected-tab
            :collectible  collectible}]]]))))

(defn- get-title-bottom-y-position
  [y-element-position element-height]
  (let [{:keys [top]} (safe-area/get-insets)
        title-height  -56]
    (+ y-element-position
       element-height
       title-height
       (when platform/ios? (* top -2)))))

(defn view
  [_]
  (let [{:keys [top]}      (safe-area/get-insets)
        theme              (quo.theme/use-theme)
        title-bottom-coord (rn/use-ref-atom 0)
        set-title-bottom   (rn/use-callback
                            (fn [_ y _ height]
                              (reset! title-bottom-coord
                                (get-title-bottom-y-position y height))))
        scroll-amount      (reanimated/use-shared-value 0)
        title-opacity      (reanimated/use-shared-value 0)]
    [rn/view {:style (style/background-color theme)}
     [animated-header
      {:scroll-amount scroll-amount
       :title-opacity title-opacity
       :page-nav-type :title-description
       :theme         theme}]
     [reanimated/scroll-view
      {:style     (style/scroll-view top)
       :on-scroll #(on-scroll % scroll-amount title-opacity title-bottom-coord)}
      [collectible-details
       {:set-title-bottom set-title-bottom
        :theme            theme}]]]))
