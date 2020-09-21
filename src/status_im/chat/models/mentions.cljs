(ns status-im.chat.models.mentions
  (:require [clojure.string :as string]
            [status-im.utils.fx :as fx]
            [status-im.contact.db :as contact.db]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]
            [status-im.utils.utils :as utils]))

(def at-sign "@")

(defn get-mentionable-users
  [{{:keys          [current-chat-id]
     :contacts/keys [contacts] :as db} :db}]
  (let [{:keys [group-chat public? users] :as chat}
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
               (assoc acc public-key
                      {:alias      alias
                       :identicon  identicon
                       :public-key public-key
                       :name       name}))
             {}
             group-contacts))

          :else users)
        {:keys [name preferred-name public-key photo-path]}
        (:multiaccount db)]
    (reduce
     (fn [acc [key {:keys [alias name identicon]}]]
       (let [name (utils/safe-replace name ".stateofus.eth" "")]
         (assoc acc key
                {:alias      alias
                 :name       (or name alias)
                 :identicon  identicon
                 :public-key key})))
     (assoc chat-specific-suggestions
            public-key
            {:alias      name
             :name       (or preferred-name name)
             :identicon  photo-path
             :public-key public-key})
     contacts)))

(def ending-chars "[\\s\\.,;:]")
(def ending-chars-regex (re-pattern ending-chars))
(def word-regex (re-pattern (str "^[\\w\\d]*" ending-chars "|^[\\S]*$")))

(defn mentioned? [{:keys [alias name]} text]
  (let [lcase-name  (string/lower-case name)
        lcase-alias (string/lower-case alias)
        regex       (re-pattern
                     (string/join
                      "|"
                      [(str "^" lcase-name ending-chars)
                       (str "^" lcase-name "$")
                       (str "^" lcase-alias ending-chars)
                       (str "^" lcase-alias "$")]))
        lcase-text  (string/lower-case text)]
    (re-find regex lcase-text)))

(defn get-suggestions [users searched-text]
  (reduce
   (fn [acc [k {:keys [alias name] :as user}]]
     (if-let [match
              (cond
                (and alias
                     (string/starts-with?
                      (string/lower-case alias)
                      searched-text))
                alias

                (string/starts-with?
                 (string/lower-case name)
                 searched-text)
                name)]
       (assoc acc k (assoc user
                           :match match
                           :searched-text searched-text))
       acc))
   {}
   users))

(defn match-mention
  ([text users mention-key-idx]
   (match-mention text users mention-key-idx (inc mention-key-idx) []))
  ([text users mention-key-idx next-word-idx words]
   (when-let [word (re-find word-regex (subs text next-word-idx))]
     (let [new-words       (conj words word)
           searched-text   (let [text      (-> new-words
                                               string/join
                                               string/lower-case
                                               string/trim)
                                 last-char (dec (count text))]
                             (if (re-matches ending-chars-regex (str (nth text last-char nil)))
                               (subs text 0 last-char)
                               text))
           suggestions     (get-suggestions users searched-text)
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
                   next-word-start (+ next-word-idx word-len)]
               (when (> text-len next-word-start)
                 (match-mention text users mention-key-idx
                                next-word-start new-words))))))))

(defn replace-mentions
  ([text users-fn]
   (replace-mentions text users-fn 0))
  ([text users-fn idx]
   (if (string/blank? text)
     text
     (let [mention-key-idx (string/index-of text at-sign idx)]
       (if-not mention-key-idx
         text
         (let [users (users-fn)]
           (if-not (seq users)
             text
             (let [{:keys [public-key match]}
                   (match-mention text users mention-key-idx)]
               (if-not match
                 (recur text (fn [] users) (inc mention-key-idx))
                 (let [new-text (string/join
                                 [(subs text 0 (inc mention-key-idx))
                                  public-key
                                  (subs text (+ (inc mention-key-idx)
                                                (count match)))])
                       mention-end (+ (inc mention-key-idx) (count public-key))]
                   (recur new-text (fn [] users) mention-end)))))))))))

