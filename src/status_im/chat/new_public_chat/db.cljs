(ns status-im.chat.new-public-chat.db
  (:require [cljs.spec.alpha :as spec]
            status-im.utils.db
            [status-im.constants :refer [console-chat-id]]
            [clojure.string :as string]
            [status-im.utils.homoglyph :as utils]))

(defn legal-name? [username]
  (let [username (some-> username string/trim)]
    (not (utils/matches username console-chat-id))))

(spec/def ::legal-name legal-name?)

(spec/def ::name (spec/and :global/not-empty-string
                           ::legal-name))

(spec/def ::topic (spec/and :global/not-empty-string
                            ::legal-name
                            (partial re-matches #"[a-z0-9\-]+")))
