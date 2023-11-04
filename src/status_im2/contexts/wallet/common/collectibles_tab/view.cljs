(ns status-im2.contexts.wallet.common.collectibles-tab.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im2.contexts.wallet.common.empty-tab.view :as empty-tab]
    [utils.i18n :as i18n]
    [clojure.string :as string]
    [utils.re-frame :as rf]))

(defn- collectible-image-url
  [{:keys [image-url animation-url animation-media-type]}]
  (if (and (not (string/blank? animation-url))
           (not= animation-media-type "image/svg+xml"))
    animation-url
    image-url))

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
