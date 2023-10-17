(ns status-im2.contexts.wallet.collectible.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [status-im2.common.scroll-page.view :as scroll-page]
    [status-im2.contexts.wallet.collectible.style :as style]
    [status-im2.contexts.wallet.common.temp :as temp]
    [utils.i18n :as i18n]))

(defn header
  [{:keys [name description collection-image]}]
  [rn/view {:style style/header}
   [quo/text
    {:weight :semi-bold
     :size   :heading-1} name]
   [rn/view style/collection-container
    [quo/collection-avatar {:image collection-image}]
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
  [rn/view {:style style/traits-section}
   [quo/section-label
    {:section (i18n/label :t/traits)}]]

  [rn/flat-list
   {:render-fn               (fn [{:keys [title subtitle]}]
                               [rn/view {:style style/traits-item}
                                [quo/data-item
                                 {:description :default
                                  :card?       true
                                  :status      :default
                                  :size        :default
                                  :title       title
                                  :subtitle    subtitle}]])
    :data                    traits
    :key                     :collectibles-list
    :key-fn                  :id
    :num-columns             2
    :content-container-style style/traits-container}])

(defn info
  []
  [rn/view
   {:style style/info-container}
   [rn/view {:style style/account}
    [quo/data-item
     {:description         :account
      :card?               true
      :status              :default
      :size                :default
      :title               (i18n/label :t/account-title)
      :subtitle            "Collectibles vault"
      :emoji               "🎮"
      :customization-color :yellow}]]

   [rn/view {:style style/network}
    [quo/data-item
     {:description :network
      :card?       true
      :status      :default
      :size        :default
      :title       (i18n/label :t/network)
      :subtitle    (i18n/label :t/mainnet)}]]])

(defn view
  []
  (let [{:keys [name description image traits] :as props} temp/collectible-details]
    [scroll-page/scroll-page
     {:navigate-back? true
      :height         148
      :page-nav-props {:type        :title-description
                       :title       name
                       :description description
                       :right-side  [{:icon-name :i/options
                                      :on-press  #(js/alert "pressed")}]
                       :picture     image}}
     [rn/view {:style style/container}
      [rn/image
       {:source image
        :style  style/preview}]
      [header props]
      [cta-buttons]
      [tabs]
      [info]
      [traits-section traits]]]))
