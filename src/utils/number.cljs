(ns utils.number)

(defn naive-round
  "Quickly and naively round number `n` up to `decimal-places`.

  Example usage: use it to avoid re-renders caused by floating-point number
  changes in Reagent atoms. Such numbers can be rounded up to a certain number
  of `decimal-places` in order to avoid re-rendering due to tiny fractional
  changes.

  Don't use this function for arbitrary-precision arithmetic."
  [n decimal-places]
  (let [scale (Math/pow 10 decimal-places)]
    (/ (Math/round (* n scale))
       scale)))

(defn parse-int
  "Parses `n` as an integer. Defaults to zero or `default` instead of NaN."
  ([n]
   (parse-int n 0))
  ([n default]
   (let [maybe-int (js/parseInt n 10)]
     (if (integer? maybe-int)
       maybe-int
       default))))

(defn value-in-range
  "Returns `num` if is in the range [`lower-bound` `upper-bound`]
  if `num` exceeds a given bound, then returns the bound exceeded."
  [number lower-bound upper-bound]
  (max lower-bound (min number upper-bound)))
