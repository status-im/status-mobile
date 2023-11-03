(ns status-im2.contexts.wallet.common.collectibles-tab.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im2.contexts.wallet.common.empty-tab.view :as empty-tab]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- collectible-image-url
  [{:keys [image_url animation_url animation_media_type]}]
  (if (and (not= "" animation_url)
           (not= animation_media_type "image/svg+xml"))
    animation_url
    image_url))

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
        :render-fn               (fn [collectible]
                                   [quo/collectible
                                    {:images   [(collectible-image-url collectible)]
                                     :on-press #(rf/dispatch
                                                 [:navigate-to
                                                  :wallet-collectible])}])}])))
