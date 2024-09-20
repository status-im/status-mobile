(ns status-im.common.privacy.view
  (:require-macros [legacy.status-im.utils.slurp :refer [slurp]])
  (:require [quo.core :as quo]
            [react-native.gesture :as gesture]))

(def privacy-statement-text (slurp "resources/privacy.mdwn"))

(defn privacy-statement
  []
  [gesture/scroll-view {:margin 20}
   [quo/text privacy-statement-text]])
