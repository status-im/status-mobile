(ns status-im.ethereum.eip681
  "Utility function related to [EIP681](https://eips.ethereum.org/EIPS/eip-681)

   This EIP standardize how ethereum payment request can be represented as URI (say to embed them in a QR code).

   e.g. ethereum:0x1234@1/transfer?to=0x5678&value=1e18&gas=5000"
  (:require [clojure.string :as string]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.tokens :as tokens]
            [status-im.utils.money :as money]))

(def scheme "ethereum")
(def scheme-separator ":")
(def chain-id-separator "@")
(def function-name-separator "/")
(def query-separator "?")
(def parameter-separator "&")
(def key-value-separator "=")

;;TODO(goranjovic) - rewrite all of these with something more readable than regex
(def uri-pattern (re-pattern (str scheme scheme-separator "([^" query-separator "]*)(?:\\" query-separator "(.*))?")))
(def authority-path-pattern (re-pattern (str "^([^" chain-id-separator function-name-separator "]*)(?:" chain-id-separator "(\\d+))?(?:" function-name-separator "(\\w*))?")))
(def key-value-format (str "([^" parameter-separator key-value-separator "]+)"))
(def query-pattern (re-pattern (str key-value-format key-value-separator key-value-format)))

(def valid-native-arguments #{:value :gas :gasPrice})

(defn- parse-query [s]
  (into {} (for [[_ k v] (re-seq query-pattern (or s ""))]
             [(keyword k) v])))

(defn- parse-native-arguments [m]
  (select-keys m valid-native-arguments))

(defn- parse-arguments [function-name s]
  (let [m         (parse-query s)
        arguments (parse-native-arguments m)]
    (if function-name
      (merge arguments {:function-name function-name}
             (when (seq m)
               {:function-arguments (apply dissoc m valid-native-arguments)}))
      arguments)))

;; TODO add ENS support

(defn parse-uri
  "Parse a EIP 681 URI as a map (keyword / strings). Parsed map will contain at least the key `address`.
   Note that values are not decoded and you might need to rely on specific methods for some fields (parse-value, parse-number).
   Invalid URI will be parsed as `nil`."
  [s]
  (when (string? s)
    (let [[_ authority-path query] (re-find uri-pattern s)]
      (when authority-path
        (let [[_ address chain-id function-name] (re-find authority-path-pattern authority-path)]
          (when (ethereum/address? address)
            (when-let [arguments (parse-arguments function-name query)]
              (merge {:address address :chain-id (if chain-id (js/parseInt chain-id) (ethereum/chain-keyword->chain-id :mainnet))}
                     arguments))))))))

(defn parse-eth-value [s]
  "Takes a map as returned by `parse-uri` and returns value as BigNumber"
  (when (string? s)
    (let [eth? (string/ends-with? s "ETH")
          n (money/bignumber (string/replace s "ETH" ""))]
      (if eth? (.times ^js n 1e18) n))))

(defn extract-request-details [{:keys [value address chain-id function-name function-arguments]} all-tokens]
  "Return a map encapsulating request details (with keys `value`, `address` and `symbol`) from a parsed URI.
   Supports ethereum and erc20 token."
  (when address
    (case function-name
      nil
      {:value   (parse-eth-value value)
       :symbol  :ETH
       :address address}
      "transfer"
      {:value   (money/bignumber (:uint256 function-arguments))
       :symbol  (:symbol (tokens/address->token all-tokens (ethereum/chain-id->chain-keyword chain-id) address))
       :address (:address function-arguments)}
      nil)))

(defn- generate-query-string [m]
  (string/join parameter-separator
               (for [[k v] m]
                 (str (name k) key-value-separator v))))

(defn generate-uri
  "Generate a EIP 681 URI based on `address` and a map (keywords / {bignumbers/strings} ) of extra properties.
   No validation of address format is performed."
  [address {:keys [chain-id function-name function-arguments] :as m}]
  (when (ethereum/address? address)
    (let [parameters (dissoc (into {} (filter second m)) :chain-id)] ;; filter nil values
      (str scheme scheme-separator address
           (when (and chain-id (not= chain-id (ethereum/chain-keyword->chain-id :mainnet)))
             ;; Add chain-id if specified and is not main-net
             (str chain-id-separator chain-id))
           (when-not (empty? parameters)
             (if function-name
               (str function-name-separator function-name query-separator
                    (let [native-parameters (dissoc parameters :function-name :function-arguments)]
                      (generate-query-string (merge function-arguments native-parameters))))
               (str query-separator (generate-query-string parameters))))))))

(defn generate-erc20-uri
  "Generate a EIP 681 URI encapsulating ERC20 token transfer"
  [address {:keys [symbol value chain-id] :as m} all-tokens]
  (when-let [token (tokens/symbol->token all-tokens (if chain-id (ethereum/chain-id->chain-keyword chain-id) :mainnet) symbol)]
    (generate-uri (:address token)
                  (merge (dissoc m :value :symbol)
                         {:function-name      "transfer"
                          :function-arguments {:uint256 value :address address}}))))
