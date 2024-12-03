(ns status-im.contexts.settings.common.blur-header
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [utils.re-frame :as rf]))

(defn container []
  {:position    :absolute
   :top         0
   :left        0
   :right       0
   :z-index     1
   :padding-top (safe-area/get-top)})

(def absolute-fill
  {:position :absolute
   :top      0
   :left     0
   :right    0
   :bottom   0})

(defn- navigate-back [] (rf/dispatch [:navigate-back]))

(defn view [{:keys [title]}]
  [rn/view {:style (container)}
   [quo/blur {:style         absolute-fill
              :blur-radius   20
              :blur-amount   15
              :overlay-color colors/neutral-80-opa-1-blur}]
   [quo/page-nav
    {:background :blur
     :icon-name  :i/arrow-left
     :on-press   navigate-back}]
   [quo/page-top {:title title}]])
