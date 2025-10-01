(ns status-im.app-build.core
  (:require-macros [status-im.app-build.core :as build]))

(def commit-hash (build/get-current-sha))
(def build-no (build/get-build-no))
(def version (build/git-short-version))

(def app-short-version
  (str version " (" build-no ")"))
