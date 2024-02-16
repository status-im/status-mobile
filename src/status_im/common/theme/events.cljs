(ns status-im.common.theme.events
  (:require
    [re-frame.core :as re-frame]
    [status-im.common.theme.core :as theme]))

(re-frame/reg-fx
 :theme/init-theme
 (fn []
   (theme/add-device-theme-change-listener)))
