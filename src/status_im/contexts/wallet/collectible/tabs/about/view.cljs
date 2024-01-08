(ns status-im.contexts.wallet.collectible.tabs.about.view
  (:require [quo.core :as quo]
            [quo.theme]
            [react-native.core :as rn]
            [status-im.contexts.wallet.collectible.tabs.about.style :as style]
            [status-im.contexts.wallet.temp :as temp]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(def ^:private link-card-space 28)

(defn- view-internal
  []
  (let [window-width               (rf/sub [:dimensions/window-width])
        item-width                 (- (/ window-width 2) link-card-space)
        {:keys [collectible-data]} (rf/sub [:wallet/last-collectible-details])
        link-card-container-style  (style/link-card item-width)]
    [:<>
     [rn/view {:style style/title}
      [quo/text
       {:size   :heading-2
        :weight :semi-bold}
       (:name collectible-data)]]
     [rn/view {:style style/description}
      [quo/text
       {:size :paragraph-2}
       (:description collectible-data)]]
     [quo/section-label
      {:container-style style/section-label
       :section         (i18n/label :t/on-the-web)}]
     [rn/view {:style style/link-cards-container}
      (for [item (:cards temp/collectible-about)]
        ^{:key (:title item)}
        [quo/link-card (assoc item :container-style link-card-container-style)])]]))

(def view (quo.theme/with-theme view-internal))
