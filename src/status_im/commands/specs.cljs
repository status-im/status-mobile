(ns status-im.commands.specs
  (:require [cljs.spec.alpha :as spec]))

(spec/def :commands/access-scope->commands-responses (spec/nilable map?))
