(ns status-im.chat.models.mentions
  (:require [clojure.string :as string]
            [quo.react :as react]
            [quo.react-native :as rn]
            [re-frame.core :as re-frame]
            [status-im2.constants :as constants]
            [status-im.contact.db :as contact.db]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.native-module.core :as status]
            [utils.re-frame :as rf]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

(def at-sign "@")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn re-pos
  [re s]
  (loop [res      []
         s        s
         last-idx 0]
    (if-let [m (.exec re s)]
      (let [new-idx (.-index m)
            idx     (+ last-idx new-idx)
            c       (get m 0)]
        (recur (conj res [idx c]) (subs s (inc new-idx)) (inc idx)))
      res)))

(defn check-style-tag
  [text idxs idx]
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

(defn apply-style-tag
  [data idx pos c len start? end?]
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
                     (assoc c
                            {:len len
                             :idx pos})
                     (clear-pending-at-signs pos))
       :next-idx (+ idx len)}

      :else
      {:data     data
       :next-idx (+ idx len)})))

(defn code-tag-len
  [idxs idx]
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
                            (assoc ">" {:idx pos}))
                        (inc idx))
                 (recur data (inc idx))))

             at-sign?
             (recur (update-in data [at-sign :pending] (fnil conj []) pos)
                    (inc idx))

             code-tag?
             (let [len                     (code-tag-len idxs idx)
                   {:keys [data next-idx]}
                   (apply-style-tag data idx pos c len true true)]
               (recur data next-idx))

             styling-tag?
             (let [[len can-be-start? can-be-end?]
                   (check-style-tag text idxs idx)
                   {:keys [data next-idx]}
                   (apply-style-tag data idx pos c len can-be-start? can-be-end?)]
               (recur data next-idx))

             :else          (recur data (inc idx)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

(defn add-searchable-phrases-to-contact
  [{:keys [alias name added? blocked? identicon public-key nickname ens-verified]} community-chat?]
  (when (and alias
             (not (string/blank? alias))
             (or name
                 nickname
                 added?
                 community-chat?)
             (not blocked?))
    (add-searchable-phrases
     {:alias        alias
      :name         (or (and ens-verified (utils/safe-replace name ".stateofus.eth" "")) alias)
      :identicon    identicon
      :nickname     nickname
      :ens-verified ens-verified
      :public-key   public-key})))

(defn mentionable-contacts
  [contacts]
  (reduce
   (fn [acc [key contact]]
     (let [mentionable-contact (add-searchable-phrases-to-contact contact false)]
       (if (nil? mentionable-contact)
         acc
         (assoc acc key mentionable-contact))))
   {}
   contacts))

(defn mentionable-contacts-from-identites
  [contacts my-public-key identities]
  (reduce (fn [acc identity]
            (let [contact             (multiaccounts/contact-by-identity
                                       contacts
                                       identity)
                  contact             (if (string/blank? (:alias contact))
                                        (assoc contact
                                               :alias
                                               (get-in contact [:names :three-words-name]))
                                        contact)
                  mentionable-contact (add-searchable-phrases-to-contact
                                       contact
                                       true)]
              (if (nil? mentionable-contact)
                acc
                (assoc acc identity mentionable-contact))))
          {}
          (remove #(= my-public-key %) identities)))

(defn get-mentionable-users
  [chat all-contacts current-multiaccount community-members]
  (let [{:keys [name preferred-name public-key]}   current-multiaccount
        {:keys [chat-id users contacts chat-type]} chat
        mentionable-contacts                       (mentionable-contacts all-contacts)
        mentionable-users                          (assoc users
                                                          public-key
                                                          {:alias      name
                                                           :name       (or preferred-name name)
                                                           :public-key public-key})]
    (cond
      (= chat-type constants/private-group-chat-type)
      (merge mentionable-users
             (mentionable-contacts-from-identites all-contacts public-key contacts))

      (= chat-type constants/one-to-one-chat-type)
      (assoc mentionable-users
             chat-id
             (get mentionable-contacts
                  chat-id
                  (-> chat-id
                      contact.db/public-key->new-contact
                      contact.db/enrich-contact)))

      (= chat-type constants/community-chat-type)
      (mentionable-contacts-from-identites
       all-contacts
       public-key
       (distinct (concat (keys community-members) (keys mentionable-users))))

      (= chat-type constants/public-chat-type)
      (merge mentionable-users (select-keys mentionable-contacts (keys mentionable-users)))

      :else                                           mentionable-users)))

(def ending-chars "[\\s\\.,;:]")
(def ending-chars-regex (re-pattern ending-chars))
(def word-regex (re-pattern (str "^[\\w\\d]*" ending-chars "|^[\\S]*$")))

(defn mentioned?
  [{:keys [alias name]} text]
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

(defn get-user-suggestions
  [users searched-text]
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
       (assoc acc
              k
              (assoc user
                     :match         match
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
       (cond
         (zero? user-suggestions-cnt)
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
             (match-mention text
                            users
                            mention-key-idx
                            next-word-start
                            new-words))))))))

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
                                (subs text
                                      (+ (inc mention-key-idx)
                                         (count match)))])]
                 (recur new-text
                        users
                        (rest idxs)
                        (+ diff (- (count text) (count new-text)))))))))))))

