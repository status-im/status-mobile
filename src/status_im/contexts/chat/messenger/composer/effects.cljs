(ns status-im.contexts.chat.messenger.composer.effects
  (:require
    [react-native.core :as rn]
    [utils.number]))

(defn use-edit
  [input-ref edit]
  (rn/use-effect
   (fn []
     (when (and edit @input-ref)
       (js/setTimeout #(.focus ^js @input-ref) 600)))
   [(:message-id edit)]))

(defn use-reply
  [input-ref reply]
  (rn/use-effect
   (fn []
     (when (and reply @input-ref)
       (js/setTimeout #(.focus ^js @input-ref) 600)))
   [(:message-id reply)]))
