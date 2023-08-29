(ns quo2.components.reactions.resource
  (:require-macros [quo2.components.reactions.resource :refer [resolve-all-reactions]]))

(def ^:private reactions
  (resolve-all-reactions))

(defn get-reaction
  [reaction]
  (assert (keyword? reaction) "Reaction should be a keyword")
  (assert (= "reaction" (namespace reaction))
          "Reaction keyword should be namespaced with :reaction")
  (get reactions (name reaction)))
