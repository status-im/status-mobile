(ns status-im.contexts.settings.wallet.saved-addresses.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.common.resources :as resources]
            [status-im.contexts.settings.wallet.saved-addresses.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn empty-state
  []
  (let [theme (quo.theme/use-theme)]
    [quo/empty-state
     {:title           (i18n/label :t/no-saved-addresses)
      :description     (i18n/label :t/you-like-to-type-43-characters)
      :image           (resources/get-themed-image :sweating-man theme)
      :container-style style/empty-container-style}]))

(defn render-item
  [{:keys [address ens] color :colorId saved-address-name :name}]
  [quo/saved-address
   {:active-state? false
    :user-props
    {:name saved-address-name
     :address address
     :ens (when-not (string/blank? ens) ens)
     :customization-color (keyword color)}}])

(defn view
  []
  (let [inset-top           (safe-area/get-top)
        customization-color (rf/sub [:profile/customization-color])
        saved-addresses     (rf/sub [:wallet/saved-addresses])
        navigate-back       (rn/use-callback #(rf/dispatch [:navigate-back]))]
    (rn/use-mount #(rf/dispatch [:wallet/get-saved-addresses]))
    [quo/overlay
     {:type            :shell
      :container-style (style/page-wrapper inset-top)}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   navigate-back}]
     [rn/view {:style style/title-container}
      [quo/standard-title
       {:title               (i18n/label :t/saved-addresses)
        :accessibility-label :saved-addresses-header
        :right               :action
        :customization-color customization-color
        :icon                :i/add}]]
     [rn/view {:style {:flex 1}}
      [rn/section-list
       {:key-fn                         :title
        :sticky-section-headers-enabled false
        :sections                       (mapv (fn [[title items]]
                                                {:title title
                                                 :data  items})
                                              (sort (group-by #(string/upper-case (first (:name %))) saved-addresses)))
        :render-section-header-fn       (fn [{:keys [title]}]
                                          [quo/divider-label {:container-style {:margin-top 12
                                                                                :border-top-color colors/white-opa-5}}
                                           title])
        :render-section-footer-fn       (fn []
                                          [rn/view {:style {:height 8}}])
        :content-container-style        {:padding-bottom 20}
        :render-fn                      render-item
        :separator                      [rn/view {:style {:height 4}}]}]]
     (when-not (seq saved-addresses)
       [empty-state])]))
