(ns status-im.discover.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :discoveries/discoveries (s/nilable map?))                                  ;; {id (string) descovery (map)}
(s/def :discoveries/discover-search-tags (s/nilable seq?))
(s/def :discoveries/tags (s/nilable vector?))
(s/def :discoveries/current-tag (s/nilable map?))
(s/def :discoveries/request-discoveries-timer (s/nilable int?))
(s/def :discoveries/new-discover (s/nilable map?))