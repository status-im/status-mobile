(ns status-im.utils.build
  (:require-macros [status-im.utils.build :refer [git-short-version]]))

(def version (git-short-version))
