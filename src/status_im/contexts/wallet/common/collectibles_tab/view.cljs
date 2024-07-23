(ns status-im.contexts.wallet.common.collectibles-tab.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.contexts.wallet.collectible.utils :as utils]
    [status-im.contexts.wallet.common.collectibles-tab.style :as style]
    [status-im.contexts.wallet.common.empty-tab.view :as empty-tab]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- collectible-item
  [{:keys [preview-url collection-data collectible-data total-owned on-press on-long-press]
    :as   collectible}
   index]
  (let [gradient-color   (keyword (str "gradient-" (inc (mod index 5))))
        on-press-fn      (rn/use-callback
                          (fn [aspect-ratio]
                            (when (fn? on-press)
                              (on-press {:collectible    (dissoc collectible :on-press :on-long-press)
                                         :aspect-ratio   aspect-ratio
                                         :gradient-color gradient-color}))))
        on-long-press-fn (rn/use-callback
                          (fn [aspect-ratio]
                            (when on-long-press
                              (on-long-press
                               (dissoc collectible :on-press :on-long-press)
                               aspect-ratio))))]
    [quo/collectible-list-item
     {:type                 :card
      :image-src            (:uri preview-url)
      :avatar-image-src     (:image-url collection-data)
      :collectible-name     (:name collectible-data)
      :supported-file?      (utils/supported-file? (:animation-media-type collectible-data))
      :gradient-color-index gradient-color
      :counter              (utils/collectible-owned-counter total-owned)
      :container-style      style/collectible-container
      :on-press             on-press-fn
      :on-long-press        on-long-press-fn}]))

(defn view
  [{:keys [collectibles filtered? on-end-reached on-collectible-press
           current-account-address on-collectible-long-press]}]
  (let [theme                   (quo.theme/use-theme)
        no-results-match-query? (and filtered? (empty? collectibles))]
    (cond
      no-results-match-query?
      [rn/view {:style {:flex 1 :justify-content :center}}
       [quo/empty-state
        {:title       (i18n/label :t/nothing-found)
         :description (i18n/label :t/try-to-search-something-else)
         :image       (resources/get-themed-image :no-collectibles theme)}]]

      (empty? collectibles)
      [empty-tab/view
       {:title       (i18n/label :t/no-collectibles)
        :description (i18n/label :t/no-collectibles-description)
        :image       (resources/get-themed-image :no-collectibles theme)}]

      :else
      ;; TODO: https://github.com/status-im/status-mobile/issues/20137
      ;; 1. If possible, move `collectibles-data` calculation to a subscription
      ;; 2. Optimization: do not recalculate all the collectibles, process only the new ones
      (let [collectibles-data (map-indexed
                               (fn [index {:keys [ownership] :as collectible}]
                                 (let [total-owned (rf/sub [:wallet/total-owned-collectible ownership
                                                            current-account-address])]
                                   (assoc collectible
                                          :total-owned       total-owned
                                          :on-long-press     on-collectible-long-press
                                          :on-press          on-collectible-press
                                          :collectible-index index)))
                               collectibles)]
        [rn/flat-list
         {:data                     collectibles-data
          :style                    {:flex 1}
          :content-container-style  style/list-container-style
          :window-size              11
          :num-columns              2
          :render-fn                collectible-item
          :on-end-reached           on-end-reached
          :key-fn                   :collectible-index
          :on-end-reached-threshold 4}]))))
