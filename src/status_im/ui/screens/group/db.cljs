(ns status-im.ui.screens.group.db
  (:require [cljs.spec.alpha :as spec]))

(spec/def :group/selected-contacts (spec/nilable (spec/coll-of string? :kind set?)))
