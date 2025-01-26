(ns status-im.contexts.wallet.common.empty-tab.view
  (:require
    [quo.core :as quo]
    [status-im.contexts.wallet.common.empty-tab.style :as style]))

(defn view
  [props]
  [quo/empty-state
   (assoc props :container-style style/empty-container-style)])
