(ns status-im.common.terms.view
  (:require-macros [legacy.status-im.utils.slurp :refer [slurp]])
  (:require [quo.core :as quo]
            [react-native.gesture :as gesture]))

(def terms-of-use-text (slurp "resources/terms-of-use.mdwn"))

(defn terms-of-use
  []
  [gesture/scroll-view {:margin 20}
   [quo/text terms-of-use-text]])
