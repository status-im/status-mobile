(ns status-im.chat.models.mentions
  (:require [status-im.utils.fx :as fx]
            [clojure.string :as string]
            [taoensso.timbre :as log]))

(defn get-suggestions
  "Returns a sorted list of users matching the input"
  [users input]
  (let [input (string/lower-case input)]
    (->> (filter (fn [[alias _]]
                   (when (string? alias)
                     (-> alias
                         string/lower-case
                         (string/starts-with? input))))
                 users)
         vals
         (sort-by (comp string/lower-case :alias))
         seq)))

(defn tag-text-parts
  "Takes a string, a map of users and a regexp of known mentions
   and splits the string into parts each parts being a vector
   with a part type:
   - `text` for regular text
   - `mention` for mentions, which will contain the user alias and public key
   This is used to quickly render the input field with the proper styling"
  [text users mention-regexp]
  (if mention-regexp
    (loop [[text mention & rest] (string/split text mention-regexp)
           previous-mention?     false
           computed-text         []]
      (if mention
        (let [mention (subs mention 1)]
          (recur rest true (conj computed-text
                                 [:text (if previous-mention?
                                          (subs text 1)
                                          text)]
                                 [:mention
                                  mention
                                  (get-in users [mention :public-key])])))
        (if text
          (conj computed-text
                [:text (if previous-mention?
                         (subs text 1)
                         text)])
          computed-text)))
    [[:text text]]))

(def end-mention-regexp
  #"(@(?:(?:[A-Z][a-z]*(?:\s[A-Z]?[a-z]*)?(?:\s[A-Z]?[a-z]*)?)|(?:[a-zA-Z0-9\.]*))$)")

(defn compute-all-text-parts
  "Takes the chat inputs and a cursor position and extracts the mentions
   from the text, returning new chat inputs which are important params for
   rendering (suggestions, tagged text parts, new cursor position)"
  [{:keys [mentioned users text]} cursor]
  (let [text               (or text "")
        ;; split text at current cursor position
        text-before-cursor (subs text 0 cursor)
        text-after-cursor  (subs text cursor)
        ;;check if user is completing a mention
        [completing? _]    (re-find end-mention-regexp
                                    text-before-cursor)
        ;; update suggestions
        suggestions        (if completing?
                             (get-suggestions users
                                              (subs completing? 1))
                             [])
        ;; check if a mention has been completed
        completed?
        (when (and completing?
                   (= 1 (count suggestions))
                   (= (string/lower-case completing?)
                      (-> suggestions
                          first
                          :alias
                          string/lower-case)))
          completing?)
        ;; remove the current mention from the text before the cursor
        text-before-cursor (if (or completed?
                                   (not completing?))
                             text-before-cursor
                             (subs text-before-cursor
                                   0
                                   (- (count text-before-cursor)
                                      (count completing?))))
        text-after-cursor  (str (when completed? " ") text-after-cursor)
        mentioned          (cond-> mentioned
                             completed? (conj mentioned completed?))
        ;; prepare regexp for currently mentioned users
        mention-regexp
        (when (seq mentioned)
          (re-pattern (str "(@" (string/replace
                                 (string/join "|@" mentioned)
                                 " " "\\s")
                           ")")))]
    (cond-> {:users users
             :text text
             :before-cursor (tag-text-parts text-before-cursor
                                            users
                                            mention-regexp)
             :after-cursor (tag-text-parts text-after-cursor
                                           users
                                           mention-regexp)}
      mentioned
      (assoc :mentioned mentioned)
      completed?
      (assoc :cursor (inc (count text-before-cursor)))
      (and completing?
           (not completed?))
      (assoc :completing? [(if (seq suggestions)
                             :current
                             :text)
                           completing?]
             :suggestions suggestions))))

(defn recompute-input-text
  "Reconstructs the input text from the tagged text parts"
  [{:keys [before-cursor after-cursor completing? replace-mentions?]}]
  (letfn [(add-s [acc t s k]
            (str acc (if (= :mention t)
                       (str "@" (if replace-mentions?
                                  k
                                  s)
                            " ")
                       s)))]
    (reduce (fn [acc [t s k]]
              (add-s acc t s k))
            (str (reduce (fn [acc [t s k]]
                           (add-s acc t s k))
                         ""
                         before-cursor)
                 completing?)
            after-cursor)))

(fx/defn fetch-mentionable-users
  "Gets map of users with props to extract mention suggestions from. It is
   made with the contacts and users whose messages are currently loaded in the
   chat"
  {:events [::mention-pressed]}
  [{{:keys          [current-chat-id messages]
     :contacts/keys [contacts] :as db} :db}]
  (let [messages (get messages current-chat-id)
        mentionable-users
        (reduce (fn [acc [key {:keys [name identicon]}]]
                  (let [alias (string/replace name ".stateofus.eth" "")]
                    (assoc acc alias {:alias      alias
                                      :identicon  identicon
                                      :public-key key})))
                (reduce (fn [acc [_ {:keys [alias identicon from]}]]
                          (assoc acc alias {:alias      alias
                                            :identicon  identicon
                                            :public-key from}))
                        (get-in db [:chat/inputs current-chat-id :users])
                        messages)
                contacts)]
    (println :mention mentionable-users)
    {:db (-> db
             (assoc-in [:chat/inputs current-chat-id :users]
                       mentionable-users)
             (assoc-in [:chat/inputs current-chat-id :suggestions]
                       (get-suggestions mentionable-users "")))}))

(fx/defn recompute-input
  "On selection change we want to recompute the chat inputs because
   the user may have landed the cursor in the middle of a mention, which
   is now incomplete
   For simplicity we actually do it everytime the cursor moves
   NOTE: to avoid recomputing on every selection change
   which happens every time the cursor moves, we could use a state machine
   that starts recomputing on `@` key-press
   relevant further key-presses would be `Backspace` and ` `"
  {:events [::selection-change]}
  [{{:keys [current-chat-id chats] :as db} :db} cursor]
  (log/info :selection-changed)
  (let [input             (-> (get-in db [:chat/inputs current-chat-id])
                              (compute-all-text-parts cursor))
        input-text        (recompute-input-text input)
        input-text-empty? (if (seq input-text)
                            (-> input-text string/trim string/blank?)
                            true)]
    {:db (assoc-in db
                   [:chat/inputs current-chat-id]
                   (assoc input
                          :input-text input-text
                          :input-text-empty? input-text-empty?))}))

(fx/defn compute-completion
  "When the user selects a suggestions we complete the current mention
   with it."
  {:events [::complete-mention]}
  [{{:keys [current-chat-id] :as db} :db} alias public-key]
  (let [{:keys [cursor users before-cursor completing?] :as input}
        (get-in db [:chat/inputs current-chat-id])
        before-cursor
        (conj before-cursor
              [:mention alias (get-in users [alias :public-key])])
        new-input (-> input
                      (dissoc :completing? :suggestions)
                      (update :mentioned conj alias)
                      (assoc :completed? true
                             :before-cursor before-cursor
                             :cursor (reduce (fn [acc [t s]]
                                               (+ acc (if (= :mention t)
                                                        (+ 2 (count s))
                                                        (count s))))
                                             0
                                             before-cursor)))]
    {:db (assoc-in db
                   [:chat/inputs current-chat-id]
                   (assoc new-input :text (recompute-input-text new-input)))}))
