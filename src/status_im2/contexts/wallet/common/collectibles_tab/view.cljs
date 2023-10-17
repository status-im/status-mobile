(ns status-im2.contexts.wallet.common.collectibles-tab.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im2.contexts.wallet.common.empty-tab.view :as empty-tab]
    [status-im2.contexts.wallet.common.temp :as temp]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))


(defn view
  []
  (let [collectible-list temp/collectible-list]
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
        :render-fn               (fn [item] [quo/collectible
                                             {:images   (repeat 1 item)
                                              :on-press #(rf/dispatch [:navigate-to
                                                                       :wallet-collectible])}])}])))
