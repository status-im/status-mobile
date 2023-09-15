(ns status-im2.contexts.emoji-picker.events
  (:require [utils.re-frame :as rf]))

(rf/defn open-emoji-picker
  {:events [:emoji-picker/open]}
  [_ {:keys [on-select]}]
  {:dispatch [:open-modal :emoji-picker {:on-select on-select}]})
