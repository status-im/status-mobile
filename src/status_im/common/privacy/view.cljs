(ns status-im.common.privacy.view
  (:require-macros [legacy.status-im.utils.slurp :refer [slurp]])
  (:require [quo.core :as quo]
            [react-native.gesture :as gesture]
            [status-im.contexts.settings.common.header :as header]
            [utils.i18n :as i18n]))

(def privacy-statement-text (slurp "resources/privacy.mdwn"))

(defn privacy-statement
  []
  [gesture/scroll-view {:style {:margin 20}}
   [quo/text privacy-statement-text]])

(defn view
  []
  [quo/overlay {:type :shell}
   [header/view {:title (i18n/label :t/privacy-policy)}]
   [gesture/scroll-view {:style {:padding-horizontal 20}}
    [quo/text privacy-statement-text]]])
