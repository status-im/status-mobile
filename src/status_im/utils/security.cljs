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
