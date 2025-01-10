(ns status-im.contexts.chat.messenger.composer.utils
  (:require
    [utils.number]
    [utils.re-frame :as rf]))

(defn blur-input
  [input-ref]
  (when @input-ref
    (rf/dispatch [:chat.ui/set-input-focused false])
    (.blur ^js @input-ref)))

(defn cancel-reply-message
  [input-ref]
  (js/setTimeout #(blur-input input-ref) 100)
  (rf/dispatch [:chat.ui/cancel-message-reply]))

(defn cancel-edit-message
  [input-ref]
  ;; NOTE: adding a timeout to assure the input is blurred on the next tick
  ;; after the `text-value` was cleared. Otherwise the height will be calculated
  ;; with the old `text-value`, leading to wrong composer height after blur.
  (js/setTimeout
   (fn []
     (blur-input input-ref))
   100)
  (.setNativeProps ^js @input-ref (clj->js {:text ""}))
  (rf/dispatch [:chat.ui/cancel-message-edit]))
