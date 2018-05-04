(ns status-im.translations.sr-rs-latn
  (:require [status-im.translations.sr-rs-cyrl :as sr-rs-cyrl]
            [clojure.string :as string]))

(def letter-pairs
  {"А" "A"
   "Б" "B"
   "В" "V"
   "Г" "G"
   "Д" "D"
   "Ђ" "Đ"
   "Е" "E"
   "Ж" "Ž"
   "З" "Z"
   "И" "I"
   "Ј" "J"
   "К" "K"
   "Л" "L"
   "Љ" "Lj"
   "М" "M"
   "Н" "N"
   "Њ" "Nj"
   "О" "O"
   "П" "P"
   "Р" "R"
   "С" "S"
   "Т" "T"
   "Ћ" "Ć"
   "У" "U"
   "Ф" "F"
   "Х" "H"
   "Ц" "C"
   "Ч" "Č"
   "Џ" "Dž"
   "Ш" "Š"

   "а" "a"
   "б" "b"
   "в" "v"
   "г" "g"
   "д" "d"
   "ђ" "đ"
   "е" "e"
   "ж" "ž"
   "з" "z"
   "и" "i"
   "ј" "j"
   "к" "k"
   "л" "l"
   "љ" "lj"
   "м" "m"
   "н" "n"
   "њ" "nj"
   "о" "o"
   "п" "p"
   "р" "r"
   "с" "s"
   "т" "t"
   "ћ" "ć"
   "у" "u"
   "ф" "f"
   "х" "h"
   "ц" "c"
   "ч" "č"
   "џ" "dž"
   "ш" "š"})

(defn cyr->lat [cyr]
  (cond
    (string? cyr) (string/join
                   (map #(get letter-pairs (str %) (str %)) cyr))
    (map? cyr) (into {}
                     (map (fn [[k v]] [k (cyr->lat v)]) cyr))
    :else nil))

(def translations
  (cyr->lat sr-rs-cyrl/translations))
