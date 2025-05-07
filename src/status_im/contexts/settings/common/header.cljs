(ns status-im.contexts.settings.common.header
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.common.events-helper :as events-helper]))

(defn view
  [{:keys [title]}]
  [rn/view {:style {:padding-top (safe-area/get-top)}}
   [quo/page-nav
    {:background :blur
     :icon-name  :i/arrow-left
     :on-press   events-helper/navigate-back}]
   [quo/page-top {:title title}]])
