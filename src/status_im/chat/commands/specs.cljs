(ns status-im.chat.commands.specs
  (:require [cljs.spec.alpha :as spec]))

(spec/def :chat/id->command (spec/nilable map?))
(spec/def :chat/access-scope->command-id (spec/nilable map?))
