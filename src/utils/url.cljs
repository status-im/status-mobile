(ns utils.url
  (:require
    [clojure.string :as string]))

(defn normalize-url
  [url]
  (str (when (and (string? url)
                  (not (re-find #"^[a-zA-Z-_]+:/" url)))
         "https://")
       ((fnil string/trim "") url)))

(def normalize-and-decode-url (comp js/decodeURI normalize-url))

(defn url-host
  [url]
  (try
    (when-let [host (.getDomain ^js (goog.Uri. url))]
      (when-not (string/blank? host)
        (string/replace host #"www." "")))
    (catch :default _ nil)))

(defn url?
  [s]
  (try
    (when-let [host (.getDomain ^js (goog.Uri. s))]
      (not (string/blank? host)))
    (catch :default _ nil)))

(defn url-sanitized?
  [uri]
  (not (nil? (re-find #"^(https:)([/|.|\w|\s|-])*\.(?:jpg|svg|png)$" uri))))

(defn- split-param
  [param]
  (->
    (string/split param #"=")
    (concat (repeat ""))
    (->>
      (take 2))))

(defn- url-decode
  [string]
  (some-> string
          str
          (string/replace #"\+" "%20")
          (js/decodeURIComponent)))

(defn query->map
  [qstr]
  (when-not (string/blank? qstr)
    (some->> (string/split qstr #"&")
             seq
             (mapcat split-param)
             (map url-decode)
             (apply hash-map))))

(defn replace-port
  [url new-port]
  (when url
    (string/replace url #"(:\d+)" (str ":" new-port))))

(defn add-slash-separator
  [url]
  (if (not= (last url) "/")
    (str url "/")
    url))

(defn add-path
  ([url path value]
   (-> url
       (add-path path)
       (add-path value)))
  ([url path]
   (-> url
       add-slash-separator
       (str (name path)))))
