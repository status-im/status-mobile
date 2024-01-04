(ns status-im.setup.schema-preload
  ":dev/always is needed so that the compiler doesn't cache this file."
  {:dev/always true}
  (:require [status-im.setup.schema :as schema]))

(schema/setup!)
