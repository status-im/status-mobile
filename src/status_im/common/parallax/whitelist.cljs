(ns status-im.common.parallax.whitelist
  (:require
    [status-im.common.parallax.blacklist :as blacklist]))

(def whitelisted?
  (not blacklist/blacklisted?))
