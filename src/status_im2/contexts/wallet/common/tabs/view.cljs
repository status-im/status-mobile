(ns status-im2.contexts.wallet.common.tabs.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [status-im2.contexts.wallet.common.tabs.style :as style]))

(defn empty-tab
  [{:keys [title description]}]
  [rn/view {:style style/empty-container}
   [rn/view {:style style/image-placeholder}]
   [quo/text {:weight :semi-bold :style {:margin-top 12}} title]
   [quo/text {:size :paragraph-2 :style {:margin-top 2}} description]])
