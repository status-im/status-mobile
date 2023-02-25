(ns status-im2.setup.schema-preload
  ":dev/always is needed so that the compiler doesn't cache this file."
  {:dev/always true}
  (:require [status-im2.setup.schema :as schema]))

(schema/setup!)
