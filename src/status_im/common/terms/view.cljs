(ns status-im.common.terms.view
  (:require-macros [legacy.status-im.utils.slurp :refer [slurp]])
  (:require [quo.core :as quo]
            [react-native.gesture :as gesture]
            [status-im.contexts.settings.common.header :as header]
            [utils.i18n :as i18n]))

(def terms-of-use-text (slurp "resources/terms-of-use.mdwn"))

(defn terms-of-use
  []
  [gesture/scroll-view {:style {:margin 20}}
   [quo/text terms-of-use-text]])

(defn view
  []
  [quo/overlay {:type :shell}
   [header/view {:title (i18n/label :t/terms-of-service)}]
   [gesture/scroll-view {:style {:padding-horizontal 20}}
    [quo/text terms-of-use-text]]])
