(ns status-im.utils.db)

(defn valid-public-key? [s]
 (and (string? s) (not-empty s) (boolean (re-matches #"0x04[0-9a-f]{128}" s))))