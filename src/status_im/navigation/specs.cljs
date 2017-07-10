(ns status-im.navigation.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :navigation/view-id keyword?)                                  ;;current view
(s/def :navigation/modal (s/nilable keyword?))                        ;;modal view id
(s/def :navigation/navigation-stack seq?)                             ;;stack of view's ids (keywords)
(s/def :navigation/prev-tab-view-id keyword?)
(s/def :navigation/prev-view-id keyword?)