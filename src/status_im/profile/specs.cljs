(ns status-im.profile.specs
  (:require [cljs.spec.alpha :as s]))

;EDIT PROFILE
(s/def :profile/profile-edit (s/nilable map?))