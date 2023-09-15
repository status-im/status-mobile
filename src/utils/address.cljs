(ns utils.address
  ;; TODO move to status-im2
  (:require [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip55 :as eip55]))

(defn get-shortened-key
  "Takes first and last 4 digits from address including leading 0x
  and adds unicode ellipsis in between"
  [value]
  (when value
    (str (subs value 0 6) "\u2026" (subs value (- (count value) 3) (count value)))))

(defn get-shortened-checksum-address
  [address]
  (when address
    (get-shortened-key (eip55/address->checksum (ethereum/normalized-hex address)))))

(defn get-abbreviated-profile-url
  "The goal here is to generate a string that begins with
   join.status.im/u/ joined with the 1st 5 characters
   of the compressed public key followed by an ellipsis followed by
   the last 10 characters of the compressed public key"
  [base-url public-key]
  (if (and public-key base-url (> (count public-key) 17) (= "join.status.im/u/" base-url))
    (let [first-part-of-public-pk (subs public-key 0 5)
          ellipsis                "..."
          public-key-size         (count public-key)
          last-part-of-public-key (subs public-key (- public-key-size 10) public-key-size)
          abbreviated-url         (str base-url
                                       first-part-of-public-pk
                                       ellipsis
                                       last-part-of-public-key)]
      abbreviated-url)
    nil))

(defn get-shortened-compressed-key
  "The goal here is to generate a string that begins with 1st 3
  characters of the compressed public key followed by an ellipsis followed by
  the last 6 characters of the compressed public key"
  [public-key]
  (if (and public-key (> (count public-key) 9))
    (let [first-part-of-public-key (subs public-key 0 3)
          ellipsis                 "..."
          public-key-size          (count public-key)
          last-part-of-public-key  (subs public-key (- public-key-size 6) public-key-size)
          abbreviated-public-key   (str first-part-of-public-key ellipsis last-part-of-public-key)]
      abbreviated-public-key)
    nil))
