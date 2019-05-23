(ns status-im.utils.multihash
  "Core multihash type definition and helper methods."
  (:require
   [alphabase.base58 :as b58]
   [alphabase.bytes :as bytes]
   [alphabase.hex :as hex]))

(def ^:const algorithm-codes
  "Map of information about the available content hashing algorithms."
  {:sha1     0x11
   :sha2-256 0x12
   :sha2-512 0x13
   :sha3     0x14
   :blake2b  0x40
   :blake2s  0x41})

(defn app-code?
  "True if the given code number is assigned to the application-specfic range.
  Returns nil if the argument is not an integer."
  [code]
  (when (integer? code)
    (< 0 code 0x10)))

(defn get-algorithm
  "Looks up an algorithm by keyword name or code number. Returns `nil` if the
  value does not map to any valid algorithm."
  [value]
  (cond
    (keyword? value)
    (when-let [code (get algorithm-codes value)]
      {:code code, :name value})

    (not (integer? value))
    nil

    (app-code? value)
    {:code value, :name (keyword (str "app-" value))}

    :else
    (some #(when (= value (val %))
             {:code value, :name (key %)})
          algorithm-codes)))

;; ## Multihash Type

;; Multihash identifiers have two properties:
;;
;; - `code` is a numeric code for an algorithm entry in `algorithm-codes` or an
;;   application-specific algorithm code.
;; - `hex-digest` is a string holding the hex-encoded algorithm output.
;;
;; Multihash values also support metadata.
(deftype ^js Multihash [code hex-digest _meta]

  Object

  (toString
    [this]
    (str "hash:" (name (:name (get-algorithm code))) \: hex-digest))

  (-equiv
    [this that]
    (cond
      (identical? this that) true
      (instance? Multihash that)
      (and (= code (:code that))
           (= hex-digest (:hex-digest that)))
      :else false))

  IHash

  (-hash
    [this]
    (hash-combine code hex-digest))

  IComparable

  (-compare
    [this that]
    (cond
      (= this that) 0
      (< code (:code that)) -1
      (> code (:code that)) 1
      :else (compare hex-digest (:hex-digest that))))

  ILookup

  (-lookup
    [this k]
    (-lookup this k nil))

  (-lookup
    [this k not-found]
    (case k
      :code code
      :algorithm (:name (get-algorithm code))
      :length (/ (count hex-digest) 2)
      :digest (hex/decode hex-digest)
      :hex-digest hex-digest
      not-found))

  IMeta

  (-meta
    [this]
    _meta)

  IWithMeta

  (-with-meta
    [this meta-map]
    (Multihash. code hex-digest meta-map)))

(defn create
  "Constructs a new Multihash identifier. Accepts either a numeric algorithm
  code or a keyword name as the first argument. The digest may either by a byte
  array or a hex string."
  [algorithm digest]
  (let [algo (get-algorithm algorithm)]
    (when-not (integer? (:code algo))
      (throw (ex-info
              (str "Argument " (pr-str algorithm) " does not "
                   "represent a valid hash algorithm.")
              {:algorithm algorithm})))
    (let [hex-digest (if (string? digest) digest (hex/encode digest))
          byte-len   (/ (count hex-digest) 2)]
      (when (< 127 byte-len)
        (throw (ex-info (str "Digest length must be less than 128 bytes: "
                             byte-len)
                        {:length byte-len})))
      (when-let [err (hex/validate hex-digest)]
        (throw (ex-info err {:hex-digest hex-digest})))
      (->Multihash (:code algo) hex-digest nil))))

;; ## Encoding and Decoding

(defn encode
  "Encodes a multihash into a binary representation."
  ^bytes
  [mhash]
  (let [length  (:length mhash)
        encoded (bytes/byte-array (+ length 2))]
    (bytes/set-byte encoded 0 (:code mhash))
    (bytes/set-byte encoded 1 length)
    (bytes/copy (:digest mhash) 0 encoded 2 length)
    encoded))

(defn hex
  "Encodes a multihash into a hexadecimal string."
  [mhash]
  (when mhash
    (hex/encode (encode mhash))))

(defn base58
  "Encodes a multihash into a Base-58 string."
  [mhash]
  (when mhash
    (b58/encode (encode mhash))))

(defn decode-array
  "Decodes a byte array directly into multihash. Throws `ex-info` with a `:type`
  of `:multihash/bad-input` if the data is malformed or invalid."
  [^bytes encoded]
  (let [encoded-size (alength encoded)
        min-size     3]
    (when (< encoded-size min-size)
      (throw (ex-info
              (str "Cannot read multihash from byte array: " encoded-size
                   " is less than the minimum of " min-size)
              {:type :multihash/bad-input}))))
  (let [code    (bytes/get-byte encoded 0)
        length  (bytes/get-byte encoded 1)
        payload (- (alength encoded) 2)]
    (when-not (pos? length)
      (throw (ex-info
              (str "Encoded length " length " is invalid")
              {:type :multihash/bad-input})))
    (when (< payload length)
      (throw (ex-info
              (str "Encoded digest length " length " exceeds actual "
                   "remaining payload of " payload " bytes")
              {:type :multihash/bad-input})))
    (let [digest (bytes/byte-array length)]
      (bytes/copy encoded 2 digest 0 length)
      (create code digest))))
