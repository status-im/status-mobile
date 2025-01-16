(ns utils.address
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [utils.ethereum.eip.eip681 :as eip681]))


(def hex-prefix "0x")
;; EIP-3770 is a format used by Status and described here: https://eips.ethereum.org/EIPS/eip-3770
(def regx-eip-3770-address #"^(?:(?:eth:|arb1:|oeth:)(?=:|))*0x[0-9a-fA-F]{40}$")
(def regx-metamask-address #"^ethereum:(0x[0-9a-fA-F]{40})(?:@(0x1|0xa|0xa4b1))?$")
(def regx-address-contains #"(?i)0x[a-fA-F0-9]{40}")
(def zero-address "0x0000000000000000000000000000000000000000")

(defn normalized-hex
  [hex]
  (when hex
    (if (string/starts-with? hex hex-prefix)
      hex
      (str hex-prefix hex))))

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

(defn eip-155-suffix->eip-3770-prefix
  [eip-155-suffix]
  (case eip-155-suffix
    "0x1"    "eth:"
    "0xa4b1" "arb1:"
    "0xa"    "oeth:"
    nil))

(defn split-metamask-address
  [address]
  (re-find regx-metamask-address address))

(defn metamask-address?
  [address]
  (boolean (split-metamask-address address)))

(defn eip-3770-address?
  "Checks if address follows EIP-3770 format which is default for Status"
  [address]
  (re-find regx-eip-3770-address address))

(defn eip-681-address?
  [scanned-text]
  (-> scanned-text
      eip681/parse-uri
      :address
      boolean))

(defn supported-address?
  [s]
  (boolean (or (eip-3770-address? s)
               (metamask-address? s))))

(defn supported-scan-address?
  [s]
  (boolean (or (eip-681-address? s)
               (supported-address? s))))

(defn metamask-address->eip-3770-address
  [metamask-address]
  (when-let [[_ address metamask-network-suffix] (split-metamask-address metamask-address)]
    (if-let [status-network-prefix (eip-155-suffix->eip-3770-prefix metamask-network-suffix)]
      (str status-network-prefix address)
      address)))

(defn extract-address-without-chains-info
  [address]
  (re-find regx-address-contains address))

(defn metamask-address->eth-address
  [eip-3770-address]
  (extract-address-without-chains-info eip-3770-address))

(defn eip-3770-address->eth-address
  [eip-3770-address]
  (extract-address-without-chains-info eip-3770-address))

(defn eip-681-address->eth-address
  [eip-681-address]
  (-> eip-681-address
      eip681/parse-uri
      :address))

(defn supported-address->eth-address
  [address]
  (cond
    (eip-681-address? address)
    (eip-681-address->eth-address address)

    (eip-3770-address? address)
    (eip-3770-address->eth-address address)

    (metamask-address? address)
    (metamask-address->eth-address address)))

(defn zero-address?
  [address]
  (= address zero-address))

(defn public-key->address
  [public-key]
  (let [length         (count public-key)
        normalized-key (case length
                         132 (str "0x" (subs public-key 4))
                         130 public-key
                         128 (str "0x" public-key)
                         nil)]
    (when normalized-key
      (subs (native-module/sha3 normalized-key) 26))))
