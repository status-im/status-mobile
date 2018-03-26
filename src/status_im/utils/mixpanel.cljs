(ns status-im.utils.mixpanel
  (:require-macros [status-im.utils.slurp :as slurp])
  (:require [cljs.reader :as reader]
            [goog.crypt.base64 :as b64]
            [status-im.utils.build :as build]
            [status-im.utils.config :as config]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.http :as http]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]))

(def base-url "http://api.mixpanel.com/")
(def base-track-url (str base-url "track/"))

(defn encode [m]
  (b64/encodeString (types/clj->json m)))

(defn- build-url [id label props]
  (str base-track-url
       "?data="
       (encode {:event      label
                :properties (merge
                              {:token       config/mixpanel-token
                               :distinct_id id
                               :os          platform/os
                               :os-version  platform/version
                               :app-version build/version
                               :time        (datetime/timestamp)}
                              props)})))

(defn track [id label props]
  (http/get (build-url id label props)))

;; Mixpanel events definition

(def events (reader/read-string (slurp/slurp "./src/status_im/utils/mixpanel_events.edn")))
(def event-by-trigger (reduce-kv #(assoc %1 (:trigger %3) %3) {} events))

(defn matches? [event trigger]
  (cond (= 1 (count trigger))
        (= (first event) (first trigger))
        (= 2 (count trigger))
        (and
          (= (first event) (first trigger))
          (= (second event) (second trigger)))
        :else
        (= event trigger)))

(defn matching-events [event definitions]
  (reduce-kv #(if (matches? event %2) (conj %1 %3) %1) [] definitions))

