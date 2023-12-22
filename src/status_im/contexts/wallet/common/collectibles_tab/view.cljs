(ns status-im.contexts.wallet.common.collectibles-tab.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.contexts.wallet.common.empty-tab.view :as empty-tab]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- view-internal
  [{:keys [theme address]}]
  (let [collectible-list (if address
                           (rf/sub [:wallet/collectibles-per-account address])
                           (rf/sub [:wallet/all-collectibles]))]
    (if (empty? collectible-list)
      [empty-tab/view
       {:title       (i18n/label :t/no-collectibles)
        :description (i18n/label :t/no-collectibles-description)
        :image       (resources/get-themed-image :no-collectibles theme)}]
      [rn/flat-list
       {:data                    collectible-list
        :style                   {:flex 1}
        :content-container-style {:align-items :center}
        :num-columns             2
        :render-fn               (fn [{:keys [preview-url id]}]
                                   [quo/collectible
                                    {:images   [preview-url]
                                     :on-press (fn []
                                                 (rf/dispatch [:wallet/get-collectible-details id])
                                                 (rf/dispatch
                                                  [:navigate-to
                                                   :wallet-collectible]))}])}])))

(def view (quo.theme/with-theme view-internal))
