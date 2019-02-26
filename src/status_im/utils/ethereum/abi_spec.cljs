(ns status-im.utils.ethereum.abi-spec
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [status-im.js-dependencies :as dependencies]))
;; Utility functions for encoding

(def utils dependencies/web3-utils)

(defn right-pad [x]
  (let [len (count x)
        to-pad (- 64 (mod len 64))]
    (if (= 64 to-pad)
      x
      (.rightPad utils x (+ len to-pad)))))

(defn left-pad [x]
  (let [len (count x)
        to-pad (- 64 (mod len 64))]
    (if (= 64 to-pad)
      x
      (.leftPad utils x (+ len to-pad)))))

(defn to-two-complement [x]
  (when x
    (subs (.toTwosComplement utils x) 2)))

(defn from-utf8 [x]
  (when x
    (subs (.fromUtf8 utils x) 2)))

(defn bytes-to-hex [x]
  (when x
    (subs (.bytesToHex utils x) 2)))

(defn number-to-hex [x]
  (when x
    (subs (.numberToHex utils x) 2)))

(defn hex-to-utf8 [x]
  (.hexToUtf8 utils (str "0x" x)))

(defn hex-to-number [x]
  (when x
    (.toNumber (dependencies/Web3.prototype.toBigNumber (str "0x" x) 16))))

(defn sha3 [s]
  (.sha3 utils (str s)))

(defn is-hex? [value]
  (when value
    (string/starts-with? value "0x")))

;; Encoder for parsed abi spec

(defmulti enc :type)

;; bool: as in the uint8 case, where 1 is used for true and 0 for false
(defmethod enc :bool
  [{:keys [value]}]
  (left-pad (if value "1" "0")))

;; int<M>: enc(X) is the big-endian two’s complement encoding of X, padded on the
;; higher-order (left) side with 0xff for negative X and with zero bytes for
;; positive X such that the length is 32 bytes.
(defmethod enc :int
  [{:keys [value]}]
  (to-two-complement value))

;; uint<M>: enc(X) is the big-endian encoding of X, padded on the
;; higher-order (left) side with zero-bytes such that the length is 32 bytes.
(defmethod enc :uint
  [{:keys [value]}]
  (left-pad (number-to-hex value)))

;; address: as in the uint160 case
(defmethod enc :address
  [{:keys [value]}]
  (when (string? value)
    (left-pad (string/replace value "0x" ""))))

;; bytes, of length k (which is assumed to be of type uint256):
;; enc(X) = enc(k) pad_right(X), i.e. the number of bytes is encoded as a
;; uint256 followed by the actual value of X as a byte sequence,
;; followed by the minimum number of zero-bytes such that len(enc(X))
;; is a multiple of 32.
;; bytes<M>: enc(X) is the sequence of bytes in X padded with trailing
;; zero-bytes to a length of 32 bytes.
(defmethod enc :bytes
  [{:keys [value size dynamic?]
    :or {size 256}}]
  ;; in the examples of the abi specifications strings are passed for
  ;; bytes parameters, in our ens resolver we pass encoded bytes directly
  ;; for namehash, this handles both cases by checking if the value is already
  ;; hex
  (let [encoded-value? (is-hex? value)
        encoded-value  (if encoded-value?
                         (subs value 2)
                         (from-utf8 value))]
    (str (when dynamic? (enc {:type :int :value (if encoded-value?
                                                  (count encoded-value)
                                                  (/ (count encoded-value) 2))}))
         (right-pad encoded-value))))

;; string: enc(X) = enc(enc_utf8(X)), i.e. X is utf-8 encoded and this
;; value is interpreted as of bytes type and encoded further.
;; Note that the length used in this subsequent encoding is the number
;; of bytes of the utf-8 encoded string, not its number of characters.
(defmethod enc :string
  [{:keys [value dynamic?]}]
  (enc {:type :bytes :value value :dynamic? dynamic?}))

;; fixed<M>x<N>: enc(X) is enc(X * 10**N) where X * 10**N is
;; interpreted as a int256.
(defmethod enc :fixed
  [{:keys [value size power]
    :or {size 128
         power 18}}]
  (enc {:type  :int
        :value (* value (Math/pow 10 power))}))

;; ufixed: as in the ufixed128x18 case
(defmethod enc :ufixed
  [{:keys [value size power]
    :or {size 128
         power 18}}]
  (enc {:type  :uint
        :value (* value (Math/pow 10 power))}))

;; T[k] for any T and k:
;; enc(X) = enc((X[0], ..., X[k-1]))
;; i.e. it is encoded as if it were a tuple with k elements of the same type.

