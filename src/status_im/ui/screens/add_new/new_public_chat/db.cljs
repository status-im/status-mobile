(ns status-im.ui.screens.add-new.new-public-chat.db
  (:require [clojure.string :as string]
            [cljs.spec.alpha :as spec]
            [status-im.constants :as constants]
            [status-im.utils.homoglyph :as utils]
            status-im.utils.db))

(defn- legal-name? [username]
  (let [username (some-> username string/trim)]
    (not (utils/matches username constants/console-chat-id))))

(spec/def ::legal-name legal-name?)

(spec/def ::name (spec/and :global/not-empty-string
                           ::legal-name))

(spec/def ::topic (spec/and :global/not-empty-string
                            ::legal-name
                            (partial re-matches #"[a-z0-9\-]+")))
