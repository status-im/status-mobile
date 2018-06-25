(ns status-im.ui.screens.group.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]))

(spec/def :group/selected-contacts (spec/nilable (spec/* string?)))
