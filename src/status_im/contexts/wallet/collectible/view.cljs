(ns status-im.contexts.wallet.collectible.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.svg :as svg]
    [reagent.core :as reagent]
    [status-im.common.scroll-page.view :as scroll-page]
    [status-im.contexts.wallet.collectible.style :as style]
    [status-im.contexts.wallet.collectible.tabs.view :as tabs]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.url :as url]))

(defn header
  [collectible-name collection-name collection-image-url]
  [rn/view {:style style/header}
   [quo/text
    {:weight :semi-bold
     :size   :heading-1} collectible-name]
   [rn/view {:style style/collection-container}
    [rn/view {:style style/collection-avatar-container}
     [quo/collection-avatar {:image collection-image-url}]]
    [quo/text
     {:weight :semi-bold
      :size   :paragraph-1}
     collection-name]]])

(defn cta-buttons
  []
  [rn/view {:style style/buttons-container}
   [quo/button
    {:container-style style/send-button
     :type            :outline
     :size            40
     :icon-left       :i/send}
    (i18n/label :t/send)]
   [quo/button
    {:container-style style/opensea-button
     :type            :outline
     :size            40
     :icon-left       :i/opensea}
    (i18n/label :t/opensea)]])

(def tabs-data
  [{:id                  :overview
    :label               (i18n/label :t/overview)
    :accessibility-label :overview-tab}
   {:id                  :activity
    :label               (i18n/label :t/activity)
    :accessibility-label :activity-tab}
   {:id                  :about
    :label               (i18n/label :t/about)
    :accessibility-label :about-tab}])

(defn collectible-actions-sheet
  []
  [quo/action-drawer
   [[{:icon                :i/messages
      :accessibility-label :share-opensea-link
      :label               (i18n/label :t/share-opensea-link)}
     {:icon                :i/link
      :accessibility-label :view-on-eth
      :label               (i18n/label :t/view-on-eth)}
     {:icon                :i/download
      :accessibility-label :save-image-to-photos
      :label               (i18n/label :t/save-image-to-photos)}
     {:icon                :i/copy
      :accessibility-label :copy-all-details
      :label               (i18n/label :t/copy-all-details)}
     {:icon                :i/share
      :accessibility-label :share-details
      :label               (i18n/label :t/share-details)}]]])

(defn options-drawer
  [images index]
  (let [{:keys [image]} (nth images index)
        uri             (url/replace-port image (rf/sub [:mediaserver/port]))]
    [quo/action-drawer
     [[{:icon                :i/link
        :accessibility-label :view-on-etherscan
        :label               (i18n/label :t/view-on-eth)}]
      [{:icon                :i/save
        :accessibility-label :save-image
        :label               (i18n/label :t/save-image-to-photos)
        :on-press            (fn []
                               (rf/dispatch [:hide-bottom-sheet])
                               (rf/dispatch
                                [:lightbox/save-image-to-gallery
                                 uri
                                 #(rf/dispatch [:toasts/upsert
                                                {:id              :random-id
                                                 :type            :positive
                                                 :container-style {:bottom (when platform/android? 20)}
                                                 :text            (i18n/label :t/photo-saved)}])]))}]
      [{:icon                :i/share
        :accessibility-label :share-collectible
        :label               (i18n/label :t/share-collectible)
        :right-icon          :i/external}]]]))

(defn f-view-internal
  [{:keys [theme] :as _props}]
  (let [selected-tab  (reagent/atom :overview)
        on-tab-change #(reset! selected-tab %)]
    (fn []
      (let [collectible                 (rf/sub [:wallet/last-collectible-details])
            animation-shared-element-id (rf/sub [:animation-shared-element-id])
            {:keys [id
                    preview-url
                    collection-data
                    collectible-data]}  collectible
            {svg?        :svg?
             preview-uri :uri}          preview-url
            token-id                    (:token-id id)
            {collection-image :image-url
             collection-name  :name}    collection-data
            {collectible-name :name}    collectible-data]
        (rn/use-unmount #(rf/dispatch [:wallet/clear-last-collectible-details]))
        [scroll-page/scroll-page
         {:navigate-back? true
          :height         148
          :page-nav-props {:type        :title-description
                           :title       collectible-name
                           :description collection-name
                           :right-side  [{:icon-name :i/options
                                          :on-press  #(rf/dispatch
                                                       [:show-bottom-sheet
                                                        {:content collectible-actions-sheet
                                                         :theme   theme}])}]
                           :picture     preview-uri}}
         [rn/view {:style style/container}
          [rn/view {:style style/preview-container}
           [rn/touchable-opacity
            {:active-opacity 1
             :on-press       (fn []
                               (if svg?
                                 (js/alert "Can't visualize SVG images in lightbox")
                                 (rf/dispatch
                                  [:lightbox/navigate-to-lightbox
                                   token-id
                                   {:images           [{:image        preview-uri
                                                        :image-width  300 ; collectibles don't have
                                                                          ; width/height but we need
                                                                          ; to pass something
                                                        :image-height 300 ; without it animation
                                                                          ; doesn't work smoothly
                                                                          ; and :border-radius not
                                                                          ; applied
                                                        :id           token-id
                                                        :header       collectible-name
                                                        :description  collection-name}]
                                    :index            0
                                    :on-options-press (fn [images index]
                                                        (rf/dispatch [:show-bottom-sheet
                                                                      {:content (fn []
                                                                                  [options-drawer
                                                                                   images
                                                                                   index])}]))}])))}
            (if svg?
              [rn/view
               {:style     (assoc style/preview :overflow :hidden)
                :native-ID (when (= animation-shared-element-id token-id)
                             :shared-element)}
               [svg/svg-uri (assoc style/preview :uri preview-uri)]]
              [rn/image
               {:source    preview-uri
                :style     style/preview
                :native-ID (when (= animation-shared-element-id token-id) :shared-element)}])]]
          [header collectible-name collection-name collection-image]
          [cta-buttons]
          [quo/tabs
           {:size           32
            :style          style/tabs
            :scrollable?    true
            :default-active @selected-tab
            :on-change      on-tab-change
            :data           tabs-data}]
          [tabs/view {:selected-tab @selected-tab}]]]))))

(defn- view-internal
  [props]
  [:f> f-view-internal props])

(def view (quo.theme/with-theme view-internal))
