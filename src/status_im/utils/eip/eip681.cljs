(ns status-im.utils.eip.eip681
  "Utility function related to [EIP681](https://github.com/ethereum/EIPs/issues/681)

   This EIP standardize how ethereum payment request can be represented as URI (say to embed them in a QR code).

   e.g. ethereum:0x1234@1/transfer?to=0x5678&value=1e18&gas=5000"
  (:require [clojure.string :as string]
            [status-im.constants :as constants]
            [status-im.utils.money :as money]))

(def scheme "ethereum")
(def scheme-separator ":")
(def chain-id-separator "@")
(def function-name-separator "/")
(def query-separator "?")
(def parameter-separator "&")
(def key-value-separator "=")

(def uri-pattern (re-pattern (str scheme scheme-separator "([^" query-separator "]*)(?:\\" query-separator "(.*))?")))
(def authority-path-pattern (re-pattern (str "^([^" chain-id-separator "]*)(?:" chain-id-separator "(\\d))?(?:" function-name-separator "(\\w*))?")))
(def key-value-format (str "([^" parameter-separator key-value-separator "]+)"))
(def query-pattern (re-pattern (str key-value-format key-value-separator key-value-format)))

(defn- parse-query [s]
  (when s
    (into {} (for [[_ k v] (re-seq query-pattern s)]
               [(keyword k) v]))))

(defn parse-value [{:keys [value function-name]}]
  "Takes a map as returned by `parse-uri` and returns value as BigNumber"
  (when (and value (not function-name)) ;; TODO(jeluard) Add ERC20 support
    (let [eth? (string/ends-with? value "ETH")
          n (money/bignumber (string/replace value "ETH" ""))]
      (if eth? (.times n 1e18) n))))

(defn parse-uri
  "Parse a EIP 681 URI as a map (keyword / strings). Parsed map will contain at least the key `address`.
   Note that values are not decoded and you might need to rely on specific methods for some fields (parse-value, parse-number).
   Invalid URI will be parsed as `nil`."
  [s]
  (when (string? s)
    (let [[_ authority-path query] (re-find uri-pattern s)]
      (when authority-path
        (let [[_ address chain-id function-name] (re-find authority-path-pattern authority-path)]
          (when-not (or (string/blank? address) function-name) ;; Native token support only TODO(jeluard) Add ERC20 support
            (merge {:address address :chain-id (if chain-id (js/parseInt chain-id) constants/mainnet-id)}
                   (parse-query query))))))))


(defn- generate-query-string [m]
  (string/join parameter-separator
               (for [[k v] m]
                 (str (name k) key-value-separator v))))

(defn generate-uri
  "Generate a EIP 681 URI based on `address` and a map (keywords / {bignumbers/strings} ) of extra properties.
   No validation of address format is performed."
  [address {:keys [function-name chain-id] :as m}]
  (when (and address (not function-name)) ;; Native token support only TODO(jeluard) Add ERC20 support
    (let [parameters (dissoc (into {} (filter second m)) :chain-id)] ;; filter nil values
      (str scheme scheme-separator address
           (when (and chain-id (not= chain-id constants/mainnet-id))
             ;; Add chain-id if specified and is not main-net
             (str chain-id-separator chain-id))
           (when-not (empty? parameters)
             (str query-separator (generate-query-string parameters)))))))