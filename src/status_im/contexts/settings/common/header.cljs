(ns status-im.contexts.settings.common.header
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [utils.re-frame :as rf]))

(defn- navigate-back [] (rf/dispatch [:navigate-back]))

(defn view
  [{:keys [title]}]
  [rn/view {:style {:padding-top (safe-area/get-top)}}
   [quo/page-nav
    {:background :blur
     :icon-name  :i/arrow-left
     :on-press   navigate-back}]
   [quo/page-top {:title title}]])