(defn check-mentions
  [{:keys [db]} text]
  (let [current-chat-id      (:current-chat-id db)
        chat                 (get-in db [:chats current-chat-id])
        all-contacts         (:contacts/contacts db)
        current-multiaccount (:multiaccount db)
        community-members    (get-in db [:communities (:community-id chat) :members])
        mentionable-users    (get-mentionable-users chat
                                                    all-contacts
                                                    current-multiaccount
                                                    community-members)]
    (replace-mentions text mentionable-users)))

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
      (let [diff                   (- new-text-len old-text-len)
            {:keys [state added?]}
            (->> at-idxs
                 (keep (fn [{:keys [from to] :as entry}]
                         (let [to+1 (inc to)]
                           (cond
                             ;; starts after change
                             (>= from old-end)
                             (assoc entry
                                    :from (+ from diff)
                                    :to   (+ to diff))

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
                                (into state
                                      (map (fn [idx]
                                             {:from     idx
                                              :checked? false})
                                           new-idxs))
                                entry)
                       :added? true}
                      (update acc :state conj entry)))
                  {:state []}))]
        (if added?
          state
          (into state
                (map (fn [idx]
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
        {:from     from
         :to       (+ from (count user-match))
         :checked? true
         :mention? true}
        {:from     from
         :to       (count text)
         :checked? true
         :mention  false}))))

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

(rf/defn on-text-input
  {:events [::on-text-input]}
  [{:keys [db]} {:keys [previous-text start end] :as args}]
  (log/debug "[mentions] on-text-input args" args)
  (let [normalized-previous-text
        ;; NOTE(rasom): on iOS `previous-text` contains entire input's text. To
        ;; get only removed part of text we have cut it.
        (if platform/android?
          previous-text
          (subs previous-text start end))
        chat-id                  (:current-chat-id db)
        previous-state           (get-in db [:chats/mentions chat-id :mentions])
        new-state                (-> previous-state
                                     (merge args)
                                     (assoc :previous-text normalized-previous-text))
        new-at-idxs              (calc-at-idxs new-state)
        new-state                (assoc new-state :at-idxs new-at-idxs)]
    (log/debug "[mentions] on-text-input state" new-state)
    {:db (assoc-in db [:chats/mentions chat-id :mentions] new-state)}))

(defn calculate-input
  [text [first-idx :as idxs]]
  (if-not first-idx
    [[:text text]]
    (let [idx-cnt   (count idxs)
          last-from (get-in idxs [(dec idx-cnt) :from])]
      (reduce
       (fn [acc {:keys [from to next-at-idx mention?]}]
         (cond
           (and mention? next-at-idx)
           (into acc
                 [[:mention (subs text from (inc to))]
                  [:text (subs text (inc to) next-at-idx)]])

           (and mention? (= last-from from))
           (into acc
                 [[:mention (subs text from (inc to))]
                  [:text (subs text (inc to))]])

           :else
           (conj acc [:text (subs text from (inc to))])))
       (let [first-from (:from first-idx)]
         (if (zero? first-from)
           []
           [[:text (subs text 0 first-from)]]))
       idxs))))

(rf/defn recheck-at-idxs
  [{:keys [db]} mentionable-users]
  (let [chat-id          (:current-chat-id db)
        text             (get-in db [:chat/inputs chat-id :input-text])
        state            (get-in db [:chats/mentions chat-id :mentions])
        new-at-idxs      (check-idx-for-mentions
                          text
                          (:at-idxs state)
                          mentionable-users)
        calculated-input (calculate-input text new-at-idxs)]
    (log/debug "[mentions] new-at-idxs" new-at-idxs calculated-input)
    {:db (-> db
             (update-in
              [:chats/mentions chat-id :mentions]
              assoc
              :at-idxs
              new-at-idxs)
             (assoc-in [:chat/inputs-with-mentions chat-id] calculated-input))}))

