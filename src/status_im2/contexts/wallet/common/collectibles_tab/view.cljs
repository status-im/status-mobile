(ns status-im2.contexts.wallet.common.collectibles-tab.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im2.contexts.wallet.common.empty-tab.view :as empty-tab]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [collectible-list (rf/sub [:wallet/collectibles])]
    (if (empty? collectible-list)
      [empty-tab/view
       {:title        (i18n/label :t/no-collectibles)
        :description  (i18n/label :t/no-collectibles-description)
        :placeholder? true}]
      [rn/flat-list
       {:data                    collectible-list
        :style                   {:flex 1}
        :content-container-style {:align-items :center}
        :num-columns             2
        :render-fn               (fn [{:keys [preview-url]}]
                                   [quo/collectible
                                    {:images   [preview-url]
                                     :on-press #(rf/dispatch
                                                 [:navigate-to
                                                  :wallet-collectible])}])}])))
