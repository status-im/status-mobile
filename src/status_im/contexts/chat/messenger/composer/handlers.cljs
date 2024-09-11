(ns status-im.contexts.chat.messenger.composer.handlers
  (:require
    [clojure.string :as string]
    [oops.core :as oops]
    [react-native.core :as rn]
    [status-im.contexts.chat.messenger.composer.constants :as constants]
    [status-im.contexts.chat.messenger.composer.selection :as selection]
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

(defn selection-change
  "A method that handles our custom selector for `B I U`"
  [event
   {:keys [input-ref selection-event selection-manager]}
   {:keys [lock-selection? cursor-position first-level? menu-items]}]
  (let [start             (oops/oget event "nativeEvent.selection.start")
        end               (oops/oget event "nativeEvent.selection.end")
        selection?        (not= start end)
        text-input-handle (rn/find-node-handle @input-ref)]
    (when-not @lock-selection?
      (reset! cursor-position end))
    (when (and selection? (not @first-level?))
      (js/setTimeout #(oops/ocall selection-manager :startActionMode text-input-handle) 500))
    (when (and (not selection?) (not @first-level?))
      (oops/ocall selection-manager :hideLastActionMode)
      (selection/reset-to-first-level-menu first-level? menu-items))
    (when @selection-event
      (let [{:keys [start end text-input-handle]} @selection-event]
        (selection/update-selection text-input-handle start end)
        (reset! selection-event nil)))))
