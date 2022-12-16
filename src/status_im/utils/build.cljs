(ns status-im.utils.build (:require-macros [status-im.utils.build :as build]))

(def commit-hash (build/get-current-sha))
(def version (build/git-short-version))
(def build-no (build/get-build-no))

(def app-short-version
  (str version " (" build-no ")"))
