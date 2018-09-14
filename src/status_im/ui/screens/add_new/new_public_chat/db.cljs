(ns status-im.ui.screens.add-new.new-public-chat.db
  (:require [clojure.string :as string]
            [cljs.spec.alpha :as spec]
            [status-im.utils.homoglyph :as utils]
            status-im.utils.db))

(spec/def ::name :global/not-empty-string)

(spec/def ::topic (spec/and :global/not-empty-string
                            (partial re-matches #"[a-z0-9\-]+")))
