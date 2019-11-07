(ns status-im.utils.security)

(defprotocol Unmaskable
  ;; Retrieve the stored value.
  (unmask [this]))

;; MaskedData ensures that the object passed to it won't be occasionally printed
;; via println or log functions. Useful for keeping sensitive data, such as passwords
;; to avoid accidentally exposing them.
(deftype MaskedData [data]
  Object
  (toString [_] "******")
  Unmaskable
  (unmask [this]
    (.-data this)))

;; Returns a MaskedData instance that stores the piece of data.
(defn mask-data [data]
  (MaskedData. data))

(defn safe-unmask-data [data]
  (if (instance? MaskedData data)
    (unmask data)
    data))

;; Links starting with javascript:// should not be handled at all
(def javascript-link-regex #"(?i)javascript://.*")
;; Anything with rtlo character we don't handle as it might be a spoofed url
(def rtlo-link-regex #".*\u202e.*")

(defn safe-link?
  "Check the link is safe to be handled, it is not a javavascript link or contains
  an rtlo character, which might mean is a spoofed url"
  [link]
  (not (or (re-matches javascript-link-regex link)
           (re-matches rtlo-link-regex link))))

(defn safe-link-text?
  "Check the text of the message containing a link  is safe to be handled
  and does not contain an rtlo character, which might mean that the url is spoofed"
  [text]
  (not (re-matches rtlo-link-regex text)))
