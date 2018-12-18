(ns status-im.ui.screens.log-level-settings.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.config :as config]))

(re-frame/reg-sub
 :settings/current-log-level
 (fn [db _]
   (or (get-in db [:account/account :settings :log-level])
       config/log-level-status-go)))

(re-frame/reg-sub
 :settings/logging-enabled
 (fn [db _]
   (or (get-in db [:desktop/desktop :logging-enabled]) false)))

