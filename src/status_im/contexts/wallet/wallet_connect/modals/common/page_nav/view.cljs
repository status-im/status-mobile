(ns status-im.contexts.wallet.wallet-connect.modals.common.page-nav.view
  (:require [quo.core :as quo]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [accessibility-label]}]
  [quo/page-nav
   {:icon-name           :i/close
    :background          :blur
    :on-press            #(do (rf/dispatch [:navigate-back])
                              (rf/dispatch [:wallet-connect/reject-session-request]))
    :accessibility-label accessibility-label}])
