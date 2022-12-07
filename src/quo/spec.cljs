(ns quo.spec
  (:require [clojure.spec.alpha :as s]))

(s/def ::style (s/nilable map?))
