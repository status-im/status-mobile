(ns status-im.contexts.profile.core
  (:require [status-im.constants :as constants]))

(defn get-currency
  [profile]
  (or (:currency profile)
      constants/profile-default-currency))

(defn get-customization-color
  [profile]
  (or (:customization-color profile)
      constants/profile-default-color))
