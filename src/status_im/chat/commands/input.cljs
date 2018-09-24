(ns status-im.chat.commands.input
  (:require [clojure.string :as string]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.constants :as chat-constants]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.input :as input-model]
            [status-im.utils.fx :as fx]))

(defn- current-param-position [input-text selection]
  (when selection
    (when-let [subs-input-text (subs input-text 0 selection)]
      (let [input-params   (input-model/split-command-args subs-input-text)
            param-index    (dec (count input-params))
            wrapping-count (get (frequencies subs-input-text) chat-constants/arg-wrapping-char 0)]
        (if (and (string/ends-with? subs-input-text chat-constants/spacing-char)
                 (even? wrapping-count))
          param-index
          (dec param-index))))))

(defn- command-completion [input-params params]
  (let [input-params-count (count input-params)
        params-count       (count params)]
    (cond
      (= input-params-count params-count) :complete
      (< input-params-count params-count) :less-then-needed
      (> input-params-count params-count) :more-than-needed)))

(defn selected-chat-command
  "Takes input text, text-selection and `protocol/id->command-props` map (result of
  the `chat-commands` fn) and returns the corresponding `command-props` entry,
  or nothing if input text doesn't match any available command.
  Besides keys `:params` and `:type`, the returned map contains:
  * `:input-params` - parsed parameters from the input text as map of `param-id->entered-value`
  # `:current-param-position` - index of the parameter the user is currently focused on (cursor position
  in relation to parameters), could be nil if the input is not selected
  # `:command-completion` - indication of command completion, possible values are `:complete`,
  `:less-then-needed` and `more-then-needed`"
  [input-text text-selection id->command-props]
  (when (input-model/starts-as-command? input-text)
    (let [[command-name & input-params] (input-model/split-command-args input-text)]
      (when-let [{:keys [params] :as command-props} (get id->command-props (subs command-name 1))] ;; trim leading `/` for lookup
        command-props
        (let [input-params (into {}
                                 (keep-indexed (fn [idx input-value]
                                                 (when (not (string/blank? input-value))
                                                   (when-let [param-name (get-in params [idx :id])]
                                                     [param-name input-value]))))
                                 input-params)]
          (assoc command-props
                 :input-params input-params
                 :current-param-position (current-param-position input-text text-selection)
                 :command-completion (command-completion input-params params)))))))

(fx/defn set-command-parameter
  "Set value as command parameter for the current chat"
  [{:keys [db]} last-param? param-index value]
  (let [{:keys [current-chat-id chats]} db
        [command & params]  (-> (get-in chats [current-chat-id :input-text])
                                input-model/split-command-args)
        param-count         (count params)
        ;; put the new value at the right place in parameters array
        new-params          (cond-> (into [] params)
                              (< param-index param-count) (assoc param-index value)
                              (>= param-index param-count) (conj value))
        ;; if the parameter is not the last one for the command, add space
        input-text                (cond-> (str command chat-constants/spacing-char
                                               (input-model/join-command-args
                                                new-params))
                                    (and (not last-param?)
                                         (or (= 0 param-count)
                                             (= param-index (dec param-count))))
                                    (str chat-constants/spacing-char))]
    {:db (-> db
             (chat-model/set-chat-ui-props {:validation-messages nil})
             (assoc-in [:chats current-chat-id :input-text] input-text))}))

(fx/defn select-chat-input-command
  "Takes command and (optional) map of input-parameters map and sets it as current-chat input"
  [{:keys [db]} {:keys [type params]} input-params]
  (let [{:keys [current-chat-id chat-ui-props]} db]
    {:db (-> db
             (chat-model/set-chat-ui-props {:show-suggestions?   false
                                            :validation-messages nil})
             (assoc-in [:chats current-chat-id :input-text]
                       (str (commands/command-name type)
                            chat-constants/spacing-char
                            (input-model/join-command-args input-params))))}))

(defn parse-parameters
  "Parses parameters from input for defined command params,
  returns map of `param-name->param-value`"
  [params input-text]
  (let [input-params (->> (input-model/split-command-args input-text) rest (into []))]
    (into {}
          (keep-indexed (fn [idx {:keys [id]}]
                          (when-let [value (get input-params idx)]
                            [id value])))
          params)))
