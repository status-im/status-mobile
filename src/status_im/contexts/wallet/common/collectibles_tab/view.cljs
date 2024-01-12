(ns status-im.contexts.wallet.common.collectibles-tab.view
  (:require
   [quo.core :as quo]
   [quo.theme]
   [react-native.core :as rn]
   [status-im.common.resources :as resources]
   [status-im.contexts.wallet.common.empty-tab.view :as empty-tab]
   [utils.i18n :as i18n]))

(defn- view-internal
  [{:keys [theme collectibles filtered? on-collectible-press]}]
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
       {:data                    collectibles
        :style                   {:flex 1}
        :content-container-style {:align-items :center}
        :num-columns             2
        :render-fn               (fn [{:keys [preview-url id]}]
                                   [quo/collectible
                                    {:images   [preview-url]
                                     :on-press #(on-collectible-press id)}])}])))

(def view (quo.theme/with-theme view-internal))
