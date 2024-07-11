(ns status-im.contexts.centralized-metrics.effects
  (:require
    [native-module.core :as native-module]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/reg-fx :effects.centralized-metrics/toggle-metrics
 (fn [enabled?]
   (native-module/toggle-centralized-metrics enabled? #(log/debug "toggled-metrics" % enabled?))))
