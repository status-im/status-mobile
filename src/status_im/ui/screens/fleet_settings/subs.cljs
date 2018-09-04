(ns status-im.ui.screens.fleet-settings.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.config :as config]
            [status-im.models.fleet :as fleet]))

(re-frame/reg-sub
 :settings/current-fleet
 (fn [db _]
   (fleet/current-fleet db)))
