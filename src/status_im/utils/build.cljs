(ns status-im.utils.build
  (:require-macros [status-im.utils.build :as build]))

(def version (build/git-short-version))
