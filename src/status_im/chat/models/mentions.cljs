(ns status-im.chat.models.mentions
  (:require [clojure.string :as string]
            [status-im.contact.db :as contact.db]))

(defn get-mentionable-users
  [{{:keys          [current-chat-id messages]
     :contacts/keys [contacts] :as db} :db}]
  (let [{:keys [group-chat public?] :as chat}
        (get-in db [:chats current-chat-id])
        chat-specific-suggestions
        (cond
          (and group-chat (not public?))
          (let [{:keys [public-key]} (:multiaccount db)
                all-contacts (:contacts/contacts db)
                group-contacts
                (contact.db/get-all-contacts-in-group-chat
                 (disj (:contacts chat) public-key)
                 nil
                 all-contacts
                 nil)]
            (reduce
             (fn [acc {:keys [alias public-key identicon name]}]
               (assoc acc alias
                      {:alias      alias
                       :identicon  identicon
                       :public-key public-key
                       :name       name}))
             {}
             group-contacts))

          (and group-chat public?)
          (reduce
           (fn [acc [_ {:keys [alias name identicon from]}]]
             (assoc acc alias {:alias      alias
                               :name       (or name alias)
                               :identicon  identicon
                               :public-key from}))
           nil
           (get messages current-chat-id))

          :else {})]
    (reduce
     (fn [acc [key {:keys [alias name identicon]}]]
       (let [name (string/replace name ".stateofus.eth" "")]
         (assoc acc alias {:alias      alias
                           :name       (or name alias)
                           :identicon  identicon
                           :public-key key})))
     chat-specific-suggestions
     contacts)))

(def word-regex #"^[\S]*\s|^[\S]*$")

(defn mentioned? [{:keys [alias name]} text]
  (let [lcase-name  (string/lower-case name)
        lcase-alias (string/lower-case alias)
        regex       (re-pattern
                     (string/join
                      "|"
                      [(str "^" lcase-name "\\s")
                       (str "^" lcase-name "$")
                       (str "^" lcase-alias "\\s")
                       (str "^" lcase-alias "$")]))
        lcase-text  (string/lower-case text)]
    (re-find regex lcase-text)))

(defn match-mention
  ([text users mention-key-idx]
   (match-mention text users mention-key-idx (inc mention-key-idx) []))
  ([text users mention-key-idx next-word-idx words]
   (let [trimmed-text (subs text next-word-idx)]
     (when-let [word (string/trim (re-find word-regex trimmed-text))]
       (let [words           (conj words word)
             searched-text   (string/lower-case (string/join " " words))
             suggestions     (filter
                              (fn [[_ {:keys [alias name]}]]
                                (let [names (set [alias name])]
                                  (some
                                   (fn [username]
                                     (string/starts-with?
                                      (string/lower-case username)
                                      searched-text))
                                   names)))
                              users)
             suggestions-cnt (count suggestions)]
         (cond (zero? suggestions-cnt)
               nil

               (and (= 1 suggestions-cnt)
                    (mentioned? (second (first suggestions))
                                (subs text (inc mention-key-idx))))
               (second (first suggestions))

               (> suggestions-cnt 1)
               (let [word-len        (count word)
                     text-len        (count text)
                     next-word-start (+ next-word-idx (inc word-len))]
                 (when (> text-len next-word-start)
                   (match-mention text users mention-key-idx
                                  next-word-start words)))))))))

(defn replace-mentions
  ([text users-fn]
   (replace-mentions text users-fn 0))
  ([text users-fn idx]
   (let [mention-key-idx (string/index-of text "@" idx)]
     (if-not mention-key-idx
       text
       (let [users (users-fn)
             {:keys [public-key alias]}
             (match-mention text users mention-key-idx)]
         (if-not alias
           (recur text (fn [] users) (inc mention-key-idx))
           (let [new-text (string/join
                           [(subs text 0 (inc mention-key-idx))
                            public-key
                            (subs text (+ (inc mention-key-idx)
                                          (count alias)))])
                 mention-end (+ (inc mention-key-idx) (count public-key))]
             (recur new-text (fn [] users) mention-end))))))))

(defn check-mentions [cofx text]
  (replace-mentions text #(get-mentionable-users cofx)))
