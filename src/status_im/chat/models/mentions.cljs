(ns status-im.chat.models.mentions
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.contact.db :as contact.db]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]
            [status-im.utils.utils :as utils]
            [status-im.native-module.core :as status]
            [quo.react-native :as rn]
            [quo.react :as react]))

(def at-sign "@")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn re-pos [re s]
  (loop [res [] s s last-idx 0]
    (if-let [m (.exec re s)]
      (let [new-idx (.-index m)
            idx     (+ last-idx new-idx)
            c       (get m 0)]
        (recur (conj res [idx c]) (subs s (inc new-idx)) (inc idx)))
      res)))

(defn check-style-tag [text idxs idx]
  (let [[pos c]       (get idxs idx)
        [pos2 c2]     (get idxs (inc idx))
        [pos3 c3]     (get idxs (+ 2 idx))
        prev-c        (get text (dec pos))
        len           (cond
                        (and (= c c2 c3)
                             (= pos (dec pos2) (- pos3 2)))
                        3
                        (and (= c c2)
                             (= pos (dec pos2)))
                        2
                        :else 1)
        next-idx      (inc (first (get idxs (+ idx (dec len)))))
        next-c        (get text next-idx)
        can-be-end?   (if (= 1 len)
                        (and prev-c
                             (not (string/blank? prev-c))
                             (or (nil? next-c)
                                 (string/blank? next-c)))
                        (and prev-c
                             (not (string/blank? prev-c))))
        can-be-start? (and next-c
                           (not (string/blank? next-c)))]
    [len can-be-start? can-be-end?]))

(defn clear-pending-at-signs
  [data from]
  (let [{:keys [pending]} (get data at-sign)
        new-idxs          (filter (partial > from) pending)]
    (-> data
        (update at-sign dissoc :pending)
        (update-in [at-sign :checked]
                   (fn [checked]
                     ;; NOTE(rasom): there might be stack overflow without doall
                     (doall
                      ((fnil concat []) checked new-idxs)))))))

(defn apply-style-tag [data idx pos c len start? end?]
  (let [was-started?   (get data c)
        tripple-tilde? (and (= "~" c) (= 3 len))]
    (cond
      (and was-started? end?)
      (let [old-len (:len was-started?)
            tag-len (cond
                      (and tripple-tilde?
                           (= 3 old-len))
                      2

                      (>= old-len len)
                      len

                      :else
                      old-len)
            old-idx (:idx was-started?)]
        {:data     (-> data
                       (dissoc c)
                       (clear-pending-at-signs old-idx))
         :next-idx (+ idx tag-len)})

      start?
      {:data     (-> data
                     (assoc c {:len len
                               :idx pos})
                     (clear-pending-at-signs pos))
       :next-idx (+ idx len)}

      :else
      {:data     data
       :next-idx (+ idx len)})))

(defn code-tag-len [idxs idx]
  (let [[pos c]   (get idxs idx)
        [pos2 c2] (get idxs (inc idx))
        [pos3 c3] (get idxs (+ 2 idx))]
    (cond
      (and (= c c2 c3)
           (= pos (dec pos2) (- pos3 2)))
      3
      (and (= c c2)
           (= pos (dec pos2)))
      2
      :else 1)))

