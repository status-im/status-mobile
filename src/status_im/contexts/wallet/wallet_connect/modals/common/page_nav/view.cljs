(ns status-im.contexts.wallet.wallet-connect.modals.common.page-nav.view
  (:require [quo.core :as quo]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [accessibility-label]}]
  [quo/page-nav
   {:icon-name           :i/close
    :background          :blur
    :on-press            #(rf/dispatch [:wallet-connect/dismiss-request-modal])
    :accessibility-label accessibility-label}])
