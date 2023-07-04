(ns utils.validators)

(defn valid-public-key?
  [s]
  (and (string? s)
       (not-empty s)
       (boolean (re-matches #"0x04[0-9a-f]{128}" s))))

(defn valid-compressed-key?
  [s]
  (and (string? s)
       (not-empty s)
       (boolean (re-matches #"^zQ3[0-9A-HJ-NP-Za-km-z]{46}" s))))