(defn get-at-signs
  ([text]
   (let [idxs (re-pos #"[@~\\*_\n>`]{1}" text)]
     (loop [data nil
            idx  0]
       (let [quote-started? (get data ">")
             [pos c]        (get idxs idx)
             styling-tag?   (get #{"*" "_" "~"} c)
             code-tag?      (= "`" c)
             quote?         (= ">" c)
             at-sign?       (= at-sign c)
             newline?       (= "\n" c)]
         (if (nil? c)
           (let [{:keys [checked pending]} (get data at-sign)]
             (concat checked pending))
           (cond
             newline?
             (let [prev-newline (first (get data :newline))]
               (recur
                (cond-> (update data :newline (fnil conj '()) pos)
                  (and quote-started?
                       prev-newline
                       (string/blank? (subs text prev-newline (dec pos))))
                  (dissoc ">"))
                (inc idx)))

             quote-started?
             (recur data (inc idx))

             quote?
             (let [prev-newlines (take 2 (get data :newline))]
               (if (or (zero? pos)
                       (and (= 1 (count prev-newlines))
                            (string/blank? (subs text 0 (dec pos))))
                       (and (= 2 (count prev-newlines))
                            (string/blank? (subs text (first prev-newlines) (dec pos)))))
                 (recur (-> data
                            (dissoc :newline "*" "_" "~" "`")
                            (assoc ">" {:idx pos})) (inc idx))
                 (recur data (inc idx))))

             at-sign?
             (recur (update-in data [at-sign :pending] (fnil conj []) pos)
                    (inc idx))

             code-tag?
             (let [len (code-tag-len idxs idx)
                   {:keys [data next-idx]}
                   (apply-style-tag data idx pos c len true true)]
               (recur data next-idx))

             styling-tag?
             (let [[len can-be-start? can-be-end?]
                   (check-style-tag text idxs idx)
                   {:keys [data next-idx]}
                   (apply-style-tag data idx pos c len can-be-start? can-be-end?)]
               (recur data next-idx))

             :else (recur data (inc idx)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
        {:keys [name preferred-name public-key]}
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

(defn get-user-suggestions [users searched-text]
  (reduce
   (fn [acc [k {:keys [alias name nickname searchable-phrases] :as user}]]
     (if-let [match
              (if (seq searchable-phrases)
                (when (some
                       (fn [s]
                         (string/starts-with?
                          (string/lower-case s)
                          searched-text))
                       searchable-phrases)
                  (or name alias))
                (cond
                  (and nickname
                       (string/starts-with?
                        (string/lower-case nickname)
                        searched-text))
                  (or name alias)

                  (and alias
                       (string/starts-with?
                        (string/lower-case alias)
                        searched-text))
                  alias

                  (string/starts-with?
                   (string/lower-case name)
                   searched-text)
                  name))]
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
     (let [new-words            (conj words word)
           searched-text        (let [text      (-> new-words
                                                    string/join
                                                    string/lower-case
                                                    string/trim)
                                      last-char (dec (count text))]
                                  (if (re-matches ending-chars-regex (str (nth text last-char nil)))
                                    (subs text 0 last-char)
                                    text))
           user-suggestions     (get-user-suggestions users searched-text)
           user-suggestions-cnt (count user-suggestions)]
       (cond (zero? user-suggestions-cnt)
             nil

             (and (= 1 user-suggestions-cnt)
                  (mentioned? (second (first user-suggestions))
                              (subs text (inc mention-key-idx))))
             (second (first user-suggestions))

             (> user-suggestions-cnt 1)
             (let [word-len        (count word)
                   text-len        (count text)
                   next-word-start (+ next-word-idx word-len)]
               (when (> text-len next-word-start)
                 (match-mention text users mention-key-idx
                                next-word-start new-words))))))))

(defn replace-mentions
  ([text users]
   (let [idxs (get-at-signs text)]
     (replace-mentions text users idxs 0)))
  ([text users idxs diff]
   (if (or (string/blank? text)
           (empty? idxs))
     text
     (let [mention-key-idx (- (first idxs) diff)]
       (if-not mention-key-idx
         text
         (if-not (seq users)
           text
           (let [{:keys [public-key match]}
                 (match-mention text users mention-key-idx)]
             (if-not match
               (recur text users (rest idxs) diff)
               (let [new-text (string/join
                               [(subs text 0 (inc mention-key-idx))
                                public-key
                                (subs text (+ (inc mention-key-idx)
                                              (count match)))])]
                 (recur new-text users (rest idxs)
                        (+ diff (- (count text) (count new-text)))))))))))))

(defn check-mentions [cofx text]
  (replace-mentions text (get-mentionable-users cofx)))

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
  [text {:keys [from checked?] :as entry} mentionable-users]
  (if checked?
    entry
    (let [{user-match :match}
          (match-mention (str text "@") mentionable-users from)]
      (if user-match
        {:from from
         :to (+ from (count user-match))
         :checked? true
         :mention? true}
        {:from from
         :to (count text)
         :checked? true
         :mention false}))))

(defn check-idx-for-mentions
  [text idxs mentionable-users]
  (let [idxs
        (reduce
         (fn [acc {:keys [from] :as entry}]
           (let [previous-entry-idx (dec (count acc))
                 new-entry          (check-entry text entry mentionable-users)]
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
        previous-state (get-in db [:chats chat-id :mentions])
        text     (get-in db [:chat/inputs chat-id :input-text])
        new-state (-> previous-state
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
        {:keys [new-text start end] :as state}
        (get-in db [:chats chat-id :mentions])
        new-at-idxs (check-idx-for-mentions
                     text
                     (:at-idxs state)
                     mentionable-users)
        calculated-input (calculate-input text new-at-idxs)]
    (log/debug "[mentions] new-at-idxs" new-at-idxs calculated-input)
    {:db (-> db
             (update-in
              [:chats chat-id :mentions]
              assoc
              :at-idxs new-at-idxs)
             (assoc-in [:chat/inputs-with-mentions chat-id] calculated-input))}))

(fx/defn calculate-suggestions
  {:events [::calculate-suggestions]}
  [{:keys [db] :as cofx} mentionable-users]
  (let [chat-id  (:current-chat-id db)
        text     (get-in db [:chat/inputs chat-id :input-text])
        {:keys [new-text at-idxs start end] :as state}
        (get-in db [:chats chat-id :mentions])
        new-text (or new-text text)]
    (log/debug "[mentions] calculate suggestions"
               "state" state)
    (if-not (seq at-idxs)
      {:db (-> db
               (assoc-in [:chats/mention-suggestions chat-id] nil)
               (assoc-in [:chats chat-id :mentions :at-idxs] nil)
               (assoc-in [:chat/inputs-with-mentions chat-id] [[:text text]]))}
      (let [new-at-idxs (check-idx-for-mentions
                         text
                         at-idxs
                         mentionable-users)
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
              (get-user-suggestions mentionable-users searched-text))]
        (log/debug "[mentions] mention check"
                   "addition" addition?
                   "at-sign-idx" at-sign-idx
                   "start" start
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
                 (assoc-in [:chat/inputs-with-mentions chat-id] calculated-input)
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
    {:db (update db :chats/mention-suggestions dissoc chat-id)}))

(fx/defn clear-mentions
  [{:keys [db] :as cofx}]
  (log/debug "[mentions] clear mentions")
  (let [chat-id (:current-chat-id db)]
    (fx/merge
     cofx
     {:db (-> db
              (update-in [:chats chat-id] dissoc :mentions)
              (update :chat/inputs-with-mentions dissoc chat-id))}
     (clear-suggestions))))

(fx/defn clear-cursor
  {:events [::clear-cursor]}
  [{:keys [db]}]
  (log/debug "[mentions] clear cursor")
  {:db
   (update db :chats/cursor dissoc (:current-chat-id db))})

(fx/defn check-selection
  {:events [::on-selection-change]}
  [{:keys [db] :as cofx}
   {:keys [start end] :as selection}
   mentionable-users]
  (let [chat-id (:current-chat-id db)
        {:keys [mention-end at-idxs]}
        (get-in db [:chats chat-id :mentions])]
    (when (seq at-idxs)
      (if (some
           (fn [{:keys [from to] :as idx}]
             (when (and (not (< start from))
                        (<= (dec end) to))
               idx))
           at-idxs)
        (fx/merge
         cofx
         {:db (update-in db [:chats chat-id :mentions]
                         assoc
                         :start end
                         :end end
                         :new-text "")}
         (calculate-suggestions mentionable-users))
        (clear-suggestions cofx)))))

(re-frame/reg-fx
 ::reset-text-input-cursor
 (fn [[ref cursor]]
   (when ref
     (status/reset-keyboard-input
      (rn/find-node-handle (react/current-ref ref))
      cursor))))

(fx/defn reset-text-input-cursor
  [_ ref cursor]
  {::reset-text-input-cursor [ref cursor]})

(defn add-searchable-phrases
  [{:keys [alias name nickname] :as user}]
  (reduce
   (fn [user s]
     (if (nil? s)
       user
       (let [new-words (concat
                        [s]
                        (rest (string/split s " ")))]
         (update user :searchable-phrases (fnil concat []) new-words))))
   user
   [alias name nickname]))