;; T[] where X has k elements (k is assumed to be of type uint256):
;; enc(X) = enc(k) enc([X[0], ..., X[k-1]])
;; i.e. it is encoded as if it were an array of static size k,
;; prefixed with the number of elements.
(defmethod enc :array
  [{:keys [value dynamic? array-of] :as x}]
  (str (when dynamic?
         (enc {:type :int
               :value (count value)}))
       (enc {:type :tuple
             :value (map #(assoc array-of :value %)
                         value)})))

;; (T1,...,Tk) for k >= 0 and any types T1, …, Tk
;; enc(X) = head(X(1)) ... head(X(k)) tail(X(1)) ... tail(X(k))
;; where X = (X(1), ..., X(k))

;; for Ti being static type:
;; head(X(i)) = enc(X(i))
;; tail(X(i)) = "" (the empty string)

;; for dynamic types:
;; head(X(i)) = enc(len(head(X(1)) ... head(X(k))
;;                      tail(X(1)) ... tail(X(i-1)) ))
;; tail(X(i)) = enc(X(i))

;; Note that in the dynamic case, head(X(i)) is well-defined since
;; the lengths of the head parts only depend on the types and not
;; the values. Its value is the offset of the beginning of tail(X(i))
;; relative to the start of enc(X).
(defmethod enc :tuple
  [{:keys [value]}]
  (let [[len x] (reduce
                 (fn [[len acc] {:keys [dynamic?] :as x}]
                   (let [enc-x (enc x)]
                     (if dynamic?
                       [(+ len 32)
                        (conj acc (assoc x :tail enc-x))]
                       [(+ len (/ (count enc-x) 2))
                        (conj acc (assoc x :head enc-x))])))
                 [0 []]
                 value)
        [_ heads tails] (reduce (fn [[len heads tails] {:keys [head tail] :as x}]
                                  (if tail
                                    [(+ len (/ (count tail) 2))
                                     (conj heads (enc {:type :int :value len}))
                                     (conj tails tail)]
                                    [len
                                     (conj heads head)
                                     tails]))
                                [len [] []]
                                x)]
    (apply str (concat heads tails))))

;;
;; Parser for method signatures
;;

