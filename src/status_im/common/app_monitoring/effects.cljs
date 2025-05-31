(ns status-im.common.app-monitoring.effects
  (:require
    [native-module.core :as native-module]
    [utils.re-frame :as rf]))

(rf/reg-fx :effects.app-monitoring/intended-panic
 (fn [message]
   (native-module/intended-panic message)))
