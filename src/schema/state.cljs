(ns schema.state
  (:require [reagent.core :as reagent]))

(def errors
  "Set of schema identifiers, usually namespaced keywords. When the set is empty,
  no schema errors will be displayed on the app. See `schema.view/view`."
  (reagent/atom #{}))