(spec/def ::params (spec/* (spec/cat ::param ::param
                                     ::separator (spec/? ::comma))))

(spec/def ::param (spec/cat ::type ::string
                            ::size (spec/? ::number)
                            ::x (spec/? ::x)
                            ::power (spec/? ::number)
                            ::array (spec/* ::array)))

(spec/def ::array (spec/cat ::open-bracket ::open-bracket
                            ::size (spec/? ::number)
                            ::close-bracket ::close-bracket))

(spec/def ::x #{\x})

(spec/def ::open-bracket #{\[})

(spec/def ::close-bracket #{\]})

(spec/def ::comma #{\,})

(spec/def ::number int?)

(spec/def ::string string?)

(defn- single-char [code]
  (if-let [m (#{\, \[ \] \x} (first code))]
    [1 m]))

(defn- number [code]
  (if-let [m (re-find #"^[0-9]+" code)]
    [(count m) (js/parseInt m)]))

(defn- string [s]
  (if-let [m (re-find #"^[a-z]+" s)]
    [(count m) m]))

(defn tokenise [code]
  (if (seq code)
    (if-let [[len token] (or (string code)
                             (single-char code)
                             (number code))]
      (cons token (tokenise (subs code len)))
      (throw (ex-info "Unexpected token" {:code code})))))

;; Definition: The following types are called “dynamic”:
;; - bytes
;; - string
;; - T[] for any T
;; - T[k] for any dynamic T and any k >= 0
;; - (T1,...,Tk) if Ti is dynamic for some 1 <= i <= k
(defn parse-param
  "Takes a parsed parameter and returns a parameter that can
  be encoded by associng the :dynamic? key for dynamic parameters
  and recursively defining arrays"
  [{::keys [type size array power] :as param}]
  (if array
    (let [{::keys [size]} (last array)]
      {:type     :array
       :dynamic? (nil? size)
       :array-of (parse-param (update param ::array butlast))})
    (let [type  (keyword type)
          param {:type     type
                 :dynamic? (or (= type :string)
                               (and (= type :bytes)
                                    (nil? size)))
                 :size     size}]
      (if power
        (assoc param :power power)
        param))))

(defn parse-params [method-signature]
  (let [tokens (tokenise (second (re-find #"\((.*)\)" method-signature)))
        params (spec/conform ::params tokens)]
    (if (spec/invalid? params)
      (spec/explain-data ::params tokens)
      (map #(parse-param (::param %))
           params))))

(defn signature->method-id [signature]
  (apply str (take 10 (sha3 signature))))

(defn encode [method params]
  (let [method-id (signature->method-id method)]
    (let [params (map #(assoc %1 :value %2)
                      (parse-params method)
                      params)]
      (str method-id (enc {:type  :tuple
                           :value params})))))

;; ======= decode

(defn substr [val s l]
  (subs val s (+ s l)))

;; "[]" -> 0 , "[1]" -> 1
(defn arr-size [val]
  (int (apply str (rest (butlast val)))))

;; [2] -> 2 , [1] -> 1 ,  [] - > 1
(defn nested-size [val]
  (let [num (arr-size val)]
    (if (zero? num) 1 num)))

;; '("[1]" "[]") or nil
(defn list-of-nested-types [type]
  (when-let [res (re-seq #"(\[[0-9]*\])" type)]
    (map first res)))

(defn nested-name [type]
  (let [ntypes (list-of-nested-types type)]
    (if ntypes
      (subs type 0 (- (count type) (count (last ntypes))))
      type)))

(defn is-arr? [type]
  (boolean (list-of-nested-types type)))

(defn is-dynamic-arr? [type]
  (let [ntypes (list-of-nested-types type)]
    (and ntypes (zero? (arr-size (last ntypes))))))

(defn static-arr-len [type]
  (let [ntypes (list-of-nested-types type)]
    (if ntypes
      (nested-size (last ntypes))
      1)))

(defn static-part-length [type]
  (apply * (conj (map nested-size (or (list-of-nested-types type) '("1"))) 32)))

(defn offset-reducer [{:keys [cnt coll]} val]
  (let [cnt' (+ cnt val)]
    {:cnt  cnt'
     :coll (conj coll cnt')}))

(defn get-offsets [types]
  (let [lengths (map static-part-length types)]
    (conj (butlast (:coll (reduce offset-reducer {:cnt 0 :coll []} lengths))) 0)))

(defn hex-to-bytes [hex]
  (let [len (* (hex-to-number (subs hex 0 64)) 2)]
    (substr hex 64 len)))

(defn dyn-hex-to-value [hex type]
  (cond
    (string/starts-with? type "bytes")
    (str "0x" (hex-to-bytes hex))

    (string/starts-with? type "string")
    (hex-to-utf8 (hex-to-bytes hex))))

(defn hex-to-bytesM [hex type]
  (let [size (int (second (re-matches #"^bytes([0-9]*)" type)))]
    (subs hex 0 (* 2 size))))

(defn hex-to-value [hex type]
  (cond
    (= "bool" type) (= hex "0000000000000000000000000000000000000000000000000000000000000001")
    (string/starts-with? type "uint") (hex-to-number hex)
    (string/starts-with? type "int") (hex-to-number hex)
    (string/starts-with? type "address") (str "0x" (subs hex (- (count hex) 40)))
    (string/starts-with? type "bytes") (hex-to-bytesM hex type)))

(defn dec-type [bytes]
  (fn [offset type]
    (cond
      (is-arr? type)

      (let [dyn-arr? (is-dynamic-arr? type)
            arr-off (js/parseInt (str "0x" (substr bytes (* offset 2) 64)))
            len (if dyn-arr?
                  (js/parseInt (str "0x" (substr bytes (* arr-off 2) 64)))
                  (static-arr-len type))
            arr-start (if dyn-arr? (+ arr-off 32) offset)

            nname (nested-name type)
            nstatpartlen (static-part-length nname)
            rnstatpartlen (* (js/Math.floor (/ (+ nstatpartlen 31) 32)) 32)]
        (loop [res [] i 0]
          (if (>= i (* len rnstatpartlen))
            res
            (recur (conj res ((dec-type bytes) (+ arr-start i) nname))  (+ i rnstatpartlen)))))

      (or (re-matches #"^bytes(\[([0-9]*)\])*$" type)
          (string/starts-with? type "string"))

      (let [dyn-off (js/parseInt (str "0x" (substr bytes (* offset 2) 64)))
            len (js/parseInt (str "0x" (substr bytes (* dyn-off 2) 64)))
            rlen (js/Math.floor (/ (+ len 31) 32))
            val (substr bytes (* dyn-off 2) (* (+ rlen 1) 64))]
        (dyn-hex-to-value val type))

      :else

      (let [len (static-part-length type)
            val (substr bytes (* offset 2) (* len 2))]
        (hex-to-value val type)))))

(defn decode [bytes types]
  (when-not (empty? bytes)
    (let [offsets (get-offsets types)]
      (map (dec-type bytes) offsets types))))
