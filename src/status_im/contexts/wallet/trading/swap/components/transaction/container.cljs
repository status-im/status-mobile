(ns status-im.contexts.wallet.trading.swap.components.transaction.container
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.floating-button-page.view :as floating-button-page]
            [status-im.contexts.wallet.trading.swap.components.transaction.style :as style]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [footer]} & children]
  (let [customization-color (rf/sub [:trading.swap/customization-color])]
    [rn/view {:style style/container}
     [floating-button-page/view
      {:footer-container-padding 0
       :content-container-style  {:margin-bottom 168}
       :header                   [quo/page-nav
                                  {:icon-name           :i/close
                                   :on-press            events-helper/navigate-back
                                   :margin-top          8
                                   :background          :blur
                                   :accessibility-label :top-bar}]
       :footer                   [footer]
       :gradient-cover?          true
       :customization-color      customization-color}
      (into
       [rn/scroll-view {:style style/scroll-view-container}]
       children)]]))
