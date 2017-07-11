(ns status-im.discover.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :discoveries/discoveries map?)                                  ;; {id (string) descovery (map)}
(s/def :discoveries/discover-search-tags seq?)
(s/def :discoveries/tags vector?)
(s/def :discoveries/current-tag map?)
(s/def :discoveries/request-discoveries-timer int?)
(s/def :discoveries/new-discover map?)