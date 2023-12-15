(ns status-im2.contexts.wallet.collectible.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im2.common.scroll-page.view :as scroll-page]
    [status-im2.contexts.wallet.collectible.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn header
  [collectible-name description collection-image-url]
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
     description]]])

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

(defn tabs
  []
  [quo/tabs
   {:size        32
    :style       style/tabs
    :scrollable? true
    :data        [{:id                  :overview
                   :label               (i18n/label :t/overview)
                   :accessibility-label :overview-tab}
                  {:id                  :activity
                   :label               (i18n/label :t/activity)
                   :accessibility-label :activity-tab}
                  {:id                  :permissions
                   :label               (i18n/label :t/permissions)
                   :accessibility-label :permissions-tab}
                  {:id                  :about
                   :label               (i18n/label :t/about)
                   :accessibility-label :about-tab}]}])

(defn traits-section
  [traits]
  (when (pos? (count traits))
    [rn/view
     [quo/section-label
      {:section         (i18n/label :t/traits)
       :container-style style/traits-title-container}]
     [rn/flat-list
      {:render-fn               (fn [{:keys [trait-type value]}]
                                  [quo/data-item
                                   {:card?           true
                                    :status          :default
                                    :size            :default
                                    :title           trait-type
                                    :subtitle        value
                                    :subtitle-type   :default
                                    :container-style style/traits-item}])
       :data                    traits
       :key                     :collectibles-list
       :key-fn                  :id
       :num-columns             2
       :content-container-style style/traits-container}]]))

(defn info
  [chain-id]
  (let [network         (rf/sub [:wallet/network-details-by-chain-id
                                 chain-id])
        network-keyword (get network :network-name)
        network-name    (when network-keyword
                          (string/capitalize (name network-keyword)))]
    [rn/view
     {:style style/info-container}
     [rn/view {:style style/account}
      [quo/data-item
       {:card?               true
        :status              :default
        :size                :default
        :title               (i18n/label :t/account-title)
        :subtitle            "Collectibles vault"
        :subtitle-type       :account
        :emoji               "🎮"
        :customization-color :yellow}]]

     [rn/view {:style style/network}
      [quo/data-item
       {:card?         true
        :status        :default
        :size          :default
        :title         (i18n/label :t/network)
        :network-image (quo.resources/get-network network-keyword)
        :subtitle      network-name
        :subtitle-type :network}]]]))

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

(defn view-internal
  [{:keys [theme] :as _props}]
  (let [collectible                     (rf/sub [:wallet/last-collectible-details])
        {:keys [collectible-data preview-url
                collection-data]}       collectible
        {traits           :traits
         collectible-name :name
         description      :description} collectible-data
        chain-id                        (rf/sub [:wallet/last-collectible-chain-id])]
    [scroll-page/scroll-page
     {:navigate-back? true
      :height         148
      :page-nav-props {:type        :title-description
                       :title       collectible-name
                       :description description
                       :right-side  [{:icon-name :i/options
                                      :on-press  #(rf/dispatch
                                                   [:show-bottom-sheet
                                                    {:content collectible-actions-sheet
                                                     :theme   theme}])}]
                       :picture     preview-url}}
     [rn/view {:style style/container}
      [rn/view {:style style/preview-container}
       [rn/image
        {:source preview-url
         :style  style/preview}]]
      [header collectible-name description (:image-url collection-data)]
      [cta-buttons]
      [tabs]
      [info chain-id]
      [traits-section traits]]]))

(def view (quo.theme/with-theme view-internal))
