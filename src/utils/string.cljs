(ns utils.string
  (:require
    [clojure.string :as string]
    [utils.transforms :as transforms]))

(defn has-lower-case?
  [s]
  (some? (re-find #"[a-z]" s)))

(defn has-upper-case?
  [s]
  (some? (re-find #"[A-Z]" s)))

(defn has-numbers?
  [s]
  (some? (re-find #"\d" s)))

(defn has-symbols?
  [s]
  (some? (re-find #"[^a-zA-Z0-9\s]" s)))

(defn at-least-n-chars?
  [s n]
  (>= (count s) n))

(defn at-most-n-chars?
  [s n]
  (<= (count s) n))

(defn safe-trim
  [s]
  (when (string? s)
    (string/trim s)))

(defn safe-replace
  [s m r]
  (when (string? s)
    (string/replace s m r)))

(defn get-initials
  "Returns `n` number of initial letters from `s`, all uppercased."
  [s n]
  (let [words (-> s str string/trim (string/split #"\s+"))]
    (->> words
         (take n)
         (map (comp string/upper-case str first))
         string/join)))

(def emoji-data (transforms/js->clj (js/require "../resources/data/emojis/en.json")))
(def emoji-unicode-values (map :unicode emoji-data))

(defn contains-emoji?
  [s]
  (some (fn [emoji]
          (string/includes? s emoji))
        emoji-unicode-values))

(defn contains-special-character?
  [s]
  (re-find #"[^a-zA-Z0-9\s]" s))

(defn remove-trailing-slash
  "Given a URL, checks if it has a trailing slash and removes it.
  Returns the URL as-is if there is no trailing slash."
  [url]
  (if (and (string? url) (string/ends-with? url "/"))
    (subs url 0 (dec (count url)))
    url))

(defn remove-http-prefix
  "Given a URL, removes the 'http://' or 'https://' prefix if present.
  Returns the URL without the prefix."
  [url]
  (when (string? url)
    (string/replace url #"^https?://" "")))

(defn valid-amount-for-token-decimals?
  [token-decimals amount-text]
  (let [regex-pattern (str "^\\d*\\.?\\d{0," token-decimals "}$")
        regex         (re-pattern regex-pattern)]
    (boolean (re-matches regex amount-text))))
