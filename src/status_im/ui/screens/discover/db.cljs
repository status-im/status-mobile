(ns status-im.ui.screens.discover.db
  (:require [cljs.spec.alpha :as s]))

;; {id (string) descovery (map)}
(s/def :discoveries/discoveries (s/nilable map?))
(s/def :discoveries/discover-search-tags (s/nilable set?))
(s/def :discoveries/tags (s/nilable vector?))
(s/def :discoveries/current-tag (s/nilable map?))
(s/def :discoveries/request-discoveries-timer (s/nilable int?))