(defn check-mentions [cofx text]
  (replace-mentions text #(get-mentionable-users cofx)))

(defn check-for-at-sign
  ([text]
   (check-for-at-sign text 0 0))
  ([text from cnt]
   (if-let [idx (string/index-of text at-sign from)]
     (recur text (inc idx) (inc cnt))
     cnt)))

(defn at-sign-change [previous-text new-text]
  (cond
    (= "" previous-text)
    (check-for-at-sign new-text)

    (= "" new-text)
    (- (check-for-at-sign previous-text))

    :else
    (- (check-for-at-sign new-text)
       (check-for-at-sign previous-text))))

(defn get-at-sign-idxs
  ([text start]
   (get-at-sign-idxs text start 0 []))
  ([text start from idxs]
   (if-let [idx (string/index-of text at-sign from)]
     (recur text start (inc idx) (conj idxs (+ start idx)))
     idxs)))

(defn calc-at-idxs
  [{:keys [at-idxs new-text previous-text start]}]
  (let [new-idxs     (get-at-sign-idxs new-text start)
        new-idx-cnt  (count new-idxs)
        last-new-idx (when (pos? new-idx-cnt)
                       (nth new-idxs (dec new-idx-cnt)))
        new-text-len (count new-text)
        old-text-len (count previous-text)
        old-end      (+ start old-text-len)]
    (if-not (seq at-idxs)
      (map (fn [idx]
             {:from     idx
              :checked? false})
           new-idxs)
      (let [diff (- new-text-len old-text-len)
            {:keys [state added?]}
            (->> at-idxs
                 (keep (fn [{:keys [from to] :as entry}]
                         (let [to+1 (inc to)]
                           (cond
                             ;; starts after change
                             (>= from old-end)
                             (assoc entry
                                    :from (+ from diff)
                                    :to (+ to diff))

                             ;; starts and end before change
                             (and
                              (< from start)
                              (or
                               ;; is not checked yet
                               (not to+1)
                               (< to+1 start)))
                             entry

                             ;; starts before change intersects with it 
                             (and (< from start)
                                  (>= to+1 start))
                             {:from     from
                              :checked? false}

                             ;; starts in changed part of text
                             :else nil))))
                 (reduce
                  (fn [{:keys [state added?] :as acc} {:keys [from] :as entry}]
                    (if (and last-new-idx
                             (> from last-new-idx)
                             (not added?))
                      {:state  (conj
                                (into state (map (fn [idx]
                                                   {:from     idx
                                                    :checked? false})
                                                 new-idxs))
                                entry)
                       :added? true}
                      (update acc :state conj entry)))
                  {:state []}))]
        (if added?
          state
          (into state (map (fn [idx]
                             {:from     idx
                              :checked? false})
                           new-idxs)))))))

(defn check-entry
  [text {:keys [from checked?] :as entry} users-fn]
  (if checked?
    entry
    (let [{:keys [match]}
          (match-mention (str text "@") (users-fn) from)]
      (if match
        {:from from
         :to (+ from (count match))
         :checked? true
         :mention? true}
        {:from from
         :to (count text)
         :checked? true
         :mention false}))))

(defn check-idx-for-mentions [text idxs users-fn]
  (let [idxs
        (reduce
         (fn [acc {:keys [from] :as entry}]
           (let [previous-entry-idx (dec (count acc))
                 new-entry          (check-entry text entry users-fn)]
             (cond-> acc
               (and (>= previous-entry-idx 0)
                    (not (get-in acc [previous-entry-idx :mention?])))
               (assoc-in [previous-entry-idx :to] (dec from))

               (>= previous-entry-idx 0)
               (assoc-in [previous-entry-idx :next-at-idx] from)

               :always
               (conj (dissoc new-entry :next-at-idx)))))
         []
         idxs)]
    (when (seq idxs)
      (let [last-idx (dec (count idxs))]
        (if (get-in idxs [last-idx :mention?])
          idxs
          (-> idxs
              (assoc-in [last-idx :to] (dec (count text)))
              (assoc-in [last-idx :checked?] false)))))))

(fx/defn on-text-input
  {:events [::on-text-input]}
  [{:keys [db] :as cofx} {:keys [new-text previous-text start end] :as args}]
  (log/debug "[mentions] on-text-input args" args)
  (let [normalized-previous-text
        ;; NOTE(rasom): on iOS `previous-text` contains entire input's text. To
        ;; get only removed part of text we have cut it.
        (if platform/android?
          previous-text
          (subs previous-text start end))
        chat-id        (:current-chat-id db)
        change         (at-sign-change normalized-previous-text new-text)
        previous-state (get-in db [:chats chat-id :mentions])
        text     (get-in db [:chat/inputs chat-id :input-text])
        new-state (-> previous-state
                      (update :at-sign-counter + change)
                      (merge args)
                      (assoc :previous-text normalized-previous-text))
        old-at-idxs (:at-idxs new-state)
        new-at-idxs (calc-at-idxs new-state)
        new-state (assoc new-state :at-idxs new-at-idxs)]
    (log/debug "[mentions] on-text-input state" new-state)
    {:db (assoc-in db [:chats chat-id :mentions] new-state)}))

(defn calculate-input [text [first-idx :as idxs]]
  (if-not first-idx
    [[:text text]]
    (let [idx-cnt (count idxs)
          last-from (get-in idxs [(dec idx-cnt) :from])]
      (reduce
       (fn [acc {:keys [from to next-at-idx mention?]}]
         (cond
           (and mention? next-at-idx)
           (into acc [[:mention (subs text from (inc to))]
                      [:text (subs text (inc to) next-at-idx)]])

           (and mention? (= last-from from))
           (into acc [[:mention (subs text from (inc to))]
                      [:text (subs text (inc to))]])

           :else
           (conj acc [:text (subs text from (inc to))])))
       (let [first-from (:from first-idx)]
         (if (zero? first-from)
           []
           [[:text (subs text 0 first-from)]]))
       idxs))))

(fx/defn recheck-at-idxs
  [{:keys [db]} mentionable-users]
  (let [chat-id  (:current-chat-id db)
        text     (get-in db [:chat/inputs chat-id :input-text])
        {:keys [new-text at-sign-counter start end] :as state}
        (get-in db [:chats chat-id :mentions])
        new-at-idxs (check-idx-for-mentions
                     text
                     (:at-idxs state)
                     (fn [] mentionable-users))
        calculated-input (calculate-input text new-at-idxs)]
    (log/debug "[mentions] new-at-idxs" new-at-idxs calculated-input)
    {:db (-> db
             (update-in
              [:chats chat-id :mentions]
              assoc
              :at-idxs new-at-idxs)
             (assoc-in [:chats/input-with-mentions chat-id] calculated-input))}))

(fx/defn calculate-suggestion
  {:events [::calculate-suggestions]}
  [{:keys [db] :as cofx} mentionable-users]
  (let [chat-id  (:current-chat-id db)
        text     (get-in db [:chat/inputs chat-id :input-text])
        {:keys [new-text at-sign-counter start end] :as state}
        (get-in db [:chats chat-id :mentions])
        new-text (or new-text text)]
    (log/debug "[mentions] calculate suggestions"
               "state" state)
    (if-not (pos? at-sign-counter)
      {:db (-> db
               (assoc-in [:chats/mention-suggestions chat-id] nil)
               (assoc-in [:chats chat-id :mentions :at-idxs] nil)
               (assoc-in [:chats/input-with-mentions chat-id] [[:text text]]))}
      (let [new-at-idxs (check-idx-for-mentions
                         text
                         (:at-idxs state)
                         (fn [] mentionable-users))
            calculated-input (calculate-input text new-at-idxs)
            addition?     (<= start end)
            end           (if addition?
                            (+ start (count new-text))
                            start)
            at-sign-idx   (string/last-index-of text at-sign start)
            searched-text (string/lower-case (subs text (inc at-sign-idx) end))
            mentions
            (when (and (not (> at-sign-idx start))
                       (not (> (- end at-sign-idx) 100)))
              (get-suggestions mentionable-users searched-text))]
        (log/debug "[mentions] mention detected"
                   "addition" addition?
                   "end" end
                   "searched-text" (pr-str searched-text)
                   "mentions" (count mentions))
        (log/debug "[mentions] new-at-idxs" new-at-idxs calculated-input)
        {:db (-> db
                 (update-in [:chats chat-id :mentions]
                            assoc
                            :at-sign-idx at-sign-idx
                            :at-idxs new-at-idxs
                            :mention-end end)
                 (assoc-in [:chats/input-with-mentions chat-id] calculated-input)
                 (assoc-in [:chats/mention-suggestions chat-id] mentions))}))))

(defn new-input-text-with-mention
  [{:keys [db]} {:keys [name]}]
  (let [chat-id (:current-chat-id db)
        text    (get-in db [:chat/inputs chat-id :input-text])
        {:keys [mention-end at-sign-idx]}
        (get-in db [:chats chat-id :mentions])]
    (log/debug "[mentions] clear suggestions"
               "state" new-input-text-with-mention)
    (string/join
     [(subs text 0 (inc at-sign-idx))
      name
      (let [next-char (get text mention-end)]
        (when (or (not next-char)
                  (and next-char
                       (not (re-matches #"\s" next-char))))
          " "))
      (subs text mention-end)])))

(fx/defn clear-suggestions
  [{:keys [db]}]
  (log/debug "[mentions] clear suggestions")
  (let [chat-id (:current-chat-id db)]
    {:db (-> db
             (update-in [:chats chat-id] dissoc :mentions)
             (update :chats/input-with-mentions dissoc chat-id)
             (update :chats/mention-suggestions dissoc chat-id))}))

(fx/defn clear-cursor
  {:events [::clear-cursor]}
  [{:keys [db]}]
  (log/debug "[mentions] clear cursor")
  {:db
   (update db :chats/cursor dissoc (:current-chat-id db))})
