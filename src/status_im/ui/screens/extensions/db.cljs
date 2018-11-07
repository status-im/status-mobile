(ns status-im.ui.screens.extensions.db
  (:require
   [clojure.string :as string]
   [cljs.spec.alpha :as spec]))

(spec/def ::not-blank-string (complement string/blank?))

(spec/def :extension/id ::not-blank-string)
(spec/def :extension/name ::not-blank-string)
(spec/def :extension/url ::not-blank-string)
(spec/def :extension/active? boolean?)
(spec/def :extension/data (spec/nilable string?))
(spec/def :extension/extension (spec/keys :req-un [:extension/id
                                                   :extension/name
                                                   :extension/url
                                                   :extension/active?]
                                          :opt-un [:extension/data
                                                   :extension/hooks]))

(spec/def :extensions/extensions (spec/nilable (spec/map-of :extension/id :extension/extension)))
