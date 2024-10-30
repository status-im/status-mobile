(ns status-im.contexts.profile.logout.effects
  (:require [native-module.core :as native-module]
            [re-frame.core :as rf]))

(rf/reg-fx :effects.profile/logout native-module/logout)
