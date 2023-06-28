(ns status-im2.common.theme.events
  (:require [re-frame.core :as re-frame]
            [status-im2.common.theme.core :as theme]))

(re-frame/reg-fx
 :theme/init-theme
 (fn []
   (theme/add-device-theme-change-listener)))
