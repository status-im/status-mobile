(ns status-im.common.privacy.view
  (:require-macros [legacy.status-im.utils.slurp :refer [slurp]])
  (:require [quo.core :as quo]
            [status-im.contexts.settings.common.blur-header :as blur-header]
            [react-native.gesture :as gesture]
            [utils.re-frame :as rf]))

(def privacy-statement-text (slurp "resources/privacy.mdwn"))

(defn- navigate-back [] (rf/dispatch [:navigate-back]))

(defn privacy-statement
  []
  [gesture/scroll-view {:style {:margin 20}}
   [quo/text privacy-statement-text]])

(defn view []
  [quo/overlay {:type :shell :top-inset? true}
   [blur-header/view {:title "Privacy Policy"}]
   [gesture/scroll-view {:style {:padding-horizontal 20}}
    [quo/text privacy-statement-text]]])
