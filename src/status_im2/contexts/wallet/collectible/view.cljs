(ns status-im2.contexts.wallet.collectible.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [react-native.core :as rn]
    [status-im2.common.scroll-page.view :as scroll-page]
    [status-im2.contexts.wallet.collectible.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn header
  [{:keys [name description] :as _collectible-details} collection-image-url]
  [rn/view {:style style/header}
   [quo/text
    {:weight :semi-bold
     :size   :heading-1} name]
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
        network-name    (string/capitalize (name network-keyword))]
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

(defn view
  []
  (let [collectible                                               (rf/sub
                                                                   [:wallet/last-collectible-details])
        {:keys [id collectible-data preview-url collection-data]} collectible
        {:keys [traits description]}                              collectible-data
        chain-id                                                  (get-in id [:contract-id :chain-id])]
    [scroll-page/scroll-page
     {:navigate-back? true
      :height         148
      :page-nav-props {:type        :title-description
                       :title       name
                       :description description
                       :right-side  [{:icon-name :i/options
                                      :on-press  #(js/alert "pressed")}]
                       :picture     preview-url}}
     [rn/view {:style style/container}
      [rn/view {:style style/preview-container}
       [rn/image
        {:source preview-url
         :style  style/preview}]]
      [header collectible-data (:image-url collection-data)]
      [cta-buttons]
      [tabs]
      [info chain-id]
      [traits-section traits]]]))
