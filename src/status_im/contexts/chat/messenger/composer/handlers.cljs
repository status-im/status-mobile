(ns status-im.contexts.chat.messenger.composer.handlers
  (:require
    [clojure.string :as string]
    [status-im.contexts.chat.messenger.composer.constants :as constants]
    [utils.debounce :as debounce]
    [utils.number]
    [utils.re-frame :as rf]))

(defn change-text
  "Update `text-value`, update cursor selection, find links, find mentions"
  [text]
  (rf/dispatch [:chat.ui/set-chat-input-text text])
  (debounce/debounce-and-dispatch [:link-preview/unfurl-urls text] constants/unfurl-debounce-ms)
  (if (string/ends-with? text "@")
    (rf/dispatch [:mention/on-change-text text])
    (debounce/debounce-and-dispatch [:mention/on-change-text text] 300)))
