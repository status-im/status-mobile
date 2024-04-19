(ns utils.address
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [utils.ethereum.eip.eip55 :as eip55]))

(def hex-prefix "0x")

(defn normalized-hex
  [hex]
  (when hex
    (if (string/starts-with? hex hex-prefix)
      hex
      (str hex-prefix hex))))

(defn naked-address
  [s]
  (when s
    (string/replace s hex-prefix "")))

(defn address?
  [address]
  (native-module/address? address))

(defn address=
  [address1 address2]
  (and address1
       address2
       (= (string/lower-case (normalized-hex address1))
          (string/lower-case (normalized-hex address2)))))

(defn get-shortened-key
  "Takes first and last 4 digits from address including leading 0x
  and adds unicode ellipsis in between"
  [value]
  (when value
    (str (subs value 0 6) "\u2026" (subs value (- (count value) 3) (count value)))))

(defn get-shortened-checksum-address
  [address]
  (when address
    (get-shortened-key (eip55/address->checksum (normalized-hex address)))))

(defn get-abbreviated-profile-url
  "The goal here is to generate a string that begins with status.app/u/ joined
  with the 1st 5 characters of the encoded data followed by an ellipsis
  followed by the last 10 characters of the compressed public key"
  [universal-profile-url]
  (when-let [re-find-result (re-find #"^https://(status.app/u/)(.*)#(.*)$" (str universal-profile-url))]
    (let [[_whole-url base-url encoded-data public-key] re-find-result]
      (when (> (count public-key) 9)
        (let [first-part-of-encoded-data (subs encoded-data 0 5)
              ellipsis                   "..."
              public-key-size            (count public-key)
              last-part-of-public-key    (subs public-key (- public-key-size 10) public-key-size)
              abbreviated-url            (str base-url
                                              first-part-of-encoded-data
                                              ellipsis
                                              last-part-of-public-key)]
          abbreviated-url)))))

(defn get-shortened-compressed-key
  "The goal here is to generate a string that begins with 1st 3
  characters of the compressed public key followed by an ellipsis followed by
  the last 6 characters of the compressed public key"
  [public-key]
  (if (and public-key (> (count public-key) 9))
    (let [first-part-of-public-key (subs public-key 0 3)
          ellipsis                 "..."
          public-key-size          (count public-key)
          last-part-of-public-key  (subs public-key (- public-key-size 5) public-key-size)
          abbreviated-public-key   (str first-part-of-public-key ellipsis last-part-of-public-key)]
      abbreviated-public-key)
    nil))

(defn get-short-wallet-address
  [value]
  (when value
    (str (subs value 0 5) "..." (subs value (- (count value) 3) (count value)))))
