(ns status-im.common.theme.effects
  (:require
    [status-im.common.theme.core :as theme]
    [utils.re-frame :as rf]))

(rf/reg-fx :theme/legacy-theme-fx
 (fn [theme]
   (theme/set-legacy-theme theme)))