(rf/defn calculate-suggestions
  {:events [::calculate-suggestions]}
  [{:keys [db]} mentionable-users]
  (let [chat-id                                        (:current-chat-id db)
        text                                           (get-in db [:chat/inputs chat-id :input-text])
        {:keys [new-text at-idxs start end] :as state}
        (get-in db [:chats/mentions chat-id :mentions])
        new-text                                       (or new-text text)]
    (log/debug "[mentions] calculate suggestions"
               "state"
               state)
    (if-not (seq at-idxs)
      {:db (-> db
               (assoc-in [:chats/mention-suggestions chat-id] nil)
               (assoc-in [:chats/mentions chat-id :mentions :at-idxs] nil)
               (assoc-in [:chat/inputs-with-mentions chat-id] [[:text text]]))}
      (let [new-at-idxs      (check-idx-for-mentions
                              text
                              at-idxs
                              mentionable-users)
            calculated-input (calculate-input text new-at-idxs)
            addition?        (<= start end)
            end              (if addition?
                               (+ start (count new-text))
                               start)
            at-sign-idx      (string/last-index-of text at-sign start)
            searched-text    (string/lower-case (subs text (inc at-sign-idx) end))
            mentions
            (when (and (not (> at-sign-idx start))
                       (not (> (- end at-sign-idx) 100)))
              (get-user-suggestions mentionable-users searched-text))]
        (log/debug "[mentions] mention check"
                   "addition"      addition?
                   "at-sign-idx"   at-sign-idx
                   "start"         start
                   "end"           end
                   "searched-text" (pr-str searched-text)
                   "mentions"      (count mentions))
        (log/debug "[mentions] new-at-idxs" new-at-idxs calculated-input)
        {:db (-> db
                 (update-in [:chats/mentions chat-id :mentions]
                            assoc
                            :at-sign-idx at-sign-idx
                            :at-idxs     new-at-idxs
                            :mention-end end)
                 (assoc-in [:chat/inputs-with-mentions chat-id] calculated-input)
                 (assoc-in [:chats/mention-suggestions chat-id] mentions))}))))

