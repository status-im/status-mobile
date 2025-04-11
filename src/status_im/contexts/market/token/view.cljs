(ns status-im.contexts.market.token.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.events-helper :as events-helper]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.wallet.collectible.utils :as collectible-utils]
    [status-im.contexts.wallet.send.from.style :as style]
    [status-im.setup.hot-reload :as hot-reload]
    [utils.i18n :as i18n]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(defn view
  []
  [floating-button-page/view
   {:footer-container-padding 0
    :header                   [quo/page-nav
                               {:type       :no-title
                                :icon-name  :i/close
                                :on-press   events-helper/navigate-back
                                :margin-top (safe-area/get-top)
                                :background :blur}]}
   [quo/page-top
    {:title                     (i18n/label :t/from-label)
     :title-accessibility-label :title-label}]
   [rn/view]])
