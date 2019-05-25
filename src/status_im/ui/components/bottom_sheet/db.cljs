(ns status-im.ui.components.bottom-sheet.db
  (:require  [cljs.spec.alpha :as spec]))

(spec/def :bottom-sheet/show? (spec/nilable boolean?))
(spec/def :bottom-sheet/view (spec/nilable any?))
(spec/def :bottom-sheet/options (spec/nilable map?))
