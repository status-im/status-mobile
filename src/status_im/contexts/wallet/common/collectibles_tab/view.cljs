(ns status-im.contexts.wallet.common.collectibles-tab.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.contexts.wallet.collectible.utils :as utils]
    [status-im.contexts.wallet.common.empty-tab.view :as empty-tab]
    [utils.i18n :as i18n]))

(defn- render-fn
  [{:keys [preview-url collection-data ownership collectible-data] :as collectible} index address
   on-press on-long-press]
  (let [total-owned (utils/total-owned-collectible ownership address)]
    [quo/collectible-list-item
     {:type                 :card
      :image-src            (:uri preview-url)
      :avatar-image-src     (:image-url collection-data)
      :collectible-name     (:name collection-data)
      :supported-file?      (utils/supported-file? (:animation-media-type collectible-data))
      :gradient-color-index (keyword (str "gradient-" (inc (mod index 5))))
      :counter              (utils/collectible-owned-counter total-owned)
      :container-style      {:padding 8
                             :width   "50%"}
      :on-press             #(when on-press
                               (on-press collectible))
      :on-long-press        #(when on-long-press
                               (on-long-press collectible))}]))

(defn view
  [{:keys [theme collectibles filtered? on-collectible-press on-end-reached current-account-address
           on-collectible-long-press]}]
  (let [no-results-match-query? (and filtered? (empty? collectibles))]
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
      [rn/flat-list
       {:data                     collectibles
        :style                    {:flex 1}
        :content-container-style  {:margin-horizontal 12}
        :window-size              11
        :num-columns              2
        :render-fn                (fn [item index]
                                    (render-fn item
                                               index
                                               current-account-address
                                               on-collectible-press
                                               on-collectible-long-press))
        :on-end-reached           on-end-reached
        :on-end-reached-threshold 4}])))
