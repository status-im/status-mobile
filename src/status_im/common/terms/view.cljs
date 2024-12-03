(ns status-im.common.terms.view
  (:require-macros [legacy.status-im.utils.slurp :refer [slurp]])
  (:require [quo.core :as quo]
            [react-native.gesture :as gesture]
            [utils.re-frame :as rf]))

(def terms-of-use-text (slurp "resources/terms-of-use.mdwn"))

(defn- navigate-back [] (rf/dispatch [:navigate-back]))

(defn terms-of-use
  []
  [gesture/scroll-view {:style {:margin 20}}
   [quo/text terms-of-use-text]])

(defn view []
  [quo/overlay {:type :shell :top-inset? true}
   [quo/page-nav
    {:background :blur
     :icon-name  :i/arrow-left
     :on-press   navigate-back}]
   [quo/page-top {:title "Terms of use"}]
   [gesture/scroll-view {:style {:padding-horizontal 20}}
    [quo/text terms-of-use-text]]])