(defn new-input-text-with-mention
  [{:keys [db]} {:keys [name]}]
  (let [chat-id                           (:current-chat-id db)
        text                              (get-in db [:chat/inputs chat-id :input-text])
        {:keys [mention-end at-sign-idx]}
        (get-in db [:chats/mentions chat-id :mentions])]
    (log/debug "[mentions] clear suggestions"
               "state"
               new-input-text-with-mention)
    (string/join
     [(subs text 0 (inc at-sign-idx))
      name
      (let [next-char (get text mention-end)]
        (when (or (not next-char)
                  (and next-char
                       (not (re-matches #"\s" next-char))))
          " "))
      (subs text mention-end)])))

(rf/defn clear-suggestions
  [{:keys [db]}]
  (log/debug "[mentions] clear suggestions")
  (let [chat-id (:current-chat-id db)]
    {:db (update db :chats/mention-suggestions dissoc chat-id)}))

(rf/defn clear-mentions
  [{:keys [db] :as cofx}]
  (log/debug "[mentions] clear mentions")
  (let [chat-id (:current-chat-id db)]
    (rf/merge
     cofx
     {:db (-> db
              (update-in [:chats/mentions chat-id] dissoc :mentions)
              (update :chat/inputs-with-mentions dissoc chat-id))}
     (clear-suggestions))))

(rf/defn clear-cursor
  {:events [::clear-cursor]}
  [{:keys [db]}]
  (log/debug "[mentions] clear cursor")
  {:db
   (update db :chats/cursor dissoc (:current-chat-id db))})

(rf/defn check-selection
  {:events [::on-selection-change]}
  [{:keys [db] :as cofx}
   {:keys [start end] :as selection}
   mentionable-users]
  (let [chat-id                       (:current-chat-id db)
        {:keys [mention-end at-idxs]}
        (get-in db [:chats/mentions chat-id :mentions])]
    (when (seq at-idxs)
      (if (some
           (fn [{:keys [from to] :as idx}]
             (when (and (not (< start from))
                        (<= (dec end) to))
               idx))
           at-idxs)
        (rf/merge
         cofx
         {:db (update-in db
                         [:chats/mentions chat-id :mentions]
                         assoc
                         :start    end
                         :end      end
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

(rf/defn reset-text-input-cursor
  [_ ref cursor]
  {::reset-text-input-cursor [ref cursor]})

(defn is-valid-terminating-character?
  [c]
  (case c
    "\t" true ; tab
    "\n" true ; newline
    "\f" true ; new page
    "\r" true ; carriage return
    " "  true ; whitespace
    ","  true
    "."  true
    ":"  true
    ";"  true
    false))

(def hex-reg #"[0-9a-f]")

(defn is-public-key-character?
  [c]
  (.test hex-reg c))

(def mention-length 133)

(defn ->input-field
  "->input-field takes a string with mentions in the @0xpk format
  and retuns a list in the format
  [{:type :text :text text} {:type :mention :text 0xpk}...]"
  [text]
  (let [{:keys [text
                current-mention-length
                current-text
                current-mention]}
        (reduce (fn [{:keys [text
                             current-text
                             current-mention
                             current-mention-length]} character]
                  (let [is-pk-character          (is-public-key-character? character)
                        is-termination-character (is-valid-terminating-character? character)]
                    (cond
                      ;; It's a valid mention.
                      ;; Add any text that is before if present
                      ;; and add the mention.
                      ;; Set the text to the new termination character
                      (and (= current-mention-length mention-length)
                           is-termination-character)
                      {:current-mention-length 0
                       :current-mention        ""
                       :current-text           character
                       :text                   (cond-> text
                                                 (seq current-text)
                                                 (conj [:text current-text])
                                                 :always
                                                 (conj [:mention current-mention]))}


                      ;; It's either a pk character, or the `x` in the pk
                      ;; in this case add the text to the mention and continue


                      (or
                       (and is-pk-character
                            (pos? current-mention-length))
                       (and (= 2 current-mention-length)
                            (= "x" character)))
                      {:current-mention-length (inc current-mention-length)
                       :current-text           current-text
                       :current-mention        (str current-mention character)
                       :text                   text}


                      ;; The beginning of a mention, discard the @ sign
                      ;; and start following a mention


                      (= "@" character)
                      {:current-mention-length 1
                       :current-mention        ""
                       :current-text           current-text
                       :text                   text}

                      ;; Not a mention character, but we were following a mention
                      ;; discard everything up to know an count as text
                      (and (not is-pk-character)
                           (pos? current-mention-length))
                      {:current-mention-length 0
                       :current-text           (str current-text "@" current-mention character)
                       :current-mention        ""
                       :text                   text}

                      ;; Just a normal text character
                      :else
                      {:current-mention-length 0
                       :current-mention        ""
                       :current-text           (str current-text character)
                       :text                   text})))
                {:current-mention-length 0
                 :current-text           ""
                 :current-mention        ""
                 :text                   []}
                text)]
    ;; Process any remaining mention/text
    (cond-> text
      (seq current-text)
      (conj [:text current-text])
      (= current-mention-length mention-length)
      (conj [:mention current-mention]))))

(defn ->info
  "->info convert a input-field representation of mentions to
  a db based representation used to indicate where mentions are placed in the
  input string"
  [m]
  (reduce (fn [{:keys [start end at-idxs at-sign-idx mention-end]} [t text]]
            (if (= :mention t)
              (let [new-mention   {:checked? true
                                   :mention? true
                                   :from     mention-end
                                   :to       (+ start (count text))}
                    has-previous? (seq at-idxs)]
                {:new-text      (last text)
                 :previous-text ""
                 :start         (+ start (count text))
                 :end           (+ end (count text))
                 :at-idxs       (cond-> at-idxs
                                  has-previous?
                                  (-> pop
                                      (conj (assoc (peek at-idxs) :next-at-idx mention-end)))
                                  :always
                                  (conj new-mention))
                 :at-sign-idx   mention-end
                 :mention-end   (+ mention-end (count text))})
              {:new-text      (last text)
               :previous-text ""
               :start         (+ start (count text))
               :end           (+ end (count text))
               :at-idxs       at-idxs
               :at-sign-idx   at-sign-idx
               :mention-end   (+ mention-end (count text))}))
          {:start       -1
           :end         -1
           :at-idxs     []
           :mention-end 0}
          m))
