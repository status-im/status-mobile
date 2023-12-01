(ns status-im2.contexts.wallet.collectible.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.common.scroll-page.view :as scroll-page] 
    [status-im2.contexts.wallet.collectible.style :as style]
    [status-im2.contexts.wallet.collectible.tabs.view :as tabs]
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

(def tabs-data
  [{:id                  :overview
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
    :accessibility-label :about-tab}])

(defn view
  []
  (let [collectible-details                              (rf/sub [:wallet/last-collectible-details])
        {:keys [name description preview-url]}           collectible-details
        selected-tab                                     (reagent/atom :about)]
    (fn []
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
        [header collectible-details]
        [cta-buttons]
        [quo/tabs
         {:size        32
          :style       style/tabs
          :scrollable? true
          :default-active   @selected-tab
          :on-change        #(reset! selected-tab %)
          :data        tabs-data}]
        [tabs/view {:selected-tab @selected-tab}]]])))
