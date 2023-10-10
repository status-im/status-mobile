(ns status-im2.contexts.wallet.common.empty-tab.view
  (:require [quo2.core :as quo]
            [status-im2.contexts.wallet.common.empty-tab.style :as style]))

(defn view
  [props]
  [quo/empty-state
   (merge props {:container-style style/empty-container-style})])
